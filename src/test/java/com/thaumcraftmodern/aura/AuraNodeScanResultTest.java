package com.thaumcraftmodern.aura;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class AuraNodeScanResultTest {
    @Test
    void exposesPinnedNodeParametersAndRoundedTenPercentScanRewards() {
        EnumMap<PrimalAspect, Integer> current = values(0);
        current.put(PrimalAspect.AER, 10);
        current.put(PrimalAspect.TERRA, 11);
        current.put(PrimalAspect.IGNIS, 114);
        current.put(PrimalAspect.AQUA, 115);
        current.put(PrimalAspect.ORDO, 113);
        EnumMap<PrimalAspect, Integer> maximum = values(120);
        UUID nodeId = UUID.fromString("49a90d97-6d5b-44e0-94ef-9f723589d293");
        AuraNodeState state = new AuraNodeState(
                nodeId,
                AuraNodeType.NORMAL,
                AuraNodeModifier.NORMAL,
                current,
                maximum,
                7L
        );

        AuraNodeScanResult result = AuraNodeScanResult.from(state.snapshot());

        assertEquals(nodeId, result.nodeId());
        assertEquals(AuraNodeType.NORMAL, result.type());
        assertEquals(AuraNodeModifier.NORMAL, result.modifier());
        assertEquals(current, result.current());
        assertEquals(maximum, result.maximum());
        assertEquals(7L, result.revision());
        assertEquals(
                Map.of("aer", 10, "terra", 1, "ignis", 11, "aqua", 12, "ordo", 11),
                result.rewards().stream().collect(java.util.stream.Collectors.toMap(
                        reward -> reward.aspectId(),
                        reward -> reward.amount()
                ))
        );
    }

    @Test
    void scanRewardUsesFullSmallPoolsAndMathematicalRoundingAboveTen() {
        assertEquals(1, AuraNodeScanResult.researchReward(1));
        assertEquals(10, AuraNodeScanResult.researchReward(10));
        assertEquals(1, AuraNodeScanResult.researchReward(11));
        assertEquals(1, AuraNodeScanResult.researchReward(14));
        assertEquals(2, AuraNodeScanResult.researchReward(15));
        assertEquals(11, AuraNodeScanResult.researchReward(113));
    }

    @Test
    void pinnedScanIdentityRejectsAReplacementNode() {
        UUID originalId = UUID.fromString("670f977f-784a-4035-93a9-3281168c9fc5");
        AuraNodeScanIdentity identity = new AuraNodeScanIdentity(originalId);

        assertTrue(identity.stillMatches(AuraNodeFactory.ordinary(originalId)));
        assertEquals(
                AuraNodeScanIdentity.SCAN_KEY + "/" + originalId,
                identity.scanKey()
        );
        assertFalse(identity.stillMatches(AuraNodeFactory.ordinary(
                UUID.fromString("bd2a15b6-5a6f-42ff-9cdf-a448609bf2e7")
        )));
    }

    private static EnumMap<PrimalAspect, Integer> values(int amount) {
        EnumMap<PrimalAspect, Integer> result = new EnumMap<>(PrimalAspect.class);
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            result.put(aspect, amount);
        }
        return result;
    }
}
