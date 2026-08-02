package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.registry.ModBlocks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Restores block entities missing from mana pods saved before aspect NBT. */
@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID)
public final class ManaPodMigrationEvents {
    private ManaPodMigrationEvents() {
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }
        chunk.findBlocks(
                state -> state.is(ModBlocks.MANA_POD.get()),
                (position, state) -> {
                    if (chunk.getBlockEntity(position) != null) {
                        return;
                    }
                    ManaPodBlockEntity pod = new ManaPodBlockEntity(
                            position,
                            state
                    );
                    chunk.setBlockEntity(pod);
                    chunk.setUnsaved(true);
                    level.sendBlockUpdated(
                            position,
                            state,
                            state,
                            Block.UPDATE_CLIENTS
                    );
                }
        );
    }
}
