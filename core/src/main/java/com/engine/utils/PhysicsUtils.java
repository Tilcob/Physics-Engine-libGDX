package com.engine.utils;

import com.engine.core.MeshData;
import com.engine.core.MeshDataD;
import com.engine.core.entity.AABB;
import com.engine.core.entity.Entity;
import com.engine.physics.Body;
import org.joml.Vector3d;

public class PhysicsUtils {
    private PhysicsUtils() {}

    public static void calcInertia(MeshData mesh, Body body) {
        MeshDataD meshD = toMeshDataD(mesh);
        double[] positions = meshD.getPositions();

        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < positions.length; i+=3) {
            double x = positions[i];
            double y = positions[i + 1];
            double z = positions[i + 2];
            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;
            if (z < minZ) minZ = z;
            if (z > maxZ) maxZ = z;
        }

        double hx = (maxX - minX) * .5;
        double hy = (maxY - minY) * .5;
        double hz = (maxZ - minZ) * .5;
        body.setHalfExtent(hx, hy, hz);

        if (!body.isDynamic()) return;

        double width = 2 *  hx;
        double height = 2 * hy;
        double depth = 2 *  hz;
        double volume = width * height * depth;
        body.setMass(volume * body.getDensity());

        double mass = body.getMass();
        double A = (1 / 12d) * mass * (height * height + depth * depth);
        double B = (1 / 12d) * mass * (width * width + depth * depth);
        double C = (1 / 12d) * mass * (width * width + height * height);

        body.getLocalInertia().zero();
        body.setLocalInertia(A, B, C);
    }

    public static MeshDataD toMeshDataD(MeshData mesh) {
        double[] positions = new double[mesh.getPositions().length];
        for(int i = 0; i < positions.length; i++) {
            positions[i] = mesh.getPositions()[i];
        }

        return new MeshDataD(positions, mesh.getIndices());
    }

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

        if (dynBottom < floorTop) {
            double penetration = floorTop - dynBottom;

            dp.y += penetration;
            dyn.setPosition(dp);

            var v = dyn.getVelocity();
            if (v.y < 0) v.y = 0;
            dyn.setVelocity(v);
        }
    }
}
