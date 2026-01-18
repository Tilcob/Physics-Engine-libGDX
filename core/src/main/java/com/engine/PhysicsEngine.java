package com.engine;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.engine.core.entity.EntityManager;
import com.engine.core.SceneManager;
import com.engine.core.render.RenderManager;
import com.engine.physics.body.RidigBody;
import org.joml.Vector3d;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class PhysicsEngine extends ApplicationAdapter {
    private RenderManager renderManager;
    private SceneManager scene;
    private EntityManager entityManager;
    private PerspectiveCamera camera;

    @Override
    public void create() {
        this.renderManager = new RenderManager();
        this.entityManager = new EntityManager();
        this.scene = new SceneManager();
        renderManager.init();
        entityManager.init();
        entityManager.createEntity(scene, new RidigBody(400), new Vector3d(10,20,0), "models/Cube.obj");
        entityManager.createEntity(scene, new RidigBody(400), new Vector3d(0,15,10), "models/Cube.obj");
        camera = new PerspectiveCamera(
            67,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );
        camera.position.set(3f, 3f, 3f);
        camera.lookAt(0f, 1f, 0f);
        camera.near = 0.1f;
        camera.far = 100f;
        camera.update();
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();
        entityManager.update(dt, scene, camera);
        renderManager.clear();
        renderManager.update(scene, camera);
    }

    @Override
    public void dispose() {
        renderManager.dispose();
        entityManager.dispose();
    }


}
