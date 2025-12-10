package com.physics.math;

import org.joml.Vector3d;

public class State {
    public Vector3d position;
    public Vector3d velocity;

    public State(Vector3d position, Vector3d velocity) {
        this.position = position;
        this.velocity = velocity;
    }
}
