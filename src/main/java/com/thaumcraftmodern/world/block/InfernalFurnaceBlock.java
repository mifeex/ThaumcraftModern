package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.construction.CraftingStructureDisassembly;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.world.block.entity.InfernalFurnaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Server-authoritative TC4 Infernal Furnace multiblock member. */
public final class InfernalFurnaceBlock extends BaseEntityBlock {
    public static final IntegerProperty PART = IntegerProperty.create("part", 0, 10);
    private static final VoxelShape CORE = Block.box(0, 0, 0, 16, 4, 16);
    private static final VoxelShape HALF_WEST = Block.box(0, 0, 0, 8, 16, 16);
    private static final VoxelShape HALF_EAST = Block.box(8, 0, 0, 16, 16, 16);
    private static final VoxelShape HALF_NORTH = Block.box(0, 0, 0, 16, 16, 8);
    private static final VoxelShape HALF_SOUTH = Block.box(0, 0, 8, 16, 16, 16);

    public InfernalFurnaceBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(PART, 0));
    }

    public BlockState stateForPart(int part) {
        return defaultBlockState().setValue(PART, part);
    }

    @Override public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override public void animateTick(BlockState state, Level level,
            BlockPos pos, RandomSource random) {
        if (state.getValue(PART) != 0) return;
        BlockPos above = pos.above();
        BlockState aboveState = level.getBlockState(above);
        if (!aboveState.isAir() && aboveState.isSolidRender(level, above)) return;

        // Vanilla 1.20.1 LavaFluid.animateTick: an occasional lava droplet
        // with a pop, plus a rarer independent ambient crackle.
        if (random.nextInt(100) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + 1.0D;
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.LAVA, x, y, z,
                    0.0D, 0.0D, 0.0D);
            level.playLocalSound(x, y, z, SoundEvents.LAVA_POP,
                    SoundSource.BLOCKS,
                    0.2F + random.nextFloat() * 0.2F,
                    0.9F + random.nextFloat() * 0.15F, false);
        }
        if (random.nextInt(200) == 0) {
            level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(),
                    SoundEvents.LAVA_AMBIENT, SoundSource.BLOCKS,
                    0.2F + random.nextFloat() * 0.2F,
                    0.9F + random.nextFloat() * 0.15F, false);
        }
    }

    @Override public VoxelShape getShape(BlockState state, BlockGetter level,
            BlockPos pos, CollisionContext context) {
        int part = state.getValue(PART);
        if (part == 0) return CORE;
        if (part != 10) return Shapes.block();
        Direction inward = inwardDirection(level, pos);
        return switch (inward) {
            case WEST -> HALF_WEST;
            case EAST -> HALF_EAST;
            case NORTH -> HALF_NORTH;
            case SOUTH -> HALF_SOUTH;
            default -> Shapes.block();
        };
    }

    @Override public void entityInside(BlockState state, Level level,
            BlockPos pos, Entity entity) {
        if (state.getValue(PART) != 0) return;
        if (entity instanceof ItemEntity item && !level.isClientSide
                && level.getBlockEntity(pos) instanceof InfernalFurnaceBlockEntity furnace
                && furnace.addItemsToInventory(item.getItem().copy())) {
            item.discard();
            return;
        }
        if (!entity.fireImmune()) {
            DamageSource hotFloor = level.damageSources().hotFloor();
            entity.hurt(hotFloor, 3.0F);
            entity.setSecondsOnFire(10);
        }
    }

    @Override public void playerWillDestroy(Level level, BlockPos pos,
            BlockState state, net.minecraft.world.entity.player.Player player) {
        if (!level.isClientSide && state.getValue(PART) == 0) {
            Blaze blaze = net.minecraft.world.entity.EntityType.BLAZE.create(level);
            if (blaze != null) {
                blaze.moveTo(pos.getX() + 0.5D, pos.getY() + 1.0D,
                        pos.getZ() + 0.5D, 0.0F, 0.0F);
                blaze.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE,
                        6000, 2));
                blaze.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE,
                        12000, 0));
                level.addFreshEntity(blaze);
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override public List<ItemStack> getDrops(BlockState state,
            LootParams.Builder params) {
        int part = state.getValue(PART);
        if (part == 0) return List.of();
        if (part == 10) return List.of(new ItemStack(Blocks.IRON_BARS));
        return List.of(new ItemStack((part % 2 == 0 || part == 5)
                ? Blocks.OBSIDIAN : Blocks.NETHER_BRICKS));
    }

    @Override public void onRemove(BlockState state, Level level, BlockPos pos,
            BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock()) && level instanceof ServerLevel server) {
            CraftingStructureDisassembly.partRemoved(server, pos, state);
        }
        super.onRemove(state, level, pos, replacement, moving);
    }

    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos,
            BlockState state) {
        int part = state.getValue(PART);
        return part == 0 || part == 10
                ? new InfernalFurnaceBlockEntity(pos, state) : null;
    }

    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(
                type, ModBlockEntities.INFERNAL_FURNACE.get(),
                InfernalFurnaceBlockEntity::serverTick);
    }

    private static Direction inwardDirection(BlockGetter level, BlockPos pos) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockState neighbour = level.getBlockState(pos.relative(direction));
            if (neighbour.getBlock() instanceof InfernalFurnaceBlock
                    && neighbour.getValue(PART) == 0) return direction;
        }
        return Direction.UP;
    }

    @Override protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART);
    }
}
