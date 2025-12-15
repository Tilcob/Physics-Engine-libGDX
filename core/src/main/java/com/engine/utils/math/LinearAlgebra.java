package com.engine.utils.math;

import com.engine.utils.math.interfaces.Function2;
import com.engine.utils.math.interfaces.Function3;
import org.joml.*;
import java.util.function.Function;

import static com.engine.config.Constants.*;

public class LinearAlgebra {
    private static final double h = DEFAULT_H;

    private LinearAlgebra() {}

    // Derivatives

    public static double derivative(double x, Function<Double, Double> function) {
        return (function.apply(x + h) - function.apply(x - h)) / (2 * h);
    }

    public static Matrix3d derivative(Matrix3d rotation, Vector3d angularVelocity) {
        Matrix3d omega = new Matrix3d(
            0, -angularVelocity.z, angularVelocity.y,
            angularVelocity.z, 0, -angularVelocity.x,
            -angularVelocity.y, angularVelocity.x, 0
        );

        return omega.mul(rotation, new Matrix3d());
    }

    public static Vector3d derivative(Vector3d angularVelocity, Matrix3d inertia, Vector3d torque) {
        Vector3d IOmega = inertia.transform(angularVelocity, new Vector3d());
        Vector3d cross = angularVelocity.cross(IOmega, new Vector3d());
        Vector3d vec = torque.sub(cross, new Vector3d());
        return inertia.invert(new Matrix3d()).transform(vec, new Vector3d());
    }

    public static double partialDerivativeX(double x, double y, double z, Function3<Double, Double, Double, Double> function) {
        return (function.apply(x + h, y, z) - function.apply(x - h, y, z)) / (2 * h);
    }
    public static double partialDerivativeX(double x, double y, Function2<Double, Double, Double> function) {
        return (function.apply(x + h, y) - function.apply(x - h, y)) / (2 * h);
    }

    public static double partialDerivativeY(double x, double y, double z, Function3<Double, Double, Double, Double> function) {
        return (function.apply(x, y + h, z) - function.apply(x, y - h, z)) / (2 * h);
    }
    public static double partialDerivativeY(double x, double y, Function2<Double, Double, Double> function) {
        return (function.apply(x, y + h) - function.apply(x, y - h)) / (2 * h);
    }

    public static double partialDerivativeZ(double x, double y, double z, Function3<Double, Double, Double, Double> function) {
        return (function.apply(x, y, z + h) - function.apply(x, y, z - h)) / (2 * h);
    }

    public static double partialDerivative(double[] point, int dimension, Function<double[], Double> function) {
        double[] forward = point.clone();
        double[] backward = point.clone();

        forward[dimension] += h;
        backward[dimension] -= h;

        return (function.apply(forward) - function.apply(backward)) / (2 * h);
    }

