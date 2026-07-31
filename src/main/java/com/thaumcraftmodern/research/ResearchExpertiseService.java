package com.thaumcraftmodern.research;

import com.thaumcraftmodern.knowledge.PlayerThaumKnowledge;

import java.util.Objects;

/**
 * Exact TC4 Research Expertise and Research Mastery perk rules.
 *
 * <p>The caller supplies a server-generated roll so all point mutations stay
 * authoritative while the probability boundaries remain directly testable.</p>
 */
public final class ResearchExpertiseService {
    public static final String EXPERTISE_RESEARCH_ID = "researcher1";
    public static final String MASTERY_RESEARCH_ID = "researcher2";

    private static final float EXPERTISE_ERASE_REFUND_CHANCE = 0.25F;
    private static final float MASTERY_ERASE_REFUND_CHANCE = 0.50F;
    private static final float MASTERY_FREE_PLACEMENT_CHANCE = 0.10F;

    private ResearchExpertiseService() {
    }

    public static boolean canInspectComponents(PlayerThaumKnowledge knowledge) {
        return requireKnowledge(knowledge)
                .hasCompletedResearch(EXPERTISE_RESEARCH_ID);
    }

    public static boolean canCombineFromPalette(PlayerThaumKnowledge knowledge) {
        return requireKnowledge(knowledge)
                .hasCompletedResearch(MASTERY_RESEARCH_ID);
    }

    public static boolean placementCostsAspect(
            PlayerThaumKnowledge knowledge,
            float roll
    ) {
        requireRoll(roll);
        return !canCombineFromPalette(knowledge)
                || roll >= MASTERY_FREE_PLACEMENT_CHANCE;
    }

    public static boolean refundsErasedAspect(
            PlayerThaumKnowledge knowledge,
            float roll
    ) {
        requireRoll(roll);
        PlayerThaumKnowledge validated = requireKnowledge(knowledge);
        float chance = validated.hasCompletedResearch(MASTERY_RESEARCH_ID)
                ? MASTERY_ERASE_REFUND_CHANCE
                : validated.hasCompletedResearch(EXPERTISE_RESEARCH_ID)
                        ? EXPERTISE_ERASE_REFUND_CHANCE
                        : 0.0F;
        return roll < chance;
    }

    private static PlayerThaumKnowledge requireKnowledge(
            PlayerThaumKnowledge knowledge
    ) {
        return Objects.requireNonNull(knowledge, "knowledge");
    }

    private static void requireRoll(float roll) {
        if (Float.isNaN(roll) || roll < 0.0F || roll >= 1.0F) {
            throw new IllegalArgumentException(
                    "roll must be in the range [0, 1)"
            );
        }
    }
}
