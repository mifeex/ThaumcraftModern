package com.thaumcraftmodern.worldgen;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Adds the two TC4 profession buildings to vanilla village house pools.
 */
public final class LegacyVillagePoolInjector {
    static final int WIZARD_TOWER_WEIGHT = 17;
    static final int BANKER_HOME_WEIGHT = 29;

    private static final List<ResourceLocation> HOUSE_POOLS = List.of(
            villagePool("plains"),
            villagePool("desert"),
            villagePool("savanna"),
            villagePool("snowy"),
            villagePool("taiga")
    );

    private LegacyVillagePoolInjector() {
    }

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        Registry<StructureTemplatePool> pools =
                event.getServer().registryAccess().registryOrThrow(
                        Registries.TEMPLATE_POOL
                );
        for (ResourceLocation poolId : HOUSE_POOLS) {
            StructureTemplatePool pool = pools.get(poolId);
            if (pool == null) {
                continue;
            }
            /*
             * TC4 registered these village pieces with weights 15 and 25.
             * The requested 15% density increase cannot be represented
             * exactly by integer jigsaw weights, so use the nearest values:
             * 17 (+13.3%) and 29 (+16%).
             */
            addIfMissing(
                    pool,
                    LegacyStructureKind.WIZARD_TOWER,
                    WIZARD_TOWER_WEIGHT
            );
            addIfMissing(
                    pool,
                    LegacyStructureKind.BANKER_HOME,
                    BANKER_HOME_WEIGHT
            );
        }
    }

    private static void addIfMissing(
            StructureTemplatePool pool,
            LegacyStructureKind kind,
            int weight
    ) {
        if (pool.rawTemplates.stream().map(Pair::getFirst)
                .anyMatch(element -> element
                        instanceof LegacyVillagePoolElement legacy
                        && legacy.kind() == kind)) {
            return;
        }
        StructurePoolElement element = new LegacyVillagePoolElement(
                kind,
                StructureTemplatePool.Projection.RIGID
        );
        if (!(pool.rawTemplates instanceof ArrayList<?>)) {
            pool.rawTemplates = new ArrayList<>(pool.rawTemplates);
        }
        pool.rawTemplates.add(Pair.of(element, weight));
        for (int copy = 0; copy < weight; copy++) {
            pool.templates.add(element);
        }
        pool.maxSize = Integer.MIN_VALUE;
    }

    private static ResourceLocation villagePool(String style) {
        return new ResourceLocation(
                "minecraft",
                "village/" + style + "/houses"
        );
    }
}
