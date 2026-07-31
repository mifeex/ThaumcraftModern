package com.thaumcraftmodern.world.block;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class VishroomBlockTest {
    @Test
    void collisionUsesClassicTenSecondLevelOneNausea() {
        assertEquals(200, VishroomBlock.NAUSEA_DURATION_TICKS);
        assertEquals(0, VishroomBlock.NAUSEA_AMPLIFIER);
    }
}
