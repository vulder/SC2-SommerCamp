package de.uni_passau.fim.sommercamp.sc2.util;

import com.github.ocraft.s2client.protocol.spatial.Point2d;
import org.junit.Test;

import static org.junit.Assert.*;

public class Vec2Test {

    @Test
    public void getPoint2d() {
        Point2d p = Point2d.of(12.3f, 4.56f);
        Vec2 a = Vec2.of(12.3f, 4.56f);
        Vec2 b = Vec2.of(-1f, 0f);
        Vec2 c = Vec2.of(0f, 256f);
        assertTrue(a.getPoint2d().isPresent());
        assertEquals(p, a.getPoint2d().get());
        assertFalse(b.getPoint2d().isPresent());
        assertFalse(c.getPoint2d().isPresent());
    }

    @Test
    public void testGetLength() {
        Vec2 v = Vec2.of(3f, 4f);
        assertEquals(5f, v.getLength(), 0f);
    }

    @Test
    public void testPlus() {
        Vec2 a = Vec2.of(1f, 2f);
        Vec2 b = Vec2.of(10f, -20f);
        Vec2 sum = a.plus(b);
        assertEquals(11, sum.getX(), 0f);
        assertEquals(-18, sum.getY(), 0f);
    }

    @Test
    public void testMinus() {
        Vec2 a = Vec2.of(1f, 2f);
        Vec2 b = Vec2.of(10f, -20f);
        Vec2 difference = a.minus(b);
        assertEquals(-9, difference.getX(), 0f);
        assertEquals(22, difference.getY(), 0f);
    }

    @Test
    public void testNegated() {
        Vec2 v = Vec2.of(10f, -20f);
        Vec2 negated = v.negated();
        assertEquals(-10, negated.getX(), 0f);
        assertEquals(20, negated.getY(), 0f);
    }

    @Test
    public void testNormalized() {
        Vec2 a = Vec2.of(42.0f, 0f).normalized();
        Vec2 b = Vec2.of(0f, -12.3f).normalized();
        Vec2 c = Vec2.of(3.9f, 7.6f).normalized();
        assertEquals(1f, a.getX(), 0f);
        assertEquals(-1f, b.getY(), 0f);
        assertEquals(1f, a.getLength(), 0.0001f);
        assertEquals(1f, b.getLength(), 0.0001f);
        assertEquals(1f, c.getLength(), 0.0001f);
    }

    @Test
    public void testScaled() {
        Vec2 v = Vec2.of(0.1f, 0.5f);
        Vec2 scaled = v.scaled(10);
        assertEquals(1f, scaled.getX(), 0.0001f);
        assertEquals(5f, scaled.getY(), 0.0001f);
        Vec2 scaled2 = v.scaled(-65.4f);
        assertEquals(-6.54f, scaled2.getX(), 0.0001f);
        assertEquals(-32.7f, scaled2.getY(), 0.0001f);
    }

    @Test
    public void testDotProduct() {
        Vec2 a = Vec2.of(1f, 0f);
        Vec2 b = Vec2.of(0f, -1f);
        Vec2 c = Vec2.of(-9f, -11f);
        Vec2 d = Vec2.of(5f, -8f);
        assertEquals(0, Vec2.dotProduct(a, b), 0f);
        assertEquals(43, Vec2.dotProduct(c, d), 0f);
    }
}