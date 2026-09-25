package dev.nebula.gui.comp;

import dev.nebula.setting.BoolSetting;
import dev.nebula.util.*;
import net.minecraft.client.util.math.MatrixStack;

public class BoolComp extends Component {
    private final BoolSetting s;
    private final Animation toggle;

    public BoolComp(BoolSetting s) {
        super(s);
        this.s = s;
        this.toggle = new Animation(s.get() ? 1 : 0, 14);
    }

    @Override public float baseHeight() { return 16; }

    @Override
    protected void draw(MatrixStack ms, double mx, double my, double a) {
        double t = toggle.animate(s.get() ? 1 : 0);
        Fonts.draw(ms, s.getName(), x + 6, y + 4, ColorUtil.fade(ColorUtil.lerp(Theme.TEXT_DIM, Theme.TEXT, Math.max(t, hover.get())), a));
        float sw = 18, sh = 9;
        float sx = x + w - sw - 6, sy = y + (16 - sh) / 2f;
        Render2D.rounded(ms, sx, sy, sw, sh, sh / 2, ColorUtil.fade(Theme.ELEMENT, a));
        if (t > 0.01) {
            Render2D.roundedHorizontal(ms, sx, sy, sw, sh, sh / 2,
                    ColorUtil.fade(Theme.accent(), a * t), ColorUtil.fade(Theme.accent2(), a * t));
        }
        float kx = (float) (sx + sh / 2 + (sw - sh) * t);
        Render2D.circle(ms, kx, sy + sh / 2, 3.2f, ColorUtil.fade(ColorUtil.lerp(0xFF9A9AAA, 0xFFFFFFFF, t), a));
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (hovered(mx, my) && button == 0) { s.toggle(); return true; }
        return false;
    }
}
