package org.liulin.helper;

public class DoubleHolder {
    private double value;

    public double getValue() {
        return value;
    }

    public DoubleHolder(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public void setValue(double value) {
        this.value = value;
    }
}
