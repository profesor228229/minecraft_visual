package dev.nebula.module.world;

import dev.nebula.module.Category;
import dev.nebula.module.Module;
import dev.nebula.util.Animation;

public class Fullbright extends Module {
    private double oldGamma = 1.0;
    private final Animation gamma = new Animation(1, 5);
    private boolean fadingOut;

    public Fullbright() {
        super("Fullbright", "See in the dark. Fades in smoothly.", Category.WORLD);
    }

    @Override
    public void onEnable() {
        if (mc.options == null) return;
        if (!fadingOut) oldGamma = mc.options.gamma;
        fadingOut = false;
        gamma.set(mc.options.gamma);
    }

    @Override
    public void onDisable() {
        if (mc.options == null) return;
        // fade back to the original gamma over the next ticks
        fadingOut = true;
        new Thread(() -> {
            try {
                while (fadingOut && Math.abs(mc.options.gamma - oldGamma) > 0.01) {
                    mc.options.gamma = gamma.animate(oldGamma);
                    Thread.sleep(16);
                }
                if (fadingOut) mc.options.gamma = oldGamma;
            } catch (InterruptedException ignored) {
            }
            fadingOut = false;
        }, "Nebula-Fullbright").start();
    }

    @Override
    public void onTick() {
        mc.options.gamma = gamma.animate(15.0);
    }
}
