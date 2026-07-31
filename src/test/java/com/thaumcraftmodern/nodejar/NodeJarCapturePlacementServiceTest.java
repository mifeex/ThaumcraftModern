package com.thaumcraftmodern.nodejar;

import com.thaumcraftmodern.aura.AuraNodeFactory;
import com.thaumcraftmodern.aura.AuraNodeState;
import com.thaumcraftmodern.aura.OperationNonceGuard;
import com.thaumcraftmodern.aura.PrimalAspect;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NodeJarCapturePlacementServiceTest {
    @Test
    void captureAcceptsAnyHeldCastingToolAndUsesExactClassicBaseCost() {
        AuraNodeState node = AuraNodeFactory.newWorldNode();
        BlockPos position = new BlockPos(0, 64, 0);
        NodeJarStructureTest.FakeWorld structure =
                NodeJarStructureTest.completeWorld(position, node.nodeId());
        FakeCaptureWorld world = new FakeCaptureWorld(node);
        FakePayment invalidItem = new FakePayment(false, true);
        NodeJarCaptureService service = new NodeJarCaptureService(
                new OperationNonceGuard(),
                () -> 0.99D
        );

        assertEquals(
                NodeJarCaptureService.Status.CASTING_TOOL_REQUIRED,
                service.capture(
                        request(node, position, UUID.randomUUID()),
                        structure,
                        world,
                        invalidItem,
                        new NodeJarLedger()
                ).status()
        );
        assertFalse(world.captured);

        FakePayment castingTool = new FakePayment(true, true);
        NodeJarLedger ledger = new NodeJarLedger();
        NodeJarCaptureService.Result result = service.capture(
                request(node, position, UUID.randomUUID()),
                structure,
                world,
                castingTool,
                ledger
        );
        assertEquals(NodeJarCaptureService.Status.CAPTURED, result.status());
        assertEquals(NodeJarCost.BASE, castingTool.requestedCost);
        assertTrue(castingTool.committed);
        NodeJarData captured = result.capturedData().orElse(null);
        assertNotNull(captured);
        assertEquals(NodeJarLedger.Status.PLACED, ledger.status(captured.payloadId()));
        assertTrue(ledger.returnToJar(captured, request(
                node,
                position,
                UUID.randomUUID()
        ).placementKey()));
        assertEquals(NodeJarLedger.Status.JARRED, ledger.status(captured.payloadId()));
    }

    @Test
    void failedWorldCaptureRollsBackReservedVisAndAllowsRetry() {
        AuraNodeState node = AuraNodeFactory.newWorldNode();
        BlockPos position = new BlockPos(0, 64, 0);
        NodeJarStructureTest.FakeWorld structure =
                NodeJarStructureTest.completeWorld(position, node.nodeId());
        FakeCaptureWorld world = new FakeCaptureWorld(node);
        world.allowCapture = false;
        FakePayment payment = new FakePayment(true, true);
        NodeJarCaptureService service = new NodeJarCaptureService(
                new OperationNonceGuard(),
                () -> 0.99D
        );
        UUID operation = UUID.randomUUID();

        assertEquals(
                NodeJarCaptureService.Status.WORLD_TRANSACTION_FAILED,
                service.capture(
                        request(node, position, operation),
                        structure,
                        world,
                        payment,
                        new NodeJarLedger()
                ).status()
        );
        assertTrue(payment.rolledBack);

        world.allowCapture = true;
        payment = new FakePayment(true, true);
        assertEquals(
                NodeJarCaptureService.Status.CAPTURED,
                service.capture(
                        request(node, position, operation),
                        structure,
                        world,
                        payment,
                        new NodeJarLedger()
                ).status()
        );
    }

    @Test
    void placedSurvivalPayloadCannotBeReplayed() {
        NodeJarData data = NodeJarFactory.captured(
                UUID.randomUUID(),
                AuraNodeFactory.newWorldNode()
        );
        NodeJarLedger ledger = new NodeJarLedger();
        assertTrue(ledger.registerCaptured(data));
        NodeJarPlacementService service = new NodeJarPlacementService(
                new OperationNonceGuard()
        );
        FakePlacementWorld world = new FakePlacementWorld();

        assertEquals(
                NodeJarPlacementService.Status.PLACED,
                service.place(
                        placementRequest(UUID.randomUUID()),
                        new FakeStack(data),
                        ledger,
                        world
                )
        );
        assertEquals(
                NodeJarPlacementService.Status.DUPLICATE_PAYLOAD,
                service.place(
                        placementRequest(UUID.randomUUID()),
                        new FakeStack(data),
                        ledger,
                        world
                )
        );
        assertEquals(1, world.placements);
    }

    private static NodeJarCaptureService.Request request(
            AuraNodeState node,
            BlockPos position,
            UUID operation
    ) {
        return new NodeJarCaptureService.Request(
                UUID.fromString("1d4a41d4-c51d-472a-b4e5-4d53e7bd3dcb"),
                operation,
                node.nodeId(),
                position,
                "minecraft:overworld@0,64,0",
                true,
                true,
                true,
                true,
                2.0D,
                6.0D
        );
    }

    private static NodeJarPlacementService.Request placementRequest(UUID operation) {
        return new NodeJarPlacementService.Request(
                UUID.fromString("fe8fc8e8-b078-4b29-a4b2-76b20f0cd5ca"),
                operation,
                new BlockPos(2, 64, 2),
                "minecraft:overworld@2,64,2",
                true,
                true,
                true,
                2.0D,
                6.0D
        );
    }

    private static final class FakePayment
            implements NodeJarCaptureService.CastingToolPayment {
        private final boolean castingTool;
        private final boolean enough;
        private Map<PrimalAspect, Integer> requestedCost;
        private boolean committed;
        private boolean rolledBack;

        private FakePayment(boolean castingTool, boolean enough) {
            this.castingTool = castingTool;
            this.enough = enough;
        }

        @Override
        public boolean isStillHeldCastingTool() {
            return castingTool;
        }

        @Override
        public Optional<NodeJarCaptureService.PaymentReservation> reserve(
                Map<PrimalAspect, Integer> baseCost
        ) {
            requestedCost = Map.copyOf(baseCost);
            if (!enough) {
                return Optional.empty();
            }
            return Optional.of(new NodeJarCaptureService.PaymentReservation() {
                @Override
                public void commit() {
                    committed = true;
                }

                @Override
                public void rollback() {
                    rolledBack = true;
                }
            });
        }
    }

    private static final class FakeCaptureWorld
            implements NodeJarCaptureService.CaptureWorld {
        private final AuraNodeState node;
        private boolean allowCapture = true;
        private boolean captured;

        private FakeCaptureWorld(AuraNodeState node) {
            this.node = node;
        }

        @Override
        public Optional<AuraNodeState> snapshotNode(BlockPos position) {
            return Optional.of(node.copy());
        }

        @Override
        public boolean captureAtomically(
                BlockPos nodePosition,
                java.util.List<BlockPos> consumedMaterials,
                NodeJarData data
        ) {
            captured = allowCapture;
            return allowCapture;
        }

        @Override
        public void rollbackCapture(BlockPos nodePosition, NodeJarData data) {
            captured = false;
        }
    }

    private static final class FakeStack
            implements NodeJarPlacementService.JarStackReservation {
        private final NodeJarData data;
        private boolean available = true;

        private FakeStack(NodeJarData data) {
            this.data = data;
        }

        @Override
        public NodeJarData data() {
            return data;
        }

        @Override
        public boolean stillMatchesHeldStack() {
            return available;
        }

        @Override
        public boolean consumeOne() {
            if (!available) {
                return false;
            }
            available = false;
            return true;
        }
    }

    private static final class FakePlacementWorld
            implements NodeJarPlacementService.PlacementWorld {
        private int placements;

        @Override
        public boolean placeAtomically(BlockPos position, NodeJarData data) {
            placements++;
            return true;
        }

        @Override
        public void rollbackPlacement(BlockPos position, NodeJarData data) {
            placements--;
        }
    }
}
