package org.liulin.last.v1.cache;

import org.liulin.last.v1.main.G;

public class LocalClusteringCoefficientCache implements Cache {
    public double[] cache;

    public G g;

    public LocalClusteringCoefficientCache(G g) {
        this.g = g;
        init();
    }

    public void init() {
        cache = new double[g.weights.length];
        for (int i = 0; i < cache.length; i++) {
            calculate(i);
        }
    }

    private void calculate(int v) {
        int[] neighbors = g.getNeighbors(v);
        int n = neighbors.length;
        if (n == 1) {
            cache[v] = 0D;
            return;
        }
        double sum = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (g.weights[i][j] != 0) {
                    sum += 2 * g.weights[i][j];
                }
            }
        }
        cache[v] = 2.0 * sum / (n * (n - 1));
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
