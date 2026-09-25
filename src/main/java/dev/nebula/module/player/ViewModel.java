package dev.nebula.module.player;

import dev.nebula.Nebula;
import dev.nebula.module.Category;
import dev.nebula.module.Module;
import dev.nebula.setting.NumberSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.Hand;

public class ViewModel extends Module {
    private final NumberSetting mainX = add(new NumberSetting("Main X", 0, -1, 1, 0.02));
    private final NumberSetting mainY = add(new NumberSetting("Main Y", 0, -1, 1, 0.02));
    private final NumberSetting mainZ = add(new NumberSetting("Main Z", 0, -1, 1, 0.02));
    private final NumberSetting offX = add(new NumberSetting("Off X", 0, -1, 1, 0.02));
    private final NumberSetting offY = add(new NumberSetting("Off Y", 0, -1, 1, 0.02));
    private final NumberSetting offZ = add(new NumberSetting("Off Z", 0, -1, 1, 0.02));
    private final NumberSetting scale = add(new NumberSetting("Scale", 1.0, 0.3, 1.5, 0.05));
    private final NumberSetting rotY = add(new NumberSetting("Rotate Y", 0, -90, 90, 1));

    public ViewModel() {
        super("ViewModel", "Move and scale your hands.", Category.PLAYER);
    }

    public static void apply(Hand hand, MatrixStack ms) {
        ViewModel v = Nebula.modules() == null ? null : Nebula.modules().get(ViewModel.class);
        if (v == null || !v.isEnabled()) return;
        boolean main = hand == Hand.MAIN_HAND;
        boolean right = main == (mc.options.mainArm == net.minecraft.util.Arm.RIGHT);
        if (main) ms.translate(v.mainX.get(), v.mainY.get(), v.mainZ.get());
        else ms.translate(v.offX.get(), v.offY.get(), v.offZ.get());
        ms.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion((float) (right ? v.rotY.get() : -v.rotY.get())));
        float s = v.scale.getFloat();
        ms.scale(s, s, s);
    }
}
