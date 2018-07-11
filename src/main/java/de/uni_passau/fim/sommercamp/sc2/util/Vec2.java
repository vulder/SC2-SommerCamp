package de.uni_passau.fim.sommercamp.sc2.util;

import com.github.ocraft.s2client.protocol.spatial.Point2d;

import java.util.Optional;

/**
 * A 2-dimensional vector providing common vector operations.
 */
@SuppressWarnings({"WeakerAccess", "SameParameterValue"})
public class Vec2 {
    private float x;
    private float y;
    private float length;

    private Vec2(float x, float y) {
        this.x = x;
        this.y = y;
        this.length = (float) Math.sqrt(dotProduct(this, this));
    }

    /**
     * Creates a new {@link Vec2 vector} from the given coordinates.
     *
     * @param x the x coordinate of the vector
     * @param y the y coordinate of the vector
     * @return the vector representing the given {@code x} and {@code y} coordinates
     * @see #getX()
     * @see #getY()
     */
    public static Vec2 of(float x, float y) {
        return new Vec2(x, y);
    }

    /**
     * Returns the wrapped {@link Point2d} if it {@link #isValidPoint2d() is a valid point} in the view of the StarCraft II API.
     *
     * @return the wrapped {@link Point2d}
     * @see Point2d
     * @see #isValidPoint2d()
     */
    public Optional<Point2d> getPoint2d() {
        if (isValidPoint2d()) {
            return Optional.of(Point2d.of(x, y));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Checks wether this vector is a valid {@link Point2d point} according to the StarCraft II API.
     *
     * @return {@code true} if the vector represents a valid point, otherwise {@code false}
     */
    public boolean isValidPoint2d() {
        return isBetween(x, 0f, 255f) && isBetween(y, 0f, 255f);
    }

    /**
     * Checks, if the given value is between the low value and the high value.
     *
     * @param val  the value to check
     * @param low  the low
     * @param high the high
     * @return {@code true}, if the value is between low and high
     */
    private boolean isBetween(float val, float low, float high) {
        return (val >= low) && (val <= high);
    }

    /**
     * Returns the x-coordinate of the vector.
     *
     * @return the x-coordinate of the vector
     * @see Point2d#getX()
     */
    public float getX() {
        return x;
    }

    /**
     * Returns the y-coordinate of the vector.
     *
     * @return the y-coordinate of the vector
     * @see Point2d#getY()
     */
    public float getY() {
        return y;
    }

    /**
     * Returns the (euclidean) length of the vector.
     *
     * @return the length of the vector
     */
    public float getLength() {
        return length;
    }

    /**
     * Returns a new {@link Vec2 vector} that is the sum of this vector and the vector given as a parameter.
     *
     * @param b the vector to add to this vector
     * @return the sum as a new {@link Vec2 vector}
     */
    public Vec2 plus(Vec2 b) {
        return Vec2.of(this.getX() + b.getX(), this.getY() + b.getY());
    }

    /**
     * Returns a new {@link Vec2 vector} that is the difference of this vector and the vector given as a parameter.
     *
     * @param b the vector to subtract from this vector
     * @return the difference as a new {@link Vec2 vector}
     */
    public Vec2 minus(Vec2 b) {
        return Vec2.of(this.getX() - b.getX(), this.getY() - b.getY());
    }

    /**
     * Returns a new vector that is the negated version of this vector.
     *
     * @return the negated version of this vector.
     */
    public Vec2 negated() {
        return scaled(-1f);
    }

    /**
     * Return a new vector that is the normalized version of this vector, that is,
     * the direction of the new vector is the same but its {@link #getLength() length} is 1.
     *
     * @return the normalized version of this vector
     */
    public Vec2 normalized() {
        return scaled(1f / getLength());
    }

    /**
     * Returns a new vector that is this vector scaled by a scalar {@code s}.
     *
     * @param s the scalar to scale the vector with
     * @return the scaled version of this vector
     */
    public Vec2 scaled(float s) {
        return Vec2.of(getX() * s, getY() * s);
    }

    /**
     * Returns the dot product {@code a * b} of two {@link Vec2 vectors}.
     *
     * @param a a vector
     * @param b another vector
     * @return the dot product of a and b
     */
    public static float dotProduct(Vec2 a, Vec2 b) {
        return (a.getX() * b.getX()) + (a.getY() * b.getY());
    }
}
