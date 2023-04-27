package org.liulin.last.v1.main;

import static org.liulin.Main.*;

import org.liulin.helper.Helper;
import org.liulin.last.v1.Edge;
import org.liulin.last.v1.H;
import org.liulin.last.v1.cache.Cache;
import org.liulin.last.v1.cache.EffCache;
import org.liulin.last.v1.cache.IcCache;
import org.liulin.last.v1.io.IO;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class G {
    public int[][] adjacencyList;
    public double[][] weights;

    public int[][] EMAP;

    public Edge[] E;

    public HashMap<String, Cache> caches;
    public Set<String> updatedSet;

    public G(Edge[] edges) {
        this.E = edges;
        caches = new HashMap<>();

        updatedSet = new HashSet<>();
        int maxNode = Integer.MIN_VALUE;
        for (Edge e : edges) {
            if (e.sour > maxNode) {
                maxNode = e.sour;
            }
            if (e.dest > maxNode) {
                maxNode = e.dest;
            }
        }
        int vertexNum = maxNode + 1;
        weights = new double[vertexNum][vertexNum];
        EMAP = new int[vertexNum][vertexNum];
        for (int[] ints : EMAP) {
            Arrays.fill(ints, -1);
        }

        for (int i = 0; i < this.E.length; ++i) {
            Edge edge = this.E[i];
            weights[edge.dest][edge.sour] = edge.weight;
            weights[edge.sour][edge.dest] = edge.weight;
            EMAP[edge.dest][edge.sour] = i;
            EMAP[edge.sour][edge.dest] = i;
        }
        adjacencyList = new int[vertexNum][];
        for (int i = 0; i < weights.length; i++) {
            int finalI = i;
            adjacencyList[i] = IntStream.range(0, weights[i].length).filter(j -> weights[finalI][j] != 0).toArray();
        }
    }

    public int[] getNeighbors(int idx) {
        return adjacencyList[idx];
    }

    public int[] findCommonNeighbors(int v, int u) {
        var a = getNeighbors(u);
        var b = getNeighbors(v);
        return H.findCommonElements(a, b);
    }

    public void del(int idx) {
        int i = E[idx].sour;
        int j = E[idx].dest;
        weights[i][j] = 0;
        weights[j][i] = 0;

        adjacencyList[i] = IntStream.range(0, weights.length).filter(k -> weights[i][k] != 0).toArray();
        adjacencyList[j] = IntStream.range(0, weights.length).filter(k -> weights[j][k] != 0).toArray();

        {
            EMAP[i][j] = -1;
            EMAP[j][i] = -1;
            var e = E[E.length - 1];
            var a = e.sour;
            var b = e.dest;
            EMAP[a][b] = EMAP[b][a] = idx;
        }


        E[idx] = E[E.length - 1];
        E = Arrays.copyOf(E, E.length - 1);

        updatedSet.clear();
        for (Cache cache : caches.values()) {
            cache.updateAfterDeleteEdge(idx, i, j);
        }
    }

    static double v = 0.1;

    public static void runMY(String fileName) {
        try {
            var edges = IO.loadEdges(fileName);
            G g = new G(edges);
            g.caches.put(EffCache.class.getName(), new EffCache(g));
            g.caches.put(IcCache.class.getName(), new IcCache(g));
            edges = g.E;
            int times = (int) (v * edges.length);
            Edge[] delEdges = new Edge[times];
            for (int t = 0; t < times; t++) {
                if (t % 10 == 0) System.out.println(t + "/" + times);

                IcCache icCache = (IcCache) g.caches.get(IcCache.class.getName());
                var A = new double[2][g.E.length];
                EffCache effCache = (EffCache) (g.caches.get(EffCache.class.getName()));
                A[0] = effCache.cache;
                for (int idx = 0; idx < A[0].length; idx++) {
                    int i = g.E[idx].sour;
                    int j = g.E[idx].dest;
                    A[1][idx] = icCache.get(i) + icCache.get(j);
                }
                var crit = Helper.calculateCrit(A);

                int maxIdx = 0;
                for (int i = 1; i < crit.length; i++) {
                    if (crit[i] > crit[maxIdx]) {
                        maxIdx = i;
                    }
                }

                var delE = g.E[maxIdx];
                g.del(maxIdx);
                delEdges[t] = delE;
            }
            IO.recordCuts("MY_" + fileName, delEdges);
        } catch (Exception e) {
            System.out.println("ERR");
        }
    }

    public static void main(String[] args) {

//        File directory = new File("data" + File.separator + "edge");
//        File[] files = directory.listFiles();
//        try (ExecutorService es = Executors.newFixedThreadPool(32)) {
//            for (int i = 0; i < Objects.requireNonNull(files).length; ++i) {
//                File file = files[i];
//                String filename = file.getName().strip().split("\\.")[0];
//                es.submit(() -> G.runMY(filename));
//                es.submit(() -> runRNDM(filename));
//                es.submit(() -> runHWGT(filename));
//                es.submit(()-> runHD(filename));
//            }
//            es.shutdown();
//            boolean b = es.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }

    }

}




