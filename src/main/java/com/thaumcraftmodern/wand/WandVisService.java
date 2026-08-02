package com.thaumcraftmodern.wand;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.item.WandItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.ToIntFunction;

/**
 * Single authority for reading and mutating wand vis. Public mutation methods
 * require a {@link ServerPlayer}; clients only read the synchronized ItemStack.
 */
public final class WandVisService {
    public static final int CENTIVIS_PER_VIS = 100;

    private WandVisService() {
    }

    public static boolean isWand(ItemStack stack) {
        return stack != null
                && !stack.isEmpty()
                && stack.getItem() instanceof WandItem;
    }

    public static boolean isCraftingTool(ItemStack stack) {
        return isWand(stack)
                && ((WandItem) stack.getItem()).form().isCraftingTool();
    }

    public static Optional<WandState> state(ItemStack stack) {
        if (!isWand(stack)) {
            return Optional.empty();
        }
        Optional<WandComponentCatalog> components =
                WandComponentRegistry.current();
        if (components.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(WandStateCodec.decode(stack, components.get()));
        } catch (RuntimeException exception) {
            ThaumcraftModern.LOGGER.warn(
                    "Rejected invalid wand stack {}: {}",
                    stack,
                    exception.getMessage()
            );
            return Optional.empty();
        }
    }

    public static int capacity(ItemStack stack) {
        int rodCapacity = state(stack)
                .flatMap(value -> WandComponentRegistry.rod(value.rodId()))
                .map(WandRodDefinition::capacityVis)
                .orElse(0);
        if (!(stack.getItem() instanceof WandItem wand)) {
            return 0;
        }
        return wand.form().applyCapacity(rodCapacity);
    }

    public static int capacityCentivis(ItemStack stack) {
        return Math.multiplyExact(capacity(stack), CENTIVIS_PER_VIS);
    }

    public static double vis(ItemStack stack, String primalId) {
        return visCentivis(stack, primalId) / (double) CENTIVIS_PER_VIS;
    }

    public static int visCentivis(ItemStack stack, String primalId) {
        PrimalAspect aspect;
        try {
            aspect = PrimalAspect.fromId(primalId);
        } catch (RuntimeException exception) {
            return 0;
        }
        return state(stack).map(value -> value.visCentivis(aspect)).orElse(0);
    }

    public static Map<String, Integer> adjustedCostCentivis(
            ItemStack stack,
            Map<String, Integer> baseCostWholeVis
    ) {
        return adjustedCostCentivis(
                stack,
                baseCostWholeVis,
                ignored -> inherentDiscountPercent(stack)
        );
    }

    public static Map<String, Integer> adjustedCostCentivis(
            Player player,
            ItemStack stack,
            Map<String, Integer> baseCostWholeVis
    ) {
        Objects.requireNonNull(player, "player");
        return adjustedCostCentivis(
                stack,
                baseCostWholeVis,
                aspect -> Math.addExact(
                        VisDiscountService.totalPercent(player, aspect),
                        inherentDiscountPercent(stack)
                )
        );
    }

