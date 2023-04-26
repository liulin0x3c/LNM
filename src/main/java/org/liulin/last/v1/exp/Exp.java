package org.liulin.last.v1.exp;

import org.liulin.last.v1.Edge;
import org.liulin.last.v1.io.IO;
import org.liulin.last.v1.main.G;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.liulin.Main.*;

public class Exp {
    private final int[][] adjacency;
    private final double[][] w;
    private int[] ints;

    private int[] neighbors(int v) {
        return adjacency[v];
    }

    private double weight(int i, int j) {
        return w[i][j];
    }

    public Exp(G g, int percent) {
        this.adjacency = g.adjacencyList;
        this.w = g.weights;
        initIS(percent);
    }


    public double expectedValue() {
        try {
            int vNum = adjacency.length;
            int numSimulations = 10_0000;// 模拟次数
            CountDownLatch countDownLatch = new CountDownLatch(numSimulations);
            Object lock = new Object();
            int nThreads = 24;
            long[] sum = new long[nThreads];
            ExecutorService es = Executors.newFixedThreadPool(nThreads);
            for (int i = 0; i < nThreads; i++) {
                int finalI = i;
                es.execute(() -> {
                    var random = ThreadLocalRandom.current();
                    BitSet act = new BitSet(vNum);
                    int[] s = new int[vNum];
                    int tos;
                    while (true) {
                        synchronized (lock) {
                            if (countDownLatch.getCount() == 0) break;
                            countDownLatch.countDown();
                        }

                        act.clear();
                        for (var idx : ints) {
                            act.set(idx, true);
                        }

                        tos = -1;
                        for (int initNode : ints) {
                            s[++tos] = initNode;
                        }
                        while (tos != -1) {
                            int cur = s[tos--];
                            int[] neighbors = neighbors(cur);
                            for (var n : neighbors) {
                                double w = weight(cur, n);
                                if (!act.get(n) && random.nextDouble() < w) {
                                    act.set(n, true);
                                    s[++tos] = n;
                                }
                            }
                        }
                        sum[finalI] += act.cardinality();
                    }
                });
            }
            countDownLatch.await();
            es.shutdown();
            // 计算期望值
            long totalActivatedNodes = 0; // 总共激活的节点数
            for (var i : sum) {
                totalActivatedNodes += i;
            }
            return totalActivatedNodes / (numSimulations * 1.0);
        } catch (InterruptedException e) {
            System.out.println("ERR");
        }
        return -1;
    }

    public void initIS(int percent) {
        // 随机生成初始节点
        int vNum = adjacency.length;
        Set<Integer> seedNodes = new HashSet<>();
        int numSeedNodes = (int) (vNum / 100.0 * percent); // 计算初始节点数量
        Random random = new Random();
        while (seedNodes.size() < numSeedNodes) {
            int randomIndex = random.nextInt(vNum);
            seedNodes.add(randomIndex);
        }
        this.ints = seedNodes.stream().mapToInt(Integer::intValue).toArray();
    }

    public void delEdge(Edge e) {
        int i = e.sour;
        int j = e.dest;
        w[i][j] = w[j][i] = 0;
        adjacency[i] = IntStream.range(0, w.length).filter(k -> w[i][k] != 0).toArray();
        adjacency[j] = IntStream.range(0, w.length).filter(k -> w[j][k] != 0).toArray();
    }


    public static void main(String[] args) throws InterruptedException {
//        {
//            Edge[] edges = IO.loadEdges("FB_0_10");
//            for (Edge e : edges) {
//                e.weight += 0.5;
//            }
//            IO.recordEdges("FB_50_60", edges);
//        }
//        {
//            Edge[] edges = IO.loadEdges("EN_0_10");
//            for (Edge e : edges) {
//                e.weight += 0.5;
//            }
//            IO.recordEdges("EN_50_60", edges);
//        }
//        {
//            File directory = new File("data" + File.separator + "edge");
//            File[] files = directory.listFiles();
//            try (ExecutorService es = Executors.newFixedThreadPool(32)) {
//                for (int i = 0; i < Objects.requireNonNull(files).length; ++i) {
//                    File file = files[i];
//                    String filename = file.getName().strip().split("\\.")[0];
//                    es.submit(() -> G.runMY(filename));
//                    es.submit(() -> runRNDM(filename));
//                    es.submit(() -> runHWGT(filename));
//                    es.submit(() -> runHD(filename));
//                }
//                es.shutdown();
//                boolean b = es.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }

        HashMap<String, List<Double>> data = new HashMap<>();
        File directory = new File("data" + File.separator + "cut");
        File[] files = directory.listFiles();
        int percent = 1;
        int pointNum = 20;
        for (int i = 0; i < Objects.requireNonNull(files).length; ++i) {
            File file = files[i];
            String cutName = file.getName().strip().split("\\.")[0];
            String[] split = cutName.split("_");
            String sourData = split[1] + "_" + split[2] + "_" + split[3];
            var methodFileName = sourData + "_" + percent + "_" + split[0];
            data.put(methodFileName, new ArrayList<>());
            Edge[] edges = IO.loadEdges(sourData);
            G g = new G(edges);
            Exp exp = new Exp(g, percent);
            var base = exp.expectedValue();
            var df = new DecimalFormat("0.00%");
            var E = IO.loadCuts(cutName);
            double slice = E.length * 1.0 / pointNum;
            for (int j = 1; j <= pointNum; j++) {
                int start = (int) (slice * (j - 1));
                int end = (int) (slice * j);
                for (int idx = start; idx < end; idx++) {
                    exp.delEdge(E[idx]);
                }
                double cur = exp.expectedValue();
                double number = (base - cur) / base;
                data.get(methodFileName).add(number);
                System.out.println(methodFileName + "\t" + df.format(number));
            }
        }
        Set<Map.Entry<String, List<Double>>> entries = data.entrySet();
        StringBuilder log = new StringBuilder();
        for (Map.Entry<String, List<Double>> entry : entries) {
            String name = entry.getKey();
            List<Double> list = entry.getValue();
            StringBuilder line = new StringBuilder(name);
            for (Double aDouble : list) {
                line.append("\t");
                line.append(aDouble);
            }
            log.append(line).append("\n");
        }

        Path path = Paths.get("data" + File.separator + "final" + File.separator + "result" + "_" + percent + "_" + pointNum + ".txt");
        try {
            Files.writeString(path, log, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}


