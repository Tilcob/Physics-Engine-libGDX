package com.engine.physics;

import com.engine.physics.body.Body;
import org.joml.Vector3d;

public interface Integrator {

    void integrate(Body body, Vector3d torque, double dt, int steps);
}
