package com.physics.math;

public class ComplexNumber {
    private float real;
    private float imag;

    public ComplexNumber(float real, float imag) {
        this.real = real;
        this.imag = imag;
    }
    public ComplexNumber() {
        this.real = 0;
        this.imag = 0;
    }

    public void add(float a, float b) {
        this.real += a;
        this.imag += b;
    }

    public void subtract(float a, float b) {
        this.real -= a;
        this.imag -= b;
    }

    public void multiply(ComplexNumber c) {
        float newReal = this.real * c.real - this.imag * c.imag;
        float newImag = this.real * c.imag + this.imag * c.real;
        this.real = newReal;
        this.imag = newImag;
    }

    public void divide(ComplexNumber c) {
        float denom = c.real * c.real + c.imag * c.imag;
        if (denom == 0) {
            throw new ArithmeticException("Division durch Null");
        }
        float newReal = (this.real * c.real + this.imag * c.imag) / denom;
        float newImag = (this.imag * c.real - this.real * c.imag) / denom;
        this.real = newReal;
        this.imag = newImag;
    }

    public ComplexNumber conjugate() {
        return new ComplexNumber(real, -imag);
    }

    public float getReal() {
        return real;
    }

    public void setReal(float real) {
        this.real = real;
    }

    public float getImag() {
        return imag;
    }

    public void setImag(float imag) {
        this.imag = imag;
    }

    @Override
    public String toString() {
        return String.format("%f %s %fi", real, (imag >= 0 ? "+" : "-"), Math.abs(imag));
    }
}
