package com.engine.physics;

import com.engine.config.Constants;
import com.engine.utils.math.DifferentialEquationSolver;
import com.engine.utils.math.State;
import com.engine.physics.body.Body;
import org.joml.Vector3d;

import java.util.function.BiFunction;

public class RK4Integrator implements Integrator {
    BiFunction<Vector3d, Vector3d, Vector3d> accelerationFunction;

    public RK4Integrator(BiFunction<Vector3d, Vector3d, Vector3d> accelerationFunction) {
        this.accelerationFunction = accelerationFunction;
    }
    DifferentialEquationSolver solver;
    @Override
    public void integrate(Body body, Vector3d torque, double dt, int steps) {
        if (body.getInverseMass() == 0) return;

        State state = new State(body.getPosition(), body.getVelocity(), body.getRotation(), body.getAngularVelocity(), body.getLocalInertia());

        State next = DifferentialEquationSolver.solve(
            state, accelerationFunction, dt, torque, steps
        );

        body.setPosition(next.position);
        body.setVelocity(next.velocity);
        body.setRotation(next.rotation);
        body.setAngularVelocity(next.angularVelocity);
    }

    public static void gravity(Body body, Vector3d torque, double dt, double damp, int steps) {
        Integrator integrator = new RK4Integrator((pos, vel) -> {
            Vector3d a = new Vector3d(0,-Constants.EARTH_ACC,0);
            a.fma(-damp, vel);
            return a;
        });
        integrator.integrate(body, torque, dt, steps);
    }
}
