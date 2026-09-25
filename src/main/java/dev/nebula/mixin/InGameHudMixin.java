package dev.nebula.mixin;

import dev.nebula.Nebula;
import dev.nebula.module.hud.PotionsHud;
import dev.nebula.module.render.Crosshair;
import dev.nebula.module.render.NoRender;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void nebula$crosshair(CallbackInfo ci) {
        if (Nebula.modules() == null) return;
        Crosshair c = Nebula.modules().get(Crosshair.class);
        if (c != null && c.isEnabled()) ci.cancel();
    }

    @Inject(method = "renderVignetteOverlay", at = @At("HEAD"), cancellable = true)
    private void nebula$vignette(CallbackInfo ci) {
        if (NoRender.active(n -> n.vignette)) ci.cancel();
    }

    @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
    private void nebula$effects(CallbackInfo ci) {
        if (Nebula.modules() == null) return;
        PotionsHud p = Nebula.modules().get(PotionsHud.class);
        if (p != null && p.isEnabled() && p.hideVanilla.get()) ci.cancel();
    }
}
