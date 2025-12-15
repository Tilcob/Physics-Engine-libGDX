package com.engine.utils;

import com.engine.core.entity.AABB;
import com.engine.core.entity.Entity;
import com.engine.physics.body.Body;
import com.engine.physics.collisions.Contact;
import org.joml.Matrix3d;
import org.joml.Vector3d;
import java.util.List;
import java.util.Objects;

public class CollisionsUtils {
    private CollisionsUtils() {}

    public static void checkCollision(List<Entity> entities) {
        for (int i = 0; i < entities.size(); i++) {
            for (int j = i + 1; j < entities.size(); j++) {
                Entity entityA = entities.get(i);
                Entity entityB = entities.get(j);
                Contact c = computeContact(entityA, entityB);

                if (Objects.isNull(c)) continue;
                PhysicsUtils.resolveContact(c);
            }
        }
    }

    public static Contact computeContact(Entity entityA, Entity entityB) {
        Vector3d centerA = entityA.body().getPosition();
        Vector3d centerB = entityB.body().getPosition();
        Vector3d halfExtendsA = entityA.body().getHalfExtent();
        Vector3d halfExtendsB = entityB.body().getHalfExtent();
        Matrix3d rotationA =  entityA.body().getRotation();
        Matrix3d rotationB =  entityB.body().getRotation();

        Vector3d[] axesA = new Vector3d[] {
            new Vector3d(rotationA.m00, rotationA.m10, rotationA.m20),
            new Vector3d(rotationA.m01, rotationA.m11, rotationA.m21),
            new Vector3d(rotationA.m02, rotationA.m12, rotationA.m22)
        };
        Vector3d[] axesB = new Vector3d[]{
            new Vector3d(rotationB.m00, rotationB.m10, rotationB.m20),
            new Vector3d(rotationB.m01, rotationB.m11, rotationB.m21),
            new Vector3d(rotationB.m02, rotationB.m12, rotationB.m22)
        };

        Vector3d distanceAB = centerB.sub(centerA, new Vector3d()); // position vector from center A to center B
        double minOverlap = Double.POSITIVE_INFINITY;
        Vector3d bestAxis = null;

        for (int i = 0; i < axesA.length; i++) {
            Vector3d axis = axesA[i];
            if (axis.lengthSquared() < 1e-12) continue;
            if (separatedOnAxis(axesA, halfExtendsA, axesB, halfExtendsB, distanceAB, axis)) return null;
            double overlap = overlapOnAxis(axesA, halfExtendsA, axesB, halfExtendsB, distanceAB, axis);
            if (overlap < minOverlap) {
                minOverlap = overlap;
                bestAxis = new Vector3d(axis);
            }
        }
        for (int i = 0; i < axesB.length; i++) {
            Vector3d axis = axesB[i];
            if (axis.lengthSquared() < 1e-12) continue;
            if (separatedOnAxis(axesA, halfExtendsA, axesB, halfExtendsB, distanceAB, axis)) return null;
            double overlap = overlapOnAxis(axesA, halfExtendsA, axesB, halfExtendsB, distanceAB, axis);
            if (overlap < minOverlap) {
                minOverlap = overlap;
                bestAxis = new Vector3d(axis);
            }
        }

        for (int i = 0; i < axesA.length; i++) {
            for (int j = 0; j < axesB.length; j++) {
                Vector3d axis = axesA[i].cross(axesB[j], new Vector3d());
                if (axis.lengthSquared() < 1e-12) continue;
                if (separatedOnAxis(axesA, halfExtendsA, axesB, halfExtendsB, distanceAB, axis)) return null;
                double overlap = overlapOnAxis(axesA, halfExtendsA, axesB, halfExtendsB, distanceAB, axis);
                if (overlap < minOverlap) {
                    minOverlap = overlap;
                    bestAxis = new Vector3d(axis);
                }
            }
        }

        if(Objects.isNull(bestAxis)) return null;

        bestAxis.normalize();
        if (distanceAB.dot(bestAxis) < 0) bestAxis.negate();

        return new Contact(entityA, entityB, bestAxis, minOverlap);
    }


