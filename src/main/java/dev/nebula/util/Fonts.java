package dev.nebula.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;

public final class Fonts {
    private Fonts() {}

    private static TextRenderer tr() { return MinecraftClient.getInstance().textRenderer; }

    public static void draw(MatrixStack m, String s, float x, float y, int color) {
        if (ColorUtil.alpha(color) < 6) return; // vanilla treats alpha < 4 as opaque
        tr().draw(m, s, x, y, color);
    }

    public static void drawShadow(MatrixStack m, String s, float x, float y, int color) {
        if (ColorUtil.alpha(color) < 6) return;
        tr().drawWithShadow(m, s, x, y, color);
    }

    public static void drawCentered(MatrixStack m, String s, float cx, float y, int color) {
        draw(m, s, cx - width(s) / 2f, y, color);
    }

    /** Draws text with a per-character animated gradient. */
    public static void drawGradient(MatrixStack m, String s, float x, float y, double offset, int alpha, boolean shadow) {
        float cx = x;
        for (int i = 0; i < s.length(); i++) {
            String ch = String.valueOf(s.charAt(i));
            int c = ColorUtil.withAlpha(Theme.color(offset + i * 0.25), alpha);
            if (shadow) drawShadow(m, ch, cx, y, c); else draw(m, ch, cx, y, c);
            cx += width(ch);
        }
    }

    public static int width(String s) { return tr().getWidth(s); }
    public static int height() { return tr().fontHeight; }
}
