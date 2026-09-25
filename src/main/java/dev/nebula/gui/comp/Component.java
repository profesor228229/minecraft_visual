package dev.nebula.gui.comp;

import dev.nebula.setting.Setting;
import dev.nebula.util.Animation;
import net.minecraft.client.util.math.MatrixStack;

public abstract class Component {
    protected final Setting<?> setting;
    protected float x, y, w;
    public final Animation visAnim;
    protected final Animation hover = new Animation(0, 14);

    protected Component(Setting<?> setting) {
        this.setting = setting;
        this.visAnim = new Animation(setting == null || setting.isVisible() ? 1 : 0, 14);
    }

    public boolean visible() { return setting == null || setting.isVisible(); }

    /** Full height when visible. */
    public abstract float baseHeight();

    public float height() { return baseHeight() * (float) visAnim.get(); }

    public void updateVisibility() { visAnim.animate(visible() ? 1 : 0); }

    public void render(MatrixStack ms, float x, float y, float w, double mx, double my, double alpha) {
        this.x = x; this.y = y; this.w = w;
        hover.animate(hovered(mx, my) ? 1 : 0);
        draw(ms, mx, my, alpha);
    }

    protected abstract void draw(MatrixStack ms, double mx, double my, double alpha);

    public boolean hovered(double mx, double my) {
        return mx >= x && mx <= x + w && my >= y && my <= y + height();
    }

    public boolean mouseClicked(double mx, double my, int button) { return false; }
    public void mouseReleased(double mx, double my, int button) {}
    public void mouseDragged(double mx, double my) {}
    /** @return true if the key was consumed. */
    public boolean keyPressed(int key) { return false; }
    public boolean isListening() { return false; }
}
