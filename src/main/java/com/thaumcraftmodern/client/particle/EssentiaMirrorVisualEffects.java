package com.thaumcraftmodern.client.particle;

import com.thaumcraftmodern.world.block.entity.EssentiaMirrorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;

import java.util.Map;
import java.util.WeakHashMap;

/** Client half of TC4's second PacketFXEssentiaSource at the remote mirror. */
public final class EssentiaMirrorVisualEffects {
    private static final Map<EssentiaMirrorBlockEntity, Long> LAST_TICK =
            new WeakHashMap<>();

    private EssentiaMirrorVisualEffects() {
    }

    public static void tick(EssentiaMirrorBlockEntity mirror) {
        if (!(mirror.getLevel() instanceof ClientLevel level)) {
            return;
        }
        long gameTime = level.getGameTime();
        Long previous = LAST_TICK.put(mirror, gameTime);
        if (previous != null && previous == gameTime) {
            return;
        }
        BlockPos source = mirror.effectSource();
        int remaining = (int) Math.max(
                0L,
                mirror.effectUntil() - gameTime
        );
        if (source == null || remaining <= 0) {
            return;
        }
        float scale = remaining > 5
                ? 1.0F : remaining * remaining / 25.0F;
        BlockPos target = mirror.getBlockPos();
        Minecraft.getInstance().particleEngine.add(
                InfusionArcParticle.essentia(
                        level,
                        source.getX() + 0.5D,
                        source.getY() + 0.5D,
                        source.getZ() + 0.5D,
                        target.getX() + 0.5D,
                        target.getY() + 0.5D,
                        target.getZ() + 0.5D,
                        (int) gameTime - Math.max(0, 5 - remaining),
                        mirror.effectColor(),
                        scale
                )
        );
    }
}
