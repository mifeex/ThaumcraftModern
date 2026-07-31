package com.thaumcraftmodern.worldgen;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aura.AuraNodeBlockEntity;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ThaumcraftModern.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LegacyStructureGameTests {
    private LegacyStructureGameTests() {
    }

    @GameTest(
            template = "empty",
            batch = "legacyStructureMound",
            timeoutTicks = 100
    )
    public static void registeredMoundPlacesCompletePayload(
            GameTestHelper helper
    ) {
        BlockPos requested = helper.absolutePos(new BlockPos(12, 12, 12));
        BlockPos center = new BlockPos(
                requested.getX(),
                helper.getLevel().getHeight(
                        Heightmap.Types.OCEAN_FLOOR_WG,
                        requested.getX(),
                        requested.getZ()
                ),
                requested.getZ()
        );
        for (int x = -10; x <= 10; x++) {
            for (int z = -10; z <= 10; z++) {
                for (int height = 0; height <= 16; height++) {
                    helper.getLevel().setBlock(
                            center.offset(x, height, z),
                            Blocks.AIR.defaultBlockState(),
                            2
                    );
                }
                for (int depth = 1; depth <= 12; depth++) {
                    helper.getLevel().setBlock(
                            center.offset(x, -depth, z),
                            Blocks.STONE.defaultBlockState(),
                            2
                    );
                }
                helper.getLevel().setBlock(
                        center.offset(x, -1, z),
                        Blocks.GRASS_BLOCK.defaultBlockState(),
                        2
                );
            }
        }
        helper.assertTrue(
                LegacyStructuresFeature.placeRegistered(
                        LegacyStructureKind.ANCIENT_MOUND,
                        helper.getLevel(),
                        center,
                        RandomSource.create(14298531L)
                ),
                "Registered mound placement failed"
        );

        BlockPos origin = center.offset(-9, -9, -9);
        assertSpawner(
                helper,
                origin.offset(4, 5, 4),
                "minecraft:skeleton"
        );
        assertSpawner(
                helper,
                origin.offset(4, 5, 14),
                "minecraft:zombie"
        );
        helper.assertTrue(
                helper.getLevel().getBlockEntity(origin.offset(9, 8, 9))
                        instanceof AuraNodeBlockEntity,
                "Mound dark node was not placed"
        );
        helper.assertTrue(
                helper.getLevel().getBlockState(origin.offset(9, 1, 7))
                        .is(ModBlocks.LOOT_URN.get())
                        || helper.getLevel()
                                .getBlockState(origin.offset(9, 1, 7))
                                .is(ModBlocks.LOOT_CRATE.get()),
                "Mound loot vessel was not placed"
        );
        for (int x = 0; x < AncientMoundBlueprint.WIDTH; x++) {
            for (int y = 0; y < AncientMoundBlueprint.HEIGHT; y++) {
                for (int z = 0; z < AncientMoundBlueprint.DEPTH; z++) {
                    helper.assertTrue(
                            !helper.getLevel()
                                    .getBlockState(origin.offset(x, y, z))
                                    .is(Blocks.TORCH),
                            "Mound retained a torch at "
                                    + origin.offset(x, y, z)
                    );
                }
            }
        }
        for (int y : new int[]{9, 10}) {
            var bars = helper.getLevel().getBlockState(
                    origin.offset(3, y, 9)
            );
            helper.assertTrue(
                    bars.is(Blocks.IRON_BARS),
                    "Mound iron-bar gate missing at y=" + y
            );
            helper.assertTrue(
                    bars.getValue(BlockStateProperties.NORTH)
                            && bars.getValue(BlockStateProperties.SOUTH),
                    "Mound iron bars were saved as a skinny disconnected post"
            );
        }
        helper.succeed();
    }

    @GameTest(
            template = "empty",
            batch = "legacyStructureSitePolicy",
            timeoutTicks = 100
    )
    public static void unsupportedVillageBuildingIsRejected(
            GameTestHelper helper
    ) {
        BlockPos origin = helper.absolutePos(new BlockPos(8, 8, 8));
        prepareStableGround(helper, origin.below(), 7);
        helper.getLevel().setBlock(
                origin.offset(2, -1, 2),
                Blocks.AIR.defaultBlockState(),
                2
        );
        helper.assertTrue(
                !LegacyStructuresFeature.placeVillageBuilding(
                        LegacyStructureKind.BANKER_HOME,
                        helper.getLevel(),
                        origin,
                        net.minecraft.world.level.block.Rotation.NONE,
                        RandomSource.create(14298540L)
                ),
                "Banker home spawned above a missing floor support"
        );
        helper.succeed();
    }

    @GameTest(
            template = "empty",
            batch = "legacyStructureSitePolicy",
            timeoutTicks = 100
    )
    public static void floodedVillageBuildingIsRejected(
            GameTestHelper helper
    ) {
        BlockPos origin = helper.absolutePos(new BlockPos(8, 8, 8));
        prepareStableGround(helper, origin.below(), 7);
        helper.getLevel().setBlock(
                origin.offset(2, 0, 2),
                Blocks.WATER.defaultBlockState(),
                2
        );
        helper.assertTrue(
                !LegacyStructuresFeature.placeVillageBuilding(
                        LegacyStructureKind.WIZARD_TOWER,
                        helper.getLevel(),
                        origin,
                        net.minecraft.world.level.block.Rotation.NONE,
                        RandomSource.create(14298541L)
                ),
                "Wizard tower spawned in water"
        );
        helper.succeed();
    }

    @GameTest(
            template = "empty",
            batch = "legacyStructureSitePolicy",
            timeoutTicks = 100
    )
    public static void obstructedVillageBuildingIsRejectedWithoutMutation(
            GameTestHelper helper
    ) {
        BlockPos origin = helper.absolutePos(new BlockPos(8, 8, 8));
        prepareStableGround(helper, origin.below(), 7);
        BlockPos obstruction = origin.offset(2, 3, 2);
        helper.getLevel().setBlock(
                obstruction,
                Blocks.STONE.defaultBlockState(),
                2
        );
        helper.assertTrue(
                !LegacyStructuresFeature.placeVillageBuilding(
                        LegacyStructureKind.WIZARD_TOWER,
                        helper.getLevel(),
                        origin,
                        net.minecraft.world.level.block.Rotation.NONE,
                        RandomSource.create(14298543L)
                ),
                "Wizard tower spawned through a solid obstruction"
        );
        helper.assertTrue(
                helper.getLevel().getBlockState(obstruction).is(Blocks.STONE),
                "Rejected structure mutated its obstruction"
        );
        helper.succeed();
    }

    @GameTest(
            template = "empty",
            batch = "legacyVillageRotation",
            timeoutTicks = 100
    )
    public static void villageBankerFacesRotatedRoad(
            GameTestHelper helper
    ) {
        BlockPos origin = helper.absolutePos(new BlockPos(8, 8, 8));
        prepareStableGround(helper, origin.below(), 7);
        var rotation =
                net.minecraft.world.level.block.Rotation.CLOCKWISE_90;
        helper.assertTrue(
                LegacyStructuresFeature.placeVillageBuilding(
                        LegacyStructureKind.BANKER_HOME,
                        helper.getLevel(),
                        origin,
                        rotation,
                        RandomSource.create(14298542L)
                ),
                "Rotated banker home placement failed"
        );
        BlockState door = helper.getLevel().getBlockState(
                origin.offset(0, 1, 1)
        );
        helper.assertTrue(
                door.is(Blocks.OAK_DOOR)
                        && door.getValue(DoorBlock.FACING)
                        == Direction.EAST,
                "Banker door did not rotate toward its village road"
        );
        helper.succeed();
    }

    @GameTest(
            template = "empty",
            batch = "legacyStructureHilltop",
            timeoutTicks = 100
    )
    public static void registeredHilltopPlacesCompletePayload(
            GameTestHelper helper
    ) {
        BlockPos center = helper.absolutePos(new BlockPos(8, 8, 8));
        prepareStableGround(helper, center.below(), 5);
        helper.assertTrue(
                LegacyStructuresFeature.placeRegistered(
                        LegacyStructureKind.HILLTOP_STONES,
                        helper.getLevel(),
                        center,
                        RandomSource.create(14298533L)
                ),
                "Registered hilltop-stones placement failed"
        );

        helper.assertTrue(
                helper.getLevel().getBlockState(center)
                        .is(ModBlocks.OBSIDIAN_TILE.get()),
                "Hilltop center tile was not placed"
        );
        helper.assertTrue(
                helper.getLevel().getBlockState(center.above())
                        .is(Blocks.CHEST),
                "Hilltop chest was not placed"
        );
        helper.assertTrue(
                helper.getLevel().getBlockEntity(center.below())
                        instanceof SpawnerBlockEntity,
                "Hilltop wisp spawner was not placed"
        );
        assertSpawner(
                helper,
                center.below(),
                ModEntities.WISP.getId().toString()
        );
        helper.assertTrue(
                helper.getLevel().getBlockEntity(
                        center.above(HilltopStonesGeneration.NODE_HEIGHT)
                ) instanceof AuraNodeBlockEntity,
                "Hilltop dark node was not placed"
        );
        assertClearAroundHilltopPillars(helper, center.below());
        assertCardinalSupport(helper, center.below(), 3);
        helper.succeed();
    }

    @GameTest(
            template = "empty",
            batch = "legacyStructureRing",
            timeoutTicks = 100
    )
    public static void registeredEldritchRingHasClearPillarsAndDeepSupport(
            GameTestHelper helper
    ) {
        BlockPos requestedAir = helper.absolutePos(new BlockPos(8, 8, 8));
        BlockPos floor = requestedAir.below();
        prepareStableGround(helper, floor, 5);
        String footprintFailure =
                StructureSitePolicy.drySupportedFloorFailure(
                        helper.getLevel(),
                        requestedAir.offset(-3, 0, -3),
                        7,
                        7,
                        net.minecraft.world.level.block.Rotation.NONE
                );
        helper.assertTrue(footprintFailure == null, footprintFailure);
        String envelopeFailure =
                StructureSitePolicy.dryReplaceableClearanceFailure(
                        helper.getLevel(),
                        floor.offset(-4, 1, -4),
                        9,
                        9,
                        8,
                        net.minecraft.world.level.block.Rotation.NONE
                );
        helper.assertTrue(envelopeFailure == null, envelopeFailure);
        helper.assertTrue(
                LegacyStructuresFeature.placeRegistered(
                        LegacyStructureKind.ELDRITCH_RING,
                        helper.getLevel(),
                        requestedAir,
                        RandomSource.create(14298535L)
                ),
                "Registered eldritch-ring placement failed"
        );
        assertCardinalSupport(helper, floor, 3);
        for (int y = 1; y <= 7; y++) {
            helper.assertTrue(
                    helper.getLevel().getBlockState(
                            floor.offset(4, y, 0)
                    ).isAir(),
                    "Eldritch ring east clearance was obstructed at y=" + y
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(
                            floor.offset(0, y, -4)
                    ).isAir(),
                    "Eldritch ring north clearance was obstructed at y=" + y
            );
        }
        helper.succeed();
    }

    @GameTest(
            template = "empty",
            batch = "legacyStructureWizardStep",
            timeoutTicks = 100
    )
    public static void wizardDoorStepFacesAwayFromItsDoor(
            GameTestHelper helper
    ) {
        BlockPos towerBase = helper.absolutePos(new BlockPos(8, 8, 8));
        prepareStableGround(helper, towerBase.below(), 7);
        BlockPos origin = towerBase.offset(-3, 0, -3);
        String floorFailure = StructureSitePolicy.drySupportedFloorFailure(
                helper.getLevel(),
                origin,
                7,
                6,
                net.minecraft.world.level.block.Rotation.NONE
        );
        helper.assertTrue(floorFailure == null, floorFailure);
        String clearanceFailure =
                StructureSitePolicy.dryReplaceableClearanceFailure(
                        helper.getLevel(),
                        origin,
                        7,
                        6,
                        12,
                        net.minecraft.world.level.block.Rotation.NONE
                );
        helper.assertTrue(clearanceFailure == null, clearanceFailure);

        helper.assertTrue(
                LegacyStructuresFeature.placeRegistered(
                        LegacyStructureKind.WIZARD_TOWER,
                        helper.getLevel(),
                        towerBase,
                        RandomSource.create(14298537L)
                ),
                "Wizard tower placement failed"
        );
        assertSouthFacingStep(helper, towerBase.offset(0, 0, -3));
        helper.succeed();
    }

    @GameTest(
            template = "empty",
            batch = "legacyStructureBankerStep",
            timeoutTicks = 100
    )
    public static void bankerDoorStepFacesAwayFromItsDoor(
            GameTestHelper helper
    ) {
        BlockPos bankerBase = helper.absolutePos(new BlockPos(8, 8, 8));
        prepareStableGround(helper, bankerBase.below(), 7);
        helper.assertTrue(
                LegacyStructuresFeature.placeRegistered(
                        LegacyStructureKind.BANKER_HOME,
                        helper.getLevel(),
                        bankerBase,
                        RandomSource.create(14298539L)
                ),
                "Banker home placement failed"
        );
        assertSouthFacingStep(helper, bankerBase.offset(0, 0, -3));
        helper.succeed();
    }

    private static void prepareStableGround(
            GameTestHelper helper,
            BlockPos surfaceCenter,
            int radius
    ) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int height = 1; height <= 16; height++) {
                    helper.getLevel().setBlock(
                            surfaceCenter.offset(x, height, z),
                            Blocks.AIR.defaultBlockState(),
                            2
                    );
                }
                for (int depth = 0;
                        depth
                                < HilltopStonesGeneration
                                        .REQUIRED_SUPPORT_DEPTH;
                        depth++) {
                    helper.getLevel().setBlock(
                            surfaceCenter.offset(x, -depth, z),
                            Blocks.STONE.defaultBlockState(),
                            2
                    );
                }
            }
        }
    }

    private static void assertCardinalSupport(
            GameTestHelper helper,
            BlockPos floor,
            int radius
    ) {
        for (int[] sample
                : HilltopStonesGeneration.cardinalSupportSamples()) {
            for (int depth = 0;
                    depth < HilltopStonesGeneration.REQUIRED_SUPPORT_DEPTH;
                    depth++) {
                BlockPos position = floor.offset(
                        sample[0],
                        -depth,
                        sample[1]
                );
                var state = helper.getLevel().getBlockState(position);
                helper.assertTrue(
                        !state.isAir() && !state.getCollisionShape(
                                helper.getLevel(),
                                position
                        ).isEmpty(),
                        "Missing deep support at " + position
                                + ": " + state
                );
            }
        }
    }

    private static void assertClearAroundHilltopPillars(
            GameTestHelper helper,
            BlockPos floor
    ) {
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                if (!HilltopStonesGeneration.isPillarPosition(x, z)) {
                    continue;
                }
                for (int y = 1; y <= 4; y++) {
                    for (int[] side : new int[][]{
                            {-1, 0}, {1, 0}, {0, -1}, {0, 1}
                    }) {
                        int adjacentX = x + side[0];
                        int adjacentZ = z + side[1];
                        if (HilltopStonesGeneration.isPillarPosition(
                                adjacentX,
                                adjacentZ
                        )) {
                            continue;
                        }
                        helper.assertTrue(
                                helper.getLevel().getBlockState(
                                        floor.offset(adjacentX, y, adjacentZ)
                                ).isAir(),
                                "Hilltop pillar clearance was obstructed"
                        );
                    }
                }
            }
        }
    }

    private static void assertSouthFacingStep(
            GameTestHelper helper,
            BlockPos step
    ) {
        helper.assertTrue(
                helper.getLevel().getBlockState(step)
                        .is(Blocks.COBBLESTONE_STAIRS),
                "Door step missing at " + step + ": "
                        + helper.getLevel().getBlockState(step)
        );
        helper.assertTrue(
                helper.getLevel().getBlockState(step)
                        .getValue(StairBlock.FACING)
                        == net.minecraft.core.Direction.SOUTH,
                "Door step did not rotate 180 degrees at " + step
        );
    }

    private static void assertSpawner(
            GameTestHelper helper,
            BlockPos position,
            String expectedEntity
    ) {
        helper.assertTrue(
                helper.getLevel().getBlockEntity(position)
                        instanceof SpawnerBlockEntity,
                "Spawner missing at " + position
        );
        SpawnerBlockEntity spawner = (SpawnerBlockEntity)
                helper.getLevel().getBlockEntity(position);
        CompoundTag tag = spawner.getSpawner().save(new CompoundTag());
        String actualEntity = tag.getCompound("SpawnData")
                .getCompound("entity")
                .getString("id");
        helper.assertTrue(
                expectedEntity.equals(actualEntity),
                "Expected spawner " + expectedEntity + " at " + position
                        + ", got " + actualEntity
        );
    }
}
