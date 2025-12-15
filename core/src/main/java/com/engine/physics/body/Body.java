package com.engine.physics.body;

import org.joml.Matrix3d;
import org.joml.Vector3d;

public abstract class Body {
    private final BodyType type;
    private Vector3d position = new Vector3d();
    private Matrix3d rotation = new Matrix3d();
    private Vector3d velocity = new Vector3d();
    private Vector3d angularVelocity = new Vector3d();
    private Vector3d halfExtent = new Vector3d();
    private Matrix3d localInertia = new Matrix3d();
    private double mass = 0;
    private double density;
    private Vector3d mouseHit = new Vector3d();
    private double tHit = 0;

    protected Body(BodyType type, double density) {
        this.type = type;
        this.density = density;
    }

    public BodyType getType() {
        return type;
    }

    public Vector3d getPosition() {
        return new Vector3d(position);
    }

    public void setPosition(Vector3d position) {
        this.position = new Vector3d(position);
    }

    public Matrix3d getRotation() {
        return new Matrix3d(rotation);
    }

    public void setRotation(Matrix3d rotation) {
        this.rotation = new Matrix3d(rotation);
    }

    public Vector3d getVelocity() {
        return new Vector3d(velocity);
    }

    public void setVelocity(Vector3d velocity) {
        this.velocity = new Vector3d(velocity);
    }

    public Vector3d getAngularVelocity() {
        return new Vector3d(angularVelocity);
    }

    public void setAngularVelocity(Vector3d angularVelocity) {
        this.angularVelocity = new Vector3d(angularVelocity);
    }

    public double getMass() {
        return mass;
    }

    public double getInverseMass() {
        return mass != 0 ? 1 / mass : 0;
    }

    public void setMass(double mass) {
        if (mass < 0) throw new IllegalArgumentException("There is no negative mass.");
        this.mass = mass;
    }

    public double getDensity() {
        return density;
    }

    public void setDensity(double density) {
        this.density = density;
    }

    public Vector3d getHalfExtent() {
        return new Vector3d(halfExtent);
    }

    public void setHalfExtent(Vector3d halfExtent) {
        this.halfExtent = new Vector3d(halfExtent);
    }

    public void setHalfExtent(double x, double y, double z) {
        this.halfExtent = new Vector3d(x, y, z);
    }

    public Matrix3d getLocalInertia() {
        return new Matrix3d(localInertia);
    }

    public void setLocalInertia(Matrix3d localInertia) {
        this.localInertia = new Matrix3d(localInertia);
    }

    public void setLocalInertia(double A, double B, double C) {
        localInertia.m00 = A;
        localInertia.m11 = B;
        localInertia.m22 = C;
    }

    public Vector3d getMouseHit() {
        return new Vector3d(mouseHit);
    }
    public void setMouseHit(Vector3d mouseHit) {
        this.mouseHit = new Vector3d(mouseHit);
    }

    public double getTHit() {
        return tHit;
    }
    public void setTHit(double tHit) {
        this.tHit = tHit;
    }

    public boolean isDynamic() {
        return type != BodyType.STATIC;
    }
}
