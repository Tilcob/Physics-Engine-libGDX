package com.engine.utils.math;
import org.joml.Vector3d;

import java.util.function.BiFunction;
import static com.engine.config.Constants.DEFAULT_H;

public class DifferentialEquationSolver {
    private DifferentialEquationSolver() {}

    private static State rk4Step(State state, BiFunction<Vector3d, Vector3d, Vector3d> acceleration, double h) {
        if (h <= 0) h = DEFAULT_H;

        Vector3d position = state.position;
        Vector3d velocity = state.velocity;

        Vector3d k1v = acceleration.apply(position, velocity);           // dv/dt = a(t, x, v)
        Vector3d k1x = new Vector3d(velocity);                           // dx/dt = v

        Vector3d k2v = acceleration.apply(
            new Vector3d(position).fma(h * 0.5f, k1x),                   // x + h/2 * k1x
            new Vector3d(velocity).fma(h * 0.5f, k1v)                    // v + h/2 * k1v
        );
        Vector3d k2x = new Vector3d(velocity).fma(h * 0.5f, k1v);

        Vector3d k3v = acceleration.apply(
            new Vector3d(position).fma(h * 0.5f, k2x),
            new Vector3d(velocity).fma(h * 0.5f, k2v)
        );
        Vector3d k3x = new Vector3d(velocity).fma(h * 0.5f, k2v);

        Vector3d k4v = acceleration.apply(
            new Vector3d(position).fma(h, k3x),
            new Vector3d(velocity).fma(h, k3v)
        );
        Vector3d k4x = new Vector3d(velocity).fma(h, k3v);

        Vector3d dx = new Vector3d();
        dx.add(k1x).add(new Vector3d(k2x).mul(2)).add(new Vector3d(k3x).mul(2)).add(k4x).mul(h / 6f);
        Vector3d dv = new Vector3d();
        dv.add(k1v).add(new Vector3d(k2v).mul(2)).add(new Vector3d(k3v).mul(2)).add(k4v).mul(h / 6f);

        Vector3d nextPosition = new Vector3d(position).add(dx);
        Vector3d nextVelocity = new Vector3d(velocity).add(dv);

        return new State(nextPosition, nextVelocity);
    }

    public static State solve(State initialState, BiFunction<Vector3d, Vector3d, Vector3d> acceleration, double h, int steps) {
        State currentState =  initialState;

        for (int i = 0; i < steps; i++) {
            currentState = rk4Step(currentState, acceleration, h);
        }
        return currentState;
    }
}
