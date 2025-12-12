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
    public void integrate(Body body, double dt) {
        if (body.getInverseMass() == 0) return;

        State state = new State(body.getPosition(), body.getVelocity());

        State next = DifferentialEquationSolver.solve(
            state, accelerationFunction, dt, 1
        );

        body.setPosition(next.position);
        body.setVelocity(next.velocity);
    }

    public static void gravity(Body body, double dt, double damp) {
        Integrator integrator = new RK4Integrator((pos, vel) -> {
            Vector3d a = new Vector3d(0,-Constants.EARTH_ACC,0);
            a.fma(-damp, vel);
            return a;
        });
        integrator.integrate(body, dt);
    }
}