    public static double partialDerivativeVec2(double[] point, int dimension, Function<Vector2d, Double> function) {
        Vector2d forward = new Vector2d(point);
        Vector2d backward = new Vector2d(point);

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

    public static double partialDerivativeVec3(double[] point, int dimension, Function<Vector3d, Double> function) {
        Vector3d forward = new Vector3d(point);
        Vector3d backward = new Vector3d(point);

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

    public static double[] gradient(double[] p, Function<double[], Double> f) {
        double[] g = new double[p.length];
        for (int i = 0; i < p.length; i++) {
            g[i] = partialDerivative(p, i, f);
        }
        return g;
    }

    public static Vector2d gradient(double x, double y, Function2<Double, Double, Double> function) {
        return new Vector2d(
            partialDerivativeX(x, y, function),
            partialDerivativeY(x, y, function)
        );
    }

    public static Vector3d gradient(double x, double y, double z, Function3<Double, Double, Double, Double> function) {
        return new Vector3d(
            partialDerivativeX(x, y, z, function),
            partialDerivativeY(x, y, z, function),
            partialDerivativeZ(x, y, z, function)
        );
    }

    public static Vector4d gradient(double x, double y, double z, double w, Function<double[], Double> function) {
        double[] gradient = gradient(new double[]{x,y,z,w}, function);
        return new Vector4d(
            gradient[0],
            gradient[1],
            gradient[2],
            gradient[3]
        );
    }

    // Hesse-Matrix

    public static Matrix2d hessian(Vector2d vector, Function<Vector2d, Double> function) {
        double[] point = {vector.x, vector.y};

        Matrix2d hessian = new Matrix2d();

        Function<Vector2d, Double> partialF1 = p -> partialDerivativeVec2(new double[]{p.x, p.y}, 0, function);
        Function<Vector2d, Double> partialF2 = p -> partialDerivativeVec2(new double[]{p.x, p.y}, 1, function);

        hessian.m00 = partialDerivativeVec2(point, 0, partialF1);
        hessian.m01 = partialDerivativeVec2(point, 1, partialF1);
        hessian.m10 = hessian.m01;
        hessian.m11 = partialDerivativeVec2(point, 1, partialF2);
        return hessian;
    }

    public static Matrix3d hessian(Vector3d vector, Function<Vector3d, Double> function) {
        double[] point = {vector.x, vector.y, vector.z};
        Matrix3d hessian = new Matrix3d();

        Function<Vector3d, Double> partialF1 = p -> partialDerivativeVec3(new double[]{p.x, p.y, p.z}, 0, function);
        Function<Vector3d, Double> partialF2 = p -> partialDerivativeVec3(new double[]{p.x, p.y, p.z}, 1, function);
        Function<Vector3d, Double> partialF3 = p -> partialDerivativeVec3(new double[]{p.x, p.y, p.z}, 2, function);

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

    public static double[][] hessian(double[] point, Function<double[], Double> function) {
        int n = point.length;
        double[][] hessian = new double[n][n];

        for (int i = 0; i < n; i++) {
            int finalI = i;
            Function<double[], Double> partialFi = p -> partialDerivative(p, finalI, function);

            for (int j = 0; j < n; j++) {
                hessian[i][j] = partialDerivative(point, j, partialFi);
            }
        }
        return hessian;
    }


    // Jacobian-Matrix

    public static Vector2d jacobianVec2(Function<Double, Vector2d> function, double q) {
        double dx_dq = derivative(q, (dx) -> function.apply(dx).x);
        double dy_dq = derivative(q, (dy) -> function.apply(dy).y);
        return new Vector2d(dx_dq, dy_dq);
    }

    public static Matrix2d jacobianMat2(Function<Vector2d, Vector2d> function, Vector2d q) {
        Matrix2d jacobian = new Matrix2d();
        double[] point = {q.x, q.y};

        Function<Vector2d, Double> f1 = (v) -> function.apply(v).x;
        Function<Vector2d, Double> f2 = (v) -> function.apply(v).y;

        jacobian.m00 = partialDerivativeVec2(point, 0, f1);
        jacobian.m01 = partialDerivativeVec2(point, 1, f1);
        jacobian.m10 = partialDerivativeVec2(point, 0, f2);
        jacobian.m11 = partialDerivativeVec2(point, 1, f2);

        return jacobian;
    }

    public static Vector3d jacobianVec3(Function<Double, Vector3d> function, double q) {
        double dx_dq = derivative(q, (dx) -> function.apply(dx).x);
        double dy_dq = derivative(q, (dy) -> function.apply(dy).y);
        double dz_dq = derivative(q, (dz) -> function.apply(dz).z);
        return new Vector3d(dx_dq, dy_dq, dz_dq);
    }

    public static Matrix3x2d jacobianMat3x2(Function<Vector2d, Vector3d> function, Vector2d q) {
        Matrix3x2d jacobian = new Matrix3x2d();
        double[] point = {q.x, q.y};


        Function<Vector2d, Double> f1 = (v) -> function.apply(v).x;
        Function<Vector2d, Double> f2 = (v) -> function.apply(v).y;
        Function<Vector2d, Double> f3 = (v) -> function.apply(v).z;

        jacobian.m00 = partialDerivativeVec2(point, 0, f1);
        jacobian.m01 = partialDerivativeVec2(point, 1, f1);
        jacobian.m10 = partialDerivativeVec2(point, 0, f2);
        jacobian.m11 = partialDerivativeVec2(point, 1, f2);
        jacobian.m20 = partialDerivativeVec2(point, 0, f3);
        jacobian.m21 = partialDerivativeVec2(point, 1, f3);

        return jacobian;
    }

    public static Matrix3d jacobianMat3X3(Function<Vector3d, Vector3d> function, Vector3d q) {
        Matrix3d jacobian = new Matrix3d();
        double[] point = {q.x, q.y, q.z};

        Function<Vector3d, Double> f1 = (v) -> function.apply(v).x;
        Function<Vector3d, Double> f2 = (v) -> function.apply(v).y;
        Function<Vector3d, Double> f3 = (v) -> function.apply(v).z;

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

    public static double[][] jacobian(double[] point, Function<double[], Double>[] functions) {
        double[][] jacobian = new  double[functions.length][point.length];

        for (int i = 0; i < functions.length; i++) {
            double[] gradient = gradient(point, functions[i]);
            jacobian[i] = gradient;
        }
        return jacobian;
    }

    // Integration

    public static double integrate(Function<Double, Double> function, double lowerLimit, double upperLimit) {
        double xm = .5f * (upperLimit + lowerLimit);
        double xr = .5f * (upperLimit - lowerLimit);
        double sum = 0;

        for (int i = 0; i < 3; i++) {
            double dx = xr * ABSCISSA[i];
            sum += WEIGHT[i] * function.apply(xm + dx);
        }

        return sum * xr;
    }

    public static double integral(Function2<Double, Double, Double> function,
                                 double lowerLimitX, double upperLimitX,
                                 double lowerLimitY, double upperLimitY) {
        double xm = .5f * (upperLimitX + lowerLimitX);
        double xr = .5f * (upperLimitX - lowerLimitX);
        double ym = .5f * (upperLimitY + lowerLimitY);
        double yr = .5f * (upperLimitY - lowerLimitY);
        double sum = 0;

        for (int i = 0; i < 3; i++) {
            double dx = xr * ABSCISSA[i];
            for (int j = 0; j < 3; j++) {
                double dy = yr * ABSCISSA[j];
                sum += WEIGHT[i] * WEIGHT[j] * function.apply(xm + dx, ym +dy);
            }
        }
        return sum * xr * yr;
    }

    public static double integral(Function3<Double, Double, Double, Double> function,
                                 double lowerLimitX, double upperLimitX,
                                 double lowerLimitY, double upperLimitY,
                                 double lowerLimitZ, double upperLimitZ) {
        double xm = .5f * (upperLimitX + lowerLimitX);
        double xr = .5f * (upperLimitX - lowerLimitX);
        double ym = .5f * (upperLimitY + lowerLimitY);
        double yr = .5f * (upperLimitY - lowerLimitY);
        double zm = .5f * (upperLimitZ + lowerLimitZ);
        double zr = .5f * (upperLimitZ - lowerLimitZ);
        double sum = 0;

        for (int i = 0; i < 3; i++) {
            double dx = xr * ABSCISSA[i];
            for (int j = 0; j < 3; j++) {
                double dy = yr * ABSCISSA[j];
                for (int k = 0; k < 3; k++) {
                    double dz = zr * ABSCISSA[k];
                    sum += WEIGHT[i] * WEIGHT[j] * WEIGHT[k] * function.apply(xm + dx, ym + dy, zm + dz);
                }
            }
        }
        return sum * xr * yr * zr;
    }

    // Utils

    public static double[][] multiply(double[][] A, double[][] B) {
        int m = A.length;
        int n = A[0].length;
        int p = B[0].length;

        if (B.length != n) throw new IllegalArgumentException("Dimension mismatch");

        double[][] C = new double[m][p];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < p; j++) {
                double sum = 0;
                for (int k = 0; k < n; k++) {
                    sum += A[i][k] * B[k][j];
                }
                C[i][j] = sum;
            }
        }
        return C;
    }

    public static double[][] transpose(double[][] matrix) {
        int m = matrix.length;
        int n = matrix[0].length;

        double[][] transposed = new double[n][m];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                transposed[j][i] = matrix[i][j];
            }
        }

        return transposed;
    }

