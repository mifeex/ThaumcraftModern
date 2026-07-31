package com.thaumcraftmodern.worldgen;

import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class GreatwoodTreeFeatureTest {
    @Test
    void retainsEveryClassicShapeConstant() {
        assertEquals(0.618D, GreatwoodClassicSettings.HEIGHT_ATTENUATION);
        assertEquals(0.38D, GreatwoodClassicSettings.BRANCH_SLOPE);
        assertEquals(1.2D, GreatwoodClassicSettings.INITIAL_SCALE_WIDTH);
        assertEquals(1.66D, GreatwoodClassicSettings.SECOND_PASS_SCALE_WIDTH);
        assertEquals(0.9D, GreatwoodClassicSettings.LEAF_DENSITY);
        assertEquals(2, GreatwoodClassicSettings.TRUNK_SIZE);
        assertEquals(11, GreatwoodClassicSettings.HEIGHT_LIMIT_BASE);
        assertEquals(4, GreatwoodClassicSettings.LEAF_DISTANCE_LIMIT);
        assertEquals(8, GreatwoodClassicSettings.SAPLING_SPIDER_DENOMINATOR);
        assertEquals(16, GreatwoodClassicSettings.WORLDGEN_SPIDER_DENOMINATOR);
        assertEquals(50, GreatwoodClassicSettings.WEB_ATTEMPTS);
    }

    @Test
    void classicHeightLimitCoversElevenThroughTwentyOne() {
        Set<Integer> sampled = new HashSet<>();
        RandomSource random = RandomSource.create(0x4752454154574F4FL);
        for (int attempt = 0; attempt < 10_000; attempt++) {
            sampled.add(GreatwoodClassicSettings.sampledHeightLimit(random));
        }
        assertEquals(11, sampled.stream().mapToInt(Integer::intValue).min().orElseThrow());
        assertEquals(21, sampled.stream().mapToInt(Integer::intValue).max().orElseThrow());
        assertEquals(11, sampled.size());
    }

    @Test
    void secondPassRaisesTheCanopyAboveTheOldModernLimit() {
        assertEquals(16, GreatwoodClassicSettings.maximumGeneratedY(0, 11));
        assertEquals(32, GreatwoodClassicSettings.maximumGeneratedY(0, 21));
        assertTrue(
                GreatwoodClassicSettings.maximumGeneratedY(0, 21) > 21,
                "The second TC4 pass must extend beyond the old one-pass height"
        );
    }
}
