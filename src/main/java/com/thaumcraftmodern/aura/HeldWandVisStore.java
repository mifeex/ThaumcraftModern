package com.thaumcraftmodern.aura;

import com.thaumcraftmodern.wand.WandState;
import com.thaumcraftmodern.wand.WandStateCodec;
import com.thaumcraftmodern.wand.WandVisService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Objects;

/**
 * Adapter from the actually held modern wand stack to node charging.
 */
public final class HeldWandVisStore implements WandVisStore {
    private final ServerPlayer player;
    private final InteractionHand hand;
    private final ItemStack expectedStack;
    private long revision;

    public HeldWandVisStore(ServerPlayer player, InteractionHand hand) {
        this.player = Objects.requireNonNull(player, "player");
        this.hand = Objects.requireNonNull(hand, "hand");
        this.expectedStack = player.getItemInHand(hand);
        if (WandVisService.state(expectedStack).isEmpty()) {
            throw new IllegalArgumentException("held stack is not a valid wand");
        }
    }

    @Override
    public int unitsPerNodeVis() {
        return WandVisService.CENTIVIS_PER_VIS;
    }

    @Override
    public Snapshot snapshot() {
        WandState state = requireCurrentState();
        int capacity = WandVisService.capacityCentivis(expectedStack);
        return new Snapshot(
                state.visCentivis(),
                PrimalVis.uniform(capacity),
                revision
        );
    }

    @Override
    public boolean replaceCurrent(
            long expectedRevision,
            Map<PrimalAspect, Integer> nextCurrent
    ) {
        if (revision != expectedRevision || !stillHeld()) {
            return false;
        }
        WandState current = WandVisService.state(expectedStack).orElse(null);
        if (current == null) {
            return false;
        }
        try {
            WandStateCodec.write(
                    expectedStack,
                    current.withVisCentivis(nextCurrent)
            );
            revision++;
            player.getInventory().setChanged();
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    @Override
    public boolean restore(Snapshot snapshot, long expectedRevision) {
        if (revision != expectedRevision || !stillHeld()) {
            return false;
        }
        WandState current = WandVisService.state(expectedStack).orElse(null);
        if (current == null) {
            return false;
        }
        try {
            WandStateCodec.write(
                    expectedStack,
                    current.withVisCentivis(snapshot.current())
            );
            revision = snapshot.revision();
            player.getInventory().setChanged();
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    @Override
    public Object transactionLock() {
        return expectedStack;
    }

    public boolean stillHeld() {
        return player.getItemInHand(hand) == expectedStack
                && WandVisService.state(expectedStack).isPresent();
    }

    private WandState requireCurrentState() {
        if (!stillHeld()) {
            throw new IllegalStateException("held wand changed during node transfer");
        }
        return WandVisService.state(expectedStack).orElseThrow();
    }
}
