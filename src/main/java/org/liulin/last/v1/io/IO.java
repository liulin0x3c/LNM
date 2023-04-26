package org.liulin.last.v1.io;

import org.liulin.last.v1.Edge;
import org.liulin.last.v1.exp.Exp;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.liulin.Main.*;

public class IO {
    public synchronized static void recordEdges(String fileName, Edge[] edges) {
        String[] array = Arrays.stream(edges).map(edge -> edge.toString() + "\t" + edge.weight).toArray(String[]::new);
        var log = String.join("\n", array);
        Path path = Paths.get("data" + File.separator + "edge" + File.separator + fileName + ".txt");
        try {
            Files.writeString(path, log, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized static void recordCuts(String fileName, Edge[] edges) {
        String[] array = Arrays.stream(edges).map(Edge::toString).toArray(String[]::new);
        var log = String.join("\n", array);
        Path path = Paths.get("data" + File.separator + "cut" + File.separator + fileName + ".txt");
        try {
            Files.writeString(path, log, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized static Edge[] loadEdges(String fileName) {
        Path path = Paths.get("data" + File.separator + "edge" + File.separator + fileName + ".txt");
        String data;
        try {
            data = Files.readString(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String[] lines = data.split("\n");
        var edgeNum = lines.length;
        var E = new Edge[edgeNum];
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String[] blocks = line.strip().split("[ \t]+");
            int fromId = Integer.parseInt(blocks[0]);
            int toId = Integer.parseInt(blocks[1]);
            double weight = Double.parseDouble(blocks[2]);
            E[i] = new Edge(fromId, toId, weight);
        }
        return E;
    }

    public synchronized static Edge[] loadCuts(String fileName) {
        Path path = Paths.get("data" + File.separator + "cut" + File.separator + fileName + ".txt");
        String data;
        try {
            data = Files.readString(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String[] lines = data.split("\n");
        var edgeNum = lines.length;
        var E = new Edge[edgeNum];
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String[] blocks = line.strip().split("[ \t]+");
            int fromId = Integer.parseInt(blocks[0]);
            int toId = Integer.parseInt(blocks[1]);
            E[i] = new Edge(fromId, toId, 1);
        }
        return E;
    }

    public synchronized static void log(String str) {
        String time = new Date().toString();
        str = "[INFO]\t" + time + " | " + str + "\n";
        Path path = Paths.get("data" + File.separator + "log" + ".txt");
        try {
            Files.writeString(path, str, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static void pp(String sourName, double limit, String destName) {
        Edge[] edges = loadCuts(sourName);
        Random random = new Random();
        for (Edge edge : edges) {
            edge.weight = random.nextDouble() * limit;
        }
        recordEdges(destName, edges);
    }

    public static void main(String[] args) {
        runBTWN("FB_0_100");
//        String cutName = "BTWN_FB_0_100";
//        String[] split = cutName.split("_");
//        String sourData = split[1] + "_" + split[2] + "_" + split[3];
//        Edge[] edges = IO.loadEdges(sourData);
//        G g = new G(edges);
//        Exp exp = new Exp(g, 20);
//        var base = exp.expectedValue();
//        var df = new DecimalFormat("0.00%");
//        var E = IO.loadCuts(cutName);
//        double slice = E.length / 20.0;
//        for (int j = 1; j <= 20; j++) {
//            int start = (int) (slice * (j - 1));
//            int end = (int) (slice * j);
//            for (int idx = start; idx < end; idx++) {
//                exp.delEdge(E[idx]);
//            }
//
//        }
//        double cur = exp.expectedValue();
//        double number = (base - cur) / base;
//        System.out.println(cutName + "\t" + df.format(number));
//    }



//        runBTWN("FB_0_10");
//        File directory = new File("data" + File.separator + "edge");
//        File[] files = directory.listFiles();
//        try (ExecutorService es = Executors.newFixedThreadPool(32)) {
//            for (int i = 0; i < Objects.requireNonNull(files).length; ++i) {
//                File file = files[i];
//                String filename = file.getName().strip().split("\\.")[0];
//                es.submit(() -> G.runMY(filename));
//                es.submit(() -> runRNDM(filename));
//                es.submit(() -> runHWGT(filename));
//            }
////
//            es.shutdown();
//            boolean b = es.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        pp("FB", 0.1, "FB_0_10");
//        pp("FB", 1, "FB_0_100");
//
//        pp("EN", 1, "EN_0_100");
//        pp("EN", 0.1, "EN_0_10");
//        pp("CA", 1, "CA_0_100");
//        pp("CA", 0.1, "CA_0_10");
//        CA-GrQc

//        String[] lines;
//        int[][] A;
//        {
//            Path path = Paths.get("data" + File.separator + "CA-GrQc" + ".txt");
//            String data;
//            try {
//                data = Files.readString(path);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//
//            lines = data.split("\n");
//            A = new int[lines.length][2];
//            var nodeSet = new HashSet<Integer>();
//            for (int i = 0; i < lines.length; i++) {
//                String line = lines[i];
//                String[] blocks = line.strip().split("[ \t]+");
//                int fromId = Integer.parseInt(blocks[0]);
//                int toId = Integer.parseInt(blocks[1]);
//                nodeSet.add(fromId);
//                nodeSet.add(toId);
//            }
//            int[] array = nodeSet.stream().mapToInt(Integer::intValue).toArray();
//            Arrays.sort(array);
//            HashMap<Integer, Integer> map = new HashMap<>();
//            for (int idx = 0; idx < array.length; idx++) {
//                map.put(array[idx], idx);
//            }
//            for (int i = 0; i < lines.length; i++) {
//                String line = lines[i];
//                String[] blocks = line.strip().split("[ \t]+");
//                int fromId = Integer.parseInt(blocks[0]);
//                int toId = Integer.parseInt(blocks[1]);
//                A[i] = new int[]{map.get(fromId), map.get(toId)};
//                if (A[i][0] > A[i][1]) {
//                    int temp = A[i][0];
//                    A[i][0] = A[i][1];
//                    A[i][1] = temp;
//                }
//            }
//        }
//
////        Arrays.sort(A, Comparator.comparingInt(a -> a[0] * 10000 + a[1]));
//        {
//            String[] array = Arrays.stream(A).filter(a -> a[0] != a[1]).map((a) -> a[0] + "\t" + a[1]).collect(Collectors.toSet()).toArray(String[]::new);
//            var log = String.join("\n", array);
//            Path path = Paths.get("data" + File.separator + "CA" + ".txt");
//            try {
//                Files.writeString(path, log, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//
//        }
//        String fileName = "EN_0_10";
//        var E = loadCuts(fileName.split("_")[0]);
//        Set<String> set = new HashSet<>();
//        Edge[] array = Arrays.stream(E).filter((e) -> {
//            int i = e.sour;
//            int j = e.dest;
//            if (i > j) {
//                int t = i;
//                i = j;
//                j = t;
//            }
//            var str = i + "\t" + j;
//            if (!set.contains(str)) {
//                set.add(str);
//                return true;
//            } else {
//                return false;
//            }
//        }).toArray(Edge[]::new);
//        recordEdges(fileName, array);

    }

}
