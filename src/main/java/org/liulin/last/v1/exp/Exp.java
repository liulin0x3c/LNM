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

    private int[] neighbors(int v) {
        return adjacency[v];
    }

    private double weight(int i, int j) {
        return w[i][j];
    }

    public Exp(double[][] weights) {
        this.w = weights;
        this.adjacency = new int[weights.length][];
        for (int i = 0; i < weights.length; i++) {
            int finalI = i;
            adjacency[i] = IntStream.range(0, weights[i].length).filter(j -> weights[finalI][j] != 0).toArray();
        }
    }


//    public double expectedValue() {
//        try {
//            int vNum = adjacency.length;
//            int numSimulations = 1_0000;// 模拟次数
//            CountDownLatch countDownLatch = new CountDownLatch(numSimulations);
//            Object lock = new Object();
//            int nThreads = 32;
//            long[] sum = new long[nThreads];
//            ExecutorService es = Executors.newFixedThreadPool(nThreads);
//            for (int i = 0; i < nThreads; i++) {
//                int finalI = i;
//                es.execute(() -> {
//                    var random = ThreadLocalRandom.current();
//                    BitSet act = new BitSet(vNum);
//                    int[] s = new int[vNum];
//                    int tos;
//                    while (true) {
//                        synchronized (lock) {
//                            if (countDownLatch.getCount() == 0) break;
//                            countDownLatch.countDown();
//                        }
//
//                        act.clear();
//                        for (var idx : ints) {
//                            act.set(idx, true);
//                        }
//
//                        tos = -1;
//                        for (int initNode : ints) {
//                            s[++tos] = initNode;
//                        }
//                        while (tos != -1) {
//                            int cur = s[tos--];
//                            int[] neighbors = neighbors(cur);
//                            for (var n : neighbors) {
//                                double w = weight(cur, n);
//                                if (!act.get(n) && random.nextDouble() < w) {
//                                    act.set(n, true);
//                                    s[++tos] = n;
//                                }
//                            }
//                        }
//                        sum[finalI] += act.cardinality();
//                    }
//                });
//            }
//            countDownLatch.await();
//            es.shutdown();
//            // 计算期望值
//            long totalActivatedNodes = 0; // 总共激活的节点数
//            for (var i : sum) {
//                totalActivatedNodes += i;
//            }
//            return totalActivatedNodes / (numSimulations * 1.0);
//        } catch (InterruptedException e) {
//            System.out.println("ERR");
//        }
//        return -1;
//    }

    public double expectedValueSinT(Set<Integer> ints) {
        try {
            int sum = 0;
            int vNum = adjacency.length;
            int numSimulations = 1_000;// 模拟次数
            var random = new Random();
            BitSet act = new BitSet(vNum);
            int[] s = new int[vNum];
            int tos;
            for (int j = 0; j < numSimulations; j++) {
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
                sum += act.cardinality();
            }
            return 1.0 * sum / numSimulations;
        } catch (Exception e) {
            System.out.println("ERR");
        }
        return -1;
    }


    public Set<Integer> initIS(int percent) {
        // 随机生成初始节点
        int vNum = adjacency.length;
        Set<Integer> seedNodes = new HashSet<>();
        int numSeedNodes = (int) (vNum / 100.0 * percent); // 计算初始节点数量
        Random random = new Random();
        while (seedNodes.size() < numSeedNodes) {
            int randomIndex = random.nextInt(vNum);
            seedNodes.add(randomIndex);
        }
        return seedNodes;
    }

    public void delEdge(Edge e) {
        int i = e.sour;
        int j = e.dest;
        w[i][j] = w[j][i] = 0;
        adjacency[i] = IntStream.range(0, w.length).filter(k -> w[i][k] != 0).toArray();
        adjacency[j] = IntStream.range(0, w.length).filter(k -> w[j][k] != 0).toArray();
    }

    public static void fun(int percent, int pointNum) {
        File directory = new File("data" + File.separator + "cut");
        File[] files = directory.listFiles();

        ConcurrentHashMap<String, List<Double>> data = new ConcurrentHashMap<>();
        ExecutorService es = Executors.newFixedThreadPool(32);
        for (var file : files) {
            es.submit(() -> {
                String cutName = file.getName().strip().split("\\.")[0];
                String[] split = cutName.split("_");
                String sourData = split[1] + "_" + split[2] + "_" + split[3];
                var methodFileName = sourData + "_" + percent + "_" + split[0];
                data.put(methodFileName, new ArrayList<>());
                int maxNode;
                Edge[] edges = IO.loadEdges(sourData);
                maxNode = Integer.MIN_VALUE;
                for (Edge e : edges) {
                    if (e.sour > maxNode) {
                        maxNode = e.sour;
                    }
                    if (e.dest > maxNode) {
                        maxNode = e.dest;
                    }
                }

                int vertexNum = maxNode + 1;
                var weights = new double[vertexNum][vertexNum];
                for (Edge edge : edges) {
                    weights[edge.dest][edge.sour] = edge.weight;
                    weights[edge.sour][edge.dest] = edge.weight;
                }

                Exp exp = new Exp(weights);
                var t = 0;
                ArrayList<Set<Integer>> initISList = new ArrayList<>();
                for (int i = 0; i < 10; i++) {
                    Set<Integer> e = exp.initIS(percent);
                    initISList.add(e);
                    t += exp.expectedValueSinT(e);
                }
                var base = t / 10.0;
                var df = new DecimalFormat("0.00%");
                var E = IO.loadCuts(cutName);
                double slice = E.length * 1.0 / pointNum;
                for (int j = 1; j <= pointNum; j++) {
                    int start = (int) (slice * (j - 1));
                    int end = (int) (slice * j);
                    for (int idx = start; idx < end; idx++) {
                        exp.delEdge(E[idx]);
                    }
                    double total = 0;
                    for (Set<Integer> set : initISList) {
                        total += exp.expectedValueSinT(set);
                    }
                    double cur = total / 10.0;
                    double number = (base - cur) / base;
                    data.get(methodFileName).add(number);
                    System.out.println(methodFileName + "_" + j + "\t" + df.format(number));
                }
            });
        }
        es.shutdown();
        try {
            boolean b = es.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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
        System.out.println();
    }


    public static void main(String[] args) throws InterruptedException {
//        Edge[] edges = IO.loadEdges("EN_0_10");
//        {
//            int low = 10;
//            while (low <= 90) {
//
//                for (Edge e : edges) {
//                    e.weight = Math.random() / 0.1 + (low / 100.0);
//                }
//                IO.recordEdges("EN_" + low + "_" + (low + 10), edges);
//                low = low + 10;
//            }
//        }
//        System.out.println(111);

        {
            int pointNum = 10;
            fun(1, pointNum);
            fun(5, pointNum);
            fun(10, pointNum);
        }


//        {
//            File directory = new File("data" + File.separator + "edge");
//            File[] files = directory.listFiles();
//            try (ExecutorService es = Executors.newFixedThreadPool(16)) {
//                for (int i = 0; i < Objects.requireNonNull(files).length; ++i) {
//                    File file = files[i];
//                    String filename = file.getName().strip().split("\\.")[0];
//                    if (filename == "FB_0_10" || filename == "FB_0_100" || filename == "FB_50_60") continue;
//                    if (filename == "EN_0_10" || filename == "EN_0_100" || filename == "EN_50_60") continue;
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
////
//        {
//            int pointNum = 1;
//            fun(1, pointNum);
//            fun(5, pointNum);
//            fun(10, pointNum);
//        }

    }
}


