package com.engine.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.engine.core.entity.Entity;
import com.engine.physics.body.Body;
import org.joml.Matrix3d;
import org.joml.Vector3d;

import java.util.List;

public class CameraUtils {
    private CameraUtils(){}

    public static void move(PerspectiveCamera camera, float speed) {
        float dt = Gdx.graphics.getDeltaTime();
        Vector3 forward = new Vector3();
        Vector3 sideWays = new Vector3(camera.direction).crs(Vector3.Y).nor();

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            forward.add(camera.direction);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            forward.sub(camera.direction);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            forward.add(sideWays);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            forward.sub(sideWays);
        }
        if  (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            forward.add(Vector3.Y);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
            forward.sub(Vector3.Y);
        }

        if (!forward.isZero()) {
            forward.nor().scl(speed * dt);
            camera.position.add(forward);
        }
    }

    public static void mouseInput(PerspectiveCamera camera, float sensitivity) {
        if (Gdx.input.isButtonPressed(Input.Buttons.MIDDLE)) {
            float dx = -Gdx.input.getDeltaX() * sensitivity;
            float dy = -Gdx.input.getDeltaY() * sensitivity;

            camera.direction.rotate(Vector3.Y, dx);

            Vector3 sideWays = new Vector3(camera.direction).crs(Vector3.Y).nor();
            camera.direction.rotate(sideWays, dy);
        }
    }

    public static Entity mouseInput(PerspectiveCamera camera, List<Entity> entities) {
        Ray ray = camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());

        Vector3d inertialCoords = new Vector3d(ray.origin.x, ray.origin.y, ray.origin.z);
        Vector3d inertialDirection = new Vector3d(ray.direction.x, ray.direction.y, ray.direction.z);

        Entity best = null;
        double bestT = Double.POSITIVE_INFINITY;

        for (Entity entity : entities) {
            Vector3d position = entity.body().getPosition(); // in inertial coords
            Matrix3d rotationMatrix = entity.body().getRotation(); // from RIK from K-System to I-System

            Vector3d localCoords = new Vector3d(inertialCoords).sub(position, new Vector3d());
            rotationMatrix.transpose().transform(localCoords);

            Vector3d localDirection = new Vector3d(inertialDirection);
            rotationMatrix.transpose().transform(localDirection);

            double[] outT = new double[1];
            if (intersectLocalRayAABB(localCoords, localDirection, entity.body().getHalfExtent(), outT)) {
                double t = outT[0];
                if (t < bestT) {
                    bestT = t;
                    Vector3d localHit = new Vector3d(localCoords).fma(t, localDirection);
                    entity.body().setMouseHit(localHit);
                    entity.body().setTHit(bestT);
                    best = entity;
                }
            }
        }
        return best;
    }

    public static boolean intersectLocalRayAABB(Vector3d o, Vector3d d, Vector3d half, double[] outT) {
        double tMin = 0.0;
        double tMax = Double.POSITIVE_INFINITY;

        for (int i = 0; i < 3; i++) {
            double origin = (i == 0 ? o.x : i == 1 ? o.y : o.z);
            double dir = (i == 0 ? d.x : i == 1 ? d.y : d.z);
            double min = - (i == 0 ? half.x : i == 1 ? half.y : half.z);
            double max = (i == 0 ? half.x : i == 1 ? half.y : half.z);

            if (Math.abs(dir) < 1e-8) {
                if (origin < min || origin > max) return false;
            } else {
                double t1 = (min - origin) / dir;
                double t2 = (max - origin) / dir;
                if (t1 > t2) {
                    double tmp = t1;
                    t1 = t2;
                    t2 = tmp;
                }

                tMin = Math.max(tMin, t1);
                tMax = Math.min(tMax, t2);
                if (tMin > tMax) return false;
            }
        }

        if (tMax < 0) return false;
        outT[0] = tMin;
        return true;
    }
}
