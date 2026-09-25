package dev.nebula.module.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.nebula.setting.BoolSetting;
import dev.nebula.util.*;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;

import java.util.Locale;

public class TargetHud extends HudModule {
    private final BoolSetting particles = add(new BoolSetting("Particles", true));
    private final BoolSetting glow = add(new BoolSetting("Glow", true));

    private final EaseAnimation show = new EaseAnimation(300, Easing.OUT_BACK);
    private final Animation health = new Animation(1, 8);
    private final Animation healthLag = new Animation(1, 2.5);
    private LivingEntity last;
    private float lastHp = -1;
    private final java.util.List<float[]> bits = new java.util.ArrayList<>(); // x,y,vx,vy,life
    private long lastFrame = System.nanoTime();

    public TargetHud() {
        super("TargetHud", "Info about the entity you are fighting.");
        setEnabled(true, false);
    }

    @Override
    protected void defaultPosition(int sw, int sh) { x = sw / 2f + 20; y = sh / 2f + 20; }

    @Override
    protected void draw(MatrixStack ms, float delta) {
        LivingEntity t = TargetTracker.get();
        if (t == null && isEditing()) t = mc.player;
        show.setDirection(t != null);
        if (t != null) {
            if (t != last) { health.set(hp(t)); healthLag.set(hp(t)); lastHp = t.getHealth(); bits.clear(); }
            last = t;
        }
        double p = show.get();
        if (p <= 0.01 || last == null) return;
        LivingEntity e = last;

        width = 130; height = 44;
        float w = width, h = height;

        ms.push();
        ms.translate(x + w / 2, y + h / 2, 0);
        ms.scale((float) p, (float) p, 1);
        ms.translate(-(x + w / 2), -(y + h / 2), 0);
        double fade = Math.min(1, Math.max(0, p));

        if (glow.get()) Render2D.shadow(ms, x, y, w, h, 7, 10, ColorUtil.withAlpha(Theme.accent(), (int) (90 * fade)));
        Render2D.rounded(ms, x, y, w, h, 7, ColorUtil.fade(Theme.BG, fade));

        // head
        float hs = 32;
        float hx = x + 6, hy = y + 6;
        float hurt = e.hurtTime > 0 ? e.hurtTime / 10f : 0;
        float shrink = hurt * 2.5f;
        if (e instanceof AbstractClientPlayerEntity) {
            mc.getTextureManager().bindTexture(((AbstractClientPlayerEntity) e).getSkinTexture());
            RenderSystem.enableBlend();
            RenderSystem.color4f(1, 1 - hurt * 0.6f, 1 - hurt * 0.6f, (float) fade);
            int ix = (int) (hx + shrink), iy = (int) (hy + shrink), is = (int) (hs - shrink * 2);
            DrawableHelper.drawTexture(ms, ix, iy, is, is, 8, 8, 8, 8, 64, 64);
            DrawableHelper.drawTexture(ms, ix, iy, is, is, 40, 8, 8, 8, 64, 64); // hat layer
            RenderSystem.color4f(1, 1, 1, 1);
        } else {
            Render2D.rounded(ms, hx + shrink, hy + shrink, hs - shrink * 2, hs - shrink * 2, 5,
                    ColorUtil.fade(ColorUtil.lerp(Theme.ELEMENT, 0xFFFF4060, hurt), fade));
            String nm = e.getName().getString();
            String letter = nm.isEmpty() ? "?" : nm.substring(0, 1).toUpperCase();
            Fonts.drawCentered(ms, letter, hx + hs / 2f, hy + hs / 2f - 4, ColorUtil.withAlpha(Theme.TEXT, (int) (255 * fade)));
        }

        // name + info
        int ta = (int) (255 * fade);
        float tx = hx + hs + 6;
        String name = e.getName().getString();
        Render2D.pushScissor(tx, y, w - (tx - x) - 4, h);
        Fonts.draw(ms, name, tx, y + 7, ColorUtil.withAlpha(Theme.TEXT, ta));
        Render2D.popScissor();
        float hpNow = e.getHealth() + e.getAbsorptionAmount();
        String hpText = String.format(Locale.US, "%.1f HP", hpNow);
        String dist = String.format(Locale.US, "%.1fm", mc.player.distanceTo(e));
        Fonts.draw(ms, hpText, tx, y + 18, ColorUtil.withAlpha(Theme.TEXT_DIM, ta));
        if (e != mc.player) {
            boolean winning = mc.player.getHealth() + mc.player.getAbsorptionAmount() >= hpNow;
            String wl = winning ? "Winning" : "Losing";
            Fonts.draw(ms, wl, x + w - 6 - Fonts.width(wl), y + 18, ColorUtil.withAlpha(winning ? 0xFF6DFF9A : 0xFFFF6D80, ta));
        } else {
            Fonts.draw(ms, dist, x + w - 6 - Fonts.width(dist), y + 18, ColorUtil.withAlpha(Theme.TEXT_DIM, ta));
        }

        // health bar
        double hpFrac = hp(e);
        double hv = health.animate(hpFrac);
        double lag = healthLag.animate(hpFrac);
        if (lag < hv) healthLag.set(hv);
        float bx = tx, by = y + h - 11, bw = x + w - 6 - tx, bh = 5;
        Render2D.rounded(ms, bx, by, bw, bh, 2.5f, ColorUtil.fade(Theme.ELEMENT, fade));
        Render2D.rounded(ms, bx, by, (float) (bw * Math.max(lag, hv)), bh, 2.5f, ColorUtil.fade(0xFFFFFFFF, fade * 0.35));
        Render2D.roundedHorizontal(ms, bx, by, (float) (bw * hv), bh, 2.5f,
                ColorUtil.fade(Theme.accent(), fade), ColorUtil.fade(Theme.accent2(), fade));

        // damage particles flying out of the head
        if (lastHp >= 0 && e.getHealth() < lastHp && particles.get()) {
            for (int i = 0; i < 12; i++) {
                double a = Math.random() * Math.PI * 2, s = 20 + Math.random() * 50;
                bits.add(new float[]{hx + hs / 2, hy + hs / 2, (float) (Math.cos(a) * s), (float) (Math.sin(a) * s), 1f});
            }
        }
        lastHp = e.getHealth();
        long now = System.nanoTime();
        float dt = Math.min(0.05f, (now - lastFrame) / 1e9f);
        lastFrame = now;
        java.util.Iterator<float[]> it = bits.iterator();
        int idx = 0;
        while (it.hasNext()) {
            float[] b = it.next();
            b[0] += b[2] * dt; b[1] += b[3] * dt;
            b[2] *= 0.96f; b[3] = b[3] * 0.96f + 40 * dt;
            b[4] -= dt * 1.4f;
            if (b[4] <= 0) { it.remove(); continue; }
            int c = ColorUtil.fade(Theme.color(idx++ * 0.3), b[4] * fade);
            Render2D.circle(ms, b[0], b[1], 1.5f + b[4], c);
        }
        ms.pop();
    }

    private static double hp(LivingEntity e) {
        return Math.max(0, Math.min(1, (e.getHealth() + e.getAbsorptionAmount()) / Math.max(1, e.getMaxHealth() + e.getAbsorptionAmount())));
    }
}
