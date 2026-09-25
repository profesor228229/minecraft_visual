package dev.nebula.module.client;

import dev.nebula.gui.ClickGuiScreen;
import dev.nebula.module.Category;
import dev.nebula.module.Module;
import dev.nebula.setting.BoolSetting;
import dev.nebula.setting.ColorSetting;
import dev.nebula.setting.NumberSetting;
import org.lwjgl.glfw.GLFW;

public class ClickGuiModule extends Module {
    public static ClickGuiModule INSTANCE;

    public final ColorSetting accent1 = add(new ColorSetting("Accent", 0xFF8A5CFF));
    public final ColorSetting accent2 = add(new ColorSetting("Accent 2", 0xFF3FC8FF));
    public final BoolSetting rainbow = add(new BoolSetting("Rainbow", false));
    public final NumberSetting waveSpeed = add(new NumberSetting("Wave Speed", 2.0, 0.5, 6.0, 0.1));
    public final BoolSetting blur = add(new BoolSetting("Blur", true));
    public final BoolSetting darken = add(new BoolSetting("Darken", true));
    public final BoolSetting descriptions = add(new BoolSetting("Descriptions", true));
    public final NumberSetting scale = add(new NumberSetting("Scale", 1.0, 0.7, 1.4, 0.05));

    public ClickGuiModule() {
        super("ClickGui", "Opens this menu. Theme colors are used everywhere.", Category.CLIENT, GLFW.GLFW_KEY_RIGHT_SHIFT);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        setEnabled(false, false);
        if (mc.currentScreen == null) mc.openScreen(ClickGuiScreen.get());
    }

    @Override
    public boolean showInArrayList() { return false; }
}
