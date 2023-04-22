package org.liulin.last.v1.cache;


import org.liulin.last.v1.main.G;

public class WeightDegreeCache implements Cache {


    private G g;
    public double[] cache;

    public WeightDegreeCache(G g) {
        this.g = g;
        init();
    }

    public void init() {
        cache = new double[g.weights.length];
        for (int i = 0; i < cache.length; i++) {
            calculate(i);
        }
    }

    public void calculate(int i) {
        int[] neighbors = g.getNeighbors(i);
        double weightDegree = 0;
        for (int neighbor : neighbors) {
            weightDegree += g.weights[i][neighbor];
        }
        cache[i] = weightDegree;
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
        calculate(i);
        calculate(j);
    }
}
