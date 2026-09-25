package dev.nebula.gui.comp;

import dev.nebula.setting.KeySetting;
import dev.nebula.util.*;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

public class KeyComp extends Component {
    private final KeySetting s;
    private boolean listening;

    public KeyComp(KeySetting s) {
        super(s);
        this.s = s;
    }

    @Override public float baseHeight() { return 16; }

    public void listen() { listening = true; }

    @Override
    protected void draw(MatrixStack ms, double mx, double my, double a) {
        Fonts.draw(ms, s.getName(), x + 6, y + 4, ColorUtil.fade(ColorUtil.lerp(Theme.TEXT_DIM, Theme.TEXT, hover.get()), a));
        String v = listening ? "..." : s.keyName();
        float bw = Math.max(20, Fonts.width(v) + 10), bx = x + w - 6 - bw;
        double pulse = listening ? (Math.sin(System.currentTimeMillis() / 150.0) + 1) / 2 : 0;
        Render2D.rounded(ms, bx, y + 2.5f, bw, 11, 3, ColorUtil.fade(ColorUtil.lerp(Theme.ELEMENT, Theme.accent(), listening ? 0.3 + 0.3 * pulse : 0), a));
        Fonts.drawCentered(ms, v, bx + bw / 2, y + 4.5f, ColorUtil.fade(Theme.TEXT, a));
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (listening) {
            // allow binding mouse side buttons is out of scope; any click cancels
            listening = false;
            return hovered(mx, my);
        }
        if (hovered(mx, my) && button == 0) { listening = true; return true; }
        return false;
    }

    @Override
    public boolean keyPressed(int key) {
        if (!listening) return false;
        if (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_BACKSPACE) s.set(0);
        else s.set(key);
        listening = false;
        return true;
    }

    @Override public boolean isListening() { return listening; }
}
