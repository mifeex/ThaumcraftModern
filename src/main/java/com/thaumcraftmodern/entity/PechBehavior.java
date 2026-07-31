package com.thaumcraftmodern.entity;

import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.crucible.ItemAspectRegistry;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.wand.WandState;
import com.thaumcraftmodern.wand.WandStateCodec;
import com.thaumcraftmodern.wand.WandVisService;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.ItemStackHandler;

import java.util.EnumMap;

/**
 * Exact non-rendering constants and small pure operations from TC4
 * {@code EntityPech}.
 */
public final class PechBehavior {
    public static final int FORAGER = 0;
    public static final int MAGE = 1;
    public static final int STALKER = 2;
    public static final int PACK_SLOTS = 9;
    public static final int ITEM_SEARCH_RANGE = 16;
    public static final int ANGER_HORIZONTAL_RANGE = 32;
    public static final int ANGER_VERTICAL_RANGE = 16;
    public static final int MIN_ANGER_TICKS = 400;
    public static final int ANGER_VARIANCE_TICKS = 400;

    private PechBehavior() {
    }

    public static HeldItemRoll heldItemRoll(int roll) {
        return switch (Math.floorMod(roll, 20)) {
            case 0, 12 -> HeldItemRoll.WAND;
            case 1 -> HeldItemRoll.STONE_SWORD;
            case 3 -> HeldItemRoll.STONE_AXE;
            case 5 -> HeldItemRoll.IRON_SWORD;
            case 6 -> HeldItemRoll.IRON_AXE;
            case 7 -> HeldItemRoll.FISHING_ROD;
            case 8 -> HeldItemRoll.STONE_PICKAXE;
            case 9 -> HeldItemRoll.IRON_PICKAXE;
            case 2, 4, 10, 11, 13 -> HeldItemRoll.BOW;
            default -> HeldItemRoll.EMPTY;
        };
    }

    public static int typeFor(HeldItemRoll roll) {
        return roll == HeldItemRoll.WAND
                ? MAGE
                : roll == HeldItemRoll.BOW ? STALKER : FORAGER;
    }

    public static int value(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        if (stack.is(ModItems.MANA_BEAN.get())) {
            return 1;
        }
        if (stack.is(Items.GOLD_INGOT) || stack.is(Items.GOLDEN_APPLE)) {
            return 2;
        }
        if (stack.is(Items.ENDER_PEARL)) {
            return 3;
        }
        if (stack.is(Items.DIAMOND)) {
            return 4;
        }
        if (stack.is(Items.EMERALD)) {
            return 5;
        }
        return Math.min(
                32,
                ItemAspectRegistry.aspects(stack)
                        .map(aspects -> aspects.getOrDefault("lucrum", 0))
                        .orElse(0)
        );
    }

    public static boolean tames(int value, int tenSidedRoll) {
        return value > 0 && Math.floorMod(tenSidedRoll, 10) < value;
    }

    public static int angerTicks(RandomSource random) {
        return MIN_ANGER_TICKS + random.nextInt(ANGER_VARIANCE_TICKS);
    }

    /**
     * TC4 gives a mage Pech 2..7 terra/perditio/aqua and 0..3 of the other
     * primals. Modern wand storage is centivis, so the classic whole-vis rolls
     * are multiplied by one hundred.
     */
    public static void configureMageWand(
            ItemStack wand,
            RandomSource random
    ) {
        WandState state = WandVisService.state(wand).orElseThrow(() ->
                new IllegalArgumentException("Pech mage item is not a wand")
        );
        EnumMap<PrimalAspect, Integer> vis =
                new EnumMap<>(PrimalAspect.class);
        vis.put(PrimalAspect.TERRA, wholeVis(random, 2, 6));
        vis.put(PrimalAspect.PERDITIO, wholeVis(random, 2, 6));
        vis.put(PrimalAspect.AQUA, wholeVis(random, 2, 6));
        vis.put(PrimalAspect.AER, wholeVis(random, 0, 4));
        vis.put(PrimalAspect.IGNIS, wholeVis(random, 0, 4));
        vis.put(PrimalAspect.ORDO, wholeVis(random, 0, 4));
        WandStateCodec.write(wand, state.withVisCentivis(vis));
        wand.getOrCreateTag().putBoolean("PechFocus", true);
    }

    private static int wholeVis(
            RandomSource random,
            int base,
            int bound
    ) {
        return (base + random.nextInt(bound))
                * WandVisService.CENTIVIS_PER_VIS;
    }

    public static ItemStack insertPack(
            ItemStackHandler pack,
            ItemStack incoming
    ) {
        ItemStack remainder = incoming.copy();
        for (int slot = 0; slot < pack.getSlots() && !remainder.isEmpty();
             slot++) {
            remainder = pack.insertItem(slot, remainder, false);
        }
        return remainder;
    }

    public static boolean canInsertPack(
            ItemStackHandler pack,
            ItemStack incoming
    ) {
        ItemStack remainder = incoming.copy();
        for (int slot = 0; slot < pack.getSlots() && !remainder.isEmpty();
             slot++) {
            remainder = pack.insertItem(slot, remainder, true);
        }
        return remainder.getCount() < incoming.getCount();
    }

    public enum HeldItemRoll {
        EMPTY,
        WAND,
        BOW,
        STONE_SWORD,
        STONE_AXE,
        IRON_SWORD,
        IRON_AXE,
        FISHING_ROD,
        STONE_PICKAXE,
        IRON_PICKAXE
    }
}
