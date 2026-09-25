package dev.nebula.module.render;

import dev.nebula.module.Category;
import dev.nebula.module.Module;
import dev.nebula.setting.ModeSetting;
import dev.nebula.setting.NumberSetting;
import dev.nebula.util.*;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class TargetEsp extends Module {
    private final ModeSetting mode = add(new ModeSetting("Mode", "Circle", "Circle", "Souls", "Box"));
    private final NumberSetting speed = add(new NumberSetting("Speed", 1.0, 0.3, 3.0, 0.1));

    private final EaseAnimation show = new EaseAnimation(400, Easing.OUT_CUBIC);
    private LivingEntity last;

    public TargetEsp() {
        super("TargetEsp", "Highlights your current combat target.", Category.RENDER);
    }

    @Override public String getSuffix() { return mode.get(); }

    @Override
    public void onRender3D(MatrixStack ms, float delta) {
        LivingEntity t = TargetTracker.get();
        show.setDirection(t != null);
        if (t != null) last = t;
        double p = show.get();
        if (p <= 0.01 || last == null || last.world != mc.world) return;

        Vec3d pos = Render3D.interpolated(last, delta).subtract(Render3D.camPos());
        double time = System.currentTimeMillis() / 1000.0 * speed.get();
        double w = last.getWidth() * 0.8 + 0.2, h = last.getHeight();
        float hurt = last.hurtTime / 10f;

        Render3D.setup(true);
        if (mode.is("Circle")) {
            double wave = (Math.sin(time * 2) + 1) / 2;
            double y = pos.y + wave * h;
            double dir = Math.cos(time * 2) > 0 ? -1 : 1; // trail opposite to movement
            double r = w * (0.4 + 0.6 * p);
            Matrix4f m = ms.peek().getModel();
            BufferBuilder b = Tessellator.getInstance().getBuffer();
            b.begin(GL11.GL_TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            for (int i = 0; i <= 64; i++) {
                double tt = i / 64.0;
                double a = tt * Math.PI * 2;
                int c = ColorUtil.lerp(Theme.color(Math.sin(a) * 2), 0xFFFF3050, hurt);
                double cx = pos.x + Math.cos(a) * r, cz = pos.z + Math.sin(a) * r;
                Render3D.v(b, m, cx, y, cz, ColorUtil.fade(c, 0.8 * p));
                Render3D.v(b, m, cx, y + dir * 0.45 * Math.abs(Math.cos(time * 2)), cz, ColorUtil.withAlpha(c, 0));
            }
            Tessellator.getInstance().draw();
            com.mojang.blaze3d.systems.RenderSystem.lineWidth(2.5f);
            b.begin(GL11.GL_LINE_STRIP, VertexFormats.POSITION_COLOR);
            for (int i = 0; i <= 64; i++) {
                double a = i / 64.0 * Math.PI * 2;
                int c = ColorUtil.lerp(Theme.color(Math.sin(a) * 2), 0xFFFF3050, hurt);
                Render3D.v(b, m, pos.x + Math.cos(a) * r, y, pos.z + Math.sin(a) * r, ColorUtil.fade(c, p));
            }
            Tessellator.getInstance().draw();
            com.mojang.blaze3d.systems.RenderSystem.lineWidth(1f);
        } else if (mode.is("Souls")) {
            Render3D.additive();
            for (int s = 0; s < 3; s++) {
                for (int k = 0; k < 24; k++) {
                    double tt = time * 2.2 - k * 0.025 + s * (Math.PI * 2 / 3);
                    double r = w * 0.9 * p;
                    double x = pos.x + Math.cos(tt) * r;
                    double z = pos.z + Math.sin(tt) * r;
                    double y = pos.y + h * 0.5 + Math.sin(tt * 1.3 + s) * h * 0.4;
                    double fade = (1 - k / 24.0) * p;
                    int c = ColorUtil.lerp(Theme.color(s * 1.5 + k * 0.05), 0xFFFF3050, hurt);
                    ms.push();
                    ms.translate(x, y, z);
                    ms.multiply(Render3D.camera().getRotation());
                    Render3D.glowDisc(ms, (float) (0.22 * (1 - k / 30.0)), ColorUtil.fade(c, fade * 0.6));
                    if (k == 0) Render3D.glowDisc(ms, 0.07f, ColorUtil.fade(0xFFFFFFFF, p));
                    ms.pop();
                }
            }
        } else {
            double pulse = 0.05 * Math.sin(time * 4);
            double half = last.getWidth() / 2 + 0.05 + pulse;
            Vec3d wp = pos.add(Render3D.camPos());
            Box box = new Box(wp.x - half, wp.y, wp.z - half, wp.x + half, wp.y + h + 0.1, wp.z + half);
            int c = ColorUtil.lerp(Theme.accent(), 0xFFFF3050, hurt);
            Render3D.filledBox(ms, box, ColorUtil.fade(c, 0.25 * p));
            Render3D.outlineBox(ms, box, ColorUtil.fade(c, p), 2f);
        }
        Render3D.end();
    }
}
