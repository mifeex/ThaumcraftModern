package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.aspect.AspectDefinition;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.crucible.CrucibleDegradationRules;
import com.thaumcraftmodern.crucible.CrucibleTransaction;
import com.thaumcraftmodern.crucible.CrucibleItemTossEvents;
import com.thaumcraftmodern.crucible.CrucibleFluidPresentation;
import com.thaumcraftmodern.crucible.CrucibleFluxRules;
import com.thaumcraftmodern.crucible.CrucibleWaterFillRules;
import com.thaumcraftmodern.crucible.EssentiaStore;
import com.thaumcraftmodern.crucible.ItemAspectRegistry;
import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModParticles;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.world.block.CrucibleBlock;
import com.thaumcraftmodern.world.block.FluxGasBlock;
import com.thaumcraftmodern.world.block.FluxGooBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Server-authoritative modern port of TC4 {@code TileCrucible}.
 */
public final class CrucibleBlockEntity extends BlockEntity {
    private static final String EJECTED_OUTPUT_TAG =
            "ThaumcraftCrucibleOutput";
    public static final int FLUID_CAPACITY_MB =
            CrucibleWaterFillRules.CAPACITY_MB;
    public static final int MAX_ESSENTIA =
            CrucibleFluxRules.MAX_ESSENTIA;
    public static final int BOILING_HEAT = 150;
    public static final int MAX_HEAT = 200;

    private final EssentiaStore essentia = new EssentiaStore();
    private short heat;
    private int water;
    private long counter = -100L;

