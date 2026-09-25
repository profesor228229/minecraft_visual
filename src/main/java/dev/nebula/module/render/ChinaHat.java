package dev.nebula.module.render;

import com.mojang.blaze3d.systems.RenderSystem;
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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class ChinaHat extends Module {
    private final NumberSetting radius = add(new NumberSetting("Radius", 0.65, 0.3, 1.2, 0.05));
    private final NumberSetting heightS = add(new NumberSetting("Height", 0.3, 0.1, 0.8, 0.05));
    private final NumberSetting alpha = add(new NumberSetting("Alpha", 0.55, 0.1, 1.0, 0.05));
    private final BoolSetting others = add(new BoolSetting("Others", false));
    private final BoolSetting firstPerson = add(new BoolSetting("First Person", false));

    public ChinaHat() {
        super("ChinaHat", "A gradient cone hat above the head.", Category.RENDER);
    }

    @Override
    public void onRender3D(MatrixStack ms, float delta) {
        Render3D.setup(true);
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player) {
                if (mc.options.getPerspective().isFirstPerson() && !firstPerson.get()) continue;
            } else if (!others.get() || p.isInvisible()) continue;
            render(ms, p, delta);
        }
        Render3D.end();
    }

    private void render(MatrixStack ms, PlayerEntity p, float delta) {
        Vec3d cam = Render3D.camPos();
        Vec3d pos = Render3D.interpolated(p, delta).subtract(cam);
        double top = pos.y + p.getHeight() + (p.isSneaking() ? -0.03 : 0.05);
        double yaw = Math.toRadians(-MathHelper.lerp(delta, p.prevHeadYaw, p.headYaw));
        double r = radius.get(), h = heightS.get();
        int a = (int) (255 * alpha.get());
        Matrix4f m = ms.peek().getModel();
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        int seg = 48;

        b.begin(GL11.GL_TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        Render3D.v(b, m, pos.x, top + h, pos.z, ColorUtil.withAlpha(ColorUtil.lerp(Theme.accent(), 0xFFFFFFFF, 0.3), a));
        for (int i = 0; i <= seg; i++) {
            double t = i / (double) seg;
            double ang = t * Math.PI * 2 + yaw;
            int c = ColorUtil.withAlpha(Theme.color(Math.sin(t * Math.PI * 2) * 2), a);
            Render3D.v(b, m, pos.x + Math.cos(ang) * r, top, pos.z + Math.sin(ang) * r, c);
        }
        Tessellator.getInstance().draw();

        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        RenderSystem.lineWidth(2f);
        b.begin(GL11.GL_LINE_STRIP, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= seg; i++) {
            double t = i / (double) seg;
            double ang = t * Math.PI * 2 + yaw;
            int c = ColorUtil.withAlpha(Theme.color(Math.sin(t * Math.PI * 2) * 2), 255);
            Render3D.v(b, m, pos.x + Math.cos(ang) * r, top, pos.z + Math.sin(ang) * r, c);
        }
        Tessellator.getInstance().draw();
        RenderSystem.lineWidth(1f);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }
}
