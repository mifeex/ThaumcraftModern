package com.thaumcraftmodern.registry;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.LegacyMobKind;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import com.thaumcraftmodern.entity.EldritchOrbEntity;
import com.thaumcraftmodern.entity.AlumentumEntity;
import com.thaumcraftmodern.entity.BottledTaintProjectile;
import com.thaumcraftmodern.entity.PechBlastEntity;
import com.thaumcraftmodern.entity.FacelessWitnessEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(
                    ForgeRegistries.ENTITY_TYPES,
                    ThaumcraftModern.MOD_ID
            );

    private static final EnumMap<LegacyMobKind, RegistryObject<EntityType<LegacyThaumcraftMob>>>
            BY_KIND = new EnumMap<>(LegacyMobKind.class);

    public static final RegistryObject<EntityType<EldritchOrbEntity>>
            ELDRITCH_ORB = ENTITY_TYPES.register(
                    "eldritch_orb",
                    () -> EntityType.Builder.<EldritchOrbEntity>of(
                                    EldritchOrbEntity::new,
                                    MobCategory.MISC
                            )
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build(
                                    ThaumcraftModern.MOD_ID
                                            + ":eldritch_orb"
                            )
            );
    public static final RegistryObject<EntityType<AlumentumEntity>>
            ALUMENTUM = ENTITY_TYPES.register(
                    "alumentum",
                    () -> EntityType.Builder.<AlumentumEntity>of(
                                    AlumentumEntity::new,
                                    MobCategory.MISC
                            )
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build(
                                    ThaumcraftModern.MOD_ID + ":alumentum"
                            )
            );
    public static final RegistryObject<EntityType<BottledTaintProjectile>>
            BOTTLED_TAINT = ENTITY_TYPES.register(
                    "bottled_taint",
                    () -> EntityType.Builder.<BottledTaintProjectile>of(
                                    BottledTaintProjectile::new,
                                    MobCategory.MISC
                            ).sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build(ThaumcraftModern.MOD_ID + ":bottled_taint")
            );
    public static final RegistryObject<EntityType<PechBlastEntity>>
            PECH_BLAST = ENTITY_TYPES.register(
                    "pech_blast",
                    () -> EntityType.Builder.<PechBlastEntity>of(
                                    PechBlastEntity::new,
                                    MobCategory.MISC
                            )
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(8)
                            .updateInterval(10)
                            .build(
                                    ThaumcraftModern.MOD_ID + ":pech_blast"
                            )
            );
    public static final RegistryObject<EntityType<FacelessWitnessEntity>>
            FACELESS_WITNESS = ENTITY_TYPES.register(
                    "faceless_witness",
                    () -> EntityType.Builder.<FacelessWitnessEntity>of(
                                    FacelessWitnessEntity::new,
                                    MobCategory.MONSTER
                            )
                            .sized(0.8F, 2.7F)
                            .clientTrackingRange(10)
                            .updateInterval(3)
                            .build(
                                    ThaumcraftModern.MOD_ID
                                            + ":faceless_witness"
                            )
            );

    public static final RegistryObject<EntityType<LegacyThaumcraftMob>>
            ANGRY_ZOMBIE = mob(LegacyMobKind.ANGRY_ZOMBIE);
    public static final RegistryObject<EntityType<LegacyThaumcraftMob>>
            FURIOUS_ZOMBIE = mob(LegacyMobKind.FURIOUS_ZOMBIE);
    public static final RegistryObject<EntityType<LegacyThaumcraftMob>>
            WISP = mob(LegacyMobKind.WISP);
    public static final RegistryObject<EntityType<LegacyThaumcraftMob>>
            FIREBAT = mob(LegacyMobKind.FIREBAT);
    public static final RegistryObject<EntityType<LegacyThaumcraftMob>>
            PECH = mob(LegacyMobKind.PECH);
    public static final RegistryObject<EntityType<LegacyThaumcraftMob>>
            MIND_SPIDER = mob(LegacyMobKind.MIND_SPIDER);
    public static final RegistryObject<EntityType<LegacyThaumcraftMob>>
            ELDRITCH_GUARDIAN = mob(LegacyMobKind.ELDRITCH_GUARDIAN);
    public static final RegistryObject<EntityType<LegacyThaumcraftMob>>
            ELDRITCH_WARDEN = mob(LegacyMobKind.ELDRITCH_WARDEN);
    public static final RegistryObject<EntityType<LegacyThaumcraftMob>>
            CRIMSON_KNIGHT = mob(LegacyMobKind.CRIMSON_KNIGHT);
    public static final RegistryObject<EntityType<LegacyThaumcraftMob>>
            CRIMSON_CLERIC = mob(LegacyMobKind.CRIMSON_CLERIC);
    public static final RegistryObject<EntityType<LegacyThaumcraftMob>>
            CRIMSON_PRAETOR = mob(LegacyMobKind.CRIMSON_PRAETOR);
    public static final RegistryObject<EntityType<LegacyThaumcraftMob>>
            ELDRITCH_CONSTRUCT = mob(LegacyMobKind.ELDRITCH_CONSTRUCT);
    public static final RegistryObject<EntityType<LegacyThaumcraftMob>>
            ELDRITCH_CRAB = mob(LegacyMobKind.ELDRITCH_CRAB);
    public static final RegistryObject<EntityType<LegacyThaumcraftMob>>
            INHABITED_ZOMBIE = mob(LegacyMobKind.INHABITED_ZOMBIE);
    public static final RegistryObject<EntityType<LegacyThaumcraftMob>>
            THAUMIC_SLIME = mob(LegacyMobKind.THAUMIC_SLIME);
    public static final RegistryObject<EntityType<LegacyThaumcraftMob>>
            TAINTED_CRAWLER = mob(LegacyMobKind.TAINTED_CRAWLER);
    public static final RegistryObject<EntityType<LegacyThaumcraftMob>>
            TAINTACLE = mob(LegacyMobKind.TAINTACLE);
    public static final RegistryObject<EntityType<LegacyThaumcraftMob>>
            TAINT_TENDRIL = mob(LegacyMobKind.TAINT_TENDRIL);
    public static final RegistryObject<EntityType<LegacyThaumcraftMob>>
            TAINT_SPORE = mob(LegacyMobKind.TAINT_SPORE);
    public static final RegistryObject<EntityType<LegacyThaumcraftMob>>
            TAINT_SPORE_SWARMER = mob(LegacyMobKind.TAINT_SPORE_SWARMER);
    public static final RegistryObject<EntityType<LegacyThaumcraftMob>>
            TAINT_SWARM = mob(LegacyMobKind.TAINT_SWARM);
    public static final RegistryObject<EntityType<LegacyThaumcraftMob>>
            TAINTED_CHICKEN = mob(LegacyMobKind.TAINTED_CHICKEN);
    public static final RegistryObject<EntityType<LegacyThaumcraftMob>>
            TAINTED_COW = mob(LegacyMobKind.TAINTED_COW);
    public static final RegistryObject<EntityType<LegacyThaumcraftMob>>
            TAINTED_CREEPER = mob(LegacyMobKind.TAINTED_CREEPER);
    public static final RegistryObject<EntityType<LegacyThaumcraftMob>>
            TAINTED_PIG = mob(LegacyMobKind.TAINTED_PIG);
    public static final RegistryObject<EntityType<LegacyThaumcraftMob>>
            TAINTED_SHEEP = mob(LegacyMobKind.TAINTED_SHEEP);
    public static final RegistryObject<EntityType<LegacyThaumcraftMob>>
            TAINTED_VILLAGER = mob(LegacyMobKind.TAINTED_VILLAGER);
    public static final RegistryObject<EntityType<LegacyThaumcraftMob>>
            GIANT_TAINTACLE = mob(LegacyMobKind.GIANT_TAINTACLE);

    private ModEntities() {
    }

    private static RegistryObject<EntityType<LegacyThaumcraftMob>> mob(
            LegacyMobKind kind
    ) {
        RegistryObject<EntityType<LegacyThaumcraftMob>> result =
                ENTITY_TYPES.register(
                        kind.id(),
                        () -> {
                            EntityType.Builder<LegacyThaumcraftMob> builder =
                                    EntityType.Builder.of(
                                            (type, level) ->
                                                    new LegacyThaumcraftMob(
                                                            type,
                                                            level,
                                                            kind
                                                    ),
                                            MobCategory.MONSTER
                                    );
                            if (kind == LegacyMobKind.ELDRITCH_CONSTRUCT) {
                                builder.fireImmune();
                            }
                            return builder
                                    .sized(kind.width(), kind.height())
                                    .clientTrackingRange(
                                            kind.flying() ? 12 : 8
                                    )
                                    .updateInterval(3)
                                    .build(
                                            ThaumcraftModern.MOD_ID + ":"
                                                    + kind.id()
                                    );
                        }
                );
        BY_KIND.put(kind, result);
        return result;
    }

    public static RegistryObject<EntityType<LegacyThaumcraftMob>> forKind(
            LegacyMobKind kind
    ) {
        return BY_KIND.get(kind);
    }

    public static List<Map.Entry<LegacyMobKind, RegistryObject<EntityType<LegacyThaumcraftMob>>>>
            entries() {
        return List.copyOf(BY_KIND.entrySet());
    }

    public static void register(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
    }
}
