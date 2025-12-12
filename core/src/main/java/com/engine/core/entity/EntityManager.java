package com.engine.core.entity;

import com.engine.core.ObjectLoader;
import com.engine.core.SceneManager;
import com.engine.physics.RK4Integrator;
import com.engine.physics.body.Body;
import com.engine.utils.CollisionsUtils;
import com.engine.utils.PhysicsUtils;
import org.joml.Vector3d;

import java.util.List;

public class EntityManager {
    private ObjectLoader loader;

    public void init() {
        this.loader = new ObjectLoader();
    }

    public void createEntity(SceneManager scene, Body body, Vector3d position, String internalPath) {
        body.setPosition(new Vector3d(position));
        scene.add(loader.createEntity(internalPath, body));

    }

    public void update(double dt, SceneManager scene) {
        List<Entity> entities = scene.getEntities();
        for (Entity entity : entities) {
            Body body = entity.body();
            if (!body.isDynamic()) continue;
            RK4Integrator.gravity(body, dt, .1);
            entity.syncFromPhysics();
        }
        CollisionsUtils.checkCollision(entities);
    }

    public void dispose() {
    }
}
