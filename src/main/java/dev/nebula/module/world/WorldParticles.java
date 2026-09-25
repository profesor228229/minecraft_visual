package dev.nebula.module.world;

import dev.nebula.module.Category;
import dev.nebula.module.Module;
import dev.nebula.setting.NumberSetting;
import dev.nebula.util.ColorUtil;
import dev.nebula.util.Render3D;
import dev.nebula.util.Theme;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Ambient fireflies drifting around the player. */
public class WorldParticles extends Module {
    private final NumberSetting amount = add(new NumberSetting("Amount", 60, 10, 250, 5));
    private final NumberSetting range = add(new NumberSetting("Range", 12, 4, 32, 1));
    private final NumberSetting size = add(new NumberSetting("Size", 0.08, 0.03, 0.25, 0.01));

    private final List<double[]> list = new ArrayList<>(); // x,y,z,vx,vy,vz,age,life,phase
    private final Random rnd = new Random();
    private long lastFrame = System.nanoTime();

    public WorldParticles() {
        super("Fireflies", "Glowing ambient particles around you.", Category.WORLD);
    }

    @Override public void onDisable() { list.clear(); }

    private double[] spawn(Vec3d c) {
        double r = range.get();
        return new double[]{c.x + (rnd.nextDouble() - 0.5) * 2 * r, c.y + rnd.nextDouble() * r * 0.5 - 1, c.z + (rnd.nextDouble() - 0.5) * 2 * r,
                (rnd.nextDouble() - 0.5) * 0.4, (rnd.nextDouble() - 0.3) * 0.2, (rnd.nextDouble() - 0.5) * 0.4,
                0, 4 + rnd.nextDouble() * 6, rnd.nextDouble() * 10};
    }

    @Override
    public void onRender3D(MatrixStack ms, float delta) {
        long now = System.nanoTime();
        double dt = Math.min(0.05, (now - lastFrame) / 1e9);
        lastFrame = now;
        Vec3d pp = Render3D.interpolated(mc.player, delta);
        while (list.size() < amount.getInt()) list.add(spawn(pp));
        while (list.size() > amount.getInt()) list.remove(list.size() - 1);

        Vec3d cam = Render3D.camPos();
        Render3D.setup(true);
        Render3D.additive();
        double r2 = range.get() * range.get() * 1.6;
        for (int i = 0; i < list.size(); i++) {
            double[] p = list.get(i);
            p[6] += dt;
            double dx = p[0] - pp.x, dz = p[2] - pp.z;
            if (p[6] > p[7] || dx * dx + dz * dz > r2) { list.set(i, spawn(pp)); continue; }
            p[3] += Math.sin(p[6] * 1.3 + p[8]) * 0.2 * dt;
            p[5] += Math.cos(p[6] * 1.1 + p[8]) * 0.2 * dt;
            p[0] += p[3] * dt; p[1] += p[4] * dt; p[2] += p[5] * dt;
            double life = p[6] / p[7];
            double a = Math.min(1, Math.min(life * 5, (1 - life) * 5)) * (0.6 + 0.4 * Math.sin(p[6] * 4 + p[8]));
            int c = Theme.color(p[8]);
            ms.push();
            ms.translate(p[0] - cam.x, p[1] - cam.y, p[2] - cam.z);
            ms.multiply(Render3D.camera().getRotation());
            Render3D.glowDisc(ms, (float) (size.get() * 3), ColorUtil.fade(c, a * 0.4));
            Render3D.glowDisc(ms, size.getFloat(), ColorUtil.fade(ColorUtil.lerp(c, 0xFFFFFFFF, 0.5), a));
            ms.pop();
        }
        Render3D.end();
    }
}
