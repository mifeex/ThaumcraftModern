package com.thaumcraftmodern.research;

import com.thaumcraftmodern.aspect.AspectCost;

import java.util.List;
import java.util.Objects;

import net.minecraft.resources.ResourceLocation;

public record ResearchDefinition(
        String id,
        String categoryId,
        String iconItem,
        String iconResource,
        String titleKey,
        String subtitleKey,
        boolean concealed,
        boolean autoUnlock,
        boolean inactive,
        boolean virtual,
        String revealedBy,
        List<String> parents,
        List<String> hiddenParents,
        ResearchCondition revealWhen,
        ResearchCondition unlockWhen,
        int x,
        int y,
        List<ResearchPageDefinition> pages,
        int completionWarp,
        NodeFrame nodeFrame,
        boolean specialFrame,
        List<AspectCost> researchCost,
        List<AspectCost> purchaseCost,
        List<String> siblings
) {
    public enum NodeFrame {
        PRIMARY,
        ROUND,
        SECONDARY,
        HIDDEN
    }

    public ResearchDefinition(
            String id,
            String categoryId,
            String iconItem,
            String iconResource,
            String titleKey,
            String subtitleKey,
            boolean concealed,
            boolean autoUnlock,
            boolean inactive,
            boolean virtual,
            String revealedBy,
            List<String> parents,
            List<String> hiddenParents,
            ResearchCondition revealWhen,
            ResearchCondition unlockWhen,
            int x,
            int y,
            List<ResearchPageDefinition> pages
    ) {
        this(
                id,
                categoryId,
                iconItem,
                iconResource,
                titleKey,
                subtitleKey,
                concealed,
                autoUnlock,
                inactive,
                virtual,
                revealedBy,
                parents,
                hiddenParents,
                revealWhen,
                unlockWhen,
                x,
                y,
                pages,
                0,
                NodeFrame.PRIMARY,
                false,
                List.of(),
                List.of(),
                List.of()
        );
    }

    public ResearchDefinition(
            String id,
            String categoryId,
            String iconItem,
            String titleKey,
            String subtitleKey,
            boolean concealed,
            boolean autoUnlock,
            boolean inactive,
            String revealedBy,
            List<String> parents,
            int x,
            int y,
            List<ResearchPageDefinition> pages
    ) {
        this(
                id,
                categoryId,
                iconItem,
                "",
                titleKey,
                subtitleKey,
                concealed,
                autoUnlock,
                inactive,
                false,
                revealedBy,
                parents,
                List.of(),
                ResearchCondition.ALWAYS,
                ResearchCondition.ALWAYS,
                x,
                y,
                pages,
                0,
                NodeFrame.PRIMARY,
                false,
                List.of(),
                List.of(),
                List.of()
        );
    }

    public ResearchDefinition(
            String id,
            String categoryId,
            String iconItem,
            String titleKey,
            String subtitleKey,
            boolean concealed,
            boolean autoUnlock,
            boolean inactive,
            String revealedBy,
            List<String> parents,
            List<String> hiddenParents,
            ResearchCondition revealWhen,
            ResearchCondition unlockWhen,
            int x,
            int y,
            List<ResearchPageDefinition> pages
    ) {
        this(
                id,
                categoryId,
                iconItem,
                "",
                titleKey,
                subtitleKey,
                concealed,
                autoUnlock,
                inactive,
                false,
                revealedBy,
                parents,
                hiddenParents,
                revealWhen,
                unlockWhen,
                x,
                y,
                pages,
                0,
                NodeFrame.PRIMARY,
                false,
                List.of(),
                List.of(),
                List.of()
        );
    }

    public ResearchDefinition(
            String id,
            String categoryId,
            String iconItem,
            String iconResource,
            String titleKey,
            String subtitleKey,
            boolean concealed,
            boolean autoUnlock,
            boolean inactive,
            boolean virtual,
            String revealedBy,
            List<String> parents,
            List<String> hiddenParents,
            ResearchCondition revealWhen,
            ResearchCondition unlockWhen,
            int x,
            int y,
            List<ResearchPageDefinition> pages,
            NodeFrame nodeFrame,
            boolean specialFrame
    ) {
        this(
                id,
                categoryId,
                iconItem,
                iconResource,
                titleKey,
                subtitleKey,
                concealed,
                autoUnlock,
                inactive,
                virtual,
                revealedBy,
                parents,
                hiddenParents,
                revealWhen,
                unlockWhen,
                x,
                y,
                pages,
                0,
                nodeFrame,
                specialFrame,
                List.of(),
                List.of(),
                List.of()
        );
    }

    public ResearchDefinition(
            String id,
            String categoryId,
            String iconItem,
            String iconResource,
            String titleKey,
            String subtitleKey,
            boolean concealed,
            boolean autoUnlock,
            boolean inactive,
            boolean virtual,
            String revealedBy,
            List<String> parents,
            List<String> hiddenParents,
            ResearchCondition revealWhen,
            ResearchCondition unlockWhen,
            int x,
            int y,
            List<ResearchPageDefinition> pages,
            int completionWarp,
            NodeFrame nodeFrame,
            boolean specialFrame
    ) {
        this(
                id,
                categoryId,
                iconItem,
                iconResource,
                titleKey,
                subtitleKey,
                concealed,
                autoUnlock,
                inactive,
                virtual,
                revealedBy,
                parents,
                hiddenParents,
                revealWhen,
                unlockWhen,
                x,
                y,
                pages,
                completionWarp,
                nodeFrame,
                specialFrame,
                List.of(),
                List.of(),
                List.of()
        );
    }

    public ResearchDefinition(
            String id,
            String categoryId,
            String iconItem,
            String iconResource,
            String titleKey,
            String subtitleKey,
            boolean concealed,
            boolean autoUnlock,
            boolean inactive,
            boolean virtual,
            String revealedBy,
            List<String> parents,
            List<String> hiddenParents,
            ResearchCondition revealWhen,
            ResearchCondition unlockWhen,
            int x,
            int y,
            List<ResearchPageDefinition> pages,
            int completionWarp,
            NodeFrame nodeFrame,
            boolean specialFrame,
            List<AspectCost> purchaseCost
    ) {
        this(
                id, categoryId, iconItem, iconResource, titleKey, subtitleKey,
                concealed, autoUnlock, inactive, virtual, revealedBy, parents,
                hiddenParents, revealWhen, unlockWhen, x, y, pages,
                completionWarp, nodeFrame, specialFrame, List.of(),
                purchaseCost,
                List.of()
        );
    }

    public ResearchDefinition(
            String id,
            String categoryId,
            String iconItem,
            String iconResource,
            String titleKey,
            String subtitleKey,
            boolean concealed,
            boolean autoUnlock,
            boolean inactive,
            boolean virtual,
            String revealedBy,
            List<String> parents,
            List<String> hiddenParents,
            ResearchCondition revealWhen,
            ResearchCondition unlockWhen,
            int x,
            int y,
            List<ResearchPageDefinition> pages,
            int completionWarp,
            NodeFrame nodeFrame,
            boolean specialFrame,
            List<AspectCost> purchaseCost,
            List<String> siblings
    ) {
        this(
                id, categoryId, iconItem, iconResource, titleKey, subtitleKey,
                concealed, autoUnlock, inactive, virtual, revealedBy, parents,
                hiddenParents, revealWhen, unlockWhen, x, y, pages,
                completionWarp, nodeFrame, specialFrame, List.of(),
                purchaseCost, siblings
        );
    }

    public ResearchDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(categoryId, "categoryId");
        Objects.requireNonNull(iconItem, "iconItem");
        Objects.requireNonNull(iconResource, "iconResource");
        Objects.requireNonNull(titleKey, "titleKey");
        if (id.isBlank() || !id.equals(id.trim())) {
            throw new IllegalArgumentException("research id must be non-blank and trimmed");
        }
        if (categoryId.isBlank() || !categoryId.equals(categoryId.trim())) {
            throw new IllegalArgumentException(
                    "research category id must be non-blank and trimmed"
            );
        }
        if (iconItem.isBlank() == iconResource.isBlank()) {
            throw new IllegalArgumentException(
                    "research must define exactly one icon item or icon resource"
            );
        }
        if (!iconItem.isBlank() && ResourceLocation.tryParse(iconItem) == null) {
            throw new IllegalArgumentException(
                    "invalid research icon item: " + iconItem
            );
        }
        if (!iconResource.isBlank()
                && ResourceLocation.tryParse(iconResource) == null) {
            throw new IllegalArgumentException(
                    "invalid research icon resource: " + iconResource
            );
        }
        if (titleKey.isBlank()) {
            throw new IllegalArgumentException("research title key must not be blank");
        }
        subtitleKey = subtitleKey == null ? "" : subtitleKey;
        revealedBy = revealedBy == null ? "" : revealedBy;
        parents = List.copyOf(Objects.requireNonNull(parents, "parents"));
        if (parents.stream().anyMatch(parent -> parent == null || parent.isBlank())) {
            throw new IllegalArgumentException("research parents cannot be null or blank");
        }
        hiddenParents = List.copyOf(
                Objects.requireNonNull(hiddenParents, "hiddenParents")
        );
        if (hiddenParents.stream().anyMatch(parent -> parent == null || parent.isBlank())) {
            throw new IllegalArgumentException(
                    "hidden research parents cannot be null or blank"
            );
        }
        revealWhen = Objects.requireNonNull(revealWhen, "revealWhen");
        unlockWhen = Objects.requireNonNull(unlockWhen, "unlockWhen");
        pages = List.copyOf(Objects.requireNonNull(pages, "pages"));
        researchCost = List.copyOf(
                Objects.requireNonNull(researchCost, "researchCost")
        );
        purchaseCost = List.copyOf(
                Objects.requireNonNull(purchaseCost, "purchaseCost")
        );
        siblings = List.copyOf(Objects.requireNonNull(siblings, "siblings"));
        if (siblings.stream().anyMatch(sibling -> sibling == null
                || sibling.isBlank())) {
            throw new IllegalArgumentException(
                    "research siblings cannot be null or blank"
            );
        }
        if (purchaseCost.stream()
                .map(AspectCost::aspectId)
                .distinct()
                .count() != purchaseCost.size()) {
            throw new IllegalArgumentException(
                    "research purchase cost cannot repeat an aspect"
            );
        }
        if (researchCost.stream()
                .map(AspectCost::aspectId)
                .distinct()
                .count() != researchCost.size()) {
            throw new IllegalArgumentException(
                    "research cost cannot repeat an aspect"
            );
        }
        if (completionWarp < 0) {
            throw new IllegalArgumentException(
                    "research completion warp cannot be negative"
            );
        }
        nodeFrame = Objects.requireNonNull(nodeFrame, "nodeFrame");
    }

    public boolean purchasable() {
        return !purchaseCost.isEmpty();
    }
}
