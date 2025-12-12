package com.engine.core.entity;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.engine.core.MeshData;
import com.engine.physics.body.Body;
import org.joml.Vector3d;

public record Entity(ModelInstance instance, Body body, MeshData mesh) {
    public void syncFromPhysics() {
        Vector3d position = body.getPosition();
        instance.transform.setToTranslation((float) position.x, (float) position.y, (float) position.z);
    }
}
