package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class LiquidDeathBlock extends Block {
    public static final float FULL_DAMAGE = 4.0F;
    public static final ResourceKey<DamageType> DISSOLVE = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            new ResourceLocation(ThaumcraftModern.MOD_ID, "dissolve")
    );

    public LiquidDeathBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide) return;
        if (entity instanceof ItemEntity item) {
            item.discard();
            return;
        }
        if (entity instanceof LivingEntity living && level instanceof ServerLevel server) {
            DamageSource source = new DamageSource(server.registryAccess()
                    .registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DISSOLVE));
            living.hurt(source, FULL_DAMAGE);
        }
    }
}
