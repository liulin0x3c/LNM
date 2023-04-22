package org.liulin.last.v1.main;

import org.liulin.Main;
import org.liulin.helper.Helper;
import org.liulin.last.v1.Edge;
import org.liulin.last.v1.H;
import org.liulin.last.v1.cache.Cache;
import org.liulin.last.v1.cache.EffCache;
import org.liulin.last.v1.cache.IcCache;
import org.liulin.last.v1.cache.IdCache;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
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
        int vertexNum = edges[edges.length - 1].dest + 1;
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

    public static void fun() throws IOException {

        var edges = H.loadEdges("EW");
        G g = new G(edges);
        for (Edge edge : edges) {
            int i = edge.sour;
            int j = edge.dest;
            System.out.print(g.findCommonNeighbors(i, j).length + ", ");
        }
        g.caches.put(EffCache.class.getName(), new EffCache(g));
//        g.caches.put(IdCache.class.getName(), new IdCache(g));
        g.caches.put(IcCache.class.getName(), new IcCache(g));
        edges = g.E;
        int times = (int) (0.2 * edges.length);
        Edge[] delEdges = new Edge[times];
        for (int t = 0; t < times; t++) {
            if (t % 100 == 0) System.out.print(t + "/" + times + "\r");

            IcCache icCache = (IcCache) g.caches.get(IcCache.class.getName());
//            IdCache idCache = (IdCache) g.caches.get(IdCache.class.getName());
//            double[] DCCs;
//            {
//                var A = new double[2][];
//                A[0] = icCache.cache;
//                A[1] = idCache.cache;
//                DCCs = Helper.calculateCrit(A);
//            }
//
            var A = new double[2][g.E.length];
            EffCache effCache = (EffCache) (g.caches.get(EffCache.class.getName()));
            A[0] = effCache.cache;
            for (int idx = 0; idx < A[0].length; idx++) {
                int i = g.E[idx].sour;
                int j = g.E[idx].dest;
                A[1][idx] = icCache.get(i) + icCache.get(j);
//                A[1][idx] = 1;
            }
            var crit = Helper.calculateCrit(A);
//            var crit = icCache.cache;

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

        String[] strings = Arrays.stream(delEdges).map((Edge::toString)).toArray(String[]::new);
        Path path = Paths.get("data" + File.separator + "MYY" + ".txt");
        var log = String.join("\n", strings);
        Files.writeString(path, log, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        G.fun();
        Main.fun("MYY");
    }


}




