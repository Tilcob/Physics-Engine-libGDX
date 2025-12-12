package com.engine.core.entity;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.engine.core.MeshData;
import com.engine.physics.Body;
import com.engine.physics.StaticBody;
import org.joml.Vector3f;
import com.engine.config.Constants;
import java.util.ArrayList;
import java.util.List;

public class SceneManager {
    private final List<Entity> entities;
    private final Environment environment;
    private ModelInstance floor;
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
            10f, .1f, 10f,
            new Material(),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
        );
        Mesh mesh = floorModel.meshes.first();
        float[] vertices = new float[mesh.getNumVertices() * (mesh.getVertexAttributes().vertexSize / 4)];
        short[] indices = new short[mesh.getNumIndices()];
        mesh.getVertices(vertices);
        mesh.getIndices(indices);
        MeshData meshData = new MeshData(vertices, indices);
        Body body = new StaticBody();
        Entity floor = new Entity(new ModelInstance(floorModel), body, meshData);
        entities.add(floor);
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public ModelInstance getFloor() {
        return floor;
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
}
