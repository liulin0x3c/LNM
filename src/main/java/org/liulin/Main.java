package org.liulin;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.scoring.EdgeBetweennessCentrality;
import org.jgrapht.graph.*;
import org.liulin.helper.DoubleHolder;
import org.liulin.helper.WeightedIndependentCascadeModel;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class Main {

    public static int[][] loadData2E() {
        Path path = Paths.get("data\\my.txt");
        String data;
        try {
            data = Files.readString(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String[] lines = data.split("\n");
        var edgeNum = lines.length;
        var E = new int[edgeNum][3];
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String[] blocks = line.split(" ");
            int fromId = Integer.parseInt(blocks[0]);
            int toId = Integer.parseInt(blocks[1].strip());
            E[i] = new int[]{fromId, toId};
        }
        return E;
    }

    public static SimpleWeightedGraph<Integer, DefaultWeightedEdge> creatGraph(int[][] E) {
        Path path = Paths.get("data\\WEIGHTS.txt");
        String data;
        try {
            data = Files.readString(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String[] lines = data.split("\n");
        int idx = -1;

        SimpleWeightedGraph<Integer, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        // 从二维数组中添加节点和边
        for (int[] edge : E) {
            int node1 = edge[0];
            int node2 = edge[1];
            graph.addVertex(node1);
            graph.addVertex(node2);
            DefaultWeightedEdge e = graph.addEdge(node1, node2);
            double weight = Double.parseDouble(lines[++idx]);
            graph.setEdgeWeight(e, weight);
        }


        // 输出图的信息
        System.out.println("Nodes: " + graph.vertexSet().size());
        System.out.println("Edges: " + graph.edgeSet().size());

        return graph;
    }

    private static <E> Map<E, Double> calculateEdgeBetweenness(Graph<Integer, E> graph) {
        EdgeBetweennessCentrality<Integer, E> betweenness = new EdgeBetweennessCentrality<>(graph);
        return betweenness.getScores();
    }

    private static <E> E findMaxWBETScoreEdgeAndDelete(Graph<Integer, E> graph) {
        Map<E, Double> scores = calculateEdgeBetweenness(graph);
        var maxEntry = scores.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);
        if (maxEntry != null) {
            graph.removeEdge(maxEntry.getKey());
            return maxEntry.getKey();
        } else {
            System.out.println("ERR: No edges found");
            return null;
        }
    }


    public static DefaultWeightedEdge[] runRNDM(SimpleWeightedGraph<Integer, DefaultWeightedEdge> graph) {
        int n = graph.edgeSet().size();
        int k = (int) (0.2 * n);
        var delArr = new DefaultWeightedEdge[k];
        var array = graph.edgeSet().toArray(new DefaultWeightedEdge[0]);
        var randomIndices = ThreadLocalRandom.current().ints(0, n).distinct().limit(k).boxed().collect(Collectors.toCollection(ArrayList::new)); // 不重复随机数数组
        Collections.shuffle(randomIndices); // 打乱随机数数组的顺序
        for (int i = 0; i < k; i++) {
            delArr[i] = array[randomIndices.get(i)]; // 从原始数组中选取元素
        }
        return delArr;
    }

    public static DefaultWeightedEdge[] runHWGT(SimpleWeightedGraph<Integer, DefaultWeightedEdge> graph) {
        Set<DefaultWeightedEdge> edges = graph.edgeSet();
        int n = edges.size();
        int k = (int) (0.2 * n);
        return edges.stream().sorted(Comparator.comparingDouble(edge -> -graph.getEdgeWeight(edge))).limit(k).toArray(DefaultWeightedEdge[]::new);
    }


    public static DefaultEdge[] runBTWN(SimpleWeightedGraph<Integer, DefaultWeightedEdge> graph) {
        var newGraph = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
        Graphs.addAllVertices(newGraph, graph.vertexSet());
        graph.edgeSet().forEach(edge -> newGraph.addEdge(graph.getEdgeSource(edge), graph.getEdgeTarget(edge)));

        int times = (int) (0.2 * newGraph.edgeSet().size());
        var delArr = new DefaultEdge[times];
        for (int i = 0; i < times; i++) {
            var maxScoreEdge = findMaxWBETScoreEdgeAndDelete(newGraph);
            delArr[i] = maxScoreEdge;
            System.out.print("\r" + i + "/" + times);
        }
        return delArr;
    }

    public static DefaultWeightedEdge[] runWBET(SimpleWeightedGraph<Integer, DefaultWeightedEdge> graph) {
        var newGraph = new SimpleWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        Graphs.addGraph(newGraph, graph);
        graph = null;
//        update w = 1 - w 这样w就能代表距离了，直接用轮子
        newGraph.edgeSet().forEach(edge -> newGraph.setEdgeWeight(edge, 1 - newGraph.getEdgeWeight(edge)));
        int times = (int) (0.2 * newGraph.edgeSet().size());
        var delArr = new DefaultWeightedEdge[times];
        for (int i = 0; i < times; i++) {
            DefaultWeightedEdge maxScoreEdge = findMaxWBETScoreEdgeAndDelete(newGraph);
            delArr[i] = maxScoreEdge;
            System.out.print("\r" + i);
        }
        return delArr;
    }

    private static final ReadWriteLock lock = new ReentrantReadWriteLock();

    private static void log(CharSequence log, String fileName) {
        Path path = Paths.get("data" + File.separator + fileName + ".txt");
        try {
            lock.writeLock().lock();
            Files.writeString(path, log, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private static <E> void recordData(E[] edges, String fileName) {
        String[] strings = new String[edges.length];
        int idx = -1;
        for (var e : edges) {
            String string = e.toString();
            String[] split = string.substring(1, string.length() - 1).split(" : ");
            String joined = String.join(" ", split);
            strings[++idx] = joined;
        }
        log(String.join("\n", strings), fileName);
    }

    private static double calculateIff(SimpleWeightedGraph<Integer, DefaultWeightedEdge> graph, int i) {
        List<Integer> neighbors = Graphs.neighborListOf(graph, i);
        double Wi = 0;
        for (var j : neighbors) {
            Wi += graph.getEdgeWeight(graph.getEdge(i, j));
        }
        var sum = 0.0D;
        for (var j : neighbors) {
            double a = graph.getEdgeWeight(graph.getEdge(i, j)) / Wi;
            var log = Math.log(a);
            sum -= a * log;
        }
        return Wi / neighbors.size() * sum;
    }

    private static double calculateEff(SimpleWeightedGraph<Integer, DefaultWeightedEdge> graph, DefaultWeightedEdge edge) {
        var i = graph.getEdgeSource(edge);
        var j = graph.getEdgeTarget(edge);
        var commonNeighbors = Graphs.neighborSetOf(graph, i);
        commonNeighbors.retainAll(Graphs.neighborSetOf(graph, j));
        var mult = 1.0D;
        for (Integer l : commonNeighbors) {
            var wil = graph.getEdgeWeight(graph.getEdge(i, l));
            var wlj = graph.getEdgeWeight(graph.getEdge(l, j));
            mult *= 1 - wil * wlj;
        }
        var wij = graph.getEdgeWeight(graph.getEdge(i, j));
        return wij * mult;
    }

    public static DefaultWeightedEdge[] runIEED(SimpleWeightedGraph<Integer, DefaultWeightedEdge> graph) {
        var vertexSet = graph.vertexSet();
        var edgeSet = graph.edgeSet();
        Map<Integer, DoubleHolder> infCache = new HashMap<>(vertexSet.size());
        for (var v : vertexSet) {
            infCache.put(v, new DoubleHolder(calculateIff(graph, v)));
        }

        Map<DefaultWeightedEdge, DoubleHolder> effCache = new HashMap<>(edgeSet.size());
        for (var e : edgeSet) {
            effCache.put(e, new DoubleHolder(calculateEff(graph, e)));
        }

        Map<DefaultWeightedEdge, DoubleHolder[]> A = new HashMap<>(edgeSet.size());
        for (var e : edgeSet) {
            var i = graph.getEdgeSource(e);
            var j = graph.getEdgeTarget(e);
            DoubleHolder[] values = {infCache.get(i), infCache.get(j), effCache.get(e)};
            A.put(e, values);
        }

        int times = (int) (0.2 * edgeSet.size());
        DefaultWeightedEdge[] delEdges = new DefaultWeightedEdge[times];
        for (int k = 0; k < delEdges.length; k++) {
            System.out.print(k + "/" + delEdges.length + "\r");
            Map<DefaultWeightedEdge, double[]> N = new HashMap<>(edgeSet.size());
            {
                var sums = new double[3];
                for (var e : edgeSet) {
                    DoubleHolder[] values = A.get(e);
                    for (int i = 0; i < values.length; i++) {
                        sums[i] += values[i].getValue();
                    }
                }
                for (var e : edgeSet) {
                    var values = A.get(e);
                    double[] normalizedValues = new double[values.length];
                    for (int i = 0; i < normalizedValues.length; i++) {
                        normalizedValues[i] = values[i].getValue() / sums[i];
                    }
                    N.put(e, normalizedValues);
                }
            }

            var H = new double[3];
            {
                var n = edgeSet.size();
                var logn = Math.log(n);
                var sums = new double[3];
                for (var e : edgeSet) {
                    double[] values = N.get(e);
                    for (int i = 0; i < values.length; i++) {
                        double Nij = values[i];
                        if (Nij == 0) continue;
                        sums[i] += Nij * Math.log(Nij);
                    }
                }
                for (int i = 0; i < H.length; i++) {
                    H[i] = -1 * sums[i] / logn;
                }
            }

            var alpha = new double[3];
            for (int i = 0; i < 3; i++) {
                alpha[i] = (1 - H[i]) / (3 - (H[0] + H[1] + H[2]));
            }

            Map<DefaultWeightedEdge, Double> crit = new HashMap<>(edgeSet.size());
            for (var e : edgeSet) {
                double[] doubles = N.get(e);
                crit.put(e, alpha[0] * doubles[0] + alpha[1] * doubles[1] + alpha[2] * doubles[2]);
            }

            DefaultWeightedEdge maxEdge = null;
            {
                double maxWeight = Double.NEGATIVE_INFINITY;
                for (Map.Entry<DefaultWeightedEdge, Double> entry : crit.entrySet()) {
                    if (entry.getValue() > maxWeight) {
                        maxEdge = entry.getKey();
                        maxWeight = entry.getValue();
                    }
                }
                if (maxEdge == null) System.out.println("ERR:MAX EDGE IS NULL");
            }
            {
//                del
                graph.removeEdge(maxEdge);
                delEdges[k] = maxEdge;

//                update
                var i = graph.getEdgeSource(maxEdge);
                var j = graph.getEdgeTarget(maxEdge);
                infCache.get(i).setValue(calculateIff(graph, i));
                infCache.get(j).setValue(calculateIff(graph, j));

                Set<DefaultWeightedEdge> edges = new HashSet<>(graph.incomingEdgesOf(i));
                edges.addAll(graph.incomingEdgesOf(j));
                for (var e : edges) {
                    effCache.get(e).setValue(calculateEff(graph, e));
                }
            }
        }
        return delEdges;
    }


    public static void main(String[] args) throws InterruptedException {


        var graph = creatGraph(loadData2E());

//        Random random = new Random();
//        int size = graph.edgeSet().size();
//        var weights = new String[size];
//        for (int i = 0; i < size; i++) {
//            double weight = random.nextDouble(0, 1);
//            weights[i] = String.valueOf(weight);
//        }
//        log(String.join("\n", weights), "WEIGHTS");

//        {
//            var edges = runRNDM(graph);
//            recordData(edges, "RNDM");
//        }
//        {
//            var edges = runHWGT(graph);
//            recordData(edges, "HWGT");
//        }
//        {
//            var edges = runBTWN(graph);
//            recordData(edges, "BTWN");
//        }
//
//        {
//            var edges = runWBET(graph);
//            recordData(edges, "WBET");
//        }
        {
            var edges = runIEED(graph);
            recordData(edges, "IEED");
        }

        System.out.println(WeightedIndependentCascadeModel.expectedValue(graph));
        for (int i = 0; i < 20; i++) {

        }

    }
}
