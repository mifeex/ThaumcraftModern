package com.thaumcraftmodern.research;

import com.thaumcraftmodern.knowledge.PlayerThaumKnowledge;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResearchExpertiseServiceTest {
    @Test
    void expertiseRefundsEraseBelowExactTwentyFivePercentBoundary() {
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();
        knowledge.completeResearch(
                ResearchExpertiseService.EXPERTISE_RESEARCH_ID
        );

        assertTrue(
                ResearchExpertiseService.refundsErasedAspect(
                        knowledge,
                        0.2499F
                )
        );
        assertFalse(
                ResearchExpertiseService.refundsErasedAspect(
                        knowledge,
                        0.25F
                )
        );
        assertTrue(ResearchExpertiseService.canInspectComponents(knowledge));
        assertFalse(
                ResearchExpertiseService.canCombineFromPalette(knowledge)
        );
    }

    @Test
    void masteryUsesFiftyPercentRefundAndTenPercentFreePlacement() {
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();
        knowledge.completeResearch(
                ResearchExpertiseService.MASTERY_RESEARCH_ID
        );

        assertTrue(
                ResearchExpertiseService.refundsErasedAspect(
                        knowledge,
                        0.4999F
                )
        );
        assertFalse(
                ResearchExpertiseService.refundsErasedAspect(
                        knowledge,
                        0.50F
                )
        );
        assertFalse(
                ResearchExpertiseService.placementCostsAspect(
                        knowledge,
                        0.0999F
                )
        );
        assertTrue(
                ResearchExpertiseService.placementCostsAspect(
                        knowledge,
                        0.10F
                )
        );
        assertTrue(
                ResearchExpertiseService.canCombineFromPalette(knowledge)
        );
    }

    @Test
    void playerWithoutResearchAlwaysPaysAndNeverReceivesEraseRefund() {
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();

        assertTrue(
                ResearchExpertiseService.placementCostsAspect(knowledge, 0.0F)
        );
        assertFalse(
                ResearchExpertiseService.refundsErasedAspect(knowledge, 0.0F)
        );
        assertFalse(
                ResearchExpertiseService.canInspectComponents(knowledge)
        );
    }
}
