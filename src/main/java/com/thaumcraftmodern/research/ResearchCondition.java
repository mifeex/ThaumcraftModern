package com.thaumcraftmodern.research;

import com.thaumcraftmodern.knowledge.PlayerThaumKnowledge;
import com.thaumcraftmodern.knowledge.WarpType;
import com.thaumcraftmodern.scan.ScanRegistry;

import java.util.List;
import java.util.Objects;

/**
 * A data-driven, side-safe research prerequisite.
 *
 * <p>Conditions only inspect the synchronized player knowledge capability, so
 * the server remains authoritative while the Thaumonomicon can render the same
 * result without duplicating gameplay rules.</p>
 */
public sealed interface ResearchCondition permits
        ResearchCondition.Always,
        ResearchCondition.AllOf,
        ResearchCondition.AnyOf,
        ResearchCondition.Not,
        ResearchCondition.ResearchCompleted,
        ResearchCondition.ResearchRevealed,
        ResearchCondition.ScanCompleted,
        ResearchCondition.ScannedAspect,
        ResearchCondition.AspectKnown,
        ResearchCondition.AspectAmount,
        ResearchCondition.WarpAtLeast,
        ResearchCondition.CriterionRecorded {

    ResearchCondition ALWAYS = new Always();

    boolean test(PlayerThaumKnowledge knowledge);

    record Always() implements ResearchCondition {
        @Override
        public boolean test(PlayerThaumKnowledge knowledge) {
            return true;
        }
    }

    record AllOf(List<ResearchCondition> conditions) implements ResearchCondition {
        public AllOf {
            conditions = immutableConditions(conditions);
        }

        @Override
        public boolean test(PlayerThaumKnowledge knowledge) {
            return conditions.stream().allMatch(condition -> condition.test(knowledge));
        }
    }

    record AnyOf(List<ResearchCondition> conditions) implements ResearchCondition {
        public AnyOf {
            conditions = immutableConditions(conditions);
            if (conditions.isEmpty()) {
                throw new IllegalArgumentException("any_of requires at least one condition");
            }
        }

        @Override
        public boolean test(PlayerThaumKnowledge knowledge) {
            return conditions.stream().anyMatch(condition -> condition.test(knowledge));
        }
    }

    record Not(ResearchCondition condition) implements ResearchCondition {
        public Not {
            Objects.requireNonNull(condition, "condition");
        }

        @Override
        public boolean test(PlayerThaumKnowledge knowledge) {
            return !condition.test(knowledge);
        }
    }

    record ResearchCompleted(String researchId) implements ResearchCondition {
        public ResearchCompleted {
            researchId = stableId(researchId, "researchId");
        }

        @Override
        public boolean test(PlayerThaumKnowledge knowledge) {
            return knowledge.hasCompletedResearch(researchId);
        }
    }

    record ResearchRevealed(String researchId) implements ResearchCondition {
        public ResearchRevealed {
            researchId = stableId(researchId, "researchId");
        }

        @Override
        public boolean test(PlayerThaumKnowledge knowledge) {
            return knowledge.hasRevealedResearch(researchId);
        }
    }

    record ScanCompleted(String scanId) implements ResearchCondition {
        public ScanCompleted {
            scanId = stableId(scanId, "scanId");
        }

        @Override
        public boolean test(PlayerThaumKnowledge knowledge) {
            return knowledge.hasScan(scanId);
        }
    }

    /**
     * True after the player scanned at least one target carrying this aspect.
     * This mirrors TC4's aspect clue behavior and is intentionally stricter
     * than merely knowing the aspect through combination.
     */
    record ScannedAspect(String aspectId) implements ResearchCondition {
        public ScannedAspect {
            aspectId = stableId(aspectId, "aspectId");
        }

        @Override
        public boolean test(PlayerThaumKnowledge knowledge) {
            return knowledge.scans().stream()
                    .map(ScanRegistry::findByScanKey)
                    .flatMap(java.util.Optional::stream)
                    .flatMap(definition -> definition.aspects().stream())
                    .anyMatch(reward -> reward.aspectId().equals(aspectId));
        }
    }

    record AspectKnown(String aspectId) implements ResearchCondition {
        public AspectKnown {
            aspectId = stableId(aspectId, "aspectId");
        }

        @Override
        public boolean test(PlayerThaumKnowledge knowledge) {
            return knowledge.knowsAspect(aspectId);
        }
    }

    record AspectAmount(String aspectId, int minimum) implements ResearchCondition {
        public AspectAmount {
            aspectId = stableId(aspectId, "aspectId");
            if (minimum < 1) {
                throw new IllegalArgumentException("aspect amount minimum must be positive");
            }
        }

        @Override
        public boolean test(PlayerThaumKnowledge knowledge) {
            return knowledge.aspectAmount(aspectId) >= minimum;
        }
    }

    record WarpAtLeast(WarpMeasure measure, int minimum) implements ResearchCondition {
        public WarpAtLeast {
            Objects.requireNonNull(measure, "measure");
            if (minimum < 0) {
                throw new IllegalArgumentException("warp minimum cannot be negative");
            }
        }

        @Override
        public boolean test(PlayerThaumKnowledge knowledge) {
            int value = switch (measure) {
                case PERMANENT -> knowledge.warp(WarpType.PERMANENT);
                case NORMAL -> knowledge.warp(WarpType.NORMAL);
                case TEMPORARY -> knowledge.warp(WarpType.TEMPORARY);
                case NON_TEMPORARY -> knowledge.nonTemporaryWarp();
                case TOTAL -> knowledge.totalWarp();
            };
            return value >= minimum;
        }
    }

    record CriterionRecorded(String criterionId) implements ResearchCondition {
        public CriterionRecorded {
            criterionId = stableId(criterionId, "criterionId");
        }

        @Override
        public boolean test(PlayerThaumKnowledge knowledge) {
            return knowledge.hasResearchCriterion(criterionId);
        }
    }

    enum WarpMeasure {
        PERMANENT,
        NORMAL,
        TEMPORARY,
        NON_TEMPORARY,
        TOTAL
    }

    private static List<ResearchCondition> immutableConditions(
            List<ResearchCondition> conditions
    ) {
        List<ResearchCondition> copy = List.copyOf(
                Objects.requireNonNull(conditions, "conditions")
        );
        if (copy.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("conditions cannot contain null");
        }
        return copy;
    }

    private static String stableId(String id, String label) {
        Objects.requireNonNull(id, label);
        if (id.isBlank() || !id.equals(id.trim())) {
            throw new IllegalArgumentException(label + " must be non-blank and trimmed");
        }
        return id;
    }
}
