package com.thaumcraftmodern.nodejar;

import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.wand.WandState;
import com.thaumcraftmodern.wand.WandVisService;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Actual-held casting-tool adapter for NODEJAR payment. The vis debit is made
 * as a reservation and its full NBT snapshot is restored if world capture
 * fails.
 */
public final class HeldCastingToolPayment
        implements NodeJarCaptureService.CastingToolPayment {
    private final ServerPlayer player;
    private final InteractionHand hand;
    private final ItemStack expectedStack;
    private boolean reservationOpen;

    public HeldCastingToolPayment(
            ServerPlayer player,
            InteractionHand hand
    ) {
        this.player = Objects.requireNonNull(player, "player");
        this.hand = Objects.requireNonNull(hand, "hand");
        this.expectedStack = player.getItemInHand(hand);
    }

    @Override
    public boolean isStillHeldCastingTool() {
        if (player.getItemInHand(hand) != expectedStack) {
            return false;
        }
        WandState state = WandVisService.state(expectedStack).orElse(null);
        return state != null;
    }

    @Override
    public Optional<NodeJarCaptureService.PaymentReservation> reserve(
            Map<PrimalAspect, Integer> baseCost
    ) {
        Objects.requireNonNull(baseCost, "baseCost");
        if (reservationOpen || !isStillHeldCastingTool()) {
            return Optional.empty();
        }
        LinkedHashMap<String, Integer> costById = new LinkedHashMap<>();
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            costById.put(aspect.id(), baseCost.get(aspect));
        }
        if (!WandVisService.canConsume(player, expectedStack, costById)) {
            return Optional.empty();
        }

        CompoundTag previousTag = expectedStack.getTag() == null
                ? null
                : expectedStack.getTag().copy();
        if (!WandVisService.consume(player, expectedStack, costById)) {
            return Optional.empty();
        }
        reservationOpen = true;
        return Optional.of(new NodeJarCaptureService.PaymentReservation() {
            private boolean closed;

            @Override
            public void commit() {
                if (!closed) {
                    closed = true;
                    reservationOpen = false;
                }
            }

            @Override
            public void rollback() {
                if (!closed) {
                    expectedStack.setTag(
                            previousTag == null ? null : previousTag.copy()
                    );
                    player.getInventory().setChanged();
                    closed = true;
                    reservationOpen = false;
                }
            }
        });
    }
}
