package com.thaumcraftmodern.research;

import com.thaumcraftmodern.aspect.AspectCost;
import com.thaumcraftmodern.item.DiscoveryItem;
import com.thaumcraftmodern.knowledge.PlayerThaumKnowledge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/** Server-authoritative TC4 completed-research duplication transaction. */
public final class ResearchDuplicationService {
    public static final String UNLOCK_RESEARCH = "researchdupe";

    private ResearchDuplicationService() {
    }

    public static List<AspectCost> cost(ResearchDefinition research, int copies) {
        int surcharge = Math.max(0, copies);
        return research.researchCost().stream()
                .map(cost -> new AspectCost(
                        cost.aspectId(),
                        Math.addExact(cost.amount(), surcharge)
                ))
                .toList();
    }

    public static Result duplicate(
            ServerPlayer player,
            PlayerThaumKnowledge knowledge,
            ItemStack source
    ) {
        if (!knowledge.hasCompletedResearch(UNLOCK_RESEARCH)) {
            return Result.RESEARCH_LOCKED;
        }
        if (!(source.getItem() instanceof DiscoveryItem)
                || !DiscoveryItem.hasValidPayload(source)) {
            return Result.INVALID_DISCOVERY;
        }
        ResearchDefinition research = ResearchRegistry.find(
                DiscoveryItem.researchId(source)
        ).orElse(null);
        if (research == null || research.researchCost().isEmpty()) {
            return Result.UNKNOWN_RESEARCH;
        }

        Inventory inventory = player.getInventory();
        int featherSlot = find(inventory, Items.FEATHER);
        int paperSlot = find(inventory, Items.PAPER);
        if (featherSlot < 0 || paperSlot < 0) {
            return Result.MISSING_MATERIALS;
        }

        List<AspectCost> cost = cost(research, DiscoveryItem.copies(source));
        if (cost.stream().anyMatch(requirement ->
                knowledge.aspectAmount(requirement.aspectId()) < requirement.amount())) {
            return Result.MISSING_ASPECTS;
        }

        for (AspectCost requirement : cost) {
            for (int point = 0; point < requirement.amount(); point++) {
                if (!knowledge.tryConsumeAspect(requirement.aspectId())) {
                    throw new IllegalStateException(
                            "prevalidated research duplication aspect became unavailable"
                    );
                }
            }
        }
        inventory.getItem(featherSlot).shrink(1);
        inventory.getItem(paperSlot).shrink(1);

        int nextCopies = Math.addExact(DiscoveryItem.copies(source), 1);
        DiscoveryItem.setCopies(source, nextCopies);
        ItemStack duplicate = source.copy();
        duplicate.setCount(1);
        if (!inventory.add(duplicate)) {
            player.drop(duplicate, false);
        }
        inventory.setChanged();
        return Result.CREATED;
    }

    private static int find(Inventory inventory, net.minecraft.world.item.Item item) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot).is(item)) {
                return slot;
            }
        }
        return -1;
    }

    public enum Result {
        CREATED,
        RESEARCH_LOCKED,
        INVALID_DISCOVERY,
        UNKNOWN_RESEARCH,
        MISSING_MATERIALS,
        MISSING_ASPECTS
    }
}
