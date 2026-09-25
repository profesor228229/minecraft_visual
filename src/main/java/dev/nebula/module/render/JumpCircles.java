package dev.nebula.module.render;

import dev.nebula.module.Category;
import dev.nebula.module.Module;
import dev.nebula.setting.NumberSetting;
import dev.nebula.util.ColorUtil;
import dev.nebula.util.Easing;
import dev.nebula.util.Render3D;
import dev.nebula.util.Theme;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class JumpCircles extends Module {
    private final NumberSetting radius = add(new NumberSetting("Radius", 1.2, 0.4, 3.0, 0.1));
    private final NumberSetting duration = add(new NumberSetting("Duration", 1.2, 0.3, 3.0, 0.1, "s"));
    private final NumberSetting thickness = add(new NumberSetting("Thickness", 0.25, 0.05, 1.0, 0.05));

    private final List<double[]> circles = new ArrayList<>(); // x,y,z,startMs
    private boolean wasOnGround = true;

    public JumpCircles() {
        super("JumpCircles", "Expanding rings where you jump.", Category.RENDER);
    }

    @Override
    public void onTick() {
        boolean og = mc.player.isOnGround();
        if (wasOnGround && !og && mc.player.getVelocity().y > 0.1) {
            circles.add(new double[]{mc.player.prevX, mc.player.prevY + 0.01, mc.player.prevZ, System.currentTimeMillis()});
        }
        wasOnGround = og;
    }

    @Override
    public void onRender3D(MatrixStack ms, float delta) {
        if (circles.isEmpty()) return;
        long now = System.currentTimeMillis();
        long dur = (long) (duration.get() * 1000);
        circles.removeIf(c -> now - c[3] > dur);
        Vec3d cam = Render3D.camPos();
        Render3D.setup(true);
        for (double[] c : circles) {
            double t = (now - c[3]) / (double) dur;
            double e = Easing.OUT_CUBIC.apply(t);
            double outer = radius.get() * e;
            double inner = Math.max(0, outer - thickness.get() * (1 - t * 0.5));
            double a = 1 - Easing.IN_OUT_CUBIC.apply(t);
            double rot = t * Math.PI;
            int ia = (int) (40 * a), oa = (int) (230 * a);
            Render3D.ring(ms, c[0] - cam.x, c[1] - cam.y, c[2] - cam.z, inner, outer,
                    ColorUtil.withAlpha(0, ia), ColorUtil.withAlpha(0, oa), rot, true,
                    tt -> Theme.color(Math.sin(tt * Math.PI * 2) * 2));
            // thin bright edge
            Render3D.ring(ms, c[0] - cam.x, c[1] - cam.y + 0.001, c[2] - cam.z, outer, outer + 0.03,
                    ColorUtil.withAlpha(0, (int) (255 * a)), ColorUtil.withAlpha(0, 0), rot, true,
                    tt -> ColorUtil.lerp(Theme.color(Math.sin(tt * Math.PI * 2) * 2), 0xFFFFFFFF, 0.3));
        }
        Render3D.end();
    }
}
