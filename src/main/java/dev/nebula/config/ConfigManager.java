package dev.nebula.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.nebula.Nebula;
import dev.nebula.gui.ClickGuiScreen;
import dev.nebula.module.Module;
import dev.nebula.module.hud.HudModule;
import dev.nebula.setting.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path file = FabricLoader.getInstance().getConfigDir().resolve("nebula").resolve("config.json");

    public void save() {
        try {
            JsonObject root = new JsonObject();
            JsonObject mods = new JsonObject();
            for (Module m : Nebula.modules().getModules()) {
                JsonObject o = new JsonObject();
                o.addProperty("enabled", m.isEnabled());
                o.addProperty("bind", m.bind.get());
                JsonObject s = new JsonObject();
                for (Setting<?> st : m.getSettings()) s.add(st.getName(), st.toJson());
                o.add("settings", s);
                if (m instanceof HudModule) {
                    o.addProperty("x", ((HudModule) m).getX());
                    o.addProperty("y", ((HudModule) m).getY());
                }
                mods.add(m.getName(), o);
            }
            root.add("modules", mods);
            root.add("gui", ClickGuiScreen.savePanels());
            Files.createDirectories(file.getParent());
            try (Writer w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                GSON.toJson(root, w);
            }
        } catch (Exception e) {
            Nebula.LOGGER.error("Failed to save config", e);
        }
    }

    public void load() {
        if (!Files.exists(file)) return;
        try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonObject root = new JsonParser().parse(r).getAsJsonObject();
            JsonObject mods = root.getAsJsonObject("modules");
            if (mods != null) {
                for (Module m : Nebula.modules().getModules()) {
                    if (!mods.has(m.getName())) continue;
                    JsonObject o = mods.getAsJsonObject(m.getName());
                    try {
                        if (o.has("bind")) m.bind.set(o.get("bind").getAsInt());
                        JsonObject s = o.getAsJsonObject("settings");
                        if (s != null) {
                            for (Setting<?> st : m.getSettings()) {
                                JsonElement e = s.get(st.getName());
                                if (e != null) {
                                    try { st.fromJson(e); } catch (Exception ignored) {}
                                }
                            }
                        }
                        if (m instanceof HudModule && o.has("x")) {
                            ((HudModule) m).setPosition(o.get("x").getAsFloat(), o.get("y").getAsFloat());
                        }
                        if (o.has("enabled")) m.setEnabled(o.get("enabled").getAsBoolean(), false);
                    } catch (Exception ex) {
                        Nebula.LOGGER.warn("Bad config entry for {}", m.getName());
                    }
                }
            }
            if (root.has("gui")) ClickGuiScreen.loadPanels(root.getAsJsonObject("gui"));
        } catch (Exception e) {
            Nebula.LOGGER.error("Failed to load config", e);
        }
    }
}
