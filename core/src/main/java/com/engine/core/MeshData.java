package com.engine.core;

public class MeshData {
    private final float[] positions;
    private final int[] indices;

    public MeshData(float[] positions, int[] indices) {
        this.positions = positions;
        this.indices = indices;
    }

    public MeshData(float[] positions, short[] indices) {
        this.positions = positions;
        this.indices = copyArray(indices);
    }

    public float[] getPositions() {
        return positions;
    }

    public int[] getIndices() {
        return indices;
    }

    private int[] copyArray(short[] array) {
        int[] c = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            c[i] = array[i];
        }
        return c;
    }
}
