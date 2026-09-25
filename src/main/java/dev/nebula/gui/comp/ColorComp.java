package dev.nebula.gui.comp;

import dev.nebula.setting.ColorSetting;
import dev.nebula.util.*;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;

public class ColorComp extends Component {
    private final ColorSetting s;
    private final Animation open = new Animation(0, 14);
    private boolean expanded, dragSB, dragHue;
    private float hue, sat, bri;

    public ColorComp(ColorSetting s) {
        super(s);
        this.s = s;
        float[] h = s.hsb();
        hue = h[0]; sat = h[1]; bri = h[2];
    }

    @Override public float baseHeight() { return 16 + 62 * (float) open.get(); }

    @Override
    protected void draw(MatrixStack ms, double mx, double my, double a) {
        double o = open.animate(expanded ? 1 : 0);
        if (dragSB || dragHue) mouseDragged(mx, my);
        Fonts.draw(ms, s.getName(), x + 6, y + 4, ColorUtil.fade(ColorUtil.lerp(Theme.TEXT_DIM, Theme.TEXT, hover.get()), a));
        float sw = 18;
        float sx = x + w - sw - 6;
        Render2D.shadow(ms, sx, y + 4, sw, 8, 3, 3, ColorUtil.fade(ColorUtil.withAlpha(s.get(), 120), a));
        Render2D.rounded(ms, sx, y + 4, sw, 8, 3, ColorUtil.fade(s.get() | 0xFF000000, a));
        if (o < 0.02) return;

        double pa = a * o;
        float px = x + 6, py = y + 18, pw = w - 12 - 12, ph = 54;
        int hueColor = 0xFF000000 | Color.HSBtoRGB(hue, 1, 1);
        Render2D.horizontalGradient(ms, px, py, pw, ph * (float) o, ColorUtil.fade(0xFFFFFFFF, pa), ColorUtil.fade(hueColor, pa));
        Render2D.verticalGradient(ms, px, py, pw, ph * (float) o, 0x00000000, ColorUtil.fade(0xFF000000, pa));
        // SB cursor
        float cx = px + sat * pw, cy = py + (1 - bri) * ph * (float) o;
        Render2D.circle(ms, cx, cy, 3, ColorUtil.fade(0xFFFFFFFF, pa));
        Render2D.circle(ms, cx, cy, 2, ColorUtil.fade(s.get() | 0xFF000000, pa));
        // hue bar
        float hx = px + pw + 4, hw = 8;
        int steps = 12;
        for (int i = 0; i < steps; i++) {
            float y1 = py + ph * (float) o * i / steps, hh = ph * (float) o / steps;
            int c1 = 0xFF000000 | Color.HSBtoRGB(i / (float) steps, 1, 1);
            int c2 = 0xFF000000 | Color.HSBtoRGB((i + 1) / (float) steps, 1, 1);
            Render2D.verticalGradient(ms, hx, y1, hw, hh + 0.1f, ColorUtil.fade(c1, pa), ColorUtil.fade(c2, pa));
        }
        float hy = py + hue * ph * (float) o;
        Render2D.rounded(ms, hx - 1, hy - 1.5f, hw + 2, 3, 1.5f, ColorUtil.fade(0xFFFFFFFF, pa));
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (!hovered(mx, my)) return false;
        if (my <= y + 16) {
            if (button == 0 || button == 1) expanded = !expanded;
            if (expanded) { float[] h = s.hsb(); hue = h[0]; sat = h[1]; bri = h[2]; }
            return true;
        }
        if (!expanded || button != 0) return true;
        float px = x + 6, pw = w - 24, hx = px + pw + 4;
        if (mx >= hx - 1) dragHue = true; else if (mx <= px + pw) dragSB = true;
        mouseDragged(mx, my);
        return true;
    }

    @Override
    public void mouseDragged(double mx, double my) {
        float px = x + 6, py = y + 18, pw = w - 24, ph = 54;
        if (dragSB) {
            sat = (float) Math.max(0, Math.min(1, (mx - px) / pw));
            bri = (float) Math.max(0, Math.min(1, 1 - (my - py) / ph));
        } else if (dragHue) {
            hue = (float) Math.max(0, Math.min(0.999, (my - py) / ph));
        } else return;
        s.setHsb(hue, sat, bri);
    }

    @Override
    public void mouseReleased(double mx, double my, int button) { dragSB = dragHue = false; }
}
