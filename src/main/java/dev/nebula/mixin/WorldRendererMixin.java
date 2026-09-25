package dev.nebula.mixin;

import dev.nebula.Nebula;
import dev.nebula.module.render.BlockOverlay;
import dev.nebula.module.render.NoRender;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Inject(method = "drawBlockOutline", at = @At("HEAD"), cancellable = true)
    private void nebula$outline(CallbackInfo ci) {
        if (Nebula.modules() == null) return;
        BlockOverlay b = Nebula.modules().get(BlockOverlay.class);
        if (b != null && b.isEnabled()) ci.cancel();
    }

    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
    private void nebula$weather(CallbackInfo ci) {
        if (NoRender.active(n -> n.weather)) ci.cancel();
    }

    @Inject(method = "tickRainSplashing", at = @At("HEAD"), cancellable = true)
    private void nebula$rainSplash(CallbackInfo ci) {
        if (NoRender.active(n -> n.weather)) ci.cancel();
    }
}
