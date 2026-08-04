package com.thaumcraftmodern.client.particle;

import com.thaumcraftmodern.world.block.entity.ArcanePedestalBlockEntity;
import com.thaumcraftmodern.world.block.entity.RunicMatrixBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.WeakHashMap;

/** Client equivalent of TC4 TileInfusionMatrix.doEffects(). */
public final class InfusionMatrixVisualEffects {
    private static final Map<RunicMatrixBlockEntity, Long> LAST_TICK =
            new WeakHashMap<>();

    private InfusionMatrixVisualEffects() {
    }

    public static void tick(RunicMatrixBlockEntity matrix) {
        if (!(matrix.getLevel() instanceof ClientLevel level)) return;
        long gameTime = level.getGameTime();
        Long previousTick = LAST_TICK.put(matrix, gameTime);
        if (previousTick != null && previousTick == gameTime) return;
        if (matrix.crafting()) spawnRune(level, matrix.getBlockPos());
        BlockPos source = matrix.effectSource();
        if (source == null || matrix.effectUntil() < gameTime) return;
        switch (matrix.effectType()) {
            case ESSENTIA -> spawnEssentia(level, matrix, source, gameTime);
            case COMPONENT -> spawnComponent(level, matrix.getBlockPos(), source);
            default -> {
            }
        }
    }

    private static void spawnRune(ClientLevel level, BlockPos matrix) {
        Minecraft.getInstance().particleEngine.add(new InfusionRuneParticle(level,
                matrix.getX() + 0.5D, matrix.getY() - 1.5D,
                matrix.getZ() + 0.5D));
    }

    private static void spawnEssentia(ClientLevel level,
            RunicMatrixBlockEntity matrix, BlockPos source, long gameTime) {
        int remaining = (int) Math.max(0L, matrix.effectUntil() - gameTime);
        float scale = remaining > 5 ? 1.0F : remaining * remaining / 25.0F;
        Minecraft.getInstance().particleEngine.add(InfusionArcParticle.essentia(
                level,
                source.getX() + 0.5D,
                source.getY() + 0.5D,
                source.getZ() + 0.5D,
                matrix.getBlockPos().getX() + 0.5D,
                matrix.getBlockPos().getY() + 0.5D,
                matrix.getBlockPos().getZ() + 0.5D,
                matrix.clientCraftTicks() - Math.max(0, 5 - remaining),
                matrix.effectColor(), scale));
    }

    private static void spawnComponent(ClientLevel level, BlockPos matrix,
            BlockPos source) {
        if (!(level.getBlockEntity(source) instanceof ArcanePedestalBlockEntity pedestal)) {
            return;
        }
        ItemStack stack = pedestal.item();
        if (stack.isEmpty()) return;
        RandomSource random = level.random;
        double targetX = matrix.getX() + 0.5D;
        double targetY = matrix.getY() - 0.5D;
        double targetZ = matrix.getZ() + 0.5D;
        Minecraft minecraft = Minecraft.getInstance();
        if (random.nextInt(3) == 0) {
            minecraft.particleEngine.add(InfusionArcParticle.boreSparkle(level,
                    source.getX() + random.nextFloat(),
                    source.getY() + 1.0D + random.nextFloat(),
                    source.getZ() + random.nextFloat(),
                    targetX, targetY, targetZ));
            return;
        }
        int amount = Minecraft.useFancyGraphics() ? 2 : 1;
        for (int index = 0; index < amount; index++) {
            if (stack.getItem() instanceof BlockItem blockItem) {
                BlockState state = blockItem.getBlock().defaultBlockState();
                minecraft.particleEngine.add(InfusionBoreParticle.forBlock(level,
                        source.getX() + random.nextFloat(),
                        source.getY() + 1.0D + random.nextFloat(),
                        source.getZ() + random.nextFloat(),
                        targetX, targetY, targetZ, state, source));
            } else {
                minecraft.particleEngine.add(InfusionBoreParticle.forItem(level,
                        source.getX() + 0.4D + random.nextFloat() * 0.2D,
                        source.getY() + 1.23D + random.nextFloat() * 0.2D,
                        source.getZ() + 0.4D + random.nextFloat() * 0.2D,
                        targetX, targetY, targetZ, stack));
            }
        }
    }
}
