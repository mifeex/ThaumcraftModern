package com.thaumcraftmodern.research;

import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.knowledge.KnowledgeSync;
import com.thaumcraftmodern.knowledge.PlayerThaumKnowledge;
import com.thaumcraftmodern.knowledge.WarpType;
import net.minecraft.server.level.ServerPlayer;
import com.thaumcraftmodern.network.ModNetwork;
import com.thaumcraftmodern.network.packet.WarpFeedbackPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Single authority for research visibility and availability.
 *
 * <p>The model mirrors classic Thaumcraft's distinction between a topic being
 * discovered (visible in the Thaumonomicon) and being completed. Reveals are
 * monotonic: once a clue exposes a topic, losing temporary state cannot make
 * the icon disappear again.</p>
 */
public final class ResearchProgressService {
    private ResearchProgressService() {
    }

    public static Update reconcile(PlayerThaumKnowledge knowledge) {
        Objects.requireNonNull(knowledge, "knowledge");
        List<String> revealed = new ArrayList<>();
        List<String> completed = new ArrayList<>();
        boolean changed;
        int passes = 0;
        int maximumPasses = Math.max(1, ResearchRegistry.all().size() + 1);
        do {
            changed = false;
            passes++;
            for (ResearchDefinition definition : ResearchRegistry.all()) {
                if (!knowledge.hasRevealedResearch(definition.id())
                        && shouldReveal(definition, knowledge)
                        && knowledge.revealResearch(definition.id())) {
                    revealed.add(definition.id());
                    changed = true;
                }
            }
            for (ResearchDefinition definition : ResearchRegistry.all()) {
                if (!definition.inactive()
                        && definition.autoUnlock()
                        && !knowledge.hasCompletedResearch(definition.id())
                        && isAvailable(definition, knowledge)
                        && knowledge.completeResearch(definition.id())) {
                    completed.add(definition.id());
                    changed = true;
                }
            }
        } while (changed && passes < maximumPasses);
        return new Update(revealed, completed);
    }

    public static boolean isVisible(
            ResearchDefinition definition,
            PlayerThaumKnowledge knowledge
    ) {
        if (knowledge.hasCompletedResearch(definition.id())) {
            return true;
        }
        if (!knowledge.hasRevealedResearch(definition.id())
                || !definition.revealWhen().test(knowledge)) {
            return false;
        }
        if (!definition.revealedBy().isBlank()
                && !knowledge.hasCompletedResearch(definition.revealedBy())) {
            return false;
        }
        return !definition.concealed()
                || allCompleted(definition.parents(), knowledge)
                && allCompleted(definition.hiddenParents(), knowledge);
    }

    public static boolean isAvailable(
            ResearchDefinition definition,
            PlayerThaumKnowledge knowledge
    ) {
        if (definition.inactive()
                || !isVisible(definition, knowledge)
                || !allCompleted(definition.parents(), knowledge)
                || !allCompleted(definition.hiddenParents(), knowledge)
                || !definition.revealWhen().test(knowledge)
                || !definition.unlockWhen().test(knowledge)) {
            return false;
        }
        return definition.revealedBy().isBlank()
                || knowledge.hasCompletedResearch(definition.revealedBy());
    }

    public static boolean hasVisibleResearch(
            String categoryId,
            PlayerThaumKnowledge knowledge
    ) {
        Objects.requireNonNull(categoryId, "categoryId");
        Objects.requireNonNull(knowledge, "knowledge");
        return ResearchRegistry.all().stream()
                .filter(definition -> definition.categoryId().equals(categoryId))
                .filter(definition -> !definition.virtual())
                .anyMatch(definition -> isVisible(definition, knowledge));
    }

    public static boolean canCreateNotes(
            ResearchDefinition definition,
            PlayerThaumKnowledge knowledge
    ) {
        return !definition.purchasable()
                && !definition.autoUnlock()
                && !knowledge.hasCompletedResearch(definition.id())
                && isAvailable(definition, knowledge);
    }

    public static boolean recordCriterion(
            ServerPlayer player,
            String criterionId,
            String reason
    ) {
        return KnowledgeAccess.get(player).map(knowledge -> {
            boolean changed = knowledge.recordResearchCriterion(criterionId);
            if (changed) {
                KnowledgeSync.send(player, "criterion:" + reason);
            }
            return changed;
        }).orElse(false);
    }

    public static int addWarp(
            ServerPlayer player,
            WarpType type,
            int amount,
            String reason
    ) {
        return KnowledgeAccess.get(player).map(knowledge -> {
            int value = knowledge.addWarp(type, amount);
            KnowledgeSync.send(player, "warp:" + reason);
            ModNetwork.sendTo(player, new WarpFeedbackPacket(
                    switch (type) {
                        case PERMANENT -> WarpFeedbackPacket.PERMANENT;
                        case NORMAL -> WarpFeedbackPacket.NORMAL;
                        case TEMPORARY -> WarpFeedbackPacket.TEMPORARY;
                    },
                    amount,
                    WarpFeedbackPacket.VISUAL_NONE
            ));
            return value;
        }).orElse(0);
    }

    private static boolean shouldReveal(
            ResearchDefinition definition,
            PlayerThaumKnowledge knowledge
    ) {
        if (knowledge.hasCompletedResearch(definition.id())) {
            return true;
        }
        if (!definition.revealWhen().test(knowledge)) {
            return false;
        }
        return !definition.concealed()
                || allCompleted(definition.parents(), knowledge)
                && allCompleted(definition.hiddenParents(), knowledge);
    }

    private static boolean allCompleted(
            List<String> researchIds,
            PlayerThaumKnowledge knowledge
    ) {
        return researchIds.stream().allMatch(knowledge::hasCompletedResearch);
    }

    public record Update(List<String> revealed, List<String> autoCompleted) {
        public Update {
            revealed = List.copyOf(revealed);
            autoCompleted = List.copyOf(autoCompleted);
        }

        public boolean changed() {
            return !revealed.isEmpty() || !autoCompleted.isEmpty();
        }
    }
}
