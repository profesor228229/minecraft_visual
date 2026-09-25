package dev.nebula.module;

import dev.nebula.module.client.NotificationsModule;
import dev.nebula.setting.KeySetting;
import dev.nebula.setting.Setting;
import dev.nebula.util.Animation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    private final String name;
    private final String description;
    private final Category category;
    private final List<Setting<?>> settings = new ArrayList<>();
    public final KeySetting bind;
    private boolean enabled;

    /** Used by the ArrayList HUD for slide animations. */
    public final Animation arrayAnim = new Animation(0, 12);
    public final Animation arrayY = new Animation(-1, 14);

    protected Module(String name, String description, Category category) {
        this(name, description, category, 0);
    }

    protected Module(String name, String description, Category category, int key) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.bind = new KeySetting("Bind", key);
    }

    protected <T extends Setting<?>> T add(T s) {
        settings.add(s);
        return s;
    }

    public void toggle() { setEnabled(!enabled); }

    public void setEnabled(boolean enabled) {
        setEnabled(enabled, true);
    }

    public void setEnabled(boolean enabled, boolean notify) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) onEnable(); else onDisable();
        if (notify && showInArrayList()) NotificationsModule.onToggle(this);
    }

    public boolean isEnabled() { return enabled; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public List<Setting<?>> getSettings() { return settings; }

    /** Extra info shown next to the module in the ArrayList. */
    public String getSuffix() { return null; }
    public boolean showInArrayList() { return true; }

    protected boolean nullCheck() { return mc.player == null || mc.world == null; }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}
    public void onRender2D(MatrixStack matrices, float delta) {}
    public void onRender3D(MatrixStack matrices, float delta) {}
    public void onAttack(Entity target) {}
}
