package dev.nebula.module.hud;

import dev.nebula.setting.BoolSetting;
import dev.nebula.util.*;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InfoHud extends HudModule {
    private final BoolSetting coords = add(new BoolSetting("Coords", true));
    private final BoolSetting nether = add(new BoolSetting("Nether Coords", true));
    private final BoolSetting speed = add(new BoolSetting("Speed", true));
    private final BoolSetting direction = add(new BoolSetting("Direction", true));
    private final BoolSetting background = add(new BoolSetting("Background", true));

    private double lastX, lastZ, bps;
    private final Animation bpsAnim = new Animation(0, 6);

    public InfoHud() {
        super("Info", "Coordinates, speed and facing.");
        setEnabled(true, false);
    }

    @Override
    protected void defaultPosition(int sw, int sh) { x = 4; y = sh - 50; }

    @Override
    public void onTick() {
        double dx = mc.player.getX() - lastX, dz = mc.player.getZ() - lastZ;
        bps = Math.sqrt(dx * dx + dz * dz) * 20.0;
        lastX = mc.player.getX();
        lastZ = mc.player.getZ();
    }

    @Override
    protected void draw(MatrixStack ms, float delta) {
        List<String[]> lines = new ArrayList<>();
        if (coords.get()) lines.add(new String[]{"XYZ", String.format(Locale.US, "%.1f %.1f %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ())});
        if (nether.get()) {
            boolean inNether = mc.world.getRegistryKey() == net.minecraft.world.World.NETHER;
            double f = inNether ? 8 : 0.125;
            lines.add(new String[]{inNether ? "Overworld" : "Nether", String.format(Locale.US, "%.1f %.1f", mc.player.getX() * f, mc.player.getZ() * f)});
        }
        if (speed.get()) lines.add(new String[]{"Speed", String.format(Locale.US, "%.2f b/s", bpsAnim.animate(bps))});
        if (direction.get()) lines.add(new String[]{"Facing", mc.player.getHorizontalFacing().asString()});

        float lh = 11;
        float w = 20;
        for (String[] l : lines) w = Math.max(w, Fonts.width(l[0] + "  " + l[1]) + 10);
        width = w;
        height = Math.max(lh, lines.size() * lh + 4);

        if (background.get()) {
            Render2D.shadow(ms, x, y, width, height, 4, 6, 0x70000000);
            Render2D.rounded(ms, x, y, width, height, 4, ColorUtil.withAlpha(Theme.BG, 200));
        }
        float cy = y + 3;
        int i = 0;
        for (String[] l : lines) {
            Fonts.drawShadow(ms, l[0], x + 5, cy + 1, Theme.color(i * 0.4));
            Fonts.drawShadow(ms, l[1], x + 5 + Fonts.width(l[0] + "  "), cy + 1, Theme.TEXT);
            cy += lh;
            i++;
        }
    }
}
