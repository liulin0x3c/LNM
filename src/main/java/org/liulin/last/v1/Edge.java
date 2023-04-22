package org.liulin.last.v1;

public class Edge {
    public int sour;
    public int dest;
    public double weight;

    public Edge(int sour, int dest, double weight) {
        this.sour = sour;
        this.dest = dest;
        this.weight = weight;
    }

    @Override
    public String toString() {
        return sour + "\t" + dest;
    }
}
