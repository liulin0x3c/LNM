package org.liulin.last.v1.main;

import org.liulin.last.v1.Edge;
import org.liulin.last.v1.io.IO;

import java.util.concurrent.atomic.AtomicInteger;

import static org.liulin.Main.creatGraph;

public class dataFeature {
    public static void main(String[] args) {
        Edge[] edges = IO.loadEdges("EN_10_20");
        var graph = creatGraph(edges);
        AtomicInteger sum = new AtomicInteger();
        graph.vertexSet().forEach(v->{
            int degree = graph.degreeOf(v);
            sum.addAndGet(degree);
        });
        System.out.println(graph.vertexSet().size());
        System.out.println(graph.edgeSet().size());
        System.out.println(sum.get()*1.0/graph.vertexSet().size());
    }
}
