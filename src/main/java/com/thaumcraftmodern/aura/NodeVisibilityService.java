package com.thaumcraftmodern.aura;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Decides node disclosure from actual player equipment state.
 *
 * <p>No client-provided reveal flag is accepted. Holding a Thaumometer reveals
 * nodes immediately, before any scan session begins. Goggles are checked only
 * in the vanilla head slot.</p>
 */
public final class NodeVisibilityService {
    private NodeVisibilityService() {
    }

    public static Visibility decide(Facts facts) {
        Objects.requireNonNull(facts, "facts");
        if (facts.gogglesInHeadSlot()) {
            return Visibility.REVEALED_BY_GOGGLES;
        }
        if (facts.heldThaumometer()) {
            return Visibility.REVEALED_BY_THAUMOMETER;
        }
        return Visibility.SUBTLE;
    }

    public static Visibility decideFromPlayer(
            Player player,
            Predicate<ItemStack> thaumometerMatcher,
            Predicate<ItemStack> gogglesMatcher
    ) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(thaumometerMatcher, "thaumometerMatcher");
        Objects.requireNonNull(gogglesMatcher, "gogglesMatcher");

        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        boolean goggles = !head.isEmpty() && gogglesMatcher.test(head);

        boolean thaumometer = thaumometerMatcher.test(player.getMainHandItem())
                || thaumometerMatcher.test(player.getOffhandItem());
        return decide(new Facts(thaumometer, goggles));
    }

    public enum Visibility {
        /**
         * Keep the server hit/scan target, but render only a dim classic
         * additive hint until revealing equipment is present.
         */
        SUBTLE,
        REVEALED_BY_THAUMOMETER,
        REVEALED_BY_GOGGLES;

        public boolean revealed() {
            return this != SUBTLE;
        }
    }

    public record Facts(boolean heldThaumometer, boolean gogglesInHeadSlot) {
    }
}
