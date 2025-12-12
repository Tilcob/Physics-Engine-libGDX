package com.engine;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.*;
import com.engine.config.Constants;
import com.engine.core.EntityManager;
import com.engine.core.ObjectLoader;
import com.engine.core.entity.AABB;
import com.engine.core.entity.Entity;
import com.engine.core.SceneManager;
import com.engine.core.render.RenderManager;
import com.engine.physics.body.Body;
import com.engine.physics.Integrator;
import com.engine.physics.RK4Integrator;
import com.engine.physics.body.RidigBody;
import com.engine.utils.PhysicsUtils;
import org.joml.Vector3d;

import java.util.List;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class PhysicsEngine extends ApplicationAdapter {
    private RenderManager renderManager;
    private SceneManager scene;
    private EntityManager entityManager;

    @Override
    public void create() {
        this.renderManager = new RenderManager();
        this.entityManager = new EntityManager();
        this.scene = new SceneManager();
        renderManager.init();
        entityManager.init();
        entityManager.createEntity(scene, new RidigBody(7800), new Vector3d(0,100,0), "models/Cube.obj");
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();
        entityManager.update(dt, scene);
        renderManager.clear();
        renderManager.update(scene);
    }

    @Override
    public void dispose() {
        renderManager.dispose();
        entityManager.dispose();
    }


}