    public CrucibleBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.CRUCIBLE.get(), position, state);
    }

    public static void serverTick(
            ServerLevel level,
            BlockPos position,
            BlockState state,
            CrucibleBlockEntity crucible
    ) {
        crucible.counter++;
        crucible.updateHeat(level, position);
        if (crucible.canProcessItems()) {
            crucible.processItems(level, position);
        }
        if (CrucibleFluxRules.shouldOverflow(
                crucible.essentia.total(),
                crucible.counter
        )) {
            crucible.removeRandomAspect(level);
            crucible.spill(level);
            crucible.sync();
        }
        if (crucible.counter
                > CrucibleDegradationRules.INTERVAL_TICKS
                && crucible.heat > BOILING_HEAT) {
            crucible.counter = 0L;
            crucible.degradeOne(level);
        }
    }

    public static void clientTick(
            Level level,
            BlockPos position,
            BlockState state,
            CrucibleBlockEntity crucible
    ) {
        if (crucible.water <= 0) {
            return;
        }
        if (crucible.heat > BOILING_HEAT) {
            double height = position.getY() + crucible.fluidHeight();
            level.addParticle(
                    ModParticles.CRUCIBLE_FROTH.get(),
                    position.getX() + 0.2D + level.random.nextDouble() * 0.6D,
                    height,
                    position.getZ() + 0.2D + level.random.nextDouble() * 0.6D,
                    0.0D,
                    0.0D,
                    0.0D
            );
            if (crucible.essentia.total() > MAX_ESSENTIA) {
                crucible.spawnOverflowFroth(level, position);
            }
        }
        if (!crucible.essentia.isEmpty() && level.random.nextInt(6) == 0) {
            int color = crucible.randomAspectColor(level);
            level.addParticle(
                    ModParticles.CRUCIBLE_BUBBLE.get(),
                    position.getX() + 0.2D + level.random.nextDouble() * 0.6D,
                    position.getY() + crucible.fluidHeight() + 0.02D,
                    position.getZ() + 0.2D + level.random.nextDouble() * 0.6D,
                    ((color >> 16) & 0xFF) / 255.0D,
                    ((color >> 8) & 0xFF) / 255.0D,
                    (color & 0xFF) / 255.0D
            );
        }
    }

    private void spawnOverflowFroth(Level level, BlockPos position) {
        for (int index = 0; index < 2; index++) {
            spawnFrothDown(
                    level,
                    position.getX(),
                    position.getY() + 1.0D,
                    position.getZ() + level.random.nextDouble()
            );
            spawnFrothDown(
                    level,
                    position.getX() + 1.0D,
                    position.getY() + 1.0D,
                    position.getZ() + level.random.nextDouble()
            );
            spawnFrothDown(
                    level,
                    position.getX() + level.random.nextDouble(),
                    position.getY() + 1.0D,
                    position.getZ()
            );
            spawnFrothDown(
                    level,
                    position.getX() + level.random.nextDouble(),
                    position.getY() + 1.0D,
                    position.getZ() + 1.0D
            );
        }
    }

    private void spawnFrothDown(
            Level level,
            double x,
            double y,
            double z
    ) {
        level.addParticle(
                ModParticles.CRUCIBLE_FROTH.get(),
                x,
                y,
                z,
                0.0D,
                -1.0D,
                0.0D
        );
    }

    private void updateHeat(ServerLevel level, BlockPos position) {
        short previous = heat;
        if (water > 0 && isHeatSource(level, position.below())) {
            if (heat < MAX_HEAT) {
                heat++;
            }
        } else if (heat > 0) {
            heat--;
        }
        if (previous <= BOILING_HEAT && heat > BOILING_HEAT
                || previous > BOILING_HEAT && heat <= BOILING_HEAT) {
            sync();
        }
    }

    public static boolean isHeatSource(Level level, BlockPos position) {
        BlockState state = level.getBlockState(position);
        return state.is(Blocks.FIRE)
                || state.is(Blocks.SOUL_FIRE)
                || state.getFluidState().is(FluidTags.LAVA)
                || state.is(ModBlocks.NITOR.get());
    }

    private void processItems(ServerLevel level, BlockPos position) {
        AABB inside = new AABB(
                position.getX() + 0.125D,
                position.getY() + 0.45D,
                position.getZ() + 0.125D,
                position.getX() + 0.875D,
                position.getY() + 1.2D,
                position.getZ() + 0.875D
        );
        for (ItemEntity entity : level.getEntitiesOfClass(
                ItemEntity.class,
                inside,
                entity -> entity.isAlive()
                        && !entity.getPersistentData()
                        .getBoolean(EJECTED_OUTPUT_TAG)
        )) {
            processItem(level, entity);
            if (!canProcessItems()) {
                break;
            }
        }
    }

    private void processItem(ServerLevel level, ItemEntity entity) {
        ItemStack live = entity.getItem();
        int remaining = live.getCount();
        boolean dissolved = false;
        boolean crafted = false;
        while (remaining > 0 && canProcessItems()) {
            ItemStack one = live.copyWithCount(1);
            Optional<ItemStack> output = CrucibleTransaction.craft(
                    one,
                    water,
                    essentia,
                    researchPredicate(level, entity)
            );
            if (output.isPresent()) {
                water -= CrucibleTransaction.WATER_PER_RECIPE_MB;
                eject(level, output.get());
                remaining--;
                crafted = true;
                counter = -250L;
                continue;
            }
            Optional<Map<String, Integer>> aspects =
                    ItemAspectRegistry.aspects(one);
            if (aspects.isEmpty()) {
                entity.setDeltaMovement(
                        (level.random.nextDouble() - level.random.nextDouble())
                                * 0.2D,
                        0.35D,
                        (level.random.nextDouble() - level.random.nextDouble())
                                * 0.2D
                );
                level.playSound(
                        null,
                        worldPosition,
                        SoundEvents.ITEM_PICKUP,
                        SoundSource.BLOCKS,
                        0.2F,
                        (level.random.nextFloat() - level.random.nextFloat())
                                * 0.7F + 1.0F
                );
                break;
            }
            aspects.get().forEach(essentia::add);
            remaining--;
            dissolved = true;
            counter = -150L;
        }
        if (remaining <= 0) {
            entity.discard();
        } else if (remaining != live.getCount()) {
            live.setCount(remaining);
            entity.setItem(live);
        }
        if (dissolved) {
            level.playSound(
                    null,
                    worldPosition,
                    ModSounds.BUBBLE.get(),
                    SoundSource.BLOCKS,
                    0.2F,
                    1.0F + level.random.nextFloat() * 0.4F
            );
        }
        if (crafted) {
            level.playSound(
                    null,
                    worldPosition,
                    ModSounds.SPILL.get(),
                    SoundSource.BLOCKS,
                    0.25F,
                    1.0F
            );
        }
        if (dissolved || crafted) {
            sync();
        }
    }

    private Predicate<String> researchPredicate(
            ServerLevel level,
            ItemEntity entity
    ) {
        Entity owner = entity.getOwner();
        ServerPlayer player = owner instanceof ServerPlayer serverPlayer
                ? serverPlayer
                : null;
        if (player == null) {
            return research ->
                    CrucibleItemTossEvents.hasResearch(entity, research);
        }
        return research -> KnowledgeAccess.get(player)
                .map(knowledge -> knowledge.hasCompletedResearch(research))
                .orElseGet(() ->
                        CrucibleItemTossEvents.hasResearch(entity, research)
                );
    }

    private void eject(ServerLevel level, ItemStack output) {
        ItemStack remaining = output.copy();
        boolean first = true;
        while (!remaining.isEmpty()) {
            int count = Math.min(
                    remaining.getCount(),
                    remaining.getMaxStackSize()
            );
            ItemStack part = remaining.split(count);
            ItemEntity result = new ItemEntity(
                    level,
                    worldPosition.getX() + 0.5D,
                    worldPosition.getY() + 0.71D,
                    worldPosition.getZ() + 0.5D,
                    part
            );
            result.setDeltaMovement(
                    first
                            ? 0.0D
                            : (level.random.nextDouble()
                            - level.random.nextDouble()) * 0.01D,
                    0.1D,
                    first
                            ? 0.0D
                            : (level.random.nextDouble()
                            - level.random.nextDouble()) * 0.01D
            );
            result.getPersistentData().putBoolean(EJECTED_OUTPUT_TAG, true);
            level.addFreshEntity(result);
            first = false;
        }
    }

    private void degradeOne(ServerLevel level) {
        CrucibleDegradationRules.Result result =
                CrucibleDegradationRules.degradeOne(
                        essentia,
                        AspectRegistryRuntime::find,
                        level.random::nextInt,
                        level.random::nextBoolean
                );
        if (!result.changed()) {
            return;
        }
        water = Math.max(
                0,
                water - CrucibleDegradationRules.WATER_COST_MB
        );
        if (result.spill()) {
            spill(level);
        }
        updateFilledState();
        sync();
    }

    private void removeRandomAspect(ServerLevel level) {
        if (essentia.isEmpty()) {
            return;
        }
        List<String> aspects = new ArrayList<>(essentia.view().keySet());
        essentia.remove(aspects.get(level.random.nextInt(aspects.size())), 1);
    }

    public void spillRemnants(ServerLevel level) {
        spillRemnants(level, true);
    }

    /**
     * Finalizes the contents while the owning block is being replaced.
     *
     * <p>This deliberately skips {@link #updateFilledState()}: writing the
     * empty {@code filled} state during {@code Block#onRemove} would place the
     * Crucible back into the world while it is being destroyed.</p>
     */
    public void spillRemnantsOnRemoval(ServerLevel level) {
        spillRemnants(level, false);
    }

    private void spillRemnants(
            ServerLevel level,
            boolean updateLiveBlock
    ) {
        if (water <= 0 && essentia.isEmpty()) {
            return;
        }
        water = 0;
        int spills = CrucibleFluxRules.remnantSpillAttempts(
                essentia.total()
        );
        for (int index = 0; index < spills; index++) {
            spill(level);
        }
        essentia.clear();
        level.playSound(
                null,
                worldPosition,
                ModSounds.SPILL.get(),
                SoundSource.BLOCKS,
                0.5F,
                1.0F
        );
        if (updateLiveBlock) {
            updateFilledState();
            sync();
        }
    }

    private void spill(ServerLevel level) {
        if (!CrucibleFluxRules.materializesFlux(
                level.random.nextInt(
                        CrucibleFluxRules.SPILL_CHANCE_DENOMINATOR
                )
        )) {
            return;
        }
        BlockPos above = worldPosition.above();
        BlockState state = level.getBlockState(above);
        if (state.isAir()) {
            level.setBlock(
                    above,
                    level.random.nextBoolean()
                            ? ModBlocks.FLUX_GAS.get().defaultBlockState()
                            : ModBlocks.FLUX_GOO.get().defaultBlockState()
                                    .setValue(
                                            FluxGooBlock.LEVEL,
                                            CrucibleFluxRules
                                                    .INITIAL_FLUX_LEVEL
                                    ),
                    Block.UPDATE_ALL
            );
            return;
        }
        if (state.is(ModBlocks.FLUX_GOO.get())
                && state.getValue(FluxGooBlock.LEVEL) < 7) {
            level.setBlock(
                    above,
                    state.setValue(
                            FluxGooBlock.LEVEL,
                            state.getValue(FluxGooBlock.LEVEL) + 1
                    ),
                    Block.UPDATE_ALL
            );
            return;
        }
        if (state.is(ModBlocks.FLUX_GAS.get())
                && state.getValue(FluxGasBlock.LEVEL) < 7) {
            level.setBlock(
                    above,
                    state.setValue(
                            FluxGasBlock.LEVEL,
                            state.getValue(FluxGasBlock.LEVEL) + 1
                    ),
                    Block.UPDATE_ALL
            );
            return;
        }
        BlockPos nearby = worldPosition.offset(
                level.random.nextInt(3) - 1,
                level.random.nextInt(3) - 1,
                level.random.nextInt(3) - 1
        );
        if (level.getBlockState(nearby).isAir()) {
            level.setBlock(
                    nearby,
                    level.random.nextBoolean()
                            ? ModBlocks.FLUX_GAS.get().defaultBlockState()
                            : ModBlocks.FLUX_GOO.get().defaultBlockState()
                                    .setValue(
                                            FluxGooBlock.LEVEL,
                                            CrucibleFluxRules
                                                    .INITIAL_FLUX_LEVEL
                                    ),
                    Block.UPDATE_ALL
            );
        }
    }

    public boolean fillWater() {
        int filled = CrucibleWaterFillRules.fillFromBucket(water);
        if (filled == water) {
            return false;
        }
        water = filled;
        updateFilledState();
        sync();
        return true;
    }

    public boolean fillWaterBottle() {
        int filled = CrucibleWaterFillRules.fillFromBottle(water);
        if (filled == water) {
            return false;
        }
        water = filled;
        updateFilledState();
        sync();
        return true;
    }

    public boolean canProcessItems() {
        return heat > BOILING_HEAT && water > 0;
    }

    public int water() {
        return water;
    }

    public short heat() {
        return heat;
    }

    public int essentiaAmount() {
        return essentia.total();
    }

    public Map<String, Integer> essentia() {
        return essentia.view();
    }

    public float fluidHeight() {
        return CrucibleFluidPresentation.height(
                water,
                FLUID_CAPACITY_MB,
                essentia.total(),
                MAX_ESSENTIA
        );
    }

    private int randomAspectColor(Level level) {
        List<String> aspects = new ArrayList<>(essentia.view().keySet());
        if (aspects.isEmpty()) {
            return 0x3F76E4;
        }
        String aspect = aspects.get(level.random.nextInt(aspects.size()));
        return AspectRegistryRuntime.find(aspect)
                .map(AspectDefinition::color)
                .orElse(0xFFFFFF);
    }

    private void updateFilledState() {
        if (level == null) {
            return;
        }
        BlockState state = getBlockState();
        boolean filled = water > 0;
        if (state.hasProperty(CrucibleBlock.FILLED)
                && state.getValue(CrucibleBlock.FILLED) != filled) {
            level.setBlock(
                    worldPosition,
                    state.setValue(CrucibleBlock.FILLED, filled),
                    Block.UPDATE_ALL
            );
        }
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(
                    worldPosition,
                    getBlockState(),
                    getBlockState(),
                    Block.UPDATE_CLIENTS
            );
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putShort("Heat", heat);
        tag.putInt("Water", water);
        tag.put("Aspects", essentia.save());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        heat = tag.getShort("Heat");
        water = Mth.clamp(
                tag.getInt("Water"),
                0,
                FLUID_CAPACITY_MB
        );
        essentia.load(tag.getCompound("Aspects"));
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(
            Connection connection,
            ClientboundBlockEntityDataPacket packet
    ) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            load(tag);
        }
    }
}
