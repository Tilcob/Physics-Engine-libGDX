package com.engine.core.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.collision.Ray;
import com.engine.config.Constants;
import com.engine.core.ObjectLoader;
import com.engine.core.SceneManager;
import com.engine.physics.RK4Integrator;
import com.engine.physics.body.Body;
import com.engine.utils.CameraUtils;
import com.engine.utils.CollisionsUtils;
import org.joml.Matrix3d;
import org.joml.Vector3d;

import java.util.List;

public class EntityManager {
    private ObjectLoader loader;
    private Entity pickedEntity = null;
    private boolean leftWasDown = false;

    public void init() {
        this.loader = new ObjectLoader();
    }

    public void createEntity(SceneManager scene, Body body, Vector3d position, String internalPath) {
        body.setPosition(new Vector3d(position));
        scene.add(loader.createEntity(internalPath, body));

    }

    public void update(double dt, SceneManager scene, PerspectiveCamera camera) {
        boolean leftNow = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        boolean leftJustPressed  =  leftNow && !leftWasDown;
        boolean leftJustReleased = !leftNow &&  leftWasDown;

        if (leftJustPressed) {
            pickedEntity = CameraUtils.mouseInput(camera, scene.getEntities());
            System.out.println("picked = " + pickedEntity);
        }
        if (leftJustReleased) {
            pickedEntity = null;
        }
        List<Entity> entities = scene.getEntities();
        for (Entity entity : entities) {
            Body body = entity.body();
            if (!body.isDynamic()) continue;

            RK4Integrator integrator = new RK4Integrator((pos, vel) -> {
                Vector3d totalForce = new Vector3d(0, -body.getMass() * Constants.EARTH_ACC, 0);
                if (pickedEntity == entity && leftNow) {
                    Vector3d springForce = CameraUtils.applayMouseGrip(camera, body);
                    totalForce.add(springForce);
                }

                Vector3d a = new Vector3d(totalForce).mul(body.getInverseMass());
                a.fma(-.1, vel);
                return a;
            });

            Vector3d localTorque = body.getRotation().transpose(new Matrix3d()).transform(new Vector3d(10,0,0));
            Vector3d netTorque = new Vector3d();

            integrator.integrate(body, netTorque, dt, 1);
            entity.syncFromPhysics();

        }
        CollisionsUtils.checkCollision(entities);
        leftWasDown = leftNow;
    }

    public void dispose() {
    }
}
