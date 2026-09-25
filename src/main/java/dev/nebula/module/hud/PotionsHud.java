package dev.nebula.module.hud;

import dev.nebula.setting.BoolSetting;
import dev.nebula.util.*;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;

import java.util.ArrayList;
import java.util.List;

public class PotionsHud extends HudModule {
    public final BoolSetting hideVanilla = add(new BoolSetting("Hide Vanilla", true));
    private final Animation heightAnim = new Animation(14, 12);

    public PotionsHud() {
        super("Potions", "Clean list of active effects.");
        setEnabled(true, false);
    }

    @Override
    protected void defaultPosition(int sw, int sh) { x = sw - 110; y = sh / 2f - 30; }

    private static String roman(int n) {
        String[] r = {"", "", " II", " III", " IV", " V", " VI", " VII", " VIII", " IX", " X"};
        return n < r.length ? r[n] : " " + n;
    }

    @Override
    protected void draw(MatrixStack ms, float delta) {
        List<StatusEffectInstance> effects = new ArrayList<>(mc.player.getStatusEffects());
        float rowH = 13;
        width = 100;
        if (effects.isEmpty() && !isEditing()) { heightAnim.set(14); return; }

        float target = 16 + Math.max(1, effects.size()) * rowH + 3;
        height = (float) heightAnim.animate(target);
        Render2D.shadow(ms, x, y, width, height, 5, 7, 0x80000000);
        Render2D.rounded(ms, x, y, width, height, 5, Theme.BG);
        Fonts.drawGradient(ms, "Potions", x + 6, y + 4, 0, 255, false);
        Render2D.horizontalGradient(ms, x + 6, y + 14, width - 12, 0.7f, ColorUtil.withAlpha(Theme.accent(), 140), 0x00000000);

        Render2D.pushScissor(x, y, width, height);
        float cy = y + 17;
        if (effects.isEmpty()) Fonts.draw(ms, "No effects", x + 6, cy + 2, Theme.TEXT_DIM);
        for (StatusEffectInstance e : effects) {
            int color = 0xFF000000 | e.getEffectType().getColor();
            String name = I18n.translate(e.getEffectType().getTranslationKey()) + roman(e.getAmplifier() + 1);
            String dur = StatusEffectUtil.durationToString(e, 1.0f);
            Render2D.circle(ms, x + 9, cy + 5, 2.5f, color);
            boolean blink = e.getDuration() < 200 && (System.currentTimeMillis() / 300) % 2 == 0;
            Fonts.draw(ms, name, x + 15, cy + 1.5f, Theme.TEXT);
            Fonts.draw(ms, dur, x + width - 6 - Fonts.width(dur), cy + 1.5f, blink ? 0xFFFF7A8E : Theme.TEXT_DIM);
            cy += rowH;
        }
        Render2D.popScissor();
    }
}
