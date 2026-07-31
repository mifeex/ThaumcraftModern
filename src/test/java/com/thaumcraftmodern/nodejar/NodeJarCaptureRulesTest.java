package com.thaumcraftmodern.nodejar;

import com.thaumcraftmodern.aura.AuraNodeFactory;
import com.thaumcraftmodern.aura.AuraNodeModifier;
import com.thaumcraftmodern.aura.AuraNodeState;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NodeJarCaptureRulesTest {
    @Test
    void classicSeventyFivePercentRollDegradesNormalToPale() {
        AuraNodeState normal = AuraNodeFactory.ordinary(UUID.randomUUID());

        AuraNodeState jarred = NodeJarCaptureRules.prepareForJar(
                normal,
                () -> 0.25D
        );

        assertEquals(AuraNodeModifier.PALE, jarred.modifier());
        assertEquals(normal.snapshot().current(), jarred.snapshot().current());
        assertEquals(normal.snapshot().maximum(), jarred.snapshot().maximum());
        assertEquals(normal.nodeId(), jarred.nodeId());
    }

    @Test
    void failedDegradationRollPreservesModifier() {
        AuraNodeState normal = AuraNodeFactory.ordinary(UUID.randomUUID());

        AuraNodeState jarred = NodeJarCaptureRules.prepareForJar(
                normal,
                () -> 0.75D
        );

        assertEquals(AuraNodeModifier.NORMAL, jarred.modifier());
    }

    @Test
    void degradationStepsMatchClassicNullBrightPaleFadingRules() {
        assertEquals(
                AuraNodeModifier.NORMAL,
                NodeJarCaptureRules.degrade(AuraNodeModifier.BRIGHT)
        );
        assertEquals(
                AuraNodeModifier.PALE,
                NodeJarCaptureRules.degrade(AuraNodeModifier.NORMAL)
        );
        assertEquals(
                AuraNodeModifier.FADING,
                NodeJarCaptureRules.degrade(AuraNodeModifier.PALE)
        );
        assertEquals(
                AuraNodeModifier.FADING,
                NodeJarCaptureRules.degrade(AuraNodeModifier.FADING)
        );
    }
}
