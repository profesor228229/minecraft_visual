package dev.nebula.module.hud;

import dev.nebula.gui.ClickGuiScreen;
import dev.nebula.module.Category;
import dev.nebula.module.Module;
import net.minecraft.client.util.math.MatrixStack;

/** A module that draws a draggable element on the HUD. */
public abstract class HudModule extends Module {
    protected float x = -1, y = -1;
    protected float width = 50, height = 12;

    protected HudModule(String name, String description) {
        super(name, description, Category.HUD);
    }

    /** Default position for a fresh config. */
    protected abstract void defaultPosition(int sw, int sh);

    protected abstract void draw(MatrixStack ms, float delta);

    @Override
    public final void onRender2D(MatrixStack ms, float delta) {
        if (mc.options.debugEnabled) return;
        int sw = mc.getWindow().getScaledWidth(), sh = mc.getWindow().getScaledHeight();
        if (x < 0 || y < 0) defaultPosition(sw, sh);
        clamp(sw, sh);
        draw(ms, delta);
    }

    public void clamp(int sw, int sh) {
        x = Math.max(0, Math.min(x, sw - width));
        y = Math.max(0, Math.min(y, sh - height));
    }

    public boolean isEditing() { return mc.currentScreen instanceof ClickGuiScreen; }

    public boolean onRightSide() { return x + width / 2f > mc.getWindow().getScaledWidth() / 2f; }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public void setPosition(float x, float y) { this.x = x; this.y = y; }

    @Override public boolean showInArrayList() { return false; }
}
