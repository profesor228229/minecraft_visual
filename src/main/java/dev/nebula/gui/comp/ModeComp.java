package dev.nebula.gui.comp;

import dev.nebula.setting.ModeSetting;
import dev.nebula.util.*;
import net.minecraft.client.util.math.MatrixStack;

public class ModeComp extends Component {
    private final ModeSetting s;
    private final Animation change = new Animation(0, 12);
    private int dir = 1;

    public ModeComp(ModeSetting s) {
        super(s);
        this.s = s;
    }

    @Override public float baseHeight() { return 16; }

    @Override
    protected void draw(MatrixStack ms, double mx, double my, double a) {
        double c = change.animate(0);
        Fonts.draw(ms, s.getName(), x + 6, y + 4, ColorUtil.fade(ColorUtil.lerp(Theme.TEXT_DIM, Theme.TEXT, hover.get()), a));
        String v = s.get();
        float vw = Fonts.width(v);
        float bw = vw + 10, bx = x + w - 6 - bw;
        Render2D.rounded(ms, bx, y + 2.5f, bw, 11, 3, ColorUtil.fade(ColorUtil.lerp(Theme.ELEMENT, Theme.PANEL_LIGHT, hover.get()), a));
        Render2D.roundedOutline(ms, bx, y + 2.5f, bw, 11, 3, 1f, (px, py) -> ColorUtil.fade(ColorUtil.lerp(Theme.accent(), Theme.accent2(), (px - bx) / bw), a * (0.3 + 0.5 * hover.get())));
        float off = (float) (c * 6 * dir);
        Fonts.draw(ms, v, bx + 5 + off, y + 4.5f, ColorUtil.fade(Theme.TEXT, a * (1 - c)));
        // index dots
        int n = s.getModes().size();
        if (n <= 6) {
            float dx = bx - 4 - n * 4;
            for (int i = 0; i < n; i++) {
                boolean cur = i == s.index();
                Render2D.circle(ms, dx + i * 4 + 1.5f, y + 8, cur ? 1.3f : 1f, ColorUtil.fade(cur ? Theme.accent() : Theme.TEXT_DIM, a * (cur ? 1 : 0.5)));
            }
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (!hovered(mx, my) || (button != 0 && button != 1)) return false;
        s.cycle(button == 0);
        dir = button == 0 ? 1 : -1;
        change.set(1);
        return true;
    }
}
