package dev.nebula.util;

import dev.nebula.module.client.ClickGuiModule;

/** Central place for the client color scheme. */
public final class Theme {
    private Theme() {}

    public static final int BG = 0xF0121218;
    public static final int PANEL = 0xF5181820;
    public static final int PANEL_LIGHT = 0xFF20202A;
    public static final int ELEMENT = 0xFF262632;
    public static final int TEXT = 0xFFEDEDF2;
    public static final int TEXT_DIM = 0xFF8E8EA0;

    public static int accent() {
        ClickGuiModule m = ClickGuiModule.INSTANCE;
        return m == null ? 0xFF8A5CFF : (m.rainbow.get() ? ColorUtil.rainbow(0, 0.6f, 1f) : m.accent1.get());
    }

    public static int accent2() {
        ClickGuiModule m = ClickGuiModule.INSTANCE;
        return m == null ? 0xFF3FC8FF : (m.rainbow.get() ? ColorUtil.rainbow(0.25, 0.6f, 1f) : m.accent2.get());
    }

    /** Animated gradient color used by HUD and world effects; offset shifts phase. */
    public static int color(double offset) {
        ClickGuiModule m = ClickGuiModule.INSTANCE;
        if (m != null && m.rainbow.get()) return ColorUtil.rainbow(offset * 0.15, 0.6f, 1f);
        double speed = m == null ? 2.0 : m.waveSpeed.get();
        return ColorUtil.wave(accent(), accent2(), speed, offset);
    }
}
