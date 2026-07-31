package com.thaumcraftmodern.api.wand;

import com.thaumcraftmodern.aura.PrimalAspect;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

import java.util.Objects;

/**
 * Extensibility point for gameplay systems that alter vis cost.
 *
 * <p>The event is posted once per primal aspect whenever cost is previewed,
 * validated, or consumed. Warp phenomena and integrations can add a signed
 * percentage contribution without creating a second vis-payment path.</p>
 */
public final class VisDiscountEvent extends Event {
    private final Player player;
    private final PrimalAspect aspect;
    private int discountPercent;

    public VisDiscountEvent(
            Player player,
            PrimalAspect aspect,
            int discountPercent
    ) {
        this.player = Objects.requireNonNull(player, "player");
        this.aspect = Objects.requireNonNull(aspect, "aspect");
        this.discountPercent = discountPercent;
    }

    public Player player() {
        return player;
    }

    public PrimalAspect aspect() {
        return aspect;
    }

    public int discountPercent() {
        return discountPercent;
    }

    public void addPercent(int signedPercent) {
        discountPercent = Math.addExact(discountPercent, signedPercent);
    }
}
