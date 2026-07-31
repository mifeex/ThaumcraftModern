package com.thaumcraftmodern.scan;

import com.thaumcraftmodern.knowledge.PlayerThaumKnowledge;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScanAspectGrantServiceTest {
    @Test
    void firstDiscoveryAddsThreeToTargetAspectAmount() {
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();

        ScanAspectGrantService.Grant grant = ScanAspectGrantService.apply(
                knowledge,
                List.of(new AspectReward("lux", 2))
        ).get(0);

        assertTrue(grant.newlyDiscovered());
        assertEquals(5, grant.amount());
        assertEquals(5, grant.total());
        assertEquals(5, knowledge.aspectAmount("lux"));
    }

    @Test
    void knownAspectReceivesOnlyTargetAmount() {
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();
        knowledge.learnAspect("lux");
        knowledge.addAspectPoints("lux", 4);

        ScanAspectGrantService.Grant grant = ScanAspectGrantService.apply(
                knowledge,
                List.of(new AspectReward("lux", 2))
        ).get(0);

        assertFalse(grant.newlyDiscovered());
        assertEquals(2, grant.amount());
        assertEquals(6, grant.total());
    }

    @Test
    void discoveryBonusIsAppliedIndependentlyPerNewAspect() {
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();
        knowledge.learnAspect("motus");

        List<ScanAspectGrantService.Grant> grants =
                ScanAspectGrantService.apply(
                        knowledge,
                        List.of(
                                new AspectReward("lux", 1),
                                new AspectReward("motus", 2),
                                new AspectReward("potentia", 4)
                        )
                );

        assertEquals(List.of(4, 2, 7), grants.stream()
                .map(ScanAspectGrantService.Grant::amount)
                .toList());
        assertEquals(List.of(true, false, true), grants.stream()
                .map(ScanAspectGrantService.Grant::newlyDiscovered)
                .toList());
    }

    @Test
    void duplicateEntriesReceiveOnlyOneDiscoveryBonus() {
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();

        List<ScanAspectGrantService.Grant> grants =
                ScanAspectGrantService.apply(
                        knowledge,
                        List.of(
                                new AspectReward("lux", 1),
                                new AspectReward("lux", 2)
                        )
                );

        assertEquals(1, grants.size());
        assertEquals(6, grants.get(0).amount());
        assertEquals(6, knowledge.aspectAmount("lux"));
    }
}
