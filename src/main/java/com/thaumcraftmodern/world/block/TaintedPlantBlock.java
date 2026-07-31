package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.config.ThaumcraftModernServerConfig;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * The four grounded TC4 taint-fibre vegetation stages. Fibrous taint itself
 * remains a multiface block; these variants occupy a normal plant position.
 */
public final class TaintedPlantBlock extends BushBlock {
    public TaintedPlantBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean mayPlaceOn(
            BlockState state,
            BlockGetter level,
            BlockPos position
    ) {
        return state.is(BlockTags.DIRT)
                || state.is(ModBlocks.CRUSTED_TAINT.get())
                || state.is(ModBlocks.TAINTED_SOIL.get());
    }

    @Override
    public void randomTick(
            BlockState state,
            ServerLevel level,
            BlockPos position,
            RandomSource random
    ) {
        TaintEcology.randomTick(level, position, state, random);
        if (!state.is(ModBlocks.MATURE_SPORE_STALK.get())
                || !ThaumcraftModernServerConfig.spawnTaintCreatures()
                || random.nextInt(10) != 0
                || !level.isEmptyBlock(position.above())
                || !level.getEntitiesOfClass(
                        com.thaumcraftmodern.entity.LegacyThaumcraftMob.class,
                        new AABB(position.above()).inflate(8.0D),
                        mob -> mob.kind()
                                == com.thaumcraftmodern.entity.LegacyMobKind
                                        .TAINT_SPORE
                ).isEmpty()) {
            return;
        }
        var spore = ModEntities.TAINT_SPORE.get().create(level);
        if (spore == null) {
            return;
        }
        spore.moveTo(
                position.getX() + 0.5D,
                position.getY() + 1.0D,
                position.getZ() + 0.5D,
                random.nextFloat() * 360.0F,
                0.0F
        );
        spore.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(position),
                MobSpawnType.MOB_SUMMONED,
                null,
                null
        );
        level.addFreshEntity(spore);
    }

    @Override
    public void entityInside(
            BlockState state,
            Level level,
            BlockPos position,
            Entity entity
    ) {
        TaintExposure.touch(level, entity);
    }
}
