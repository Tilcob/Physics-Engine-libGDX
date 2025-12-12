package com.engine.core;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.engine.core.entity.Entity;
import com.engine.physics.body.Body;
import com.engine.physics.body.StaticBody;
import com.engine.utils.PhysicsUtils;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.engine.config.Constants;
import java.util.ArrayList;
import java.util.List;

public class SceneManager {
    private final List<Entity> entities;
    private final Environment environment;
    private Vector3f ambientLight;

    public SceneManager() {
        this.entities = new ArrayList<>();
        this.environment = new Environment();
        this.ambientLight = Constants.AMBIENT_LIGHT;
        init();
    }

    private void init() {
        environment.set(
            new ColorAttribute(ColorAttribute.AmbientLight, 0.3f, 0.3f, 0.3f, 1f)
        );
        environment.add(
            new DirectionalLight().set(
                1f, 1f, 1f,
                -1f, -0.8f, -0.2f
            )
        );
        ModelBuilder modelBuilder = new ModelBuilder();
        Model floorModel = modelBuilder.createBox(
            100f, 10f, 100f,
            new Material(),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
        );
        createFloor(floorModel);
    }

    public void dispose() {

    }

    public List<Entity> getEntities() {
        return entities;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public Vector3f getAmbientLight() {
        return ambientLight;
    }

    public void setAmbientLight(Vector3f ambientLight) {
        this.ambientLight = ambientLight;
    }

    public void add(Entity entity) {
        this.entities.add(entity);
    }

    public void createFloor(Model model) {
        entities.add(PhysicsUtils.createStaticEntityWithModelBuilder(model));
    }
}
