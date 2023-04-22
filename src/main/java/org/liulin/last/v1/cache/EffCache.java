package org.liulin.last.v1.cache;

import org.liulin.last.v1.main.G;

import java.util.Arrays;

public class EffCache implements Cache {
    private final G g;

    public EffCache(G g) {
        this.g = g;
        init();
    }

    public double[] cache;

    public void init() {
        cache = new double[g.E.length];
        for (int i = 0; i < cache.length; i++) {
            calculate(i);
        }
    }

    public void calculate(int idx) {
        // 删除前后的成功率差值
        int i = g.E[idx].sour;
        int j = g.E[idx].dest;
        int[] commonNeighbors = g.findCommonNeighbors(i, j);
        double mult = 1;
        for (int n : commonNeighbors) {
            mult *= 1 - g.weights[i][n] * g.weights[j][n];
        }
        cache[idx] = g.weights[i][j] * mult;
    }


    @Override
    public void updateAfterDeleteEdge(int idx, int i, int j) {
        String name = this.getClass().getName();
        if (g.updatedSet.contains(name)) {
            return;
        }
        g.updatedSet.add(name);

        cache[idx] = cache[cache.length - 1];
        cache = Arrays.copyOf(cache, cache.length - 1);
        {
            var neighbors = g.getNeighbors(i);
            for (int neighbor : neighbors) {
                calculate(g.EMAP[i][neighbor]);
            }
        }
        {
            var neighbors = g.getNeighbors(j);
            for (int neighbor : neighbors) {
                calculate(g.EMAP[j][neighbor]);
            }
        }
    }

    public double get(int idx) {
        return cache[idx];
    }
}
