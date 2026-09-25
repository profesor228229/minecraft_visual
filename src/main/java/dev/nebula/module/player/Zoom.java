package dev.nebula.module.player;

import dev.nebula.Nebula;
import dev.nebula.module.Category;
import dev.nebula.module.Module;
import dev.nebula.setting.BoolSetting;
import dev.nebula.setting.KeySetting;
import dev.nebula.setting.NumberSetting;
import dev.nebula.util.Animation;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class Zoom extends Module {
    private final KeySetting key = add(new KeySetting("Zoom Key", GLFW.GLFW_KEY_C));
    private final NumberSetting factor = add(new NumberSetting("Factor", 4, 1.5, 15, 0.5, "x"));
    private final BoolSetting scroll = add(new BoolSetting("Scroll Adjust", true));
    private final BoolSetting smoothCam = add(new BoolSetting("Cinematic", true));

    private final Animation anim = new Animation(1, 12);
    private double scrollFactor = -1;
    private boolean wasZooming, prevCinematic;

    public Zoom() {
        super("Zoom", "Hold the zoom key for smooth optifine-like zoom.", Category.PLAYER);
        setEnabled(true, false);
    }

    @Override public boolean showInArrayList() { return false; }

    private boolean zooming() {
        return isEnabled() && mc.currentScreen == null && key.get() > 0
                && InputUtil.isKeyPressed(mc.getWindow().getHandle(), key.get());
    }

    @Override
    public void onTick() {
        boolean z = zooming();
        if (z && !wasZooming) {
            scrollFactor = factor.get();
            prevCinematic = mc.options.smoothCameraEnabled;
            if (smoothCam.get()) mc.options.smoothCameraEnabled = true;
        } else if (!z && wasZooming) {
            if (smoothCam.get()) mc.options.smoothCameraEnabled = prevCinematic;
        }
        wasZooming = z;
    }

    /** @return true if scroll was consumed. */
    public static boolean onScroll(double amount) {
        Zoom zm = get();
        if (zm == null || !zm.zooming() || !zm.scroll.get()) return false;
        zm.scrollFactor = Math.max(1.2, Math.min(40, zm.scrollFactor * (amount > 0 ? 1.2 : 1 / 1.2)));
        return true;
    }

    public static double modifyFov(double fov) {
        Zoom zm = get();
        if (zm == null) return fov;
        boolean z = zm.zooming();
        double target = z ? 1.0 / (zm.scrollFactor > 0 ? zm.scrollFactor : zm.factor.get()) : 1.0;
        double v = zm.anim.animate(target);
        return fov * v;
    }

    private static Zoom get() {
        return Nebula.modules() == null ? null : Nebula.modules().get(Zoom.class);
    }
}
