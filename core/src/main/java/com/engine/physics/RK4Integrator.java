package com.engine.physics;

import com.engine.config.Constants;
import com.engine.math.DifferentialEquationSolver;
import com.engine.math.State;
import com.engine.math.interfaces.Function2;
import org.joml.Vector3d;

public class RK4Integrator implements Integrator {
    Function2<Vector3d, Vector3d, Vector3d> accelerationFunction;

    public RK4Integrator(Function2<Vector3d, Vector3d, Vector3d> accelerationFunction) {
        this.accelerationFunction = accelerationFunction;
    }
    DifferentialEquationSolver solver;
    @Override
    public void integrate(Body body, double dt) {
        if (body.getInverseMass() == 0) return;

        State state = new State(body.getPosition(), body.getVelocity());

        State next = DifferentialEquationSolver.solve(
            state, accelerationFunction::apply, dt, 1
        );

        body.setPosition(next.position);
        body.setVelocity(next.velocity);
    }
}
