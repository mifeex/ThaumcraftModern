package com.thaumcraftmodern.item;

import com.thaumcraftmodern.aura.AuraNodeBlockEntity;
import com.thaumcraftmodern.aura.AuraNodeType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;

/** Original 256-block, 0.66-radian field-of-view sinister-node indicator. */
public final class SinisterLodestoneItem extends Item {
    public SinisterLodestoneItem(Properties properties) { super(properties); }
    public static boolean isVisibleTo(Entity holder, Vec3 nodePosition) {
        Vec3 eye = holder.getEyePosition();
        Vec3 look = holder.getLookAngle().normalize();
        Vec3 delta = nodePosition.subtract(eye);
        return delta.lengthSqr() <= 256 * 256
                && delta.lengthSqr() > 0
                && delta.normalize().dot(look) > Math.cos(.66 / 2);
    }
}
