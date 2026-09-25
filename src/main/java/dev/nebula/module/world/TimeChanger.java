package dev.nebula.module.world;

import dev.nebula.Nebula;
import dev.nebula.module.Category;
import dev.nebula.module.Module;
import dev.nebula.setting.BoolSetting;
import dev.nebula.setting.NumberSetting;
import dev.nebula.util.Animation;

public class TimeChanger extends Module {
    private final NumberSetting time = add(new NumberSetting("Time", 18000, 0, 24000, 100));
    private final BoolSetting cycle = add(new BoolSetting("Cycle", false));
    private final NumberSetting cycleSpeed = add(new NumberSetting("Cycle Speed", 50, 5, 400, 5)).visibleIf(cycle::get);
    private final Animation smooth = new Animation(18000, 4);
    private double cycled;

    public TimeChanger() {
        super("TimeChanger", "Client-side time of day.", Category.WORLD);
    }

    public static boolean active() {
        TimeChanger t = Nebula.modules() == null ? null : Nebula.modules().get(TimeChanger.class);
        return t != null && t.isEnabled();
    }

    @Override
    public void onEnable() {
        if (mc.world != null) smooth.set(mc.world.getTimeOfDay() % 24000);
        cycled = time.get();
    }

    @Override
    public void onTick() {
        double target;
        if (cycle.get()) {
            cycled = (cycled + cycleSpeed.get()) % 24000;
            target = cycled;
            smooth.set(target);
        } else {
            target = time.get();
            smooth.animate(target);
        }
        mc.world.setTimeOfDay((long) smooth.get());
    }

    @Override public String getSuffix() { return String.valueOf(time.getInt()); }
}
