package com.engine.core;

public class MeshDataD {
    private final double[] positions;
    private final int[] indices;

    public MeshDataD(double[] positions, int[] indices) {
        this.positions = positions;
        this.indices = indices;
    }

    public double[] getPositions() {
        return positions;
    }

    public int[] getIndices() {
        return indices;
    }
}
