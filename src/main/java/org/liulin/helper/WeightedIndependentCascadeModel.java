package org.liulin.helper;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

public class WeightedIndependentCascadeModel {

    static class Nedata {
        int n;
        double w;

        public Nedata(int n, double w) {
            this.n = n;
            this.w = w;
        }
    }


    public static double expectedValue(SimpleWeightedGraph<Integer, DefaultWeightedEdge> graph, int[] ints) throws InterruptedException {
        int vNum = graph.vertexSet().size();
        Nedata[][] adjacencyList = new Nedata[vNum][];
        for (int i = 0; i < adjacencyList.length; i++) {
            Set<DefaultWeightedEdge> edgesSet = graph.edgesOf(i);
            var edges = new Nedata[edgesSet.size()];
            int idx = -1;
            for (var e : edgesSet) {
                var neighbor = Graphs.getOppositeVertex(graph, e, i);
                double w = graph.getEdgeWeight(e);
                edges[++idx] = new Nedata(neighbor, w);
            }
            adjacencyList[i] = edges;
        }
        int numSimulations = 10_0000;// 模拟次数
        CountDownLatch countDownLatch = new CountDownLatch(numSimulations);
        Object lock = new Object();
        int nThreads = 16;
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
                    for (int idx : ints) {
                        s[++tos] = idx;
                    }
                    while (tos != -1) {
                        int cur = s[tos--];
                        Nedata[] nedatas = adjacencyList[cur];
                        for (Nedata nedata : nedatas) {
                            double w = nedata.w;
                            int n = nedata.n;
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
    }


    public static Set<Integer> initIS(Graph<Integer, DefaultWeightedEdge> graph, int percent) {
        // 随机生成初始节点
        Set<Integer> seedNodes = new HashSet<>();
        int numSeedNodes = (int) (graph.vertexSet().size() / 100.0 * percent); // 计算初始节点数量
        Random random = new Random();
        while (seedNodes.size() < numSeedNodes) {
            int randomIndex = random.nextInt(graph.vertexSet().size());
            seedNodes.add(randomIndex);
        }
        return seedNodes;
    }


    public static void main(String[] args) {
        // 创建一个简单的带权重的无向图
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

    }
}
