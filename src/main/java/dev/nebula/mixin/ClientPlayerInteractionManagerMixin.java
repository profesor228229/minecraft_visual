package dev.nebula.mixin;

import dev.nebula.Nebula;
import dev.nebula.util.TargetTracker;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {
    @Inject(method = "attackEntity", at = @At("HEAD"))
    private void nebula$attack(PlayerEntity player, Entity target, CallbackInfo ci) {
        TargetTracker.onAttack(target);
        if (Nebula.modules() != null) Nebula.modules().onAttack(target);
    }
}
