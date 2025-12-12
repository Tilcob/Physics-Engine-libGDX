package com.engine.utils;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.engine.core.MeshData;
import com.engine.core.MeshDataD;
import com.engine.core.entity.AABB;
import com.engine.core.entity.Entity;
import com.engine.physics.body.Body;
import com.engine.physics.body.StaticBody;
import org.joml.Vector3d;

import java.util.List;

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
        double dynTop     = dp.y + dh.y;
        double floorTop   = fp.y + fh.y;
        double floorBottom = fp.y - fh.y;

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

    public static MeshData meshDataFromMesh(Mesh mesh, Body body) {
        VertexAttributes attrs = mesh.getVertexAttributes();
        int posOffset = attrs.findByUsage(VertexAttributes.Usage.Position).offset / 4;
        int vertexSizeFloats = attrs.vertexSize / 4;

        float[] raw = new float[mesh.getNumVertices() * vertexSizeFloats];
        short[] idx = new short[mesh.getNumIndices()];
        mesh.getVertices(raw);
        mesh.getIndices(idx);

        float[] positions = new float[mesh.getNumVertices() * 3];
        float minX = Float.POSITIVE_INFINITY, maxX = Float.NEGATIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY, maxZ = Float.NEGATIVE_INFINITY;

        for (int i = 0; i < mesh.getNumVertices(); i++) {
            int base = i * vertexSizeFloats + posOffset;
            float x = raw[base];
            float y = raw[base + 1];
            float z = raw[base + 2];

            positions[i * 3]     = x;
            positions[i * 3 + 1] = y;
            positions[i * 3 + 2] = z;

            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;
            if (z < minZ) minZ = z;
            if (z > maxZ) maxZ = z;
        }

        int[] indices = new int[idx.length];
        for (int i = 0; i < idx.length; i++) indices[i] = idx[i];

        double hx = (maxX - minX) * 0.5;
        double hy = (maxY - minY) * 0.5;
        double hz = (maxZ - minZ) * 0.5;
        body.setHalfExtent(hx, hy, hz);

        return new MeshData(positions, indices);
    }

    public static Entity createStaticEntityWithModelBuilder(Model model) {
        Body body = new StaticBody();
        body.setPosition(new Vector3d());
        MeshData mesh = meshDataFromMesh(model.meshes.first(), body);
        return new Entity(new ModelInstance(model), body, mesh);
    }
}
