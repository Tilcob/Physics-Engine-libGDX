package com.engine.utils.math;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3d;

public class State {
    public Vector3d position;
    public Vector3d velocity;
    public Matrix3f rotation;
    public Vector3d angularVelocity;

    public State(Vector3d position, Vector3d velocity) {
        this.position = new Vector3d(position);
        this.velocity = new Vector3d(velocity);
    }
}
