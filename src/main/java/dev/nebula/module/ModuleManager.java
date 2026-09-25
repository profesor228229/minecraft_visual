package dev.nebula.module;

import dev.nebula.module.client.ClickGuiModule;
import dev.nebula.module.client.NotificationsModule;
import dev.nebula.module.hud.*;
import dev.nebula.module.player.SwingAnimation;
import dev.nebula.module.player.ViewModel;
import dev.nebula.module.player.Zoom;
import dev.nebula.module.render.*;
import dev.nebula.module.world.Fullbright;
import dev.nebula.module.world.TimeChanger;
import dev.nebula.module.world.WorldParticles;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        // HUD
        add(new Watermark());
        add(new ArrayListHud());
        add(new TargetHud());
        add(new Keystrokes());
        add(new PotionsHud());
        add(new InfoHud());
        // Render
        add(new Crosshair());
        add(new BlockOverlay());
        add(new HitParticles());
        add(new JumpCircles());
        add(new ChinaHat());
        add(new TargetEsp());
        add(new Trails());
        add(new NoRender());
        // World
        add(new Fullbright());
        add(new TimeChanger());
        add(new WorldParticles());
        // Player
        add(new ViewModel());
        add(new SwingAnimation());
        add(new Zoom());
        // Client
        add(new ClickGuiModule());
        add(new NotificationsModule());
    }

    private void add(Module m) { modules.add(m); }

    public List<Module> getModules() { return modules; }

    public List<Module> byCategory(Category c) {
        return modules.stream().filter(m -> m.getCategory() == c).collect(Collectors.toList());
    }

    public Module get(String name) {
        for (Module m : modules) if (m.getName().equalsIgnoreCase(name)) return m;
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T get(Class<T> cls) {
        for (Module m : modules) if (m.getClass() == cls) return (T) m;
        return null;
    }

    public List<HudModule> hudModules() {
        List<HudModule> l = new ArrayList<>();
        for (Module m : modules) if (m instanceof HudModule) l.add((HudModule) m);
        return l;
    }

    public void onKey(int key) {
        if (key <= 0) return;
        for (Module m : modules) if (m.bind.get() == key) m.toggle();
    }

    public void onTick() {
        for (Module m : modules) if (m.isEnabled()) m.onTick();
    }

    public void onRender2D(MatrixStack ms, float delta) {
        for (Module m : modules) if (m.isEnabled()) m.onRender2D(ms, delta);
    }

    public void onRender3D(MatrixStack ms, float delta) {
        for (Module m : modules) if (m.isEnabled()) m.onRender3D(ms, delta);
    }

    public void onAttack(Entity target) {
        for (Module m : modules) if (m.isEnabled()) m.onAttack(target);
    }
}
