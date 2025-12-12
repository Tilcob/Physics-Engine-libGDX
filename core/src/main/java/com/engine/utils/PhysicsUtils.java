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
import com.engine.physics.collisions.Contact;
import org.joml.Vector3d;

import java.util.List;

import static com.engine.utils.CollisionsUtils.resolveFloorCollision;

public class PhysicsUtils {
    private PhysicsUtils() {}

    public static void calcInertia(MeshData mesh, Body body) {
        MeshDataD meshD = toMeshDataD(mesh);
        double[] positions = meshD.positions();

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
        double[] positions = new double[mesh.positions().length];
        for(int i = 0; i < positions.length; i++) {
            positions[i] = mesh.positions()[i];
        }

        return new MeshDataD(positions, mesh.indices());
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

    public static void resolveContact(Contact contact) {
        Entity entity1 = contact.a();
        Entity entity2 = contact.b();
        if (entity1.body().isDynamic() && !entity2.body().isDynamic()) {
            resolveFloorCollision(entity1.body(), entity2.body());
        } else if (!entity1.body().isDynamic() && entity2.body().isDynamic()) {
            resolveFloorCollision(entity2.body(), entity1.body());
        }
    }
}
