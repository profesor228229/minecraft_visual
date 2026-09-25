package dev.nebula.module.hud;

import dev.nebula.Nebula;
import dev.nebula.setting.BoolSetting;
import dev.nebula.util.*;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Watermark extends HudModule {
    private final BoolSetting fps = add(new BoolSetting("FPS", true));
    private final BoolSetting ping = add(new BoolSetting("Ping", true));
    private final BoolSetting time = add(new BoolSetting("Time", true));
    private final BoolSetting user = add(new BoolSetting("Username", false));
    private final Animation widthAnim = new Animation(80, 10);
    private final SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");

    public Watermark() {
        super("Watermark", "Client logo with FPS, ping and time.");
        setEnabled(true, false);
    }

    @Override
    protected void defaultPosition(int sw, int sh) { x = 6; y = 6; }

    public static int fps() {
        try {
            String s = mc.fpsDebugString;
            return Integer.parseInt(s.substring(0, s.indexOf(' ')));
        } catch (Exception e) {
            return 0;
        }
    }

    public static int ping() {
        if (mc.getNetworkHandler() == null || mc.player == null) return 0;
        PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        return e == null ? 0 : e.getLatency();
    }

    @Override
    protected void draw(MatrixStack ms, float delta) {
        StringBuilder sb = new StringBuilder();
        if (user.get()) sb.append("  ").append(mc.getSession().getUsername());
        if (fps.get()) sb.append("  ").append(fps()).append(" fps");
        if (ping.get()) sb.append("  ").append(ping()).append(" ms");
        if (time.get()) sb.append("  ").append(fmt.format(new Date()));
        String info = sb.toString();

        float logo = 16;
        String name = Nebula.NAME;
        float target = logo + 6 + Fonts.width(name) + Fonts.width(info) + 8;
        float w = (float) widthAnim.animate(target);
        float h = 18;
        width = w; height = h;

        Render2D.shadow(ms, x, y, w, h, 5, 8, 0x90000000);
        Render2D.rounded(ms, x, y, w, h, 5, Theme.BG);
        // glow line under
        Render2D.roundedHorizontal(ms, x + 4, y + h - 1.5f, w - 8, 1f, 0.5f, Theme.color(0), Theme.color(2));

        // logo badge
        Render2D.roundedHorizontal(ms, x + 2.5f, y + 2.5f, logo - 3, h - 5, 3.5f, Theme.color(0), Theme.color(1.5));
        Fonts.drawCentered(ms, "N", x + 2.5f + (logo - 3) / 2f + 0.5f, y + 5, 0xFFFFFFFF);

        Render2D.pushScissor(x, y, w, h);
        float tx = x + logo + 4;
        Fonts.drawGradient(ms, name, tx, y + 5, 0, 255, false);
        Fonts.draw(ms, info, tx + Fonts.width(name), y + 5, Theme.TEXT_DIM);
        Render2D.popScissor();
    }
}
