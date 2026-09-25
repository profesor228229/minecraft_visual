package dev.nebula;

import dev.nebula.config.ConfigManager;
import dev.nebula.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Nebula implements ClientModInitializer {
    public static final String NAME = "Nebula";
    public static final String VERSION = "1.0";
    public static final Logger LOGGER = LogManager.getLogger(NAME);

    private static ModuleManager moduleManager;
    private static ConfigManager configManager;

    @Override
    public void onInitializeClient() {
        moduleManager = new ModuleManager();
        configManager = new ConfigManager();
        configManager.load();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && client.world != null) moduleManager.onTick();
        });

        HudRenderCallback.EVENT.register((matrices, delta) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.options.hudHidden) return;
            moduleManager.onRender2D(matrices, delta);
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> configManager.save());
        LOGGER.info("{} {} initialized", NAME, VERSION);
    }

    public static ModuleManager modules() { return moduleManager; }
    public static ConfigManager config() { return configManager; }
}
