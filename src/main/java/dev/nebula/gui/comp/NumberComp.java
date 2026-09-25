package dev.nebula.gui.comp;

import dev.nebula.setting.NumberSetting;
import dev.nebula.util.*;
import net.minecraft.client.util.math.MatrixStack;

public class NumberComp extends Component {
    private final NumberSetting s;
    private final Animation fill;
    private final Animation knob = new Animation(0, 16);
    private boolean dragging;

    public NumberComp(NumberSetting s) {
        super(s);
        this.s = s;
        this.fill = new Animation(frac(), 16);
    }

    private double frac() { return (s.get() - s.getMin()) / (s.getMax() - s.getMin()); }

    @Override public float baseHeight() { return 23; }

    @Override
    protected void draw(MatrixStack ms, double mx, double my, double a) {
        if (dragging) mouseDragged(mx, my);
        Fonts.draw(ms, s.getName(), x + 6, y + 3, ColorUtil.fade(ColorUtil.lerp(Theme.TEXT_DIM, Theme.TEXT, dragging ? 1 : hover.get()), a));
        String v = s.display();
        Fonts.draw(ms, v, x + w - 6 - Fonts.width(v), y + 3, ColorUtil.fade(Theme.TEXT, a));

        float tx = x + 6, tw = w - 12, ty = y + 15, th = 3;
        double f = fill.animate(frac());
        Render2D.rounded(ms, tx, ty, tw, th, 1.5f, ColorUtil.fade(Theme.ELEMENT, a));
        Render2D.roundedHorizontal(ms, tx, ty, (float) Math.max(th, tw * f), th, 1.5f, ColorUtil.fade(Theme.accent(), a), ColorUtil.fade(Theme.accent2(), a));
        double k = knob.animate(dragging ? 1 : hover.get());
        float kx = (float) (tx + tw * f), ky = ty + th / 2;
        if (k > 0.01) Render2D.circle(ms, kx, ky, (float) (3 + 3 * k), ColorUtil.fade(ColorUtil.withAlpha(Theme.accent2(), 60), a * k));
        Render2D.circle(ms, kx, ky, (float) (2.5 + k), ColorUtil.fade(0xFFFFFFFF, a));
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (hovered(mx, my) && button == 0) {
            dragging = true;
            mouseDragged(mx, my);
            return true;
        }
        return false;
    }

    @Override
    public void mouseDragged(double mx, double my) {
        if (!dragging) return;
        double f = Math.max(0, Math.min(1, (mx - (x + 6)) / (w - 12)));
        s.set(s.getMin() + (s.getMax() - s.getMin()) * f);
    }

    @Override
    public void mouseReleased(double mx, double my, int button) { dragging = false; }
}
