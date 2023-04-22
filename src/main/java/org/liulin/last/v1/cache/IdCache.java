package org.liulin.last.v1.cache;

import org.liulin.last.v1.main.G;

public class IdCache implements Cache {
    G g;
    WeightDegreeCache weightDegreeCache;

    public IdCache(G g) {
        this.g = g;
        init();
    }

    public double[] cache;

    public void init() {
        weightDegreeCache = (WeightDegreeCache) g.caches.get(WeightDegreeCache.class.getName());
        if (weightDegreeCache == null) {
            weightDegreeCache = new WeightDegreeCache(g);
            g.caches.put(WeightDegreeCache.class.getName(), weightDegreeCache);
        }
        cache = new double[g.weights.length];
        for (int i = 0; i < cache.length; i++) {
            calculate(i);
        }
    }

    public void calculate(int i) {
        int[] neighbors = g.getNeighbors(i);
        double sum = 0;
        for (int neighbor : neighbors) {
            sum += weightDegreeCache.get(neighbor);
        }
        cache[i] = sum + weightDegreeCache.get(i);
    }

    public double get(int i) {
        return cache[i];
    }


    @Override
    public void updateAfterDeleteEdge(int idx, int i, int j) {
        String name = this.getClass().getName();
        if (g.updatedSet.contains(name)) {
            return;
        }
        g.updatedSet.add(name);
        int[] neighbors = g.getNeighbors(i);
        for (int neighbor : neighbors) {
            calculate(neighbor);
        }
        neighbors = g.getNeighbors(j);
        for (int neighbor : neighbors) {
            calculate(neighbor);
        }
    }
}
