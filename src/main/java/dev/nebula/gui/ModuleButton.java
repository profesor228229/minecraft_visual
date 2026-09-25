package dev.nebula.gui;

import dev.nebula.gui.comp.*;
import dev.nebula.module.Module;
import dev.nebula.setting.*;
import dev.nebula.util.*;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;

public class ModuleButton {
    public static final float HEIGHT = 18;

    final Module module;
    private final List<Component> comps = new ArrayList<>();
    private final KeyComp bindComp;
    private final Animation toggle, hover = new Animation(0, 14), expand = new Animation(0, 13);
    final Animation filter = new Animation(1, 14);
    private boolean expanded;
    private float x, y, w;

    public ModuleButton(Module module) {
        this.module = module;
        this.toggle = new Animation(module.isEnabled() ? 1 : 0, 12);
        for (Setting<?> s : module.getSettings()) {
            if (s instanceof BoolSetting) comps.add(new BoolComp((BoolSetting) s));
            else if (s instanceof NumberSetting) comps.add(new NumberComp((NumberSetting) s));
            else if (s instanceof ModeSetting) comps.add(new ModeComp((ModeSetting) s));
            else if (s instanceof ColorSetting) comps.add(new ColorComp((ColorSetting) s));
            else if (s instanceof KeySetting) comps.add(new KeyComp((KeySetting) s));
        }
        bindComp = new KeyComp(module.bind);
        comps.add(bindComp);
    }

    private float settingsHeight() {
        float h = 0;
        for (Component c : comps) h += c.height();
        return h + 2;
    }

    public float height() {
        return (HEIGHT + settingsHeight() * (float) expand.get()) * (float) filter.get();
    }

    public void updateAnimations(boolean matches) {
        filter.animate(matches ? 1 : 0);
        expand.animate(expanded ? 1 : 0);
        for (Component c : comps) c.updateVisibility();
    }

    public void render(MatrixStack ms, float x, float y, float w, double mx, double my, double alpha, boolean mouseInPanel) {
        this.x = x; this.y = y; this.w = w;
        double f = filter.get();
        if (f < 0.01) return;
        double a = alpha * f;
        boolean hov = mouseInPanel && Render2D.hovered(mx, my, x, y, w, HEIGHT);
        double hv = hover.animate(hov ? 1 : 0);
        double t = toggle.animate(module.isEnabled() ? 1 : 0);

        if (hv > 0.01) Render2D.rect(ms, x, y, w, HEIGHT, ColorUtil.fade(0x14FFFFFF, a * hv));
        if (t > 0.01) {
            Render2D.horizontalGradient(ms, x, y, w, HEIGHT,
                    ColorUtil.fade(ColorUtil.withAlpha(Theme.accent(), 90), a * t),
                    ColorUtil.fade(ColorUtil.withAlpha(Theme.accent2(), 25), a * t));
            Render2D.verticalGradient(ms, x, y + 3, 1.5f, (HEIGHT - 6) * (float) t + 0.01f,
                    ColorUtil.fade(Theme.accent(), a * t), ColorUtil.fade(Theme.accent2(), a * t));
        }
        String name = module.getName();
        float nx = x + 7 + (float) (hv * 2);
        int nc = ColorUtil.lerp(Theme.TEXT_DIM, 0xFFFFFFFF, Math.max(t, hv * 0.6));
        Fonts.draw(ms, name, nx, y + 5, ColorUtil.fade(nc, a));

        {
            double e = expand.get();
            // animated chevron made from two tiny lines (a rotating "plus/minus")
            float cx = x + w - 9, cy = y + HEIGHT / 2f;
            int cc = ColorUtil.fade(ColorUtil.lerp(Theme.TEXT_DIM, Theme.TEXT, hv), a * 0.8);
            Render2D.rect(ms, cx - 2.5f, cy - 0.5f, 5, 1, cc);
            float vh = (float) (5 * (1 - e));
            if (vh > 0.1f) Render2D.rect(ms, cx - 0.5f, cy - vh / 2, 1, vh, cc);
        }

        double e = expand.get();
        if (e > 0.01) {
            float sy = y + HEIGHT;
            float sh = settingsHeight() * (float) e;
            Render2D.rect(ms, x, sy, w, sh, ColorUtil.fade(0x40000000, a));
            ClickGuiScreen.pushLocalScissor(x, sy, w, sh);
            float cy = sy + 1;
            for (Component c : comps) {
                if (c.visAnim.get() > 0.01) {
                    ClickGuiScreen.pushLocalScissor(x, cy, w, c.height());
                    c.render(ms, x, cy, w, mx, my, a * e * c.visAnim.get());
                    ClickGuiScreen.popScissor();
                }
                cy += c.height();
            }
            ClickGuiScreen.popScissor();
        }
    }

    void setBounds(float x, float y, float w) { this.x = x; this.y = y; this.w = w; }

    public boolean isHovered(double mx, double my) {
        return filter.get() > 0.5 && Render2D.hovered(mx, my, x, y, w, HEIGHT);
    }

    public boolean mouseClicked(double mx, double my, int button) {
        if (filter.get() < 0.5) return false;
        if (Render2D.hovered(mx, my, x, y, w, HEIGHT)) {
            if (button == 0) module.toggle();
            else if (button == 1) expanded = !expanded;
            else if (button == 2) { expanded = true; bindComp.listen(); }
            return true;
        }
        if (expanded && expand.get() > 0.9) {
            for (Component c : comps) {
                if (c.visible() && c.mouseClicked(mx, my, button)) return true;
            }
        }
        return false;
    }

    public void mouseReleased(double mx, double my, int button) {
        for (Component c : comps) c.mouseReleased(mx, my, button);
    }

    public boolean keyPressed(int key) {
        for (Component c : comps) if (c.keyPressed(key)) return true;
        return false;
    }

    public boolean isListening() {
        for (Component c : comps) if (c.isListening()) return true;
        return false;
    }
}
