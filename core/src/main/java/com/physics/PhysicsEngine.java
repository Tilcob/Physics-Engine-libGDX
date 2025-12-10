package com.physics;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.physics.math.LinearAlgebra;

import java.util.function.Function;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class PhysicsEngine extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture image;

    @Override
    public void create() {
        batch = new SpriteBatch();
        image = new Texture("libgdx.png");
    }

    @Override
    public void render() {
        double x = (double) Math.PI;
        Function<Double, Double> f1 = Math::sin;
        System.out.println("Die Ableitung von sin(t) bei t = Pi: " + LinearAlgebra.derivative(x, f1));

        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        batch.begin();
        //batch.draw(image, 140, 210);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        image.dispose();
    }
}
