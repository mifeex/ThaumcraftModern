package com.thaumcraftmodern.item;

import com.thaumcraftmodern.entity.AlumentumEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class AlumentumItem extends Item {
    public AlumentumItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getBurnTime(
            ItemStack itemStack,
            @Nullable RecipeType<?> recipeType
    ) {
        return 6400;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.ARROW_SHOOT,
                SoundSource.PLAYERS,
                0.3F,
                0.4F / (level.random.nextFloat() * 0.4F + 0.8F)
        );
        if (!level.isClientSide) {
            AlumentumEntity projectile = new AlumentumEntity(player, level);
            projectile.shootFromRotation(
                    player,
                    player.getXRot(),
                    player.getYRot(),
                    0.0F,
                    0.75F,
                    1.0F
            );
            level.addFreshEntity(projectile);
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
