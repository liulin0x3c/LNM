package org.liulin;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.scoring.EdgeBetweennessCentrality;
import org.jgrapht.graph.*;
import org.liulin.last.v1.Edge;
import org.liulin.last.v1.io.IO;
import org.liulin.last.v1.main.G;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.liulin.last.v1.exp.Exp.fun;

public class Main {

    public static SimpleWeightedGraph<Integer, DefaultWeightedEdge> creatGraph(Edge[] E) {
        var graph = new SimpleWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        try {
            // 从二维数组中添加节点和边
            for (var edge : E) {
                int node1 = edge.sour;
                int node2 = edge.dest;
                double weight = edge.weight;
                graph.addVertex(node1);
                graph.addVertex(node2);
                graph.addEdge(node1, node2);
                var e = graph.getEdge(node1, node2);
                graph.setEdgeWeight(e, weight);

            }
        } catch (Exception ee) {
            throw ee;
        }
        return graph;
    }

    private static <E> Map<E, Double> calculateEdgeBetweenness(Graph<Integer, E> graph) {
        EdgeBetweennessCentrality<Integer, E> betweenness = new EdgeBetweennessCentrality<>(graph);
        return betweenness.getScores();
    }

    private static <E> E findMaxScoreEdgeAndDelete(Graph<Integer, E> graph) {
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
    static double v = 0.1;
    public static void runRNDM(String fileName) {
        try {
            Edge[] edges = IO.loadEdges(fileName);
            var graph = creatGraph(edges);
            int n = graph.edgeSet().size();
            int k = (int) (v * n);
            var delArr = new DefaultWeightedEdge[k];
            var array = graph.edgeSet().toArray(new DefaultWeightedEdge[0]);
            var randomIndices = ThreadLocalRandom.current().ints(0, n).distinct().limit(k).boxed().collect(Collectors.toCollection(ArrayList::new)); // 不重复随机数数组
            Collections.shuffle(randomIndices); // 打乱随机数数组的顺序
            for (int i = 0; i < k; i++) {
                delArr[i] = array[randomIndices.get(i)]; // 从原始数组中选取元素
            }
            IO.recordCuts("RNDM_" + fileName, FORMAT(delArr));
        } catch (Exception e) {
            System.out.println("ERR");
        }
    }


    public static void runHWGT(String fileName) {
        try {
            var graph = creatGraph(IO.loadEdges(fileName));

            Set<DefaultWeightedEdge> edges = graph.edgeSet();
            int n = edges.size();
            int k = (int) (v * n);
            DefaultWeightedEdge[] delArr = edges.stream().sorted(Comparator.comparingDouble(edge -> -graph.getEdgeWeight(edge))).limit(k).toArray(DefaultWeightedEdge[]::new);
            IO.recordCuts("HWGT_" + fileName, FORMAT(delArr));
        } catch (Exception e) {
            System.out.println("ERR");
        }
    }

//    public static void runBTWN(String fileName) {
//        try {
//            var graph = creatGraph(IO.loadEdges(fileName));
//
//            var newGraph = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
//            Graphs.addAllVertices(newGraph, graph.vertexSet());
//            graph.edgeSet().forEach(edge -> newGraph.addEdge(graph.getEdgeSource(edge), graph.getEdgeTarget(edge)));
//
//            int times = (int) (v * newGraph.edgeSet().size());
//            int i = 0;
//            var list = new ArrayList<>(times);
//            while (i < times) {
//                int len = 100;
//                if (i + 100 >= times) {
//                    len = times - i;
//                }
//                Map<DefaultEdge, Double> defaultEdgeDoubleMap = calculateEdgeBetweenness(newGraph);
//                var topKEdges = defaultEdgeDoubleMap.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(len).map(Map.Entry::getKey).toList();
//                list.add(topKEdges);
//                System.out.println();
//                i += 100;
//                System.out.println(i + "/" + times);
//            }
//
//            IO.recordCuts("BTWN1_" + fileName, FORMAT(list.toArray(DefaultEdge[]::new)));
//        } catch (Exception e) {
//            System.out.println("ERR");
//        }
//
//    }
//
//    public static void runWBET(String fileName) {
//        try {
//            var graph = creatGraph(IO.loadEdges(fileName));
//
//            var newGraph = new SimpleWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);
//            Graphs.addGraph(newGraph, graph);
//            graph = null;
////        update w = 1 - w 这样w就能代表距离了，直接用轮子
//            newGraph.edgeSet().forEach(edge -> newGraph.setEdgeWeight(edge, 1 - newGraph.getEdgeWeight(edge)));
//            int times = (int) (v * newGraph.edgeSet().size());
//            var delArr = new DefaultWeightedEdge[times];
//            for (int i = 0; i < times; i++) {
//                System.out.println(i + "/" + times);
//                DefaultWeightedEdge maxScoreEdge = findMaxScoreEdgeAndDelete(newGraph);
//                delArr[i] = maxScoreEdge;
//            }
//            IO.recordCuts("WEBT_" + fileName, FORMAT(delArr));
//        } catch (Exception e) {
//            System.out.println("ERR");
//        }
//    }

    public static void runHD(String fileName) {
        try {
            var graph = creatGraph(IO.loadEdges(fileName));
            var newGraph = new SimpleWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);
            Graphs.addGraph(newGraph, graph);
            graph = null;
            int times = (int) (v * newGraph.edgeSet().size());
            var delArr = new DefaultWeightedEdge[times];
            HashMap<Integer, Double> degreeMap = new HashMap<>(newGraph.vertexSet().size());
            for (var v : newGraph.vertexSet()) {
                degreeMap.put(v, 0D);
            }
            for (DefaultWeightedEdge e : newGraph.edgeSet()) {
                Integer i = newGraph.getEdgeSource(e);
                Integer j = newGraph.getEdgeTarget(e);
                double w = newGraph.getEdgeWeight(e);
                degreeMap.put(i, degreeMap.get(i) + w);
                degreeMap.put(j, degreeMap.get(j) + w);
            }

            for (int i = 0; i < times; ) {
                System.out.println(i + "/" + times);
                int maxKey = degreeMap.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(-1);
                if (maxKey == -1) System.out.println("EE");
                List<Integer> list = Graphs.neighborListOf(newGraph, maxKey);
                if (list.size() == 0) System.out.println("烂完了");
                for (Integer node : list) {
                    var maxScoreEdge = newGraph.getEdge(node, maxKey);
                    double w = newGraph.getEdgeWeight(maxScoreEdge);
                    degreeMap.put(node, degreeMap.get(node) - w);
                    degreeMap.put(maxKey, degreeMap.get(maxKey) - w);
                    newGraph.removeEdge(maxScoreEdge);
                    delArr[i] = maxScoreEdge;
                    i++;
                    if (i >= times) {
                        break;
                    }
                }
            }
            IO.recordCuts("HD_" + fileName, FORMAT(delArr));
        } catch (Exception e) {
            System.out.println("ERR");
        }
    }


    private static <E> Edge[] FORMAT(E[] edges) {
        Edge[] resp = new Edge[edges.length];
        int tos = -1;
        for (E edge : edges) {
            String string = edge.toString();
            String[] split = string.substring(1, string.length() - 1).strip().split(" : ");
            resp[++tos] = new Edge(Integer.parseInt(split[0]), Integer.parseInt(split[1]), 1);
        }
        return resp;
    }

    public static void main(String[] args) {
        {
            File directory = new File("data" + File.separator + "edge");
            File[] files = directory.listFiles();
            try (ExecutorService es = Executors.newFixedThreadPool(16)) {
                for (int i = 0; i < Objects.requireNonNull(files).length; ++i) {
                    File file = files[i];
                    String filename = file.getName().strip().split("\\.")[0];
                    es.submit(() -> G.runMY(filename));
                    es.submit(() -> runRNDM(filename));
                    es.submit(() -> runHWGT(filename));
                    es.submit(() -> runHD(filename));
                }
                es.shutdown();
                boolean b = es.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        {
            int pointNum = 1;
            fun(1, pointNum);
            fun(5, pointNum);
            fun(10, pointNum);
        }
    }

}
