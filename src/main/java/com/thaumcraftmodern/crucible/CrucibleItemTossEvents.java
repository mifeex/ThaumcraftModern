package com.thaumcraftmodern.crucible;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.research.ResearchRegistry;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Captures research on the authoritative item-toss event. This mirrors TC4's
 * thrower-name lookup without depending on a live owner entity.
 */
@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID)
public final class CrucibleItemTossEvents {
    public static final String RESEARCH_TAG =
            "ThaumcraftCrucibleResearch";

    private CrucibleItemTossEvents() {
    }

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            attachResearch(player, event.getEntity());
        }
    }

    public static void attachResearch(
            ServerPlayer player,
            ItemEntity entity
    ) {
        ListTag completed = new ListTag();
        KnowledgeAccess.get(player).ifPresent(knowledge ->
                ResearchRegistry.all().stream()
                        .map(definition -> definition.id())
                        .filter(knowledge::hasCompletedResearch)
                        .map(StringTag::valueOf)
                        .forEach(completed::add)
        );
        entity.getPersistentData().put(RESEARCH_TAG, completed);
    }

    public static boolean hasResearch(ItemEntity entity, String research) {
        ListTag completed = entity.getPersistentData().getList(
                RESEARCH_TAG,
                Tag.TAG_STRING
        );
        for (int index = 0; index < completed.size(); index++) {
            if (research.equals(completed.getString(index))) {
                return true;
            }
        }
        return false;
    }
}
