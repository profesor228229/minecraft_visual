package dev.nebula.module.hud;

import dev.nebula.util.*;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.math.MatrixStack;

public class Keystrokes extends HudModule {
    private final Animation[] anims = new Animation[7];

    public Keystrokes() {
        super("Keystrokes", "Shows pressed movement keys.");
        for (int i = 0; i < anims.length; i++) anims[i] = new Animation(0, 14);
    }

    @Override
    protected void defaultPosition(int sw, int sh) { x = 6; y = 30; }

    private void key(MatrixStack ms, int idx, KeyBinding kb, String label, float kx, float ky, float kw, float kh) {
        double p = anims[idx].animate(kb.isPressed() ? 1 : 0);
        Render2D.rounded(ms, kx, ky, kw, kh, 4, ColorUtil.withAlpha(Theme.BG, 200));
        if (p > 0.01) {
            float g = (float) (1 - p) * Math.min(kw, kh) / 2f;
            Render2D.roundedHorizontal(ms, kx + g, ky + g, kw - g * 2, kh - g * 2, 4,
                    ColorUtil.fade(Theme.accent(), p * 0.85), ColorUtil.fade(Theme.accent2(), p * 0.85));
        }
        Fonts.drawCentered(ms, label, kx + kw / 2f + 0.5f, ky + kh / 2f - 3.5f, ColorUtil.lerp(Theme.TEXT, 0xFFFFFFFF, p));
    }

    @Override
    protected void draw(MatrixStack ms, float delta) {
        float s = 20, gap = 2;
        width = s * 3 + gap * 2;
        height = s * 2 + gap * 3 + 14 + 10;
        key(ms, 0, mc.options.keyForward, "W", x + s + gap, y, s, s);
        key(ms, 1, mc.options.keyLeft, "A", x, y + s + gap, s, s);
        key(ms, 2, mc.options.keyBack, "S", x + s + gap, y + s + gap, s, s);
        key(ms, 3, mc.options.keyRight, "D", x + (s + gap) * 2, y + s + gap, s, s);
        float half = (width - gap) / 2f;
        key(ms, 4, mc.options.keyAttack, "LMB", x, y + (s + gap) * 2, half, 14);
        key(ms, 5, mc.options.keyUse, "RMB", x + half + gap, y + (s + gap) * 2, half, 14);
        key(ms, 6, mc.options.keyJump, "\u2014", x, y + (s + gap) * 2 + 14 + gap, width, 10);
    }
}
