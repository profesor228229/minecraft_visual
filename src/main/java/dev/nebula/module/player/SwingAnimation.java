package dev.nebula.module.player;

import dev.nebula.Nebula;
import dev.nebula.module.Category;
import dev.nebula.module.Module;
import dev.nebula.setting.NumberSetting;

public class SwingAnimation extends Module {
    private final NumberSetting duration = add(new NumberSetting("Swing Duration", 10, 4, 24, 1, "t"));

    public SwingAnimation() {
        super("SlowSwing", "Changes hand swing speed (6 is vanilla).", Category.PLAYER);
    }

    public static int override(int vanilla) {
        SwingAnimation s = Nebula.modules() == null ? null : Nebula.modules().get(SwingAnimation.class);
        if (s == null || !s.isEnabled()) return vanilla;
        return s.duration.getInt();
    }
}
