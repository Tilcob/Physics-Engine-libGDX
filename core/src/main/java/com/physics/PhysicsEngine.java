package com.physics;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.physics.core.ObjectLoader;
import com.physics.utils.CameraUtils;
import com.physics.config.Constants;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class PhysicsEngine extends ApplicationAdapter {
    private ModelBatch modelBatch;
    private Environment environment;
    private PerspectiveCamera camera;
    private ObjectLoader loader;
    private Model cubeModel;
    private Model floorModel;
    private ModelBuilder modelBuilder;
    private ModelInstance cube;
    private ModelInstance floor;

    @Override
    public void create() {
        modelBatch = new ModelBatch();
        environment = new Environment();
        camera = new PerspectiveCamera();
        loader = new ObjectLoader();
        modelBuilder = new ModelBuilder();

        environment.set(
            new ColorAttribute(ColorAttribute.AmbientLight, 0.3f, 0.3f, 0.3f, 1f)
        );
        environment.add(
            new DirectionalLight().set(
                1f, 1f, 1f,
                -1f, -0.8f, -0.2f    // Richtung des Lichts
            )
        );

        camera = new PerspectiveCamera(
            67,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );
        camera.position.set(3f, 3f, 3f);   // Position der Kamera
        camera.lookAt(0f, 1f, 0f);        // Blickpunkt (ungefähr auf den Cube)
        camera.near = 0.1f;
        camera.far = 100f;
        camera.update();

        floorModel = modelBuilder.createBox(
            10f, .1f, 10f,
            new Material(),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
        );
        floor = new ModelInstance(floorModel);
        floor.transform.setToTranslation(0,0,0);

        cubeModel = loader.loadObjModel("models/Cube.obj");
        cube = new ModelInstance(cubeModel);
        cube.transform.setToTranslation(0,1,0);
    }

    @Override
    public void render() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        CameraUtils.move(camera, Constants.CAMERA_SPEED);
        CameraUtils.mouseInput(camera, Constants.CAMERA_ROTATION_SENSITIVITY);
        camera.update();

        modelBatch.begin(camera);
        modelBatch.render(floor,environment);
        modelBatch.render(cube, environment);
        modelBatch.end();

    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        cubeModel.dispose();
        floorModel.dispose();
    }
}
