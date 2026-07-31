package com.thaumcraftmodern.essentia;

import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * Side-aware, server-owned essentia transport contract.
 *
 * <p>The method split mirrors TC4 {@code IEssentiaTransport}. Network code
 * only depends on this interface; concrete tube types provide isolated
 * policies and are never discovered through an {@code instanceof} chain.</p>
 */
public interface EssentiaTransport {
    boolean isConnectable(Direction side);

    boolean canInputFrom(Direction side);

    boolean canOutputTo(Direction side);

    void setSuction(@Nullable String aspect, int amount);

    @Nullable String suctionType(Direction side);

    int suctionAmount(Direction side);

    @Nullable String essentiaType(Direction side);

    int essentiaAmount(Direction side);

    int minimumSuction();

    int takeEssentia(String aspect, int amount, Direction side);

    int addEssentia(String aspect, int amount, Direction side);

    /**
     * Whether network routing may return essentia into this transport.
     * Source-only devices such as the Arcane Alembic disable this in data.
     */
    default boolean canReturnEssentia() {
        return true;
    }

    default boolean renderExtendedTube() {
        return false;
    }
}
