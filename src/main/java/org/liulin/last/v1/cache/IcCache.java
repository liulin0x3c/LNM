package org.liulin.last.v1.cache;

import org.liulin.last.v1.main.G;

import java.util.HashSet;
import java.util.Set;

public class IcCache implements Cache {
    public G g;

    public IcCache(G g) {
        this.g = g;
        init();
    }


    public double[] cache;

    private LocalClusteringCoefficientCache localClusteringCoefficientCache;

    public void init() {
        localClusteringCoefficientCache = (LocalClusteringCoefficientCache) g.caches.get(LocalClusteringCoefficientCache.class.getName());
        if (localClusteringCoefficientCache == null) {
            localClusteringCoefficientCache = new LocalClusteringCoefficientCache(g);
            g.caches.put(LocalClusteringCoefficientCache.class.getName(), localClusteringCoefficientCache);
        }
        cache = new double[g.weights.length];
        for (int i = 0; i < cache.length; i++) {
            calculate(i);
        }
    }

    private void calculate(int v) {
//        double sum = 0;
//        for (var n1 : g.getNeighbors(v)) {
//            for (int n2 : g.getNeighbors(n1)) {
//                sum += g.weights[n1][n2] * g.weights[n1][v] * localClusteringCoefficientCache.get(n2);
//            }
//        }

        var n2Set = getNeighborsOfNeighbours(v);
        double sum = 0;
        for (var i : n2Set) {
            sum += localClusteringCoefficientCache.get(i);
        }
//        cache[v] = Math.exp(-localClusteringCoefficientCache.get(v)) * sum;
        cache[v] = sum;
    }

    public double get(int v) {
        return cache[v];
    }

    @Override
    public void updateAfterDeleteEdge(int idx, int i, int j) {
        String name = this.getClass().getName();
        if (g.updatedSet.contains(name)) {
            return;
        }
        g.updatedSet.add(name);
        Set<Integer> n3s = getNeighborsOfNeighboursOfNeighbours(i);
        n3s.retainAll(getNeighborsOfNeighboursOfNeighbours(j));
        for (var n3 : n3s) {
            calculate(n3);
        }
    }


    private Set<Integer> getNeighborsOfNeighbours(int v) {
        int[] neighbors = g.getNeighbors(v);
        HashSet<Integer> n2Set = new HashSet<>();
        for (int neighbor : neighbors) {
            int[] n2s = g.getNeighbors(neighbor);
            for (int n2 : n2s) {
                n2Set.add(n2);
            }
        }
        return n2Set;
    }

    private Set<Integer> getNeighborsOfNeighboursOfNeighbours(int v) {
        var n3Set = new HashSet<Integer>();
        Set<Integer> neighborsOfNeighbours = getNeighborsOfNeighbours(v);
        for (var n2 : neighborsOfNeighbours) {
            for (int n3 : g.getNeighbors(n2)) {
                n3Set.add(n3);
            }
        }
        return n3Set;
    }

}
