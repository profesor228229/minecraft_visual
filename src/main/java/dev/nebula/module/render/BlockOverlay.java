package dev.nebula.module.render;

import dev.nebula.module.Category;
import dev.nebula.module.Module;
import dev.nebula.setting.BoolSetting;
import dev.nebula.setting.NumberSetting;
import dev.nebula.util.Animation;
import dev.nebula.util.ColorUtil;
import dev.nebula.util.Render3D;
import dev.nebula.util.Theme;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;

public class BlockOverlay extends Module {
    private final BoolSetting fill = add(new BoolSetting("Fill", true));
    private final NumberSetting fillAlpha = add(new NumberSetting("Fill Alpha", 0.2, 0.05, 0.8, 0.05)).visibleIf(fill::get);
    private final NumberSetting lineWidth = add(new NumberSetting("Line Width", 2.0, 1.0, 5.0, 0.5));
    private final BoolSetting smooth = add(new BoolSetting("Smooth", true));

    private final Animation[] box = new Animation[6];
    private final Animation visible = new Animation(0, 12);
    private boolean init;

    public BlockOverlay() {
        super("BlockOverlay", "Smooth animated block selection.", Category.RENDER);
        for (int i = 0; i < 6; i++) box[i] = new Animation(0, 20);
    }

    @Override
    public void onRender3D(MatrixStack ms, float delta) {
        Box target = null;
        HitResult hr = mc.crosshairTarget;
        if (hr != null && hr.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) hr).getBlockPos();
            BlockState st = mc.world.getBlockState(pos);
            if (!st.isAir() && mc.world.getWorldBorder().contains(pos)) {
                VoxelShape shape = st.getOutlineShape(mc.world, pos);
                if (!shape.isEmpty()) target = shape.getBoundingBox().offset(pos).expand(0.002);
            }
        }
        double v = visible.animate(target != null ? 1 : 0);
        if (target != null) {
            double[] t = {target.minX, target.minY, target.minZ, target.maxX, target.maxY, target.maxZ};
            for (int i = 0; i < 6; i++) {
                if (!init || !smooth.get() || v < 0.05) box[i].set(t[i]); else box[i].animate(t[i]);
            }
            init = true;
        } else for (Animation a : box) a.update();
        if (v < 0.01) return;

        Box b = new Box(box[0].get(), box[1].get(), box[2].get(), box[3].get(), box[4].get(), box[5].get());
        Render3D.setup(true);
        if (fill.get()) Render3D.filledBox(ms, b, ColorUtil.fade(Theme.color(0), fillAlpha.get() * v));
        Render3D.outlineBox(ms, b, ColorUtil.fade(Theme.color(1), v), lineWidth.getFloat());
        Render3D.end();
    }
}
