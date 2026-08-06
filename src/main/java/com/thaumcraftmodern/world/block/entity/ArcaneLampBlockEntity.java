package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/** Server-side TC4 Arcane Lamp secondary-light placement. */
public final class ArcaneLampBlockEntity extends BlockEntity {
    private static final int RANGE = 15;

    public ArcaneLampBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ARCANE_LAMP.get(), pos, state);
    }

    public static void serverTick(Level rawLevel, BlockPos pos, BlockState state,
            ArcaneLampBlockEntity lamp) {
        if (!(rawLevel instanceof ServerLevel level)) return;
        int x = pos.getX() + level.random.nextInt(16) - level.random.nextInt(16);
        int z = pos.getZ() + level.random.nextInt(16) - level.random.nextInt(16);
        int y = pos.getY() + level.random.nextInt(16) - level.random.nextInt(16);
        y = Math.min(y, level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) + 4);
        y = Math.max(y, 5);
        BlockPos target = new BlockPos(x, y, z);
        if (level.getBlockState(target).isAir()
                && level.getMaxLocalRawBrightness(target) < 9) {
            level.setBlock(target, ModBlocks.ARCANE_LAMP_LIGHT.get()
                    .defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    public void removeLights() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -RANGE; x <= RANGE; x++) {
            for (int y = -RANGE; y <= RANGE; y++) {
                for (int z = -RANGE; z <= RANGE; z++) {
                    cursor.setWithOffset(worldPosition, x, y, z);
                    if (serverLevel.getBlockState(cursor).is(ModBlocks.ARCANE_LAMP_LIGHT.get())) {
                        serverLevel.removeBlock(cursor, false);
                    }
                }
            }
        }
    }
}
