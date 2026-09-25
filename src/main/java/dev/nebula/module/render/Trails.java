package dev.nebula.module.render;

import dev.nebula.module.Category;
import dev.nebula.module.Module;
import dev.nebula.setting.BoolSetting;
import dev.nebula.setting.NumberSetting;
import dev.nebula.util.ColorUtil;
import dev.nebula.util.Render3D;
import dev.nebula.util.Theme;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class Trails extends Module {
    private final NumberSetting length = add(new NumberSetting("Length", 1.0, 0.2, 4.0, 0.1, "s"));
    private final NumberSetting heightS = add(new NumberSetting("Height", 0.9, 0.1, 1.8, 0.05));
    private final BoolSetting firstPerson = add(new BoolSetting("First Person", false));

    private final List<double[]> points = new ArrayList<>(); // x,y,z,time

    public Trails() {
        super("Trails", "Smooth ribbon that follows you.", Category.RENDER);
    }

    @Override public void onDisable() { points.clear(); }

    @Override
    public void onRender3D(MatrixStack ms, float delta) {
        long now = System.currentTimeMillis();
        Vec3d p = Render3D.interpolated(mc.player, delta);
        if (points.isEmpty() || p.squaredDistanceTo(new Vec3d(points.get(points.size() - 1)[0], points.get(points.size() - 1)[1], points.get(points.size() - 1)[2])) > 0.0004) {
            points.add(new double[]{p.x, p.y, p.z, now});
        }
        long max = (long) (length.get() * 1000);
        points.removeIf(pt -> now - pt[3] > max);
        if (points.size() < 2) return;
        if (mc.options.getPerspective().isFirstPerson() && !firstPerson.get()) return;

        Vec3d cam = Render3D.camPos();
        Render3D.setup(true);
        Matrix4f m = ms.peek().getModel();
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(GL11.GL_TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        double h = heightS.get();
        int n = points.size();
        for (int i = 0; i < n; i++) {
            double[] pt = points.get(i);
            double age = (now - pt[3]) / (double) max;
            double a = (1 - age) * Math.min(1, i / 4.0);
            int c = Theme.color(i * 0.04);
            Render3D.v(b, m, pt[0] - cam.x, pt[1] - cam.y, pt[2] - cam.z, ColorUtil.fade(c, a * 0.7));
            Render3D.v(b, m, pt[0] - cam.x, pt[1] + h - cam.y, pt[2] - cam.z, ColorUtil.fade(c, a * 0.15));
        }
        Tessellator.getInstance().draw();
        com.mojang.blaze3d.systems.RenderSystem.lineWidth(2f);
        b.begin(GL11.GL_LINE_STRIP, VertexFormats.POSITION_COLOR);
        for (int i = 0; i < n; i++) {
            double[] pt = points.get(i);
            double a = 1 - (now - pt[3]) / (double) max;
            Render3D.v(b, m, pt[0] - cam.x, pt[1] + h - cam.y, pt[2] - cam.z, ColorUtil.fade(Theme.color(i * 0.04), a));
        }
        Tessellator.getInstance().draw();
        com.mojang.blaze3d.systems.RenderSystem.lineWidth(1f);
        Render3D.end();
    }
}
