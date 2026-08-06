package com.thaumcraftmodern.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** Registry-compatible class retained for existing straw-golem worlds. */
public final class StrawGolemEntity extends ClassicGolemEntity {
    public static final int HEALTH = 10;
    public static final int ARMOR = 0;
    public static final double SPEED = .38D;
    public static final int CARRY_LIMIT = 1;
    public static final int UPGRADE_SLOTS = 1;
    public static final int REGEN_DELAY = 75;

    public StrawGolemEntity(EntityType<? extends StrawGolemEntity> type, Level level) {
        super(type, level, GolemMaterial.STRAW);
    }
}
