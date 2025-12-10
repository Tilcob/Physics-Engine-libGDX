package com.physics.math;
import org.joml.Vector3f;

import java.util.function.BiFunction;
import java.util.function.Function;

public class DifferentialEquationSolver {
    private static final float h = 1e-5f;

    private DifferentialEquationSolver() {}

    private static State rk4Step(State state, BiFunction<Vector3f, Vector3f, Vector3f> acceleration) {
        Vector3f position = state.position;
        Vector3f velocity = state.velocity;

        Vector3f k1v = acceleration.apply(position, velocity);           // dv/dt = a(t, x, v)
        Vector3f k1x = new Vector3f(velocity);                           // dx/dt = v

        Vector3f k2v = acceleration.apply(
            new Vector3f(position).fma(h * 0.5f, k1x),                   // x + h/2 * k1x
            new Vector3f(velocity).fma(h * 0.5f, k1v)                    // v + h/2 * k1v
        );
        Vector3f k2x = new Vector3f(velocity).fma(h * 0.5f, k1v);

        Vector3f k3v = acceleration.apply(
            new Vector3f(position).fma(h * 0.5f, k2x),
            new Vector3f(velocity).fma(h * 0.5f, k2v)
        );
        Vector3f k3x = new Vector3f(velocity).fma(h * 0.5f, k2v);

        Vector3f k4v = acceleration.apply(
            new Vector3f(position).fma(h, k3x),
            new Vector3f(velocity).fma(h, k3v)
        );
        Vector3f k4x = new Vector3f(velocity).fma(h, k3v);

        Vector3f dx = new Vector3f();
        dx.add(k1x).add(new Vector3f(k2x).mul(2)).add(new Vector3f(k3x).mul(2)).add(k4x).mul(h / 6f);
        Vector3f dv = new Vector3f();
        dv.add(k1v).add(new Vector3f(k2v).mul(2)).add(new Vector3f(k3v).mul(2)).add(k4v).mul(h / 6f);

        Vector3f nextPosition = new Vector3f(position).add(dx);
        Vector3f nextVelocity = new Vector3f(velocity).add(dv);

        return new State(nextPosition, nextVelocity);
    }

    public static State solve(State initialState, BiFunction<Vector3f, Vector3f, Vector3f> acceleration, int steps) {
        State currentState =  initialState;

        for (int i = 0; i < steps; i++) {
            currentState = rk4Step(currentState, acceleration);
        }
        return currentState;
    }
}
