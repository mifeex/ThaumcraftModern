package com.thaumcraftmodern.item;

import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.world.block.entity.ArcaneAlembicBlockEntity;
import com.thaumcraftmodern.world.block.entity.EssentiaJarBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** TC4 essentia phial: empty, or carrying exactly eight points of one aspect. */
public final class EssentiaPhialItem extends Item {
    public static final int PHIAL_AMOUNT = 8;
    private static final String ASPECT_KEY = "Aspect";
    private static final String AMOUNT_KEY = "Amount";

    public EssentiaPhialItem(Properties properties) {
        super(properties);
    }

    public static ItemStack filled(Item item, String aspect) {
        if (!(item instanceof EssentiaPhialItem) || !validAspectId(aspect)) {
            throw new IllegalArgumentException("invalid essentia phial aspect");
        }
        ItemStack stack = new ItemStack(item);
        stack.getOrCreateTag().putString(ASPECT_KEY, aspect);
        stack.getOrCreateTag().putInt(AMOUNT_KEY, PHIAL_AMOUNT);
        return stack;
    }

    public static Optional<String> aspect(ItemStack stack) {
        if (!(stack.getItem() instanceof EssentiaPhialItem) || stack.getTag() == null
                || stack.getTag().getInt(AMOUNT_KEY) != PHIAL_AMOUNT) {
            return Optional.empty();
        }
        String aspect = stack.getTag().getString(ASPECT_KEY);
        return validAspectId(aspect) ? Optional.of(aspect) : Optional.empty();
    }

    public static boolean isEmpty(ItemStack stack) {
        return stack.getItem() instanceof EssentiaPhialItem
                && (stack.getTag() == null
                || !stack.getTag().contains(ASPECT_KEY, CompoundTag.TAG_STRING));
    }

    public static int color(ItemStack stack) {
        return aspect(stack).flatMap(AspectRegistryRuntime::find)
                .map(definition -> definition.color()).orElse(0xFFFFFF);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack held = context.getItemInHand();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        if (isEmpty(held)) {
            String storedAspect = null;
            int storedAmount = 0;
            if (level.getBlockEntity(pos) instanceof EssentiaJarBlockEntity jar) {
                storedAspect = jar.aspect();
                storedAmount = jar.amount();
            } else if (level.getBlockEntity(pos) instanceof ArcaneAlembicBlockEntity alembic) {
                storedAspect = alembic.storedAspect();
                storedAmount = alembic.storedAmount();
            }
            if (storedAspect == null || storedAmount < PHIAL_AMOUNT) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide) {
                boolean extracted = level.getBlockEntity(pos) instanceof EssentiaJarBlockEntity jar
                        ? jar.takeEssentia(storedAspect, PHIAL_AMOUNT, Direction.UP)
                                == PHIAL_AMOUNT
                        : level.getBlockEntity(pos) instanceof ArcaneAlembicBlockEntity alembic
                                && alembic.takeFromContainer(storedAspect, PHIAL_AMOUNT);
                if (!extracted) return InteractionResult.PASS;
                exchangeOne(held, player, level, pos,
                        filled(ModItems.ESSENTIA_PHIAL.get(), storedAspect));
                playTransferSound(level, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        String aspect = aspect(held).orElse(null);
        if (aspect == null
                || !(level.getBlockEntity(pos) instanceof EssentiaJarBlockEntity jar)
                || jar.amount() > EssentiaJarBlockEntity.CAPACITY - PHIAL_AMOUNT) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            if (jar.addEssentia(aspect, PHIAL_AMOUNT, Direction.UP) != PHIAL_AMOUNT) {
                return InteractionResult.PASS;
            }
            exchangeOne(held, player, level, pos,
                    new ItemStack(ModItems.ESSENTIA_PHIAL.get()));
            playTransferSound(level, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void exchangeOne(ItemStack held, Player player, Level level,
            BlockPos pos, ItemStack result) {
        held.shrink(1);
        if (!player.getInventory().add(result)) {
            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5D,
                    pos.getY() + 0.5D, pos.getZ() + 0.5D, result));
        }
        player.getInventory().setChanged();
    }

    private static void playTransferSound(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.PLAYER_SWIM,
                SoundSource.PLAYERS, 0.25F, 1.0F);
    }

    private static boolean validAspectId(String aspect) {
        return aspect != null && !aspect.isBlank() && aspect.equals(aspect.trim())
                && aspect.equals(aspect.toLowerCase(Locale.ROOT));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
            List<Component> tooltip, TooltipFlag flag) {
        aspect(stack).ifPresent(aspect -> tooltip.add(Component.translatable(
                "tc.aspect." + aspect).append(" x " + PHIAL_AMOUNT)
                .withStyle(ChatFormatting.DARK_AQUA)));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
