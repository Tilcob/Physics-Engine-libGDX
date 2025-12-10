package com.physics.math;

import com.physics.math.interfaces.Function2;
import com.physics.math.interfaces.Function3;
import org.joml.*;

import java.lang.Math;
import java.util.function.Function;

import static com.physics.config.Constants.*;

public class LinearAlgebra {
    private static final float h = DEFAULT_H;

    private LinearAlgebra() {}

    // Derivatives

    public static float derivative(float x, Function<Float, Float> function) {
        return (function.apply(x + h) - function.apply(x - h)) / (2 * h);
    }

    public static float partialDerivativeX(float x, float y, float z, Function3<Float, Float, Float, Float> function) {
        return (function.apply(x + h, y, z) - function.apply(x - h, y, z)) / (2 * h);
    }
    public static float partialDerivativeX(float x, float y, Function2<Float, Float, Float> function) {
        return (function.apply(x + h, y) - function.apply(x - h, y)) / (2 * h);
    }

    public static float partialDerivativeY(float x, float y, float z, Function3<Float, Float, Float, Float> function) {
        return (function.apply(x, y + h, z) - function.apply(x, y - h, z)) / (2 * h);
    }
    public static float partialDerivativeY(float x, float y, Function2<Float, Float, Float> function) {
        return (function.apply(x, y + h) - function.apply(x, y - h)) / (2 * h);
    }

    public static float partialDerivativeZ(float x, float y, float z, Function3<Float, Float, Float, Float> function) {
        return (function.apply(x, y, z + h) - function.apply(x, y, z - h)) / (2 * h);
    }

    public static float partialDerivative(float[] point, int dimension, Function<float[], Float> function) {
        float[] forward = point.clone();
        float[] backward = point.clone();

        forward[dimension] += h;
        backward[dimension] -= h;

        return (function.apply(forward) - function.apply(backward)) / (2 * h);
    }

    public static float partialDerivativeVec2(float[] point, int dimension, Function<Vector2f, Float> function) {
        Vector2f forward = new Vector2f(point);
        Vector2f backward = new Vector2f(point);

        switch (dimension) {
            case 0 -> {
                forward.x += h;
                backward.x -= h;
            }
            case 1 -> {
                forward.y += h;
                backward.y -= h;
            }
            default -> throw new IllegalArgumentException("Dimension out of bound");
        }

        return (function.apply(forward) - function.apply(backward)) / (2 * h);
    }

    public static float partialDerivativeVec3(float[] point, int dimension, Function<Vector3f, Float> function) {
        Vector3f forward = new Vector3f(point);
        Vector3f backward = new Vector3f(point);

        switch (dimension) {
            case 0 -> {
                forward.x += h;
                backward.x -= h;
            }
            case 1 -> {
                forward.y += h;
                backward.y -= h;
            }
            case 2 -> {
                forward.z += h;
                backward.z -= h;
            }
            default -> throw new IllegalArgumentException("Dimension out of bound");
        }

        return (function.apply(forward) - function.apply(backward)) / (2 * h);
    }

    // Gradients

    public static float[] gradient(float[] p, Function<float[], Float> f) {
        float[] g = new float[p.length];
        for (int i = 0; i < p.length; i++) {
            g[i] = partialDerivative(p, i, f);
        }
        return g;
    }

    public static Vector2f gradient(float x, float y, Function2<Float, Float, Float> function) {
        return new Vector2f(
            partialDerivativeX(x, y, function),
            partialDerivativeY(x, y, function)
        );
    }

    public static Vector3f gradient(float x, float y, float z, Function3<Float, Float, Float, Float> function) {
        return new Vector3f(
            partialDerivativeX(x, y, z, function),
            partialDerivativeY(x, y, z, function),
            partialDerivativeZ(x, y, z, function)
        );
    }

    public static Vector4f gradient(float x, float y, float z, float w, Function<float[], Float> function) {
        float[] gradient = gradient(new float[]{x,y,z,w}, function);
        return new Vector4f(
            gradient[0],
            gradient[1],
            gradient[2],
            gradient[3]
        );
    }

    // Hesse-Matrix

    public static Matrix2f hessian(Vector2f vector, Function<Vector2f, Float> function) {
        float[] point = {vector.x, vector.y};

        Matrix2f hessian = new Matrix2f();

        Function<Vector2f, Float> partialF1 = p -> partialDerivativeVec2(new float[]{p.x, p.y}, 0, function);
        Function<Vector2f, Float> partialF2 = p -> partialDerivativeVec2(new float[]{p.x, p.y}, 1, function);

        hessian.m00 = partialDerivativeVec2(point, 0, partialF1);
        hessian.m01 = partialDerivativeVec2(point, 1, partialF1);
        hessian.m10 = hessian.m01;
        hessian.m11 = partialDerivativeVec2(point, 1, partialF2);
        return hessian;
    }

