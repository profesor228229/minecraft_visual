package dev.nebula.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayDeque;
import java.util.Deque;

public final class Render2D {
    private Render2D() {}

    /** Color as a function of position inside a shape. */
    public interface ColorFn { int at(float x, float y); }

    public static void setup() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.disableAlphaTest();
        RenderSystem.disableCull();
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
    }

    public static void end() {
        RenderSystem.shadeModel(GL11.GL_FLAT);
        RenderSystem.enableCull();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
        RenderSystem.color4f(1, 1, 1, 1);
    }

    private static void v(BufferBuilder b, Matrix4f m, float x, float y, int c) {
        b.vertex(m, x, y, 0).color(ColorUtil.red(c), ColorUtil.green(c), ColorUtil.blue(c), ColorUtil.alpha(c)).next();
    }

    public static void rect(MatrixStack ms, float x, float y, float w, float h, int c) {
        gradient(ms, x, y, w, h, c, c, c, c);
    }

    /** Gradient quad: top-left, top-right, bottom-right, bottom-left. */
    public static void gradient(MatrixStack ms, float x, float y, float w, float h, int tl, int tr, int br, int bl) {
        if (w <= 0 || h <= 0) return;
        setup();
        Matrix4f m = ms.peek().getModel();
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);
        v(b, m, x, y, tl);
        v(b, m, x, y + h, bl);
        v(b, m, x + w, y + h, br);
        v(b, m, x + w, y, tr);
        Tessellator.getInstance().draw();
        end();
    }

    public static void horizontalGradient(MatrixStack ms, float x, float y, float w, float h, int left, int right) {
        gradient(ms, x, y, w, h, left, right, right, left);
    }

    public static void verticalGradient(MatrixStack ms, float x, float y, float w, float h, int top, int bottom) {
        gradient(ms, x, y, w, h, top, top, bottom, bottom);
    }

    public static void rounded(MatrixStack ms, float x, float y, float w, float h, float r, int c) {
        roundedShape(ms, x, y, w, h, r, (px, py) -> c);
    }

    public static void roundedHorizontal(MatrixStack ms, float x, float y, float w, float h, float r, int left, int right) {
        roundedShape(ms, x, y, w, h, r, (px, py) -> ColorUtil.lerp(left, right, (px - x) / w));
    }

    public static void roundedVertical(MatrixStack ms, float x, float y, float w, float h, float r, int top, int bottom) {
        roundedShape(ms, x, y, w, h, r, (px, py) -> ColorUtil.lerp(top, bottom, (py - y) / h));
    }

    /** Rounded rectangle drawn as a triangle fan; per-vertex color supplied by fn. */
    public static void roundedShape(MatrixStack ms, float x, float y, float w, float h, float r, ColorFn fn) {
        if (w <= 0 || h <= 0) return;
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2f));
        setup();
        Matrix4f m = ms.peek().getModel();
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(GL11.GL_TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        float cx = x + w / 2f, cy = y + h / 2f;
        v(b, m, cx, cy, fn.at(cx, cy));
        float[][] corners = {{x + w - r, y + r, -90}, {x + w - r, y + h - r, 0}, {x + r, y + h - r, 90}, {x + r, y + r, 180}};
        int seg = r < 3 ? 3 : 8;
        float fx = 0, fy = 0;
        boolean first = true;
        for (float[] cr : corners) {
            for (int i = 0; i <= seg; i++) {
                double a = Math.toRadians(cr[2] + 90.0 * i / seg);
                float px = (float) (cr[0] + Math.cos(a) * r);
                float py = (float) (cr[1] + Math.sin(a) * r);
                if (first) { fx = px; fy = py; first = false; }
                v(b, m, px, py, fn.at(px, py));
            }
        }
        v(b, m, fx, fy, fn.at(fx, fy));
        Tessellator.getInstance().draw();
        end();
    }

    /** Rounded outline with optional gradient. */
    public static void roundedOutline(MatrixStack ms, float x, float y, float w, float h, float r, float width, ColorFn fn) {
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2f));
        setup();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        RenderSystem.lineWidth(width * (float) MinecraftClient.getInstance().getWindow().getScaleFactor() / 2f);
        Matrix4f m = ms.peek().getModel();
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(GL11.GL_LINE_LOOP, VertexFormats.POSITION_COLOR);
        float[][] corners = {{x + w - r, y + r, -90}, {x + w - r, y + h - r, 0}, {x + r, y + h - r, 90}, {x + r, y + r, 180}};
        for (float[] cr : corners) {
            for (int i = 0; i <= 8; i++) {
                double a = Math.toRadians(cr[2] + 90.0 * i / 8);
                float px = (float) (cr[0] + Math.cos(a) * r);
                float py = (float) (cr[1] + Math.sin(a) * r);
                v(b, m, px, py, fn.at(px, py));
            }
        }
        Tessellator.getInstance().draw();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderSystem.lineWidth(1f);
        end();
    }

    /** Soft drop shadow / glow around a rounded rect. */
    public static void shadow(MatrixStack ms, float x, float y, float w, float h, float r, float size, int color) {
        int layers = 10;
        int baseA = ColorUtil.alpha(color);
        for (int i = layers; i >= 1; i--) {
            float t = i / (float) layers;
            float grow = size * t;
            int a = (int) (baseA * Math.pow(1 - t, 2) / 3.0);
            if (a <= 0) continue;
            rounded(ms, x - grow, y - grow, w + grow * 2, h + grow * 2, r + grow, ColorUtil.withAlpha(color, a));
        }
    }

    public static void circle(MatrixStack ms, float cx, float cy, float r, int c) {
        setup();
        Matrix4f m = ms.peek().getModel();
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(GL11.GL_TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        v(b, m, cx, cy, c);
        for (int i = 0; i <= 32; i++) {
            double a = Math.PI * 2 * i / 32;
            v(b, m, (float) (cx + Math.cos(a) * r), (float) (cy + Math.sin(a) * r), c);
        }
        Tessellator.getInstance().draw();
        end();
    }

    /** Thick arc from startDeg sweeping sweepDeg, gradient along the arc. */
    public static void arc(MatrixStack ms, float cx, float cy, float r, float thickness, float startDeg, float sweepDeg, int c1, int c2) {
        if (Math.abs(sweepDeg) < 0.5f) return;
        setup();
        Matrix4f m = ms.peek().getModel();
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(GL11.GL_TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        int seg = Math.max(6, (int) (Math.abs(sweepDeg) / 6));
        for (int i = 0; i <= seg; i++) {
            double t = i / (double) seg;
            double a = Math.toRadians(startDeg + sweepDeg * t);
            int c = ColorUtil.lerp(c1, c2, t);
            float cos = (float) Math.cos(a), sin = (float) Math.sin(a);
            v(b, m, cx + cos * r, cy + sin * r, c);
            v(b, m, cx + cos * (r - thickness), cy + sin * (r - thickness), c);
        }
        Tessellator.getInstance().draw();
        end();
    }

    public static boolean hovered(double mx, double my, double x, double y, double w, double h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    // ---------------------------------------------------------------- scissor
    private static final Deque<double[]> SCISSOR = new ArrayDeque<>();

    /** Push a scissor rectangle (screen-space scaled coords), intersected with the current one. */
    public static void pushScissor(double x, double y, double w, double h) {
        if (!SCISSOR.isEmpty()) {
            double[] p = SCISSOR.peek();
            double nx = Math.max(x, p[0]), ny = Math.max(y, p[1]);
            double nx2 = Math.min(x + w, p[0] + p[2]), ny2 = Math.min(y + h, p[1] + p[3]);
            x = nx; y = ny; w = Math.max(0, nx2 - nx); h = Math.max(0, ny2 - ny);
        }
        SCISSOR.push(new double[]{x, y, w, h});
        apply(x, y, w, h);
    }

    public static void popScissor() {
        SCISSOR.poll();
        if (SCISSOR.isEmpty()) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        } else {
            double[] p = SCISSOR.peek();
            apply(p[0], p[1], p[2], p[3]);
        }
    }

    private static void apply(double x, double y, double w, double h) {
        Window win = MinecraftClient.getInstance().getWindow();
        double s = win.getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int) Math.floor(x * s), (int) Math.floor(win.getFramebufferHeight() - (y + h) * s),
                (int) Math.ceil(Math.max(0, w * s)), (int) Math.ceil(Math.max(0, h * s)));
    }
}
