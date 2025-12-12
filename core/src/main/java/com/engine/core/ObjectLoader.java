package com.engine.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.engine.core.entity.Entity;
import com.engine.physics.body.Body;
import com.engine.utils.PhysicsUtils;
import com.engine.utils.Utils;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.*;

public class ObjectLoader {
    public Entity createEntity(String internalPath, Body body) {
        Model model = loadObjModel(internalPath);
        MeshData mesh = loadMeshData(internalPath);
        PhysicsUtils.calcInertia(mesh, body);

        return new Entity(new ModelInstance(model), body, mesh);
    }

    public Model loadObjModel(String internalPath) {
        FileHandle fileHandle = Gdx.files.internal(internalPath);
        List<String> lines = Utils.readAllLines(fileHandle.path());

        List<Vector3f> vertices = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();
        List<Vector2f> texCoords = new ArrayList<>();
        List<Vector3i> faces = new ArrayList<>();

        for (String line : lines){
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            String[] tokens = line.split("\\s+");
            switch (tokens[0]) {
                case "v":
                    vertices.add(new Vector3f(
                        Float.parseFloat(tokens[1]),
                        Float.parseFloat(tokens[2]),
                        Float.parseFloat(tokens[3])
                    ));
                    break;
                case "vt":
                    texCoords.add(new Vector2f(
                        Float.parseFloat(tokens[1]),
                        Float.parseFloat(tokens[2])
                    ));
                    break;
                case "vn":
                    normals.add(new Vector3f(
                        Float.parseFloat(tokens[1]),
                        Float.parseFloat(tokens[2]),
                        Float.parseFloat(tokens[3])
                    ));
                    break;
                case "f":
                    List<Vector3i> poly = new ArrayList<>();
                    for (int i = 1; i < tokens.length; i++) {
                        if (!tokens[i].isEmpty()) processFaceToken(tokens[i], poly);
                    }

                    for (int i = 1; i < poly.size() - 1; i++) {
                        faces.add(poly.get(0));
                        faces.add(poly.get(i));
                        faces.add(poly.get(i + 1));
                    }
                    break;
                default:
                    break;
            }
        }

        Map<String, Integer> vertexMap = new HashMap<>();
        List<Float> finalPos = new ArrayList<>();
        List<Float> finalTex = new ArrayList<>();
        List<Float> finalNor = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        for (Vector3i face : faces) {
            int posIndex = face.x;
            int texIndex = face.y;
            int norIndex = face.z;

            String key = posIndex + "/" + texIndex +  "/" + norIndex;
            Integer idx = vertexMap.get(key);
            if (idx == null) {
                Vector3f position = vertices.get(posIndex);
                finalPos.add(position.x);
                finalPos.add(position.y);
                finalPos.add(position.z);

                if (texIndex >= 0 && texIndex < texCoords.size()) {
                    Vector2f t = texCoords.get(texIndex);
                    finalTex.add(t.x);
                    finalTex.add(t.y);
                } else {
                    finalTex.add(0f);
                    finalTex.add(0f);
                }

                if (norIndex >= 0 && norIndex < normals.size()) {
                    Vector3f n = normals.get(norIndex);
                    finalNor.add(n.x);
                    finalNor.add(n.y);
                    finalNor.add(n.z);
                } else {
                    finalNor.add(0f);
                    finalNor.add(0f);
                    finalNor.add(0f);
                }

                idx = (finalPos.size() / 3) - 1;
                vertexMap.put(key, idx);
            }
            indices.add(idx);
        }
        int vertexCount = finalPos.size() / 3;
        if (vertexCount >= 65535) {
            throw new RuntimeException("Too many vertices for libGDX Mesh (max 65535).");
        }

        float[] verts = new float[vertexCount * 8];
        for (int i = 0; i < vertexCount; i++) {
            int v = i * 8;

            verts[v]     = finalPos.get(3 * i);
            verts[v + 1] = finalPos.get(3 * i + 1);
            verts[v + 2] = finalPos.get(3 * i + 2);

            verts[v + 3] = finalTex.get(2 * i);
            verts[v + 4] = finalTex.get(2 * i + 1);

            verts[v + 5] = finalNor.get(3 * i);
            verts[v + 6] = finalNor.get(3 * i + 1);
            verts[v + 7] = finalNor.get(3 * i + 2);
        }

        short[] idx = new short[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            idx[i] = (short)(int)indices.get(i);
        }

        Mesh mesh = new Mesh(
            true,
            vertexCount,
            idx.length,
            new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
            new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord0"),
            new VertexAttribute(VertexAttributes.Usage.Normal, 3, "a_normal")
        );
        mesh.setVertices(verts);
        mesh.setIndices(idx);

        ModelBuilder mb = new ModelBuilder();
        mb.begin();
        Material mat = new Material();
        mb.part("obj", mesh, GL20.GL_TRIANGLES, mat);
        return mb.end();
    }

    public MeshData loadMeshData(String internalPath) {
        FileHandle fileHandle = Gdx.files.internal(internalPath);
        List<String> lines = Utils.readAllLines(fileHandle.path());

        List<Vector3f> vertices = new ArrayList<>();

        List<Vector3i> faces = new ArrayList<>();

        for (String line : lines){
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            String[] tokens = line.split("\\s+");
            switch (tokens[0]) {
                case "v":
                    vertices.add(new Vector3f(
                        Float.parseFloat(tokens[1]),
                        Float.parseFloat(tokens[2]),
                        Float.parseFloat(tokens[3])
                    ));
                    break;
                case "f":
                    List<Vector3i> poly = new ArrayList<>();
                    for (int i = 1; i < tokens.length; i++) {
                        if (!tokens[i].isEmpty()) processFaceToken(tokens[i], poly);
                    }

                    for (int i = 1; i < poly.size() - 1; i++) {
                        faces.add(poly.get(0));
                        faces.add(poly.get(i));
                        faces.add(poly.get(i + 1));
                    }
                    break;
                default:
                    break;
            }
        }

        Map<String, Integer> vertexMap = new HashMap<>();
        List<Float> finalPos = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        for (Vector3i face : faces) {
            int posIndex = face.x;
            int texIndex = face.y;
            int norIndex = face.z;

            String key = posIndex + "/" + texIndex +  "/" + norIndex;
            Integer idx = vertexMap.get(key);
            if (idx == null) {
                Vector3f position = vertices.get(posIndex);
                finalPos.add(position.x);
                finalPos.add(position.y);
                finalPos.add(position.z);

                idx = (finalPos.size() / 3) - 1;
                vertexMap.put(key, idx);
            }
            indices.add(idx);
        }

        float[] positions = new float[finalPos.size()];
        for (int i = 0; i < positions.length; i++) {
            positions[i] = finalPos.get(i);
        }
        int[] indexArr = indices.stream().mapToInt(Integer::intValue).toArray();

        return new MeshData(positions, indexArr);
    }

    private void processFaceToken(String token, List<Vector3i> faces) {
        String[] parts = token.split("/");
        int pos, tex = -1, nor = -1;

        pos = Integer.parseInt(parts[0]) - 1;
        if (parts.length > 1 && !parts[1].isEmpty()) {
            tex = Integer.parseInt(parts[1]) - 1;
        }
        if (parts.length > 2 && !parts[2].isEmpty()) {
            nor = Integer.parseInt(parts[2]) - 1;
        }
        faces.add(new Vector3i(pos, tex, nor));
    }
}
