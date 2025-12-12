package com.engine;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.*;
import com.engine.config.Constants;
import com.engine.core.ObjectLoader;
import com.engine.core.entity.AABB;
import com.engine.core.entity.Entity;
import com.engine.core.entity.SceneManager;
import com.engine.core.render.RenderManager;
import com.engine.physics.Body;
import com.engine.physics.Integrator;
import com.engine.physics.RK4Integrator;
import com.engine.physics.RidigBody;
import com.engine.utils.PhysicsUtils;
import org.joml.Vector3d;

import java.util.List;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class PhysicsEngine extends ApplicationAdapter {
    private RenderManager renderManager;
    private Integrator integrator;
    private Model cubeModel;
    private SceneManager scene;

    @Override
    public void create() {
        renderManager = new RenderManager();
        ObjectLoader loader = new ObjectLoader();
        integrator = new RK4Integrator((pos, vel) -> {
            Vector3d a = new Vector3d(0,-Constants.EARTH_ACC,0);
            a.fma(-.1, vel);
            return a;
        });
        renderManager.init();
        scene = new SceneManager();
        Body cube = new RidigBody(7800);
        cube.setPosition(new Vector3d(0,50,0));
        scene.add(loader.createEntity("models/Cube.obj", cube));
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();
        for (Entity entity : scene.getEntities()) {
            Body body = entity.body();
            if (!body.isDynamic()) continue;
            integrator.integrate(body, dt);
            entity.syncFromPhysics();
        }
        checkCollisions(scene.getEntities());

        renderManager.clear();
        renderManager.update(scene);
    }

    @Override
    public void dispose() {
        renderManager.dispose();
        cubeModel.dispose();
    }

    private void checkCollisions(List<Entity> entities) {
        for (int i = 0; i < entities.size(); i++) {
            for (int j = i + 1; j < entities.size(); j++) {
                Entity entity1 = entities.get(i);
                Entity entity2 = entities.get(j);

                AABB a = PhysicsUtils.computeAABB(entity1);
                AABB b = PhysicsUtils.computeAABB(entity2);

                if (PhysicsUtils.overlaps(a,b)) {
                    if (entity1.body().isDynamic() && !entity2.body().isDynamic()) {
                        PhysicsUtils.resolveFloorCollision(entity1.body(), entity2.body());
                    } else if (!entity1.body().isDynamic() && entity2.body().isDynamic()) {
                        PhysicsUtils.resolveFloorCollision(entity2.body(), entity1.body());
                    }
                }
            }
        }
    }
}