    public static Matrix3f hessian(Vector3f vector, Function<Vector3f, Float> function) {
        float[] point = {vector.x, vector.y, vector.z};
        Matrix3f hessian = new Matrix3f();

        Function<Vector3f, Float> partialF1 = p -> partialDerivativeVec3(new float[]{p.x, p.y, p.z}, 0, function);
        Function<Vector3f, Float> partialF2 = p -> partialDerivativeVec3(new float[]{p.x, p.y, p.z}, 1, function);
        Function<Vector3f, Float> partialF3 = p -> partialDerivativeVec3(new float[]{p.x, p.y, p.z}, 2, function);

        hessian.m00 = partialDerivativeVec3(point, 0, partialF1);
        hessian.m01 = partialDerivativeVec3(point, 1, partialF1);
        hessian.m02 = partialDerivativeVec3(point, 2, partialF1);
        hessian.m10 = hessian.m01;
        hessian.m11 = partialDerivativeVec3(point, 1, partialF2);
        hessian.m12 = partialDerivativeVec3(point, 2, partialF2);
        hessian.m20 = hessian.m02;
        hessian.m21 = hessian.m12;
        hessian.m22 = partialDerivativeVec3(point, 2, partialF3);

        return hessian;
    }

    public static float[][] hessian(float[] point, Function<float[], Float> function) {
        int n = point.length;
        float[][] hessian = new float[n][n];

        for (int i = 0; i < n; i++) {
            int finalI = i;
            Function<float[], Float> partialFi = p -> partialDerivative(p, finalI, function);

            for (int j = 0; j < n; j++) {
                hessian[i][j] = partialDerivative(point, j, partialFi);
            }
        }
        return hessian;
    }


    // Jacobian-Matrix

    public static Vector2f jacobianVec2(Function<Float, Vector2f> function, float q) {
        float dx_dq = derivative(q, (dx) -> function.apply(dx).x);
        float dy_dq = derivative(q, (dy) -> function.apply(dy).y);
        return new Vector2f(dx_dq, dy_dq);
    }

    public static Matrix2f jacobianMat2(Function<Vector2f, Vector2f> function, Vector2f q) {
        Matrix2f jacobian = new Matrix2f();
        float[] point = {q.x, q.y};

        Function<Vector2f, Float> f1 = (v) -> function.apply(v).x;
        Function<Vector2f, Float> f2 = (v) -> function.apply(v).y;

        jacobian.m00 = partialDerivativeVec2(point, 0, f1);
        jacobian.m01 = partialDerivativeVec2(point, 1, f1);
        jacobian.m10 = partialDerivativeVec2(point, 0, f2);
        jacobian.m11 = partialDerivativeVec2(point, 1, f2);

        return jacobian;
    }

    public static Vector3f jacobianVec3(Function<Float, Vector3f> function, float q) {
        float dx_dq = derivative(q, (dx) -> function.apply(dx).x);
        float dy_dq = derivative(q, (dy) -> function.apply(dy).y);
        float dz_dq = derivative(q, (dz) -> function.apply(dz).z);
        return new Vector3f(dx_dq, dy_dq, dz_dq);
    }

    public static Matrix3x2f jacobianMat3x2(Function<Vector2f, Vector3f> function, Vector2f q) {
        Matrix3x2f jacobian = new Matrix3x2f();
        float[] point = {q.x, q.y};


        Function<Vector2f, Float> f1 = (v) -> function.apply(v).x;
        Function<Vector2f, Float> f2 = (v) -> function.apply(v).y;
        Function<Vector2f, Float> f3 = (v) -> function.apply(v).z;

        jacobian.m00 = partialDerivativeVec2(point, 0, f1);
        jacobian.m01 = partialDerivativeVec2(point, 1, f1);
        jacobian.m10 = partialDerivativeVec2(point, 0, f2);
        jacobian.m11 = partialDerivativeVec2(point, 1, f2);
        jacobian.m20 = partialDerivativeVec2(point, 0, f3);
        jacobian.m21 = partialDerivativeVec2(point, 1, f3);

        return jacobian;
    }

