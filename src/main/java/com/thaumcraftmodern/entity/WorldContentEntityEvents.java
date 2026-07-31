package com.thaumcraftmodern.entity;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.registry.ModEntities;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = ThaumcraftModern.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD
)
public final class WorldContentEntityEvents {
    private WorldContentEntityEvents() {
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        for (var entry : ModEntities.entries()) {
            event.put(
                    entry.getValue().get(),
                    LegacyThaumcraftMob.createAttributes(entry.getKey()).build()
            );
        }
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(
            SpawnPlacementRegisterEvent event
    ) {
        for (var entry : ModEntities.entries()) {
            event.register(
                    entry.getValue().get(),
                    entry.getKey().flying()
                            ? SpawnPlacements.Type.NO_RESTRICTIONS
                            : SpawnPlacements.Type.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    LegacyThaumcraftMob::checkSpawnRules,
                    SpawnPlacementRegisterEvent.Operation.REPLACE
            );
        }
    }
}
