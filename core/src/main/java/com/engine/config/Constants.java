package com.engine.config;

import org.joml.Vector3f;

public class Constants {
    private Constants() {}

    /**
     * Gravitational acceleration g of the earth in [g] = m/s²
     */
    public static final double EARTH_ACC = 9.81f;

    /**
     * Gravitational constant G or γ is a fundamental natural constant for gravity.
     * [G] or [γ] = m³ / (kg * s²)
     */
    public static final double GRAVITATIONAL_CONSTANT = 6.6743f * Math.pow(10, -11);

    // Density

    /**
     * Density ρ: [ρ] = kg / m³
     */
    public static final double MIN_DENSITY = 200f;
    public static final double MAX_DENSITY = 22_500f;
    public static final double AIR_DENSITY = 1.2f;
    public static final double WATER_DENSITY = 1000f;


    // Friction coefficients
    public static final double frictionCoefficient = .5;

    // Default values
    public static final double DEFAULT_H = 1e-5f;
    public static final double restitution = .1;
    public static final double SQRT_3_5 = Math.sqrt(3.0/5.0);
    public static final double[] ABSCISSA = {-SQRT_3_5, 0f, SQRT_3_5};
    public static final double[] WEIGHT = {5f/9, 8f/9, 5f/9};
    public static final float CAMERA_SPEED = 5f;
    public static final float CAMERA_ROTATION_SENSITIVITY = .2f;

    // Window
    public static final int WIDTH = 2000;
    public static final int HEIGHT = 1500;

    // Light
    public static final Vector3f AMBIENT_LIGHT = new Vector3f(.3f,.3f,.3f);
}
