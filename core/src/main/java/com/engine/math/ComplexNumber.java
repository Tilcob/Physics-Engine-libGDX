package com.engine.math;

public class ComplexNumber {
    private double real;
    private double imag;

    public ComplexNumber(double real, double imag) {
        this.real = real;
        this.imag = imag;
    }
    public ComplexNumber() {
        this.real = 0;
        this.imag = 0;
    }

    public void add(double a, double b) {
        this.real += a;
        this.imag += b;
    }

    public void subtract(double a, double b) {
        this.real -= a;
        this.imag -= b;
    }

    public void multiply(ComplexNumber c) {
        double newReal = this.real * c.real - this.imag * c.imag;
        double newImag = this.real * c.imag + this.imag * c.real;
        this.real = newReal;
        this.imag = newImag;
    }

    public void divide(ComplexNumber c) {
        double denom = c.real * c.real + c.imag * c.imag;
        if (denom == 0) {
            throw new ArithmeticException("Division durch Null");
        }
        double newReal = (this.real * c.real + this.imag * c.imag) / denom;
        double newImag = (this.imag * c.real - this.real * c.imag) / denom;
        this.real = newReal;
        this.imag = newImag;
    }

    public ComplexNumber conjugate() {
        return new ComplexNumber(real, -imag);
    }

    public double getReal() {
        return real;
    }

    public void setReal(double real) {
        this.real = real;
    }

    public double getImag() {
        return imag;
    }

    public void setImag(double imag) {
        this.imag = imag;
    }

    @Override
    public String toString() {
        return String.format("%f %s %fi", real, (imag >= 0 ? "+" : "-"), Math.abs(imag));
    }
}
