package com.thaumcraftmodern.nodejar;

import com.thaumcraftmodern.aura.AuraNodeBlockEntity;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.wand.WandVisService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Logical placed jar block. The central client registration should attach the
 * classic jar/node BER; this backend intentionally does not touch render
 * registries or Thaumometer visual files.
 */
public final class JarredAuraNodeBlock extends BaseEntityBlock {
    private static final VoxelShape OUTLINE = box(3, 0, 3, 13, 12, 13);
    private static final int UPDATE_FLAGS =
            Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS;

    private final Supplier<? extends BlockEntityType<?>> blockEntityType;

    public JarredAuraNodeBlock(
            Properties properties,
            Supplier<? extends BlockEntityType<?>> blockEntityType
    ) {
        super(properties);
        this.blockEntityType = Objects.requireNonNull(
                blockEntityType,
                "blockEntityType"
        );
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        /*
         * TC4 rendered the placed node jar through BlockJarRenderer and used
         * the tile renderer only for the node inside it. The baked block model
         * is the 1.20.1 equivalent of that split and carries the original
         * two-cuboid geometry and block textures.
         */
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos position,
            CollisionContext context
    ) {
        return OUTLINE;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    public boolean canBeReplaced(BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        return new JarredAuraNodeBlockEntity(blockEntityType.get(), position, state);
    }

    @Override
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos position,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (!WandVisService.isWand(player.getItemInHand(hand))) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel)
                || !(level.getBlockEntity(position)
                instanceof JarredAuraNodeBlockEntity jar)
                || jar.data().isEmpty()) {
            return InteractionResult.FAIL;
        }

        NodeJarData data = jar.data().orElseThrow();
        NodeJarSavedData savedData = NodeJarSavedData.get(serverLevel);
        String placementKey = NodeJarKeys.placement(serverLevel, position);
        if (!savedData.releasePlacedNode(data, placementKey)) {
            return InteractionResult.FAIL;
        }

        boolean released = serverLevel.setBlock(
                position,
                ModBlocks.AURA_NODE.get().defaultBlockState(),
                UPDATE_FLAGS
        );
        if (released
                && serverLevel.getBlockEntity(position)
                instanceof AuraNodeBlockEntity node) {
            released = node.initializeOnce(data.node());
        } else {
            released = false;
        }
        if (!released) {
            restoreJar(serverLevel, position, data);
            savedData.restoreReleasedNode(data, placementKey);
            return InteractionResult.FAIL;
        }

        serverLevel.levelEvent(
                null,
                2001,
                position,
                Block.getId(state)
        );
        serverLevel.playSound(
                null,
                position,
                SoundEvents.GLASS_BREAK,
                SoundSource.BLOCKS,
                1.0F,
                0.9F + serverLevel.getRandom().nextFloat() * 0.2F
        );
        player.swing(hand, true);
        return InteractionResult.CONSUME;
    }

    @Override
    public void playerDestroy(
            Level level,
            Player player,
            BlockPos position,
            BlockState state,
            @Nullable BlockEntity blockEntity,
            ItemStack tool
    ) {
        if (level instanceof ServerLevel serverLevel
                && blockEntity instanceof JarredAuraNodeBlockEntity jar) {
            jar.data().ifPresent(data -> {
                String placementKey = NodeJarKeys.placement(
                        serverLevel,
                        position
                );
                NodeJarSavedData savedData = NodeJarSavedData.get(serverLevel);
                if (player.getAbilities().instabuild) {
                    savedData.releasePlacedNode(data, placementKey);
                } else if (savedData.returnPlacedJarToItem(
                        data,
                        placementKey
                )) {
                    jar.createDrop(ModItems.JARRED_AURA_NODE.get())
                            .ifPresent(drop -> popResource(
                                    serverLevel,
                                    position,
                                    drop
                            ));
                }
            });
        }
        super.playerDestroy(level, player, position, state, blockEntity, tool);
    }

    @Override
    public ItemStack getCloneItemStack(
            BlockGetter level,
            BlockPos position,
            BlockState state
    ) {
        if (level.getBlockEntity(position)
                instanceof JarredAuraNodeBlockEntity jar) {
            return jar.createDrop(ModItems.JARRED_AURA_NODE.get())
                    .orElseGet(() -> new ItemStack(
                            ModItems.JARRED_AURA_NODE.get()
                    ));
        }
        return new ItemStack(ModItems.JARRED_AURA_NODE.get());
    }

    private static void restoreJar(
            ServerLevel level,
            BlockPos position,
            NodeJarData data
    ) {
        level.setBlock(
                position,
                ModBlocks.JARRED_AURA_NODE.get().defaultBlockState(),
                UPDATE_FLAGS
        );
        if (level.getBlockEntity(position)
                instanceof JarredAuraNodeBlockEntity restored) {
            restored.initializeOnce(data);
        }
    }
}
