package com.engine.physics;

public interface Integrator {

    void integrate(Body body, double dt);
}
