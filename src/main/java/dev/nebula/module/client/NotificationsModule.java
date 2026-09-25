package dev.nebula.module.client;

import dev.nebula.Nebula;
import dev.nebula.module.Category;
import dev.nebula.module.Module;
import dev.nebula.setting.BoolSetting;
import dev.nebula.setting.NumberSetting;
import dev.nebula.util.*;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NotificationsModule extends Module {
    private static final List<Notification> LIST = new ArrayList<>();

    public final BoolSetting toggles = add(new BoolSetting("Module Toggles", true));
    public final NumberSetting time = add(new NumberSetting("Duration", 1.8, 0.5, 5.0, 0.1, "s"));

    public NotificationsModule() {
        super("Notifications", "Smooth toasts in the bottom-right corner.", Category.CLIENT);
        setEnabled(true, false);
    }

    @Override public boolean showInArrayList() { return false; }

    public static void onToggle(Module m) {
        NotificationsModule n = Nebula.modules() == null ? null : Nebula.modules().get(NotificationsModule.class);
        if (n == null || !n.isEnabled() || !n.toggles.get()) return;
        push(m.getName(), m.isEnabled() ? "enabled" : "disabled", m.isEnabled());
    }

    public static void push(String title, String text, boolean positive) {
        NotificationsModule n = Nebula.modules().get(NotificationsModule.class);
        long dur = (long) ((n == null ? 1.8 : n.time.get()) * 1000);
        // replace existing notification for same module to avoid spam
        for (Notification x : LIST) if (x.title.equals(title) && !x.closing()) { x.closeNow(); }
        LIST.add(new Notification(title, text, positive, dur));
        if (LIST.size() > 8) LIST.get(0).closeNow();
    }

    @Override
    public void onRender2D(MatrixStack ms, float delta) {
        render(ms);
    }

    /** Also called from the ClickGui so toasts are visible while it is open. */
    public static void render(MatrixStack ms) {
        if (LIST.isEmpty()) return;
        int sw = mc.getWindow().getScaledWidth(), sh = mc.getWindow().getScaledHeight();
        float y = sh - 8;
        List<Notification> ordered = new ArrayList<>(LIST);
        java.util.Collections.reverse(ordered);
        for (Notification n : ordered) {
            double p = n.anim.get();
            if (!n.anim.isForward() && n.anim.isDone()) continue;
            float w = Math.max(110, Fonts.width(n.title + " " + n.text) + 34);
            float h = 26;
            if (n.yAnim.get() == -1) n.yAnim.set(y - h);
            double slide = n.yAnim.animate(y - h);
            float x = (float) (sw - 8 - w * p);
            float ny = (float) slide;
            int a = (int) (255 * Math.min(1, p * 1.4));
            Render2D.shadow(ms, x, ny, w, h, 6, 8, ColorUtil.withAlpha(0xFF000000, (int) (160 * p)));
            Render2D.rounded(ms, x, ny, w, h, 6, ColorUtil.fade(Theme.BG, p));
            // icon
            int ic = n.positive ? Theme.accent() : 0xFFFF5470;
            Render2D.circle(ms, x + 13, ny + 13, 7, ColorUtil.fade(ColorUtil.withAlpha(ic, 60), p));
            Render2D.circle(ms, x + 13, ny + 13, 3.5f, ColorUtil.fade(ic, p));
            Fonts.draw(ms, n.title, x + 26, ny + 5, ColorUtil.withAlpha(Theme.TEXT, a));
            Fonts.draw(ms, n.text, x + 26, ny + 15, ColorUtil.withAlpha(n.positive ? Theme.accent() : 0xFFFF7A8E, a));
            // progress
            double prog = n.progress();
            Render2D.roundedHorizontal(ms, x + 4, ny + h - 2.5f, (float) ((w - 8) * (1 - prog)), 1.5f, 0.75f,
                    ColorUtil.fade(Theme.accent(), p), ColorUtil.fade(Theme.accent2(), p));
            y -= (h + 5) * p;
        }
        Iterator<Notification> it = LIST.iterator();
        while (it.hasNext()) {
            Notification n = it.next();
            n.tick();
            if (n.dead()) it.remove();
        }
    }

    private static class Notification {
        final String title, text;
        final boolean positive;
        final long duration;
        final long start = System.currentTimeMillis();
        final EaseAnimation anim = new EaseAnimation(350, Easing.OUT_BACK);
        final Animation yAnim = new Animation(-1, 14);
        boolean closing;

        Notification(String title, String text, boolean positive, long duration) {
            this.title = title; this.text = text; this.positive = positive; this.duration = duration;
            anim.reset(true);
        }

        double progress() { return Math.min(1, (System.currentTimeMillis() - start) / (double) duration); }
        boolean closing() { return closing; }
        void closeNow() { closing = true; anim.setDirection(false); }
        void tick() { if (!closing && progress() >= 1) closeNow(); }
        boolean dead() { return closing && anim.isDone(); }
    }
}
