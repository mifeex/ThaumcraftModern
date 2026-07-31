package com.thaumcraftmodern.item;

import com.thaumcraftmodern.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Replants a mana bean as a young pod below valid wood in magical biomes.
 */
public final class ManaBeanItem extends Item {
    private static final TagKey<Biome> MAGICAL_BIOMES = TagKey.create(
            Registries.BIOME,
            new ResourceLocation("forge", "is_magical")
    );

    public ManaBeanItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getClickedFace() != Direction.DOWN) {
            return InteractionResult.PASS;
        }
        var level = context.getLevel();
        BlockPos position = context.getClickedPos().below();
        if (!level.isEmptyBlock(position)
                || !level.getBiome(position).is(MAGICAL_BIOMES)) {
            return InteractionResult.PASS;
        }
        BlockState pod = ModBlocks.MANA_POD.get().defaultBlockState();
        if (!pod.canSurvive(level, position)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            level.setBlock(position, pod, 3);
            if (context.getPlayer() == null
                    || !context.getPlayer().getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
