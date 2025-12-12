package com.engine.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.engine.core.entity.Entity;
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

    public static void mouseInput(PerspectiveCamera camera, Entity entity) {
        Ray ray = camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());

        Vector3d inertialCoords = new Vector3d(ray.origin.x, ray.origin.y, ray.origin.z);
        Vector3d inertialDirection = new Vector3d(ray.direction.x, ray.direction.y, ray.direction.z);

        Vector3d position = entity.body().getPosition(); // in inertial coords
        Matrix3d rotationMatrix = entity.body().getRotation(); // from RIK from K-System to I-System

        Vector3d localCoords = inertialCoords.sub(position, new Vector3d());
        rotationMatrix.transpose().transform(localCoords);

        Vector3d localDirection = new Vector3d(inertialDirection);
        rotationMatrix.transpose().transform(localDirection);
    }
}