    public static Matrix3f jacobianMat3X3(Function<Vector3f, Vector3f> function, Vector3f q) {
        Matrix3f jacobian = new Matrix3f();
        float[] point = {q.x, q.y, q.z};

        Function<Vector3f, Float> f1 = (v) -> function.apply(v).x;
        Function<Vector3f, Float> f2 = (v) -> function.apply(v).y;
        Function<Vector3f, Float> f3 = (v) -> function.apply(v).z;

        jacobian.m00 = partialDerivativeVec3(point, 0, f1);
        jacobian.m01 = partialDerivativeVec3(point, 1, f1);
        jacobian.m02 = partialDerivativeVec3(point, 2, f1);
        jacobian.m10 = partialDerivativeVec3(point, 0, f2);
        jacobian.m11 = partialDerivativeVec3(point, 1, f2);
        jacobian.m12 = partialDerivativeVec3(point, 2, f2);
        jacobian.m20 = partialDerivativeVec3(point, 0, f3);
        jacobian.m21 = partialDerivativeVec3(point, 1, f3);
        jacobian.m22 = partialDerivativeVec3(point, 2, f3);

        return jacobian;
    }

    public static float[][] jacobian(float[] point, Function<float[], Float>[] functions) {
        float[][] jacobian = new  float[functions.length][point.length];

        for (int i = 0; i < functions.length; i++) {
            float[] gradient = gradient(point, functions[i]);
            jacobian[i] = gradient;
        }
        return jacobian;
    }

    // Integration

    public static float integrate(Function<Float, Float> function, float lowerLimit, float upperLimit) {
        float xm = .5f * (upperLimit + lowerLimit);
        float xr = .5f * (upperLimit - lowerLimit);
        float sum = 0;

        for (int i = 0; i < 3; i++) {
            float dx = xr * ABSCISSA[i];
            sum += WEIGHT[i] * function.apply(xm + dx);
        }

        return sum * xr;
    }

    public static float integral(Function2<Float, Float, Float> function,
                                 float lowerLimitX, float upperLimitX,
                                 float lowerLimitY, float upperLimitY) {
        float xm = .5f * (upperLimitX + lowerLimitX);
        float xr = .5f * (upperLimitX - lowerLimitX);
        float ym = .5f * (upperLimitY + lowerLimitY);
        float yr = .5f * (upperLimitY - lowerLimitY);
        float sum = 0;

        for (int i = 0; i < 3; i++) {
            float dx = xr * ABSCISSA[i];
            for (int j = 0; j < 3; j++) {
                float dy = yr * ABSCISSA[j];
                sum += WEIGHT[i] * WEIGHT[j] * function.apply(xm + dx, ym +dy);
            }
        }
        return sum * xr * yr;
    }

    public static float integral(Function3<Float, Float, Float, Float> function,
                                 float lowerLimitX, float upperLimitX,
                                 float lowerLimitY, float upperLimitY,
                                 float lowerLimitZ, float upperLimitZ) {
        float xm = .5f * (upperLimitX + lowerLimitX);
        float xr = .5f * (upperLimitX - lowerLimitX);
        float ym = .5f * (upperLimitY + lowerLimitY);
        float yr = .5f * (upperLimitY - lowerLimitY);
        float zm = .5f * (upperLimitZ + lowerLimitZ);
        float zr = .5f * (upperLimitZ - lowerLimitZ);
        float sum = 0;

        for (int i = 0; i < 3; i++) {
            float dx = xr * ABSCISSA[i];
            for (int j = 0; j < 3; j++) {
                float dy = yr * ABSCISSA[j];
                for (int k = 0; k < 3; k++) {
                    float dz = zr * ABSCISSA[k];
                    sum += WEIGHT[i] * WEIGHT[j] * WEIGHT[k] * function.apply(xm + dx, ym + dy, zm + dz);
                }
            }
        }
        return sum * xr * yr * zr;
    }

    // Utils

    public static float[][] multiply(float[][] A, float[][] B) {
        int m = A.length;
        int n = A[0].length;
        int p = B[0].length;

        if (B.length != n) throw new IllegalArgumentException("Dimension mismatch");

        float[][] C = new float[m][p];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < p; j++) {
                float sum = 0;
                for (int k = 0; k < n; k++) {
                    sum += A[i][k] * B[k][j];
                }
                C[i][j] = sum;
            }
        }
        return C;
    }

    public static float[][] transpose(float[][] matrix) {
        int m = matrix.length;
        int n = matrix[0].length;

        float[][] transposed = new float[n][m];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                transposed[j][i] = matrix[i][j];
            }
        }

        return transposed;
    }
}
