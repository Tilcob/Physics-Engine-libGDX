package com.engine.utils.math;

import org.joml.Matrix3d;
import org.joml.Vector3d;

public class State {
    public Vector3d position;
    public Vector3d velocity;
    public Matrix3d rotation;
    public Vector3d angularVelocity;
    public final Matrix3d localInertia;

    public State(Vector3d position, Vector3d velocity, Matrix3d rotation, Vector3d angularVelocity, Matrix3d localInertia) {
        this.position = new Vector3d(position);
        this.velocity = new Vector3d(velocity);
        this.rotation = new Matrix3d(rotation);
        this.angularVelocity = new Vector3d(angularVelocity);
        this.localInertia = localInertia;
    }
}