    // Helper methods

    /**
     *
     * @param axes
     * @param halfExtend
     * @param axis
     * @return
     */
    public static double projectionRadius(Vector3d[] axes, Vector3d halfExtend, Vector3d axis) {
        return halfExtend.x * Math.abs(axes[0].dot(axis))
            + halfExtend.y * Math.abs(axes[1].dot(axis))
            + halfExtend.z * Math.abs(axes[2].dot(axis));
    }

    /**
     *
     * @param axesA
     * @param halfExtendA
     * @param axesB
     * @param halfExtendB
     * @param centerAB
     * @param axis
     * @return
     */
    public static boolean separatedOnAxis(Vector3d[] axesA, Vector3d halfExtendA,
                                         Vector3d[] axesB, Vector3d halfExtendB,
                                         Vector3d centerAB, Vector3d axis) {
        if (axis.lengthSquared() < 1e-12) return false; // axis degenerates -> ignore

        double radiusA = projectionRadius(axesA, halfExtendA, axis);
        double radiusB = projectionRadius(axesB, halfExtendB, axis);
        double distance = Math.abs(centerAB.dot(axis));
        return distance > radiusA + radiusB;
    }

    public static double overlapOnAxis(Vector3d[] axesA, Vector3d halfA,
                                       Vector3d[] axesB, Vector3d halfB,
                                       Vector3d centerDiffAB, // C_B - C_A
                                       Vector3d axis) {
        if (axis.lengthSquared() < 1e-12) return Double.POSITIVE_INFINITY;

        Vector3d normal = new Vector3d(axis).normalize();
        double radiusA = projectionRadius(axesA, halfA, normal);
        double radiusB = projectionRadius(axesB, halfB, normal);
        double distance = Math.abs(centerDiffAB.dot(normal));

        return radiusA + radiusB - distance;
    }

    // AABB Collision detection

    public static AABB computeAABB(Entity entity) {
        Vector3d pos = entity.body().getPosition();
        Vector3d halfExtend = entity.body().getHalfExtent();
        Vector3d min = new Vector3d(pos).sub(halfExtend);
        Vector3d max = new Vector3d(pos).add(halfExtend);
        return new AABB(min, max);
    }

    public static boolean overlaps(AABB a, AABB b) {
        return a.min().x <= b.max().x && a.max().x >= b.min().x &&
            a.min().y <= b.max().y && a.max().y >= b.min().y &&
            a.min().z <= b.max().z && a.max().z >= b.min().z;
    }

    public static void resolveFloorCollision(Body dyn, Body floor) {
        var dp = dyn.getPosition();
        var dh = dyn.getHalfExtent();
        var fp = floor.getPosition();
        var fh = floor.getHalfExtent();

        double dynBottom  = dp.y - dh.y;
        double floorTop   = fp.y + fh.y;

        if (dynBottom < floorTop && dp.y >= fp.y) {
            double penetration = floorTop - dynBottom;
            dp.y += penetration;
            dyn.setPosition(dp);
            var v = dyn.getVelocity();
            if (v.y < 0) v.y = 0;
            dyn.setVelocity(v);
        }
    }

    public static void checkCollisions(List<Entity> entities) {
        for (int i = 0; i < entities.size(); i++) {
            for (int j = i + 1; j < entities.size(); j++) {
                Entity entity1 = entities.get(i);
                Entity entity2 = entities.get(j);

                AABB a = computeAABB(entity1);
                AABB b = computeAABB(entity2);

                if (overlaps(a,b)) {
                    if (entity1.body().isDynamic() && !entity2.body().isDynamic()) {
                        resolveFloorCollision(entity1.body(), entity2.body());
                    } else if (!entity1.body().isDynamic() && entity2.body().isDynamic()) {
                        resolveFloorCollision(entity2.body(), entity1.body());
                    }
                }
            }
        }
    }
}
