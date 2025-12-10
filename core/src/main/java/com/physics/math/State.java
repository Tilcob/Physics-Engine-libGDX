package com.physics.math;

import org.joml.Vector3f;

public class State {
    public Vector3f position;
    public Vector3f velocity;

    public State(Vector3f position, Vector3f velocity) {
        this.position = position;
        this.velocity = velocity;
    }
}
