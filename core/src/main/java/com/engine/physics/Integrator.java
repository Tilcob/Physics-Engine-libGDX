package com.engine.physics;

import com.engine.physics.body.Body;

public interface Integrator {

    void integrate(Body body, double dt);
}