    /** Applies normal cap, equipment, effect, and event discounts to fractional costs. */
    public static Map<String, Integer> adjustedFractionalCostCentivis(
            Player player,
            ItemStack stack,
            Map<String, Integer> baseCostCentivis
    ) {
        Objects.requireNonNull(player, "player");
        WandState state = state(stack).orElseThrow(() ->
                new IllegalArgumentException("stack is not a valid wand"));
        WandCapDefinition cap = WandComponentRegistry.cap(state.capId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "unknown wand cap id: " + state.capId()));
        return adjustFractionalCostCentivis(
                cap,
                baseCostCentivis,
                aspect -> Math.addExact(
                        VisDiscountService.totalPercent(player, aspect),
                        inherentDiscountPercent(stack)
                )
        );
    }

    private static Map<String, Integer> adjustedCostCentivis(
            ItemStack stack,
            Map<String, Integer> baseCostWholeVis,
            ToIntFunction<PrimalAspect> discountPercent
    ) {
        WandState state = state(stack).orElseThrow(() ->
                new IllegalArgumentException("stack is not a valid wand")
        );
        WandCapDefinition cap = WandComponentRegistry.cap(state.capId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "unknown wand cap id: " + state.capId()
                ));
        return adjustedCostCentivis(
                cap,
                baseCostWholeVis,
                discountPercent
        );
    }

    static Map<String, Integer> adjustedCostCentivis(
            WandCapDefinition cap,
            Map<String, Integer> baseCostWholeVis
    ) {
        return adjustedCostCentivis(cap, baseCostWholeVis, 0);
    }

    static Map<String, Integer> adjustedCostCentivis(
            WandCapDefinition cap,
            Map<String, Integer> baseCostWholeVis,
            int visDiscountPercent
    ) {
        return adjustedCostCentivis(
                cap,
                baseCostWholeVis,
                ignored -> visDiscountPercent
        );
    }

    private static Map<String, Integer> adjustedCostCentivis(
            WandCapDefinition cap,
            Map<String, Integer> baseCostWholeVis,
            ToIntFunction<PrimalAspect> discountPercent
    ) {
        Objects.requireNonNull(cap, "cap");
        Objects.requireNonNull(discountPercent, "discountPercent");
        Map<PrimalAspect, Integer> base = validateWholeVis(baseCostWholeVis);
        LinkedHashMap<String, Integer> adjusted = new LinkedHashMap<>();
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            int baseCentivis = Math.multiplyExact(
                    base.get(aspect),
                    CENTIVIS_PER_VIS
            );
            adjusted.put(
                    aspect.id(),
                    cap.adjustCentivis(
                            baseCentivis,
                            discountPercent.applyAsInt(aspect),
                            aspect.id()
                    )
            );
        }
        return Map.copyOf(adjusted);
    }

    public static boolean canConsume(
            ItemStack stack,
            Map<String, Integer> baseCostWholeVis
    ) {
        return canConsume(
                stack,
                baseCostWholeVis,
                ignored -> inherentDiscountPercent(stack)
        );
    }

    public static boolean canConsume(
            Player player,
            ItemStack stack,
            Map<String, Integer> baseCostWholeVis
    ) {
        Objects.requireNonNull(player, "player");
        return canConsume(
                stack,
                baseCostWholeVis,
                aspect -> Math.addExact(
                        VisDiscountService.totalPercent(player, aspect),
                        inherentDiscountPercent(stack)
                )
        );
    }

    private static boolean canConsume(
            ItemStack stack,
            Map<String, Integer> baseCostWholeVis,
            ToIntFunction<PrimalAspect> discountPercent
    ) {
        Optional<WandState> current = state(stack);
        if (current.isEmpty()) {
            return false;
        }
        final WandCapDefinition cap;
        try {
            cap = WandComponentRegistry.cap(current.get().capId()).orElseThrow();
            return consumeStateWithDiscounts(
                    current.get(),
                    cap,
                    baseCostWholeVis,
                    discountPercent
            ).isPresent();
        } catch (RuntimeException exception) {
            return false;
        }
    }

    /**
     * Checks every aspect first and writes the reduced state once. A failed
     * payment never partially drains a wand.
     */
    public static boolean consume(
            ServerPlayer player,
            ItemStack stack,
            Map<String, Integer> baseCostWholeVis
    ) {
        requireServer(player);
        Optional<WandState> current = state(stack);
        if (current.isEmpty()) {
            return false;
        }
        final WandCapDefinition cap;
        try {
            cap = WandComponentRegistry.cap(current.get().capId()).orElseThrow();
        } catch (RuntimeException exception) {
            return false;
        }
        final Optional<WandState> next;
        try {
            next = consumeStateWithDiscounts(
                    current.get(),
                    cap,
                    baseCostWholeVis,
                    aspect -> Math.addExact(
                            VisDiscountService.totalPercent(player, aspect),
                            inherentDiscountPercent(stack)
                    )
            );
        } catch (RuntimeException exception) {
            return false;
        }
        if (next.isEmpty()) {
            return false;
        }
        WandStateCodec.write(stack, next.get());
        player.getInventory().setChanged();
        return true;
    }

    /**
     * Fractional counterpart to {@link #consume(ServerPlayer, ItemStack, Map)}.
     * Base values are centivis and still receive every normal vis discount.
     */
    public static boolean consumeCentivis(
            ServerPlayer player,
            ItemStack stack,
            Map<String, Integer> baseCostCentivis
    ) {
        requireServer(player);
        Optional<WandState> current = state(stack);
        if (current.isEmpty()) return false;
        final WandCapDefinition cap;
        try {
            cap = WandComponentRegistry.cap(current.get().capId()).orElseThrow();
        } catch (RuntimeException exception) {
            return false;
        }
        final Optional<WandState> next;
        try {
            Map<String, Integer> costs = adjustFractionalCostCentivis(
                    cap,
                    baseCostCentivis,
                    aspect -> Math.addExact(
                            VisDiscountService.totalPercent(player, aspect),
                            inherentDiscountPercent(stack)
                    )
            );
            next = consumeAdjustedCentivis(current.get(), costs);
        } catch (RuntimeException exception) {
            return false;
        }
        if (next.isEmpty()) return false;
        WandStateCodec.write(stack, next.get());
        player.getInventory().setChanged();
        return true;
    }

    public static int add(
            ServerPlayer player,
            ItemStack stack,
            String primalId,
            int wholeVis
    ) {
        return addCentivis(
                player,
                stack,
                primalId,
                Math.multiplyExact(wholeVis, CENTIVIS_PER_VIS)
        );
    }

    /**
     * Adds at most the available capacity and returns the amount actually
     * accepted, in centivis.
     */
    public static int addCentivis(
            ServerPlayer player,
            ItemStack stack,
            String primalId,
            int centivis
    ) {
        requireServer(player);
        if (centivis < 0) {
            throw new IllegalArgumentException("added centivis cannot be negative");
        }
        PrimalAspect aspect = PrimalAspect.fromId(primalId);
        Optional<WandState> current = state(stack);
        if (current.isEmpty() || centivis == 0) {
            return 0;
        }
        int room = capacityCentivis(stack)
                - current.get().visCentivis(aspect);
        int accepted = Math.min(room, centivis);
        if (accepted <= 0) {
            return 0;
        }
        WandState next = current.get().withVisCentivis(
                aspect,
                current.get().visCentivis(aspect) + accepted
        );
        WandStateCodec.write(stack, next);
        player.getInventory().setChanged();
        return accepted;
    }

    static Optional<WandState> consumeState(
            WandState state,
            WandCapDefinition cap,
            Map<String, Integer> baseCostWholeVis
    ) {
        return consumeState(state, cap, baseCostWholeVis, 0);
    }

    static Optional<WandState> consumeState(
            WandState state,
            WandCapDefinition cap,
            Map<String, Integer> baseCostWholeVis,
            int visDiscountPercent
    ) {
        return consumeStateWithDiscounts(
                state,
                cap,
                baseCostWholeVis,
                ignored -> visDiscountPercent
        );
    }

    private static Optional<WandState> consumeStateWithDiscounts(
            WandState state,
            WandCapDefinition cap,
            Map<String, Integer> baseCostWholeVis,
            ToIntFunction<PrimalAspect> discountPercent
    ) {
        Objects.requireNonNull(state, "state");
        Map<String, Integer> costs =
                adjustedCostCentivis(
                        cap,
                        baseCostWholeVis,
                        discountPercent
                );
        EnumMap<PrimalAspect, Integer> next =
                new EnumMap<>(PrimalAspect.class);
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            int remaining = state.visCentivis(aspect)
                    - costs.get(aspect.id());
            if (remaining < 0) {
                return Optional.empty();
            }
            next.put(aspect, remaining);
        }
        return Optional.of(state.withVisCentivis(next));
    }

    static Map<String, Integer> adjustFractionalCostCentivis(
            WandCapDefinition cap,
            Map<String, Integer> baseCostCentivis,
            int visDiscountPercent
    ) {
        return adjustFractionalCostCentivis(
                cap, baseCostCentivis, ignored -> visDiscountPercent);
    }

    private static Map<String, Integer> adjustFractionalCostCentivis(
            WandCapDefinition cap,
            Map<String, Integer> baseCostCentivis,
            ToIntFunction<PrimalAspect> discountPercent
    ) {
        Objects.requireNonNull(cap, "cap");
        Objects.requireNonNull(discountPercent, "discountPercent");
        Map<PrimalAspect, Integer> base = validateCentivis(baseCostCentivis);
        LinkedHashMap<String, Integer> adjusted = new LinkedHashMap<>();
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            adjusted.put(aspect.id(), cap.adjustCentivis(
                    base.get(aspect),
                    discountPercent.applyAsInt(aspect),
                    aspect.id()));
        }
        return Map.copyOf(adjusted);
    }

    private static Optional<WandState> consumeAdjustedCentivis(
            WandState state,
            Map<String, Integer> costs
    ) {
        EnumMap<PrimalAspect, Integer> next = new EnumMap<>(PrimalAspect.class);
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            int remaining = state.visCentivis(aspect)
                    - costs.getOrDefault(aspect.id(), 0);
            if (remaining < 0) return Optional.empty();
            next.put(aspect, remaining);
        }
        return Optional.of(state.withVisCentivis(next));
    }

    private static Map<PrimalAspect, Integer> validateWholeVis(
            Map<String, Integer> values
    ) {
        Objects.requireNonNull(values, "baseCostWholeVis");
        EnumMap<PrimalAspect, Integer> result =
                new EnumMap<>(PrimalAspect.class);
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            Integer amount = values.getOrDefault(aspect.id(), 0);
            if (amount == null || amount < 0) {
                throw new IllegalArgumentException(
                        "vis cost cannot be negative or null: " + aspect.id()
                );
            }
            result.put(aspect, amount);
        }
        for (String key : values.keySet()) {
            PrimalAspect.fromId(key);
        }
        return result;
    }

    private static Map<PrimalAspect, Integer> validateCentivis(
            Map<String, Integer> values
    ) {
        Objects.requireNonNull(values, "baseCostCentivis");
        EnumMap<PrimalAspect, Integer> result = new EnumMap<>(PrimalAspect.class);
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            Integer amount = values.getOrDefault(aspect.id(), 0);
            if (amount == null || amount < 0) {
                throw new IllegalArgumentException(
                        "vis cost cannot be negative or null: " + aspect.id());
            }
            result.put(aspect, amount);
        }
        for (String key : values.keySet()) PrimalAspect.fromId(key);
        return result;
    }

    private static void requireServer(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        if (player.level().isClientSide) {
            throw new IllegalArgumentException(
                    "wand vis can only be mutated by the logical server"
            );
        }
    }

    private static int inherentDiscountPercent(ItemStack stack) {
        return stack.getItem() instanceof WandItem wand
                ? wand.form().inherentDiscountPercent()
                : 0;
    }
}
