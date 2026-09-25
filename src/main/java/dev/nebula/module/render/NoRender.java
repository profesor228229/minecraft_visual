package dev.nebula.module.render;

import dev.nebula.Nebula;
import dev.nebula.module.Category;
import dev.nebula.module.Module;
import dev.nebula.setting.BoolSetting;

public class NoRender extends Module {
    public final BoolSetting hurtCam = add(new BoolSetting("Hurt Camera", true));
    public final BoolSetting fire = add(new BoolSetting("Fire Overlay", true));
    public final BoolSetting weather = add(new BoolSetting("Weather", false));
    public final BoolSetting vignette = add(new BoolSetting("Vignette", true));
    public final BoolSetting bossBar = add(new BoolSetting("Boss Bar", false));

    public NoRender() {
        super("NoRender", "Removes distracting overlays.", Category.RENDER);
    }

    public static boolean active(java.util.function.Function<NoRender, BoolSetting> f) {
        if (Nebula.modules() == null) return false;
        NoRender n = Nebula.modules().get(NoRender.class);
        return n != null && n.isEnabled() && f.apply(n).get();
    }
}
