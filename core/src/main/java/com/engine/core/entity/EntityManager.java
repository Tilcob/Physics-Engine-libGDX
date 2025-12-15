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
                    Vector3d springForce = applyMouseSpring(camera, body);
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

    private Vector3d applyMouseSpring(PerspectiveCamera camera, Body body) {
        // Zielpunkt entlang des aktuellen Rays
        Ray ray = camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());
        double dist = body.getTHit();
        Vector3d targetWorld = new Vector3d(
            ray.origin.x + ray.direction.x * dist,
            ray.origin.y + ray.direction.y * dist,
            ray.origin.z + ray.direction.z * dist
        );

        Matrix3d R = body.getRotation();
        Vector3d x = body.getPosition();
        Vector3d localHit = body.getMouseHit(); // zuvor beim Pick gesetzt

        // Aufpunkt im Welt-Raum
        Vector3d pWorld = new Matrix3d(R).transform(localHit, new Vector3d()).add(x);

        // Federkraft
        double k = 2000000;
        double c = 2 * Math.sqrt(k * body.getMass());
        Vector3d dir = new Vector3d(targetWorld).sub(pWorld);
        Vector3d F = new Vector3d(dir).mul(k);

        // Geschwindigkeit am Punkt
        Vector3d vCom = body.getVelocity();
        Vector3d omegaWorld = new Matrix3d(R).transform(body.getAngularVelocity(), new Vector3d());
        Vector3d r = new Vector3d(pWorld).sub(x);
        Vector3d vPoint = new Vector3d(vCom).add(omegaWorld.cross(r, new Vector3d()));

        F.fma(-c, vPoint);

        return F;
    }
}
