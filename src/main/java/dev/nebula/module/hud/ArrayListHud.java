package dev.nebula.module.hud;

import dev.nebula.Nebula;
import dev.nebula.module.Module;
import dev.nebula.setting.BoolSetting;
import dev.nebula.setting.ModeSetting;
import dev.nebula.util.*;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ArrayListHud extends HudModule {
    private final BoolSetting background = add(new BoolSetting("Background", true));
    private final BoolSetting sidebar = add(new BoolSetting("Sidebar", true));
    private final BoolSetting glow = add(new BoolSetting("Glow", true));
    private final BoolSetting suffix = add(new BoolSetting("Suffix", true));
    private final ModeSetting casing = add(new ModeSetting("Case", "Normal", "Normal", "Lower", "Upper"));

    public ArrayListHud() {
        super("ArrayList", "Animated list of enabled modules.");
        setEnabled(true, false);
    }

    @Override
    protected void defaultPosition(int sw, int sh) { x = sw - 60; y = 6; width = 54; }

    private String label(Module m) {
        String s = m.getName();
        if (casing.is("Lower")) s = s.toLowerCase();
        else if (casing.is("Upper")) s = s.toUpperCase();
        return s;
    }

    private String suffixOf(Module m) {
        String s = suffix.get() ? m.getSuffix() : null;
        return s == null ? "" : " " + s;
    }

    private float fullWidth(Module m) {
        return Fonts.width(label(m) + suffixOf(m));
    }

    @Override
    protected void draw(MatrixStack ms, float delta) {
        List<Module> list = new ArrayList<>();
        for (Module m : Nebula.modules().getModules()) {
            if (!m.showInArrayList()) continue;
            double a = m.arrayAnim.animate(m.isEnabled() ? 1 : 0);
            if (a > 0.005) list.add(m);
        }
        list.sort(Comparator.comparingDouble(m -> -fullWidth(m)));

        boolean right = onRightSide();
        float rowH = 11;
        float maxW = 40;
        for (Module m : list) maxW = Math.max(maxW, fullWidth(m) + 8);

        float oldW = width;
        width = maxW;
        if (right) x += oldW - width; // keep right edge anchored
        float edge = right ? x + width : x;

        float cy = y;
        int i = 0;
        for (Module m : list) {
            double a = m.arrayAnim.get();
            if (m.arrayY.get() < 0) m.arrayY.set(cy);
            float ry = (float) m.arrayY.animate(cy);
            String lbl = label(m);
            String suf = suffixOf(m);
            float w = Fonts.width(lbl + suf) + 8;
            float slide = (float) ((1 - Easing.OUT_CUBIC.apply(a)) * (w + 6));
            float rx = right ? edge - w + slide : edge - slide;
            int alpha = (int) (255 * a);
            int col = Theme.color(i * 0.35);

            if (glow.get()) Render2D.shadow(ms, rx, ry, w, rowH, 2, 5, ColorUtil.withAlpha(col, (int) (70 * a)));
            if (background.get()) Render2D.rect(ms, rx, ry, w, rowH, ColorUtil.fade(0xB0101016, a));
            if (sidebar.get()) {
                float bx = right ? rx + w - 1.5f : rx;
                Render2D.verticalGradient(ms, bx, ry, 1.5f, rowH, ColorUtil.fade(col, a), ColorUtil.fade(Theme.color((i + 1) * 0.35), a));
            }
            float tx = rx + (right ? 3 : 4.5f);
            Fonts.drawShadow(ms, lbl, tx, ry + 2, ColorUtil.withAlpha(col, alpha));
            if (!suf.isEmpty()) Fonts.drawShadow(ms, suf, tx + Fonts.width(lbl), ry + 2, ColorUtil.withAlpha(0xFFAAAAB4, alpha));

            cy += rowH * a;
            i++;
        }
        height = Math.max(rowH, cy - y);
    }
}
