package dev.nebula.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

/** Remembers the last entity the player attacked. */
public final class TargetTracker {
    private TargetTracker() {}

    private static LivingEntity target;
    private static long lastAttack;

    public static void onAttack(Entity e) {
        if (e instanceof LivingEntity) {
            target = (LivingEntity) e;
            lastAttack = System.currentTimeMillis();
        }
    }

    public static LivingEntity get() { return get(5000); }

    public static LivingEntity get(long timeoutMs) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return null;
        if (target != null && (target.removed || !target.isAlive() || target.world != mc.world
                || mc.player.distanceTo(target) > 12 || System.currentTimeMillis() - lastAttack > timeoutMs)) {
            target = null;
        }
        return target;
    }
}
