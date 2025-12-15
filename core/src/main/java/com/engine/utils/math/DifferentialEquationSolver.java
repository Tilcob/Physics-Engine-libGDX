package com.engine.utils.math;

import org.joml.Matrix3d;
import org.joml.Vector3d;
import java.util.function.BiFunction;
import static com.engine.config.Constants.DEFAULT_H;

public class DifferentialEquationSolver {
    private DifferentialEquationSolver() {}

    private static State rk4Step(State state, BiFunction<Vector3d, Vector3d, Vector3d> acceleration, double h, Vector3d torque) {
        if (h <= 0) h = DEFAULT_H;

        Vector3d position = state.position;
        Vector3d velocity = state.velocity;
        Matrix3d rotation = state.rotation;
        Vector3d angularVelocity = state.angularVelocity;
        Matrix3d localInertia = state.localInertia;

        Vector3d omegaWorld = new Matrix3d(rotation).transform(angularVelocity, new Vector3d());
        Vector3d k1v = acceleration.apply(position, velocity);           // dv/dt = a(t, x, v)
        Vector3d k1x = new Vector3d(velocity);                           // dx/dt = v
        Matrix3d k1r = LinearAlgebra.derivative(rotation, omegaWorld);
        Vector3d k1w = LinearAlgebra.derivative(angularVelocity, localInertia, torque);

        Vector3d x1 = new Vector3d(position).fma(h * 0.5, k1x);
        Vector3d v1 = new Vector3d(velocity).fma(h * 0.5, k1v);
        Matrix3d R1 = new Matrix3d(rotation).add(new Matrix3d(k1r).scale(h * .5));
        Vector3d w1 = new Vector3d(angularVelocity).fma(h * 0.5, k1w);
        omegaWorld = new Matrix3d(R1).transform(w1, new Vector3d());

        Vector3d k2v = acceleration.apply(x1, v1);
        Vector3d k2x = new Vector3d(v1);
        Matrix3d k2r = LinearAlgebra.derivative(R1, omegaWorld);
        Vector3d k2w = LinearAlgebra.derivative(w1, localInertia, torque);

        Vector3d x2 = new Vector3d(position).fma(h * 0.5, k2x);
        Vector3d v2 = new Vector3d(velocity).fma(h * 0.5, k2v);
        Matrix3d R2 = new Matrix3d(rotation).add(new Matrix3d(k2r).scale(h * .5));
        Vector3d w2 = new Vector3d(angularVelocity).fma(h * 0.5, k2w);
        omegaWorld = new Matrix3d(R2).transform(w2, new Vector3d());

        Vector3d k3v = acceleration.apply(x2, v2);
        Vector3d k3x = new Vector3d(v2);
        Matrix3d k3r = LinearAlgebra.derivative(R2, omegaWorld);
        Vector3d k3w = LinearAlgebra.derivative(w2, localInertia, torque);

        Vector3d x3 = new Vector3d(position).fma(h, k3x);
        Vector3d v3 = new Vector3d(velocity).fma(h, k3v);
        Matrix3d R3 = new Matrix3d(rotation).add(new Matrix3d(k3r).scale(h));
        Vector3d w3 = new Vector3d(angularVelocity).fma(h, k3w);
        omegaWorld = new Matrix3d(R3).transform(w3, new Vector3d());

        Vector3d k4v = acceleration.apply(x3, v3);
        Vector3d k4x = new Vector3d(v3);
        Matrix3d k4r = LinearAlgebra.derivative(R3, omegaWorld);
        Vector3d k4w = LinearAlgebra.derivative(w3, localInertia, torque);

        Vector3d dx = new Vector3d()
            .add(k1x)
            .add(new Vector3d(k2x).mul(2))
            .add(new Vector3d(k3x).mul(2))
            .add(k4x)
            .mul(h / 6);
        Vector3d dv = new Vector3d()
            .add(k1v)
            .add(new Vector3d(k2v).mul(2))
            .add(new Vector3d(k3v).mul(2))
            .add(k4v)
            .mul(h / 6);
        Matrix3d dR = new Matrix3d().add(k1r)
            .add(new Matrix3d(k2r).scale(2))
            .add(new Matrix3d(k3r).scale(2))
            .add(k4r)
            .scale(h / 6);
        Vector3d dw = new Vector3d()
            .add(k1w)
            .add(new Vector3d(k2w).mul(2))
            .add(new Vector3d(k3w).mul(2))
            .add(k4w)
            .mul(h / 6);

        Vector3d nextPosition = new Vector3d(position).add(dx);
        Vector3d nextVelocity = new Vector3d(velocity).add(dv);
        Matrix3d nextRotation = new Matrix3d(rotation).add(dR);
        Vector3d nextAngularVelocity = new Vector3d(angularVelocity).add(dw);

        nextRotation = LinearAlgebra.GramSchmidtOrthonormalize(nextRotation);

        return new State(nextPosition, nextVelocity, nextRotation, nextAngularVelocity, localInertia);
    }

    public static State solve(State initialState, BiFunction<Vector3d, Vector3d, Vector3d> acceleration, double h, Vector3d torque, int steps) {
        State currentState =  initialState;
        for (int i = 0; i < steps; i++) {
            currentState = rk4Step(currentState, acceleration, h, torque);
        }
        return currentState;
    }
}
