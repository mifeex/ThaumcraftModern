package com.thaumcraftmodern.essentia.tube;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

/** Persistent per-player risk accumulated by releasing clogged essentia tubes. */
@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID)
public final class TubeEssentiaReleaseRisk {
    private static final String TAG = ThaumcraftModern.MOD_ID + ":tube_release_risk";

    private TubeEssentiaReleaseRisk() {
    }

    public static int get(Player player) {
        return Math.max(0, Objects.requireNonNull(player, "player")
                .getPersistentData().getInt(TAG));
    }

    public static TubeEssentiaReleaseRules.Release preview(
            Player player,
            TubeEssentiaReleaseRules.Complexity complexity
    ) {
        return TubeEssentiaReleaseRules.accumulate(get(player), complexity);
    }

    public static void commit(
            Player player,
            TubeEssentiaReleaseRules.Release release
    ) {
        Objects.requireNonNull(player, "player").getPersistentData().putInt(
                TAG,
                Objects.requireNonNull(release, "release").createsFlux()
                        ? 0 : release.accumulatedRisk());
    }

    public static void clear(Player player) {
        Objects.requireNonNull(player, "player").getPersistentData().remove(TAG);
    }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone event) {
        int risk = get(event.getOriginal());
        if (risk > 0) event.getEntity().getPersistentData().putInt(TAG, risk);
    }
}
