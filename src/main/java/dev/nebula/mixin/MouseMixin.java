package dev.nebula.mixin;

import dev.nebula.module.player.Zoom;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void nebula$scroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (vertical != 0 && Zoom.onScroll(vertical)) ci.cancel();
    }
}
