package dev.nebula.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public final class Render3D {
    private Render3D() {}

    public static Vec3d camPos() {
        return MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
    }

    public static Camera camera() {
        return MinecraftClient.getInstance().gameRenderer.getCamera();
    }

    public static Vec3d interpolated(Entity e, float delta) {
        return new Vec3d(
                MathHelper.lerp(delta, e.prevX, e.getX()),
                MathHelper.lerp(delta, e.prevY, e.getY()),
                MathHelper.lerp(delta, e.prevZ, e.getZ()));
    }

    public static void setup(boolean depth) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.disableAlphaTest();
        RenderSystem.disableCull();
        RenderSystem.disableLighting();
        RenderSystem.disableFog();
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        RenderSystem.depthMask(false);
        if (depth) RenderSystem.enableDepthTest(); else RenderSystem.disableDepthTest();
    }

    public static void additive() {
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
    }

    public static void end() {
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.shadeModel(GL11.GL_FLAT);
        RenderSystem.enableCull();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.color4f(1, 1, 1, 1);
    }

    public static void v(BufferBuilder b, Matrix4f m, double x, double y, double z, int c) {
        b.vertex(m, (float) x, (float) y, (float) z)
                .color(ColorUtil.red(c), ColorUtil.green(c), ColorUtil.blue(c), ColorUtil.alpha(c)).next();
    }

    /** Box in camera-relative coordinates. */
    public static void filledBox(MatrixStack ms, Box box, int c) {
        Vec3d cam = camPos();
        Box bb = box.offset(-cam.x, -cam.y, -cam.z);
        Matrix4f m = ms.peek().getModel();
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);
        double x1 = bb.minX, y1 = bb.minY, z1 = bb.minZ, x2 = bb.maxX, y2 = bb.maxY, z2 = bb.maxZ;
        // bottom
        v(b, m, x1, y1, z1, c); v(b, m, x2, y1, z1, c); v(b, m, x2, y1, z2, c); v(b, m, x1, y1, z2, c);
        // top
        v(b, m, x1, y2, z1, c); v(b, m, x1, y2, z2, c); v(b, m, x2, y2, z2, c); v(b, m, x2, y2, z1, c);
        // north
        v(b, m, x1, y1, z1, c); v(b, m, x1, y2, z1, c); v(b, m, x2, y2, z1, c); v(b, m, x2, y1, z1, c);
        // south
        v(b, m, x1, y1, z2, c); v(b, m, x2, y1, z2, c); v(b, m, x2, y2, z2, c); v(b, m, x1, y2, z2, c);
        // west
        v(b, m, x1, y1, z1, c); v(b, m, x1, y1, z2, c); v(b, m, x1, y2, z2, c); v(b, m, x1, y2, z1, c);
        // east
        v(b, m, x2, y1, z1, c); v(b, m, x2, y2, z1, c); v(b, m, x2, y2, z2, c); v(b, m, x2, y1, z2, c);
        Tessellator.getInstance().draw();
    }

    public static void outlineBox(MatrixStack ms, Box box, int c, float width) {
        Vec3d cam = camPos();
        Box bb = box.offset(-cam.x, -cam.y, -cam.z);
        Matrix4f m = ms.peek().getModel();
        RenderSystem.lineWidth(width);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR);
        double x1 = bb.minX, y1 = bb.minY, z1 = bb.minZ, x2 = bb.maxX, y2 = bb.maxY, z2 = bb.maxZ;
        double[][] e = {
                {x1, y1, z1, x2, y1, z1}, {x2, y1, z1, x2, y1, z2}, {x2, y1, z2, x1, y1, z2}, {x1, y1, z2, x1, y1, z1},
                {x1, y2, z1, x2, y2, z1}, {x2, y2, z1, x2, y2, z2}, {x2, y2, z2, x1, y2, z2}, {x1, y2, z2, x1, y2, z1},
                {x1, y1, z1, x1, y2, z1}, {x2, y1, z1, x2, y2, z1}, {x2, y1, z2, x2, y2, z2}, {x1, y1, z2, x1, y2, z2}};
        for (double[] l : e) {
            v(b, m, l[0], l[1], l[2], c);
            v(b, m, l[3], l[4], l[5], c);
        }
        Tessellator.getInstance().draw();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderSystem.lineWidth(1f);
    }

    /** Soft glowing disc in the XY plane of the current matrix. */
    public static void glowDisc(MatrixStack ms, float radius, int color) {
        Matrix4f m = ms.peek().getModel();
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(GL11.GL_TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        v(b, m, 0, 0, 0, color);
        int edge = ColorUtil.withAlpha(color, 0);
        for (int i = 0; i <= 20; i++) {
            double a = Math.PI * 2 * i / 20;
            v(b, m, Math.cos(a) * radius, Math.sin(a) * radius, 0, edge);
        }
        Tessellator.getInstance().draw();
    }

    /** Horizontal ring (triangle strip) around a point in camera-relative space. */
    public static void ring(MatrixStack ms, double x, double y, double z, double inner, double outer,
                            int innerColorBase, int outerColorBase, double rotation, boolean gradientAlongAngle,
                            java.util.function.DoubleFunction<Integer> angleColor) {
        Matrix4f m = ms.peek().getModel();
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(GL11.GL_TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        int seg = 64;
        for (int i = 0; i <= seg; i++) {
            double t = i / (double) seg;
            double a = t * Math.PI * 2 + rotation;
            double cos = Math.cos(a), sin = Math.sin(a);
            int base = gradientAlongAngle ? angleColor.apply(t) : 0xFFFFFFFF;
            int ci = gradientAlongAngle ? ColorUtil.withAlpha(base, ColorUtil.alpha(innerColorBase)) : innerColorBase;
            int co = gradientAlongAngle ? ColorUtil.withAlpha(base, ColorUtil.alpha(outerColorBase)) : outerColorBase;
            v(b, m, x + cos * inner, y, z + sin * inner, ci);
            v(b, m, x + cos * outer, y, z + sin * outer, co);
        }
        Tessellator.getInstance().draw();
    }
}
