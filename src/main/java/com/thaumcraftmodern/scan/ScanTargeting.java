package com.thaumcraftmodern.scan;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.function.Predicate;

public final class ScanTargeting {
    private ScanTargeting() {
    }

    public static Optional<ItemEntity> findDroppedItem(Player player, float partialTick) {
        return findEntity(
                player,
                partialTick,
                entity -> entity instanceof ItemEntity item
                        && item.isAlive()
                        && !item.getItem().isEmpty()
        ).filter(ItemEntity.class::isInstance).map(ItemEntity.class::cast);
    }

    public static Optional<Entity> findAnyEntity(Player player, float partialTick) {
        return findEntity(
                player,
                partialTick,
                entity -> entity.isAlive()
        );
    }

    /**
     * Unlike the vanilla crosshair hit, this ray includes source and flowing
     * fluids. That makes water and lava valid Thaumometer targets.
     */
    public static Optional<BlockHitResult> findBlock(Player player, float partialTick) {
        HitResult hit = player.pick(
                ScanSessionManager.MAX_DISTANCE,
                partialTick,
                true
        );
        return hit instanceof BlockHitResult blockHit
                && hit.getType() == HitResult.Type.BLOCK
                ? Optional.of(blockHit)
                : Optional.empty();
    }

    public static Optional<Entity> findEntity(
            Player player,
            float partialTick,
            Predicate<Entity> predicate
    ) {
        Vec3 eye = player.getEyePosition(partialTick);
        Vec3 look = player.getViewVector(partialTick);
        Vec3 end = eye.add(look.scale(ScanSessionManager.MAX_DISTANCE));
        AABB search = player.getBoundingBox()
                .expandTowards(look.scale(ScanSessionManager.MAX_DISTANCE))
                .inflate(1.0D);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                player,
                eye,
                end,
                search,
                entity -> entity != player && predicate.test(entity),
                ScanSessionManager.MAX_DISTANCE_SQUARED
        );
        if (entityHit == null) {
            return Optional.empty();
        }

        HitResult blockHit = player.pick(ScanSessionManager.MAX_DISTANCE, partialTick, false);
        double entityDistance = eye.distanceToSqr(entityHit.getLocation());
        double blockDistance = blockHit.getType() == HitResult.Type.MISS
                ? Double.POSITIVE_INFINITY
                : eye.distanceToSqr(blockHit.getLocation());
        return entityDistance <= blockDistance
                ? Optional.of(entityHit.getEntity())
                : Optional.empty();
    }
}
