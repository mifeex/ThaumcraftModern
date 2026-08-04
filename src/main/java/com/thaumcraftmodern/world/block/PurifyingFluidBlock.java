package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.knowledge.WarpType;
import com.thaumcraftmodern.registry.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class PurifyingFluidBlock extends Block {
    public static final int MAX_WARD_TICKS = 32000;
    public static final int WARD_NUMERATOR = 200000;

    public PurifyingFluidBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || !(entity instanceof ServerPlayer player)
                || player.hasEffect(ModEffects.WARP_WARD.get())) return;
        int permanent = KnowledgeAccess.get(player)
                .map(knowledge -> knowledge.warp(WarpType.PERMANENT)).orElse(0);
        int divisor = Math.max(1, (int) Math.sqrt(permanent));
        player.addEffect(new MobEffectInstance(ModEffects.WARP_WARD.get(),
                Math.min(MAX_WARD_TICKS, WARD_NUMERATOR / divisor), 0, true, true));
        level.removeBlock(pos, false);
    }
}
