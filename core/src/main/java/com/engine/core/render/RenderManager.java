package com.engine.core.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.*;
import com.engine.config.Constants;
import com.engine.core.entity.Entity;
import com.engine.core.SceneManager;
import com.engine.utils.CameraUtils;

public class RenderManager {
    private final ModelBatch modelBatch;

    public RenderManager() {
        this.modelBatch = new ModelBatch();
    }

    public void init() {

    }

    public void update(SceneManager scene, PerspectiveCamera camera) {
        if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            CameraUtils.move(camera, Constants.CAMERA_SPEED);
            CameraUtils.mouseInput(camera, Constants.CAMERA_ROTATION_SENSITIVITY);
            camera.update();
        }
        modelBatch.begin(camera);
        for (Entity entity : scene.getEntities()) {
            modelBatch.render(entity.instance(), scene.getEnvironment());
        }
        modelBatch.end();
    }

    public void clear() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    }

    public void dispose() {
        modelBatch.dispose();
    }
}
