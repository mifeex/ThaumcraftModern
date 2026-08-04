package com.thaumcraftmodern.entity;

import net.minecraft.world.Difficulty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class WispSpawnPolicyTest {
    @Test
    void tc4WispsIgnoreLightButRemainDisabledInPeaceful() {
        assertFalse(WispSpawnPolicy.allows(Difficulty.PEACEFUL));
        assertTrue(WispSpawnPolicy.allows(Difficulty.EASY));
        assertTrue(WispSpawnPolicy.allows(Difficulty.NORMAL));
        assertTrue(WispSpawnPolicy.allows(Difficulty.HARD));
    }
}
