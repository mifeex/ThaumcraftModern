package com.thaumcraftmodern.crucible;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CrucibleWaterFillRulesTest {
    @Test
    void bucketFillsAnyNonFullCrucibleToCapacity() {
        assertEquals(1000, CrucibleWaterFillRules.fillFromBucket(0));
        assertEquals(1000, CrucibleWaterFillRules.fillFromBucket(50));
        assertEquals(1000, CrucibleWaterFillRules.fillFromBucket(999));
        assertEquals(1000, CrucibleWaterFillRules.fillFromBucket(1000));
    }

    @Test
    void threeWaterBottlesFillAnEmptyCrucibleLikeACauldron() {
        int water = CrucibleWaterFillRules.fillFromBottle(0);
        assertEquals(334, water);
        water = CrucibleWaterFillRules.fillFromBottle(water);
        assertEquals(668, water);
        water = CrucibleWaterFillRules.fillFromBottle(water);
        assertEquals(1000, water);
    }

    @Test
    void bottleAndBucketNeverOverfillTheCrucible() {
        assertEquals(1000, CrucibleWaterFillRules.fillFromBottle(900));
        assertEquals(1000, CrucibleWaterFillRules.fillFromBottle(1000));
        assertEquals(1000, CrucibleWaterFillRules.fillFromBucket(1000));
    }
}
