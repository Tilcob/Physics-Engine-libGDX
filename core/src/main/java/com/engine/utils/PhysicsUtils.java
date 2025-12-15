package com.engine.utils;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.engine.config.Constants;
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
        Entity entityA = contact.a();
        Body bodyA = entityA.body();
        Entity entityB = contact.b();
        Body bodyB = entityB.body();
        if (entityA.body().isDynamic() && !entityB.body().isDynamic()) {
            resolveFloorCollision(entityA.body(), entityB.body());
        } else if (!entityA.body().isDynamic() && entityB.body().isDynamic()) {
            resolveFloorCollision(entityB.body(), entityA.body());
        }

        Vector3d relativeVelocityA = new Vector3d(bodyA.getVelocity()
            .add(bodyA.getAngularVelocity()
                .cross(bodyA.getPosition(), new Vector3d()), new Vector3d()));
        Vector3d relativeVelocityB = new Vector3d(bodyB.getVelocity()
            .add(bodyB.getAngularVelocity()
                .cross(bodyB.getPosition(), new Vector3d()), new Vector3d()));

        Vector3d relativeVelocity = relativeVelocityB.sub(relativeVelocityA, new Vector3d());
        Vector3d n = new Vector3d(contact.normal());

        double normalRelVel = relativeVelocity.dot(n);
        if (normalRelVel > 0) return;

        double jN = -(1 + Constants.restitution) * normalRelVel / (bodyA.getInverseMass() + bodyB.getInverseMass());
        Vector3d normalImpulse = new Vector3d(n).mul(jN);

        Vector3d newAVelocity = bodyA.getVelocity().sub(new Vector3d(normalImpulse).mul(bodyA.getInverseMass()), new Vector3d());
        bodyA.setVelocity(newAVelocity);
        Vector3d newBVelocity = bodyB.getVelocity().add(new Vector3d(normalImpulse).mul(bodyB.getInverseMass()), new Vector3d());
        bodyB.setVelocity(newBVelocity);

        relativeVelocityA = new Vector3d(bodyA.getVelocity()
            .add(bodyA.getAngularVelocity()
                .cross(bodyA.getPosition(), new Vector3d()), new Vector3d()));
        relativeVelocityB = new Vector3d(bodyB.getVelocity()
            .add(bodyB.getAngularVelocity()
                .cross(bodyB.getPosition(), new Vector3d()), new Vector3d()));
        relativeVelocity = relativeVelocityB.sub(relativeVelocityA, new Vector3d());

        Vector3d tangentialVelocity = new Vector3d(relativeVelocity).sub(new Vector3d(n).mul(relativeVelocity.dot(n)), new Vector3d());
        double vTLength = tangentialVelocity.length();
        if (vTLength > 1e-5) {
            Vector3d t = tangentialVelocity.normalize(new Vector3d());
            double jT = -vTLength * (bodyA.getMass() + bodyB.getMass());
            double maxJT = Constants.frictionCoefficient * Math.abs(jN);
            jT = Math.max(-maxJT, Math.min(jT, maxJT));
            Vector3d impulseT = new Vector3d(t).mul(jT);

            Vector3d newAFrictionVel = bodyA.getVelocity().sub(new Vector3d(impulseT).mul(bodyA.getInverseMass()), new Vector3d());
            bodyA.setVelocity(newAFrictionVel);
            Vector3d newBFrictionVel = bodyB.getVelocity().add(new Vector3d(impulseT).mul(bodyB.getInverseMass()), new Vector3d());
            bodyB.setVelocity(newBFrictionVel);
        }

        double penetration = contact.penetration();
        if (penetration > 0) {
            double percent = 0.2;   // 20% der Penetration pro Step korrigieren
            double slop    = 0.001; // kleiner Toleranzwert gegen Jitter

            double corrMargin = Math.max(penetration - slop, 0) * percent * (bodyA.getMass() + bodyB.getMass());
            Vector3d correction = new Vector3d(contact.normal()).mul(corrMargin);

            Vector3d newPosA = bodyA.getPosition()
                .sub(new Vector3d(correction).mul(bodyA.getInverseMass()), new Vector3d());
            bodyA.setPosition(newPosA);
            Vector3d newPosB = bodyB.getPosition()
                .add(new Vector3d(correction).mul(bodyB.getInverseMass()), new Vector3d());
            bodyB.setPosition(newPosB);
        }
    }
}
