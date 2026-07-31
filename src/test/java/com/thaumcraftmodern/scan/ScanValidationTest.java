package com.thaumcraftmodern.scan;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.thaumcraftmodern.scan.ScanValidation.Reason;
import com.thaumcraftmodern.scan.ScanValidation.Request;

class ScanValidationTest {
    private static final String OVERWORLD = "minecraft:overworld";

    @Test
    void validRequestPassesAtExactDistanceAndDurationBoundaries() {
        assertEquals(
                Reason.VALID,
                ScanValidation.validate(validRequest(8.0D, 8.0D, 20, 20)));
    }

    @Test
    void rejectsMissingThaumometer() {
        Request request = new Request(
                false, OVERWORLD, OVERWORLD, true,
                4.0D, 8.0D, true, true, 20, 20);

        assertEquals(Reason.THAUMOMETER_NOT_HELD, ScanValidation.validate(request));
    }

    @Test
    void rejectsDimensionChunkDistanceAndLineOfSightFailures() {
        assertEquals(
                Reason.DIMENSION_MISMATCH,
                ScanValidation.validate(new Request(
                        true, OVERWORLD, "minecraft:the_nether", true,
                        4.0D, 8.0D, true, true, 20, 20)));
        assertEquals(
                Reason.TARGET_CHUNK_UNLOADED,
                ScanValidation.validate(new Request(
                        true, OVERWORLD, OVERWORLD, false,
                        4.0D, 8.0D, true, true, 20, 20)));
        assertEquals(
                Reason.TARGET_OUT_OF_RANGE,
                ScanValidation.validate(validRequest(8.01D, 8.0D, 20, 20)));
        assertEquals(
                Reason.LINE_OF_SIGHT_BLOCKED,
                ScanValidation.validate(new Request(
                        true, OVERWORLD, OVERWORLD, true,
                        4.0D, 8.0D, false, true, 20, 20)));
    }

    @Test
    void waitsForFullDurationBeforeReportingUnregisteredTarget() {
        assertEquals(
                Reason.TARGET_NOT_REGISTERED,
                ScanValidation.validate(new Request(
                        true, OVERWORLD, OVERWORLD, true,
                        4.0D, 8.0D, true, false, 20, 20)));
        assertEquals(
                Reason.STABLE_DURATION_NOT_REACHED,
                ScanValidation.validate(new Request(
                        true, OVERWORLD, OVERWORLD, true,
                        4.0D, 8.0D, true, false, 19, 20)));
        assertEquals(
                Reason.STABLE_DURATION_NOT_REACHED,
                ScanValidation.validate(validRequest(4.0D, 8.0D, 19, 20)));
    }

    private static Request validRequest(
            double distance,
            double maximumDistance,
            int stableTicks,
            int requiredTicks) {
        return new Request(
                true,
                OVERWORLD,
                OVERWORLD,
                true,
                distance,
                maximumDistance,
                true,
                true,
                stableTicks,
                requiredTicks);
    }
}
