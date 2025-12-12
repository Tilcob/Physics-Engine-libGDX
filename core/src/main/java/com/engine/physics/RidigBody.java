package com.engine.physics;

import com.engine.core.MeshData;
import com.engine.utils.PhysicsUtils;

public class RidigBody extends Body {
    public RidigBody(double density) {
        super(BodyType.RIGID, density);
    }
}
