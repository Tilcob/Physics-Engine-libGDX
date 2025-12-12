package com.engine.physics;

import org.joml.Vector3d;

public class Particle {
    Vector3d position;  // current position
    Vector3d velocity;
    double inverseMass;  // 1/kg (0 = fix point)

    public Particle(Vector3d position, Vector3d velocity, double inverseMass) {
        this.position = new Vector3d(position);
        this.velocity = new Vector3d(velocity);
        this.inverseMass = inverseMass;
    }

    public Vector3d getPosition() {
        return position;
    }

    public Vector3d getVelocity() {
        return velocity;
    }

    public double getInverseMass() {
        return inverseMass;
    }
}
