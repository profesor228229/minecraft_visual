package dev.nebula.mixin;

import dev.nebula.Nebula;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
    @Shadow @Final private MinecraftClient client;
    @Unique private boolean nebula$noScreen;

    @Inject(method = "onKey", at = @At("HEAD"))
    private void nebula$keyHead(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        nebula$noScreen = client.currentScreen == null;
    }

    // TAIL so the key that opens the ClickGui is not delivered to it right away
    @Inject(method = "onKey", at = @At("TAIL"))
    private void nebula$keyTail(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (action != GLFW.GLFW_PRESS || !nebula$noScreen || client.currentScreen != null) return;
        if (window != client.getWindow().getHandle() || Nebula.modules() == null) return;
        Nebula.modules().onKey(key);
    }
}
