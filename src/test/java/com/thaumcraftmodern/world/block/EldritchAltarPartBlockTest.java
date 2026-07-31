package com.thaumcraftmodern.world.block;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EldritchAltarPartBlockTest {
    @Test
    void altarCollapseExplosionIsTwentyPercentStrongerThanTnt() {
        assertEquals(4.0F, EldritchAltarPartBlock.TNT_EXPLOSION_POWER);
        assertEquals(
                4.8F,
                EldritchAltarPartBlock.ALTAR_COLLAPSE_EXPLOSION_POWER,
                0.000_001F
        );
        assertEquals(
                EldritchAltarPartBlock.TNT_EXPLOSION_POWER * 1.20F,
                EldritchAltarPartBlock.ALTAR_COLLAPSE_EXPLOSION_POWER,
                0.000_001F
        );
    }
}
