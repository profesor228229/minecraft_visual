package dev.nebula.util;

import java.awt.Color;

public final class ColorUtil {
    private ColorUtil() {}

    public static int rgba(int r, int g, int b, int a) {
        return (clamp(a) << 24) | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
    }

    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }

    public static int red(int c) { return (c >> 16) & 0xFF; }
    public static int green(int c) { return (c >> 8) & 0xFF; }
    public static int blue(int c) { return c & 0xFF; }
    public static int alpha(int c) { return (c >>> 24) & 0xFF; }

    public static int withAlpha(int c, int a) { return (c & 0x00FFFFFF) | (clamp(a) << 24); }

    /** Multiply the alpha channel of a color by a factor 0..1. */
    public static int fade(int c, double factor) {
        return withAlpha(c, (int) (alpha(c) * Math.max(0, Math.min(1, factor))));
    }

    public static int lerp(int a, int b, double t) {
        t = Math.max(0, Math.min(1, t));
        return rgba(
                (int) (red(a) + (red(b) - red(a)) * t),
                (int) (green(a) + (green(b) - green(a)) * t),
                (int) (blue(a) + (blue(b) - blue(a)) * t),
                (int) (alpha(a) + (alpha(b) - alpha(a)) * t));
    }

    public static int darker(int c, double f) {
        return rgba((int) (red(c) * f), (int) (green(c) * f), (int) (blue(c) * f), alpha(c));
    }

    public static int rainbow(double offset, float sat, float bri) {
        double hue = ((System.currentTimeMillis() / 4000.0) + offset) % 1.0;
        return 0xFF000000 | Color.HSBtoRGB((float) hue, sat, bri);
    }

    /** Smooth ping-pong between two colors. */
    public static int wave(int a, int b, double speed, double offset) {
        double t = (Math.sin(System.currentTimeMillis() / 1000.0 * speed + offset) + 1) / 2.0;
        return lerp(a, b, t);
    }

    public static float[] toFloat(int c) {
        return new float[]{red(c) / 255f, green(c) / 255f, blue(c) / 255f, alpha(c) / 255f};
    }
}
