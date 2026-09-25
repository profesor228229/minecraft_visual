package dev.nebula.gui;

import dev.nebula.Nebula;
import dev.nebula.module.Category;
import dev.nebula.module.Module;
import dev.nebula.util.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;

public class Panel {
    public static final float WIDTH = 118, HEADER = 22;

    final Category category;
    float x, y;
    boolean expanded = true;
    private final List<ModuleButton> buttons = new ArrayList<>();
    private final Animation expandAnim = new Animation(1, 12);
    private final Animation scroll = new Animation(0, 16);
    private final Animation headerHover = new Animation(0, 14);
    private boolean dragging;
    private float dragX, dragY;
    private float lastVisibleH, lastContentH;

    public Panel(Category category, float x, float y) {
        this.category = category;
        this.x = x;
        this.y = y;
        for (Module m : Nebula.modules().byCategory(category)) buttons.add(new ModuleButton(m));
    }

    private float maxHeight() {
        MinecraftClient mc = MinecraftClient.getInstance();
        float sh = mc.getWindow().getScaledHeight() / ClickGuiScreen.guiScale();
        return Math.max(60, sh - y - HEADER - 40);
    }

    public void render(MatrixStack ms, double mx, double my, double alpha, String search) {
        if (dragging) { x = (float) mx - dragX; y = (float) my - dragY; }

        float content = 0;
        for (ModuleButton b : buttons) {
            b.updateAnimations(search.isEmpty() || b.module.getName().toLowerCase().contains(search.toLowerCase()));
            content += b.height();
        }
        double e = expandAnim.animate(expanded ? 1 : 0);
        float visible = Math.min(content, maxHeight());
        float bodyH = (visible + 4) * (float) e;
        lastVisibleH = visible;
        lastContentH = content;
        double maxScroll = Math.max(0, content - visible);
        if (scroll.getTarget() > maxScroll) scroll.setTarget(maxScroll);
        double sc = scroll.update();

        float totalH = HEADER + bodyH;
        Render2D.shadow(ms, x, y, WIDTH, totalH, 7, 12, ColorUtil.fade(0xB0000000, alpha));
        Render2D.rounded(ms, x, y, WIDTH, totalH, 7, ColorUtil.fade(Theme.PANEL, alpha));

        // header
        boolean hh = Render2D.hovered(mx, my, x, y, WIDTH, HEADER);
        double hv = headerHover.animate(hh ? 1 : 0);
        Render2D.roundedHorizontal(ms, x + 4, y + 4, 14, 14, 4, ColorUtil.fade(Theme.color(0), alpha), ColorUtil.fade(Theme.color(1.5), alpha));
        Fonts.drawCentered(ms, category.icon, x + 11.5f, y + 7, ColorUtil.fade(0xFFFFFFFF, alpha));
        Fonts.draw(ms, category.display, x + 23 + (float) hv, y + 7, ColorUtil.fade(Theme.TEXT, alpha));
        int enabled = 0;
        for (ModuleButton b : buttons) if (b.module.isEnabled()) enabled++;
        String cnt = enabled + "/" + buttons.size();
        Fonts.draw(ms, cnt, x + WIDTH - 7 - Fonts.width(cnt), y + 7, ColorUtil.fade(Theme.TEXT_DIM, alpha));
        if (e > 0.01) {
            Render2D.horizontalGradient(ms, x + 6, y + HEADER - 1, (WIDTH - 12) * (float) e, 1,
                    ColorUtil.fade(Theme.accent(), alpha * e), ColorUtil.fade(ColorUtil.withAlpha(Theme.accent2(), 0), alpha * e));
        }

        if (bodyH < 1) return;
        float by = y + HEADER + 2;
        boolean mouseIn = Render2D.hovered(mx, my, x, by, WIDTH, bodyH - 4);
        ClickGuiScreen.pushLocalScissor(x, by, WIDTH, bodyH - 4);
        float cy = by - (float) sc;
        for (ModuleButton b : buttons) {
            float h = b.height();
            b.setBounds(x, cy, WIDTH);
            if (cy + h >= by - 1 && cy <= by + bodyH) b.render(ms, x, cy, WIDTH, mx, my, alpha * e, mouseIn);
            cy += h;
        }
        ClickGuiScreen.popScissor();

        // scrollbar
        if (maxScroll > 1 && e > 0.5) {
            float track = visible;
            float barH = Math.max(12, track * visible / content);
            float barY = by + (float) ((track - barH) * (sc / maxScroll));
            Render2D.rounded(ms, x + WIDTH - 2.5f, barY, 1.5f, barH, 0.75f, ColorUtil.fade(Theme.accent(), alpha * 0.6));
        }

        // fade edges when scrolled
        if (sc > 1) Render2D.verticalGradient(ms, x, by, WIDTH, 8, ColorUtil.fade(Theme.PANEL, alpha), 0x00000000);
        if (sc < maxScroll - 1) Render2D.verticalGradient(ms, x, by + visible - 8, WIDTH, 8, 0x00000000, ColorUtil.fade(Theme.PANEL, alpha));
    }

    public boolean isOver(double mx, double my) {
        float h = HEADER + (lastVisibleH + 4) * (float) expandAnim.get();
        return Render2D.hovered(mx, my, x, y, WIDTH, h);
    }

    public ModuleButton hoveredButton(double mx, double my) {
        if (!expanded || !isOver(mx, my) || my < y + HEADER) return null;
        for (ModuleButton b : buttons) if (b.isHovered(mx, my)) return b;
        return null;
    }

    public boolean mouseClicked(double mx, double my, int button) {
        if (Render2D.hovered(mx, my, x, y, WIDTH, HEADER)) {
            if (button == 0) { dragging = true; dragX = (float) (mx - x); dragY = (float) (my - y); }
            else if (button == 1) expanded = !expanded;
            return true;
        }
        if (expanded && expandAnim.get() > 0.9 && isOver(mx, my)) {
            for (ModuleButton b : buttons) if (b.mouseClicked(mx, my, button)) return true;
            return true; // swallow clicks on empty panel space
        }
        return false;
    }

    public void mouseReleased(double mx, double my, int button) {
        dragging = false;
        for (ModuleButton b : buttons) b.mouseReleased(mx, my, button);
    }

    public boolean mouseScrolled(double mx, double my, double amount) {
        if (!isOver(mx, my)) return false;
        double max = Math.max(0, lastContentH - lastVisibleH);
        scroll.setTarget(Math.max(0, Math.min(max, scroll.getTarget() - amount * 24)));
        return true;
    }

    public boolean keyPressed(int key) {
        for (ModuleButton b : buttons) if (b.keyPressed(key)) return true;
        return false;
    }

    public boolean isListening() {
        for (ModuleButton b : buttons) if (b.isListening()) return true;
        return false;
    }
}
