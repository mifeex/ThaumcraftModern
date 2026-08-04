package com.thaumcraftmodern.client;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.world.block.InfusionPillarBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Creates TESRs for pillars saved before the block acquired its original BE renderer. */
@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID, value = Dist.CLIENT)
public final class InfusionPillarClientMigration {
    private static ClientLevel previousLevel;
    private static int scansRemaining;
    private static int delay;

    private InfusionPillarClientMigration() {
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || minecraft.player == null) {
            previousLevel = null;
            return;
        }
        if (level != previousLevel) {
            previousLevel = level;
            scansRemaining = 6;
            delay = 0;
        }
        if (scansRemaining <= 0 || delay-- > 0) return;
        delay = 20;
        scansRemaining--;

        BlockPos center = minecraft.player.blockPosition();
        BlockPos.MutableBlockPos position = new BlockPos.MutableBlockPos();
        for (int y = center.getY() - 8; y <= center.getY() + 8; y++) {
            for (int x = center.getX() - 24; x <= center.getX() + 24; x++) {
                for (int z = center.getZ() - 24; z <= center.getZ() + 24; z++) {
                    position.set(x, y, z);
                    if (!level.hasChunkAt(position)) continue;
                    var state = level.getBlockState(position);
                    if (!state.is(ModBlocks.INFUSION_PILLAR.get())
                            || state.getValue(InfusionPillarBlock.CAP)) continue;
                    // Never pass the reused MutableBlockPos into a BlockEntity
                    // constructor. BlockEntity retains that object as its world
                    // position, so subsequent scan steps used to move several
                    // pillar renderers away from their registered map keys.
                    BlockPos pillarPosition = position.immutable();
                    LevelChunk chunk = level.getChunkAt(pillarPosition);
                    chunk.getBlockEntity(pillarPosition,
                            LevelChunk.EntityCreationType.IMMEDIATE);
                }
            }
        }
    }
}
