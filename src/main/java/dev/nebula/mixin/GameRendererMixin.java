package dev.nebula.mixin;

import dev.nebula.Nebula;
import dev.nebula.module.player.Zoom;
import dev.nebula.module.render.NoRender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "renderWorld", at = @At(value = "INVOKE_STRING",
            target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=hand"))
    private void nebula$render3D(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || Nebula.modules() == null) return;
        try {
            Nebula.modules().onRender3D(matrices, tickDelta);
        } catch (Throwable t) {
            Nebula.LOGGER.error("3D render error", t);
        }
    }

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void nebula$fov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
        if (changingFov) cir.setReturnValue(Zoom.modifyFov(cir.getReturnValue()));
    }

    @Inject(method = "bobViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void nebula$hurtCam(CallbackInfo ci) {
        if (NoRender.active(n -> n.hurtCam)) ci.cancel();
    }
}
