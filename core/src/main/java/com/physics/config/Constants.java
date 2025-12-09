package com.physics.config;

import com.badlogic.gdx.math.Vector3;

public class Constants {
    private Constants() {}

    /**
     * Gravitational acceleration g of the earth in [g] = m/s²
     */
    public static final float EARTH_ACC = 9.81f;

    /**
     * Gravitational constant G or γ is a fundamental natural constant for gravity.
     * [G] or [γ] = m³ / (kg * s²)
     */
    public static final float GRAVITATIONAL_CONSTANT = 6.6743f * (float) Math.pow(10f, -11f);

    // Density

    /**
     * Density ρ: [ρ] = kg / m³
     */
    public static final float MIN_DENSITY = 200f;
    public static final float MAX_DENSITY = 22_500f;
    public static final float AIR_DENSITY = 1.2f;
    public static final float WATER_DENSITY = 1000f;


    // Friction coefficients


}
