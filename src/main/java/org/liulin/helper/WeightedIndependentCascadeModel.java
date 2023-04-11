package org.liulin.helper;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class WeightedIndependentCascadeModel {

    public static double expectedValue(Graph<Integer, DefaultWeightedEdge> graph) throws InterruptedException {


        // 设定种子节点
        Set<Integer> seedNodes = initIS(graph);
        // 进行信息传播模拟
        int numSimulations = 10000; // 模拟次数
        AtomicInteger totalActivatedNodes = new AtomicInteger(0); // 总共激活的节点数
        CountDownLatch countDownLatch = new CountDownLatch(numSimulations);
        Object lock = new Object();
        ExecutorService es = Executors.newFixedThreadPool(16);
        for (int i = 0; i < 32; i++) {
            es.execute(() -> {
                var random = ThreadLocalRandom.current();
                int sum = 0;
                while (true) {
                    synchronized (lock) {
                        if (countDownLatch.getCount() == 0) {
                            break;
                        }
                        countDownLatch.countDown();
                    }
                    var oldsSet = new HashSet<>(seedNodes);
                    var actQueue = new ArrayDeque<>(seedNodes);
                    var newsSet = new HashSet<Integer>();
                    while (!actQueue.isEmpty()) {
                        var cur = actQueue.poll();
                        oldsSet.add(cur);
                        for (var edge : graph.edgesOf(cur)) {
                            var neighbor = graph.getEdgeTarget(edge);
                            if (!oldsSet.contains(neighbor)) {
                                double activationProbability = graph.getEdgeWeight(edge);
                                if (random.nextDouble() < activationProbability) { // 使用随机数生成激活概率
                                    newsSet.add(neighbor);
                                }
                            }
                        }
                        actQueue.addAll(newsSet);
                        newsSet.clear();
                    }
                    sum += oldsSet.size();
                }
                totalActivatedNodes.addAndGet(sum);
            });
        }
        countDownLatch.await();
        es.shutdown();
        // 计算期望值
        return totalActivatedNodes.get() * 1.0 / numSimulations;
    }

    public static Set<Integer> initIS(Graph<Integer, DefaultWeightedEdge> graph) {
        // 随机生成初始节点
        Set<Integer> seedNodes = new HashSet<>();
        int numSeedNodes = (int) (graph.vertexSet().size() * 0.2); // 计算初始节点数量
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