    public static Matrix3d GramSchmidtOrthonormalize(Matrix3d A) {
        Vector3d[] v = new Vector3d[]{
            new Vector3d(A.m00, A.m10, A.m20),
            new Vector3d(A.m01, A.m11, A.m21),
            new Vector3d(A.m02, A.m12, A.m22),
        };

        Vector3d[] w = new Vector3d[3];
        Vector3d[] u = new  Vector3d[3];
        u[0] = v[0];
        w[0] = u[0].normalize(new Vector3d());

        for (int i = 1; i < w.length; i++) {
            u[i] = new Vector3d(v[i]);
            for (int j = 0; j < i; j++) {
                u[i].fma(-v[i].dot(u[j]) / u[j].lengthSquared(), u[j]);
            }
            w[i] = u[i].normalize(new Vector3d());
        }
        return new Matrix3d(w[0], w[1], w[2]);
    }

    public static Matrix3d orthonormalize(Matrix3d A) {
        Vector3d x = new Vector3d(A.m00, A.m10, A.m20).normalize();
        Vector3d y = new Vector3d(A.m01, A.m11, A.m21);
        // y orthogonal zu x
        y.fma(-y.dot(x), x).normalize();
        Vector3d z = x.cross(y, new Vector3d());

        return new Matrix3d(
            x.x, y.x, z.x,
            x.y, y.y, z.y,
            x.z, y.z, z.z
        );
    }
}
