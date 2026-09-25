package dev.nebula.module.render;

import dev.nebula.module.Category;
import dev.nebula.module.Module;
import dev.nebula.setting.BoolSetting;
import dev.nebula.setting.ModeSetting;
import dev.nebula.setting.NumberSetting;
import dev.nebula.util.ColorUtil;
import dev.nebula.util.Render3D;
import dev.nebula.util.Theme;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class HitParticles extends Module {
    private final ModeSetting mode = add(new ModeSetting("Mode", "Orbs", "Orbs", "Sparks", "Stars"));
    private final NumberSetting count = add(new NumberSetting("Count", 18, 4, 60, 1));
    private final NumberSetting size = add(new NumberSetting("Size", 0.12, 0.04, 0.4, 0.01));
    private final NumberSetting life = add(new NumberSetting("Lifetime", 1.6, 0.4, 4, 0.1, "s"));
    private final NumberSetting speed = add(new NumberSetting("Speed", 1.0, 0.2, 3.0, 0.1));
    private final BoolSetting gravity = add(new BoolSetting("Gravity", true));
    private final BoolSetting physics = add(new BoolSetting("Collide", true));

    private final List<P> particles = new ArrayList<>();
    private final Random rnd = new Random();
    private long lastFrame = System.nanoTime();

    public HitParticles() {
        super("HitParticles", "Glowing particles when you hit an entity.", Category.RENDER);
    }

    @Override public String getSuffix() { return mode.get(); }

    @Override
    public void onAttack(Entity target) {
        Vec3d c = target.getPos().add(0, target.getHeight() * 0.6, 0);
        for (int i = 0; i < count.getInt(); i++) {
            double yaw = rnd.nextDouble() * Math.PI * 2;
            double pitch = (rnd.nextDouble() - 0.3) * Math.PI;
            double s = (1.5 + rnd.nextDouble() * 3.5) * speed.get();
            Vec3d v = new Vec3d(Math.cos(yaw) * Math.cos(pitch) * s, Math.sin(pitch) * s + 1.5, Math.sin(yaw) * Math.cos(pitch) * s);
            particles.add(new P(c.add((rnd.nextDouble() - 0.5) * target.getWidth(), (rnd.nextDouble() - 0.5) * target.getHeight() * 0.5,
                    (rnd.nextDouble() - 0.5) * target.getWidth()), v, life.get() * (0.7 + rnd.nextDouble() * 0.6), i * 0.3));
        }
        if (particles.size() > 800) particles.subList(0, particles.size() - 800).clear();
    }

    private boolean solid(double x, double y, double z) {
        BlockPos p = new BlockPos(x, y, z);
        return !mc.world.getBlockState(p).getCollisionShape(mc.world, p).isEmpty();
    }

    @Override
    public void onRender3D(MatrixStack ms, float delta) {
        long now = System.nanoTime();
        double dt = Math.min(0.05, (now - lastFrame) / 1e9);
        lastFrame = now;
        if (particles.isEmpty()) return;

        Vec3d cam = Render3D.camPos();
        Render3D.setup(true);
        Render3D.additive();
        Iterator<P> it = particles.iterator();
        while (it.hasNext()) {
            P p = it.next();
            p.age += dt;
            if (p.age >= p.life) { it.remove(); continue; }
            // physics
            if (gravity.get()) p.vel = p.vel.add(0, -9.0 * dt, 0);
            p.vel = p.vel.multiply(Math.pow(0.35, dt));
            Vec3d next = p.pos.add(p.vel.multiply(dt));
            if (physics.get()) {
                double vx = p.vel.x, vy = p.vel.y, vz = p.vel.z;
                if (solid(next.x, p.pos.y, p.pos.z)) vx = -vx * 0.5;
                if (solid(p.pos.x, next.y, p.pos.z)) { vy = -vy * 0.45; vx *= 0.8; vz *= 0.8; }
                if (solid(p.pos.x, p.pos.y, next.z)) vz = -vz * 0.5;
                p.vel = new Vec3d(vx, vy, vz);
                next = p.pos.add(p.vel.multiply(dt));
                if (solid(next.x, next.y, next.z)) next = p.pos;
            }
            Vec3d prev = p.pos;
            p.pos = next;

            double t = p.age / p.life;
            double alpha = t < 0.1 ? t / 0.1 : 1 - Math.pow((t - 0.1) / 0.9, 2);
            int col = Theme.color(p.phase);
            float sz = (float) (size.get() * (1 - t * 0.5));

            if (mode.is("Sparks")) {
                Matrix4f m = ms.peek().getModel();
                BufferBuilder b = Tessellator.getInstance().getBuffer();
                com.mojang.blaze3d.systems.RenderSystem.lineWidth(2.5f);
                b.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR);
                Vec3d tail = p.pos.subtract(p.vel.multiply(0.06));
                Render3D.v(b, m, p.pos.x - cam.x, p.pos.y - cam.y, p.pos.z - cam.z, ColorUtil.fade(col, alpha));
                Render3D.v(b, m, tail.x - cam.x, tail.y - cam.y, tail.z - cam.z, ColorUtil.withAlpha(col, 0));
                Tessellator.getInstance().draw();
                com.mojang.blaze3d.systems.RenderSystem.lineWidth(1f);
            }
            ms.push();
            ms.translate(p.pos.x - cam.x, p.pos.y - cam.y, p.pos.z - cam.z);
            ms.multiply(Render3D.camera().getRotation());
            if (mode.is("Stars")) {
                ms.multiply(net.minecraft.client.util.math.Vector3f.POSITIVE_Z.getDegreesQuaternion((float) (p.age * 180 + p.phase * 60)));
                star(ms, sz * 1.8f, ColorUtil.fade(col, alpha));
            }
            Render3D.glowDisc(ms, sz * 2.5f, ColorUtil.fade(col, alpha * 0.45));
            Render3D.glowDisc(ms, sz * 0.9f, ColorUtil.fade(ColorUtil.lerp(col, 0xFFFFFFFF, 0.5), alpha));
            ms.pop();
        }
        Render3D.end();
    }

    private static void star(MatrixStack ms, float r, int c) {
        Matrix4f m = ms.peek().getModel();
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(GL11.GL_TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        Render3D.v(b, m, 0, 0, 0, c);
        for (int i = 0; i <= 10; i++) {
            double a = Math.PI * 2 * i / 10 - Math.PI / 2;
            double rr = i % 2 == 0 ? r : r * 0.4;
            Render3D.v(b, m, Math.cos(a) * rr, Math.sin(a) * rr, 0, ColorUtil.fade(c, 0.6));
        }
        Tessellator.getInstance().draw();
    }

    private static class P {
        Vec3d pos, vel;
        final double life, phase;
        double age;

        P(Vec3d pos, Vec3d vel, double life, double phase) {
            this.pos = pos; this.vel = vel; this.life = life; this.phase = phase;
        }
    }
}
