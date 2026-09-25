package dev.nebula.module.render;

import dev.nebula.module.Category;
import dev.nebula.module.Module;
import dev.nebula.setting.BoolSetting;
import dev.nebula.setting.ModeSetting;
import dev.nebula.setting.NumberSetting;
import dev.nebula.util.Animation;
import dev.nebula.util.ColorUtil;
import dev.nebula.util.Render2D;
import dev.nebula.util.Theme;
import net.minecraft.client.util.math.MatrixStack;

public class Crosshair extends Module {
    private final ModeSetting mode = add(new ModeSetting("Mode", "Cross", "Cross", "Circle", "Dot"));
    private final NumberSetting size = add(new NumberSetting("Size", 4, 2, 10, 0.5));
    private final NumberSetting gap = add(new NumberSetting("Gap", 2, 0, 6, 0.5));
    private final BoolSetting dynamic = add(new BoolSetting("Dynamic", true));
    private final BoolSetting cooldown = add(new BoolSetting("Cooldown", true));
    private final BoolSetting outline = add(new BoolSetting("Outline", true));

    private final Animation gapAnim = new Animation(2, 14);
    private final Animation cdAnim = new Animation(1, 16);

    public Crosshair() {
        super("Crosshair", "Custom animated crosshair.", Category.RENDER);
    }

    @Override public String getSuffix() { return mode.get(); }

    @Override
    public void onRender2D(MatrixStack ms, float delta) {
        if (!mc.options.getPerspective().isFirstPerson() || mc.options.debugEnabled) return;
        float cx = mc.getWindow().getScaledWidth() / 2f, cy = mc.getWindow().getScaledHeight() / 2f;
        boolean moving = mc.player.input != null && (mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0);
        float cd = mc.player.getAttackCooldownProgress(0);
        double targetGap = gap.get() + (dynamic.get() ? (moving ? 2 : 0) + (mc.player.isSprinting() ? 1 : 0) + (1 - cd) * 3 : 0);
        float g = (float) gapAnim.animate(targetGap);
        float c = (float) cdAnim.animate(cd);
        int col = Theme.color(0);
        int ol = 0xB0000000;
        float s = size.getFloat(), t = 1;

        if (mode.is("Cross")) {
            float[][] bars = {
                    {cx - t / 2, cy - g - s, t, s}, {cx - t / 2, cy + g, t, s},
                    {cx - g - s, cy - t / 2, s, t}, {cx + g, cy - t / 2, s, t}};
            for (float[] b : bars) {
                if (outline.get()) Render2D.rect(ms, b[0] - 0.5f, b[1] - 0.5f, b[2] + 1, b[3] + 1, ol);
                Render2D.rect(ms, b[0], b[1], b[2], b[3], col);
            }
            if (cooldown.get() && c < 0.999f) {
                float w = 16;
                Render2D.rect(ms, cx - w / 2, cy + g + s + 3, w, 1.5f, 0x80000000);
                Render2D.horizontalGradient(ms, cx - w / 2, cy + g + s + 3, w * c, 1.5f, Theme.accent(), Theme.accent2());
            }
        } else if (mode.is("Circle")) {
            float r = g + s;
            if (outline.get()) Render2D.arc(ms, cx, cy, r + 0.5f, 2.5f, 0, 360, ol, ol);
            Render2D.arc(ms, cx, cy, r, 1.5f, 0, 360, ColorUtil.withAlpha(col, 70), ColorUtil.withAlpha(col, 70));
            Render2D.arc(ms, cx, cy, r, 1.5f, -90, 360 * (cooldown.get() ? c : 1), Theme.accent(), Theme.accent2());
            Render2D.circle(ms, cx, cy, 1f, col);
        } else {
            if (outline.get()) Render2D.circle(ms, cx, cy, s / 2f + 0.7f, ol);
            Render2D.circle(ms, cx, cy, s / 2f * (cooldown.get() ? 0.5f + 0.5f * c : 1), col);
        }
    }
}
