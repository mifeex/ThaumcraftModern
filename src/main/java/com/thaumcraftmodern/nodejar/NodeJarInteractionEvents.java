package com.thaumcraftmodern.nodejar;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aura.AuraNodeBlockEntity;
import com.thaumcraftmodern.aura.OperationNonceGuard;
import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.wand.WandVisService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Classic glass-triggered NODEJAR multiblock interaction.
 */
@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID)
public final class NodeJarInteractionEvents {
    private static final double MAXIMUM_DISTANCE = 6.0D;
    private static final NodeJarCaptureService CAPTURE =
            new NodeJarCaptureService(new OperationNonceGuard());

    private NodeJarInteractionEvents() {
    }

    @SubscribeEvent
    public static void rightClickGlass(
            PlayerInteractEvent.RightClickBlock event
    ) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !(event.getEntity() instanceof ServerPlayer player)
                || !level.getBlockState(event.getPos()).is(Blocks.GLASS)
                || !WandVisService.isWand(event.getItemStack())) {
            return;
        }

        ServerNodeJarWorld world = new ServerNodeJarWorld(
                level,
                ModBlocks.JARRED_AURA_NODE.get().defaultBlockState()
        );
        BlockPos nodePosition = findNodeForGlass(
                event.getPos(),
                level,
                world
        );
        if (nodePosition == null
                || !(level.getBlockEntity(nodePosition)
                instanceof AuraNodeBlockEntity node)) {
            return;
        }

        UUID operationId = UUID.nameUUIDFromBytes(
                ("nodejar-capture:"
                        + player.getUUID() + ":"
                        + level.getGameTime() + ":"
                        + event.getHand() + ":"
                        + nodePosition.asLong())
                        .getBytes(StandardCharsets.UTF_8)
        );
        NodeJarCaptureService.Request request =
                new NodeJarCaptureService.Request(
                        player.getUUID(),
                        operationId,
                        node.scanIdentity().nodeId(),
                        nodePosition,
                        NodeJarKeys.placement(level, nodePosition),
                        true,
                        hasNodeJarResearch(player),
                        true,
                        level.hasChunkAt(nodePosition),
                        Math.sqrt(player.distanceToSqr(
                                nodePosition.getX() + 0.5D,
                                nodePosition.getY() + 0.5D,
                                nodePosition.getZ() + 0.5D
                        )),
                        MAXIMUM_DISTANCE
                );
        NodeJarCaptureService.Result result = CAPTURE.capture(
                request,
                world,
                world,
                new HeldCastingToolPayment(player, event.getHand()),
                NodeJarSavedData.get(level)
        );
        if (result.status() != NodeJarCaptureService.Status.CAPTURED) {
            return;
        }

        for (NodeJarStructure.Cell cell : NodeJarStructure.cells()) {
            if (cell.kind() == NodeJarStructure.CellKind.AURA_NODE) {
                continue;
            }
            Block particleBlock = cell.kind()
                    == NodeJarStructure.CellKind.GLASS
                    ? Blocks.GLASS
                    : Blocks.OAK_SLAB;
            level.levelEvent(
                    null,
                    2001,
                    nodePosition.offset(cell.offset()),
                    Block.getId(particleBlock.defaultBlockState())
            );
        }
        level.playSound(
                null,
                nodePosition,
                ModSounds.WAND.get(),
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );
        player.swing(event.getHand(), true);
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.CONSUME);
    }

    private static BlockPos findNodeForGlass(
            BlockPos clickedGlass,
            ServerLevel level,
            ServerNodeJarWorld world
    ) {
        for (NodeJarStructure.Cell cell : NodeJarStructure.cells()) {
            if (cell.kind() != NodeJarStructure.CellKind.GLASS) {
                continue;
            }
            BlockPos candidate = clickedGlass.subtract(cell.offset());
            if (!(level.getBlockEntity(candidate)
                    instanceof AuraNodeBlockEntity node)) {
                continue;
            }
            NodeJarStructure.Validation validation = NodeJarStructure.validate(
                    candidate,
                    node.scanIdentity().nodeId(),
                    world
            );
            if (validation.valid()) {
                return candidate.immutable();
            }
        }
        return null;
    }

    private static boolean hasNodeJarResearch(ServerPlayer player) {
        return KnowledgeAccess.get(player)
                .map(knowledge -> knowledge.hasCompletedResearch("nodejar"))
                .orElse(false);
    }
}
