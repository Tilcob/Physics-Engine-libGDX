package com.engine.physics.collisions;

import com.engine.core.entity.Entity;
import org.joml.Vector3d;

public record Contact(Entity a, Entity b, Vector3d normal, double penetration) {
}
