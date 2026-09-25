package dev.nebula.gui;

import com.google.gson.JsonObject;
import dev.nebula.Nebula;
import dev.nebula.mixin.GameRendererAccessor;
import dev.nebula.module.Category;
import dev.nebula.module.client.ClickGuiModule;
import dev.nebula.module.client.NotificationsModule;
import dev.nebula.module.hud.HudModule;
import dev.nebula.util.*;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends Screen {
    private static ClickGuiScreen instance;
    private static JsonObject pendingPanels;

    // current transform of the panel layer (for mouse + scissor conversion)
    private static float tScale = 1, tCx, tCy, tOffY;

    private final List<Panel> panels = new ArrayList<>();
    private final EaseAnimation open = new EaseAnimation(380, Easing.OUT_BACK);
    private final EaseAnimation fade = new EaseAnimation(250, Easing.OUT_CUBIC);
    private final Animation searchAnim = new Animation(0, 14);
    private final Animation tooltipAnim = new Animation(0, 12);
    private final Animation hintAnim = new Animation(0, 4);
    private String search = "";
    private String tooltip = "";
    private boolean closing;
    private HudModule draggingHud;
    private float hudDX, hudDY;
    private long openedAt;
    private boolean fresh = true;

    private ClickGuiScreen() {
        super(new LiteralText("Nebula"));
    }

    public static ClickGuiScreen get() {
        if (instance == null) instance = new ClickGuiScreen();
        return instance;
    }

    public static float guiScale() {
        return ClickGuiModule.INSTANCE == null ? 1f : ClickGuiModule.INSTANCE.scale.getFloat();
    }

    private void ensurePanels() {
        if (!panels.isEmpty()) return;
        Category[] cats = Category.values();
        float total = cats.length * Panel.WIDTH + (cats.length - 1) * 10;
        float sw = client.getWindow().getScaledWidth() / guiScale();
        float startX = Math.max(8, (sw - total) / 2f);
        for (int i = 0; i < cats.length; i++) panels.add(new Panel(cats[i], startX + i * (Panel.WIDTH + 10), 36));
        if (pendingPanels != null) {
            for (Panel p : panels) {
                if (!pendingPanels.has(p.category.name())) continue;
                JsonObject o = pendingPanels.getAsJsonObject(p.category.name());
                p.x = o.get("x").getAsFloat();
                p.y = o.get("y").getAsFloat();
                p.expanded = o.get("expanded").getAsBoolean();
            }
            pendingPanels = null;
        }
    }

    @Override
    protected void init() {
        ensurePanels();
        if (fresh) {
            open.reset(true);
            fade.reset(true);
            openedAt = System.currentTimeMillis();
            closing = false;
            fresh = false;
        }
        if (ClickGuiModule.INSTANCE != null && ClickGuiModule.INSTANCE.blur.get()) {
            try {
                ((GameRendererAccessor) client.gameRenderer).invokeLoadShader(new Identifier("shaders/post/blur.json"));
            } catch (Throwable t) {
                Nebula.LOGGER.warn("Blur shader unavailable");
            }
        }
    }

    @Override
    public void removed() {
        fresh = true;
        closing = false;
        draggingHud = null;
        ShaderEffect s = client.gameRenderer.getShader();
        if (s != null && s.getName().contains("blur")) client.gameRenderer.disableShader();
        Nebula.config().save();
    }

    @Override
    public void onClose() {
        if (closing) return;
        closing = true;
        open.setDirection(false);
        fade.setDirection(false);
        draggingHud = null;
    }

    @Override
    public boolean isPauseScreen() { return false; }

    // ------------------------------------------------------------ transforms
    private double lx(double mx) { return tCx + (mx - tCx) / tScale; }
    private double ly(double my) { return tCy + (my - tCy - tOffY) / tScale; }

    /** Scissor given in panel-layer coordinates. */
    public static void pushLocalScissor(float x, float y, float w, float h) {
        double sx = tCx + (x - tCx) * tScale;
        double sy = tCy + tOffY + (y - tCy) * tScale;
        Render2D.pushScissor(sx, sy, w * tScale, h * tScale);
    }

    public static void popScissor() { Render2D.popScissor(); }

    // ------------------------------------------------------------ render
    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float delta) {
        if (closing && open.isDone() && fade.isDone()) {
            client.openScreen(null);
            return;
        }
        ClickGuiModule cfg = ClickGuiModule.INSTANCE;
        double f = fade.get();
        double o = open.get();

        // background
        if (cfg == null || cfg.darken.get()) {
            Render2D.verticalGradient(ms, 0, 0, width, height, ColorUtil.fade(0x90050510, f), ColorUtil.fade(0xB0050510, f));
            Render2D.verticalGradient(ms, 0, height * 0.6f, width, height * 0.4f, 0x00000000,
                    ColorUtil.fade(ColorUtil.withAlpha(Theme.accent(), 40), f));
        }

        renderHudEditor(ms, mouseX, mouseY, f);

        // panels layer with scale-in animation
        float gs = guiScale();
        tCx = width / 2f;
        tCy = height / 2f;
        tScale = (float) (gs * (0.85 + 0.15 * o));
        tOffY = (float) ((1 - o) * 25);
        double lmx = lx(mouseX), lmy = ly(mouseY);

        ms.push();
        ms.translate(tCx, tCy + tOffY, 0);
        ms.scale(tScale, tScale, 1);
        ms.translate(-tCx, -tCy, 0);
        String hoveredDesc = null;
        for (Panel p : panels) {
            p.render(ms, lmx, lmy, f, search);
        }
        for (int i = panels.size() - 1; i >= 0; i--) {
            ModuleButton b = panels.get(i).hoveredButton(lmx, lmy);
            if (b != null) { hoveredDesc = b.module.getDescription(); break; }
        }
        ms.pop();

        renderSearch(ms, f);
        renderTooltip(ms, mouseX, mouseY, hoveredDesc, f);
        renderHint(ms, f);
        NotificationsModule.render(ms);
    }

    private void renderHudEditor(MatrixStack ms, int mx, int my, double f) {
        if (draggingHud != null) {
            draggingHud.setPosition(mx - hudDX, my - hudDY);
            draggingHud.clamp(width, height);
        }
        for (HudModule h : Nebula.modules().hudModules()) {
            if (!h.isEnabled() || h.getX() < 0) continue;
            boolean hov = draggingHud == h || (draggingHud == null && Render2D.hovered(mx, my, h.getX(), h.getY(), h.getWidth(), h.getHeight()));
            float pad = 2;
            int c = hov ? Theme.accent() : 0x60FFFFFF;
            Render2D.roundedOutline(ms, h.getX() - pad, h.getY() - pad, h.getWidth() + pad * 2, h.getHeight() + pad * 2, 4, 1f,
                    (px, py) -> ColorUtil.fade(c, f));
            if (hov) {
                String n = h.getName();
                float tw = Fonts.width(n) + 6;
                float ty = h.getY() - 14 < 0 ? h.getY() + h.getHeight() + 4 : h.getY() - 14;
                Render2D.rounded(ms, h.getX() - pad, ty, tw, 11, 3, ColorUtil.fade(Theme.accent(), f));
                Fonts.draw(ms, n, h.getX() - pad + 3, ty + 2, ColorUtil.fade(0xFFFFFFFF, f));
            }
        }
        if (draggingHud != null) {
            // center guide lines
            float cx = draggingHud.getX() + draggingHud.getWidth() / 2f;
            if (Math.abs(cx - width / 2f) < 3) Render2D.rect(ms, width / 2f - 0.5f, 0, 1, height, ColorUtil.withAlpha(Theme.accent(), 120));
        }
    }

    private void renderSearch(MatrixStack ms, double f) {
        double s = searchAnim.animate(search.isEmpty() ? 0 : 1);
        if (s < 0.01) return;
        String text = search + ((System.currentTimeMillis() / 500) % 2 == 0 ? "_" : " ");
        float w = Math.max(120, Fonts.width(text) + 40);
        float h = 20;
        float x = width / 2f - w / 2f;
        float y = (float) (height - 10 - h * s);
        double a = f * s;
        Render2D.shadow(ms, x, y, w, h, 6, 8, ColorUtil.fade(0xA0000000, a));
        Render2D.rounded(ms, x, y, w, h, 6, ColorUtil.fade(Theme.BG, a));
        Render2D.roundedOutline(ms, x, y, w, h, 6, 1f, (px, py) -> ColorUtil.fade(ColorUtil.lerp(Theme.accent(), Theme.accent2(), (px - x) / w), a));
        Fonts.draw(ms, "\u2315", x + 7, y + 6, ColorUtil.fade(Theme.accent(), a));
        Fonts.draw(ms, text, x + 18, y + 6, ColorUtil.fade(Theme.TEXT, a));
    }

    private void renderTooltip(MatrixStack ms, int mx, int my, String desc, double f) {
        boolean show = desc != null && ClickGuiModule.INSTANCE != null && ClickGuiModule.INSTANCE.descriptions.get() && draggingHud == null;
        if (show) tooltip = desc;
        double t = tooltipAnim.animate(show ? 1 : 0);
        if (t < 0.02 || tooltip.isEmpty()) return;
        float w = Fonts.width(tooltip) + 10, h = 14;
        float x = Math.min(mx + 10, width - w - 4), y = my + 12;
        if (y + h > height) y = my - h - 4;
        double a = t * f;
        Render2D.shadow(ms, x, y, w, h, 4, 5, ColorUtil.fade(0x90000000, a));
        Render2D.rounded(ms, x, y, w, h, 4, ColorUtil.fade(Theme.BG, a));
        Fonts.draw(ms, tooltip, x + 5, y + 3, ColorUtil.fade(Theme.TEXT, a));
    }

    private void renderHint(MatrixStack ms, double f) {
        boolean show = System.currentTimeMillis() - openedAt < 4000;
        double h = hintAnim.animate(show ? 1 : 0);
        if (h < 0.02) return;
        String s = "LMB toggle  \u2022  RMB settings  \u2022  MMB bind  \u2022  type to search  \u2022  drag HUD elements";
        Fonts.drawCentered(ms, s, width / 2f, 12, ColorUtil.fade(Theme.TEXT_DIM, h * f));
    }

    // ------------------------------------------------------------ input
    private boolean anyListening() {
        for (Panel p : panels) if (p.isListening()) return true;
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (closing) return false;
        double mx = lx(mouseX), my = ly(mouseY);
        for (int i = panels.size() - 1; i >= 0; i--) {
            Panel p = panels.get(i);
            if (p.mouseClicked(mx, my, button)) {
                // bring to front
                panels.remove(i);
                panels.add(p);
                return true;
            }
        }
        if (button == 0) {
            List<HudModule> huds = Nebula.modules().hudModules();
            for (int i = huds.size() - 1; i >= 0; i--) {
                HudModule h = huds.get(i);
                if (h.isEnabled() && Render2D.hovered(mouseX, mouseY, h.getX(), h.getY(), h.getWidth(), h.getHeight())) {
                    draggingHud = h;
                    hudDX = (float) (mouseX - h.getX());
                    hudDY = (float) (mouseY - h.getY());
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        double mx = lx(mouseX), my = ly(mouseY);
        for (Panel p : panels) p.mouseReleased(mx, my, button);
        draggingHud = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        double mx = lx(mouseX), my = ly(mouseY);
        for (int i = panels.size() - 1; i >= 0; i--) if (panels.get(i).mouseScrolled(mx, my, amount)) return true;
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (anyListening()) {
            for (Panel p : panels) if (p.keyPressed(keyCode)) return true;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (!search.isEmpty()) search = ""; else onClose();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (!search.isEmpty()) search = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0 ? "" : search.substring(0, search.length() - 1);
            return true;
        }
        if (search.isEmpty() && ClickGuiModule.INSTANCE != null && keyCode == ClickGuiModule.INSTANCE.bind.get()) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (anyListening() || closing) return false;
        if (Character.isLetterOrDigit(chr) || chr == ' ') {
            if (search.length() < 24 && !(search.isEmpty() && chr == ' ')) search += chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    // ------------------------------------------------------------ persistence
    public static JsonObject savePanels() {
        JsonObject o = new JsonObject();
        if (instance == null || instance.panels.isEmpty()) {
            return pendingPanels != null ? pendingPanels : o;
        }
        for (Panel p : instance.panels) {
            JsonObject j = new JsonObject();
            j.addProperty("x", p.x);
            j.addProperty("y", p.y);
            j.addProperty("expanded", p.expanded);
            o.add(p.category.name(), j);
        }
        return o;
    }

    public static void loadPanels(JsonObject o) { pendingPanels = o; }
}
