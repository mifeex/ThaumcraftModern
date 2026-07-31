package com.thaumcraftmodern.nodejar;

import com.thaumcraftmodern.aura.AuraNodeState;
import com.thaumcraftmodern.aura.OperationNonceGuard;
import com.thaumcraftmodern.aura.PrimalAspect;
import net.minecraft.core.BlockPos;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleSupplier;

/**
 * Server-authoritative NODEJAR capture transaction.
 *
 * <p>The supplied adapter must represent the actually held casting tool. It
 * receives the exact base cost (70 of each primal); the shared vis service
 * applies cap, form, equipment, effect, and event modifiers.</p>
 */
public final class NodeJarCaptureService {
    private final OperationNonceGuard nonceGuard;
    private final DoubleSupplier random;

    public NodeJarCaptureService(OperationNonceGuard nonceGuard) {
        this(nonceGuard, () -> ThreadLocalRandom.current().nextDouble());
    }

    public NodeJarCaptureService(
            OperationNonceGuard nonceGuard,
            DoubleSupplier random
    ) {
        this.nonceGuard = Objects.requireNonNull(nonceGuard, "nonceGuard");
        this.random = Objects.requireNonNull(random, "random");
    }

    public Result capture(
            Request request,
            NodeJarStructure.WorldView structureWorld,
            CaptureWorld captureWorld,
            CastingToolPayment wand,
            NodeJarSavedData savedData
    ) {
        Objects.requireNonNull(savedData, "savedData");
        Result result = capture(
                request,
                structureWorld,
                captureWorld,
                wand,
                savedData.ledger()
        );
        if (result.status() == Status.CAPTURED) {
            savedData.markLedgerChanged();
        }
        return result;
    }

    public Result capture(
            Request request,
            NodeJarStructure.WorldView structureWorld,
            CaptureWorld captureWorld,
            CastingToolPayment wand,
            NodeJarLedger ledger
    ) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(structureWorld, "structureWorld");
        Objects.requireNonNull(captureWorld, "captureWorld");
        Objects.requireNonNull(wand, "wand");
        Objects.requireNonNull(ledger, "ledger");

        Status validation = validate(request, wand);
        if (validation != Status.CAPTURED) {
            return Result.failure(validation);
        }
        NodeJarStructure.Validation structure = NodeJarStructure.validate(
                request.nodePosition(),
                request.expectedNodeId(),
                structureWorld
        );
        if (!structure.valid()) {
            return new Result(Status.INVALID_STRUCTURE, null, structure);
        }
        AuraNodeState node = captureWorld.snapshotNode(request.nodePosition()).orElse(null);
        if (node == null || !node.nodeId().equals(request.expectedNodeId())) {
            return Result.failure(Status.NODE_CHANGED);
        }
        if (!nonceGuard.tryBegin(request.actorId(), request.operationId())) {
            return Result.failure(Status.DUPLICATE_OPERATION);
        }

        PaymentReservation payment = wand.reserve(NodeJarCost.BASE).orElse(null);
        if (payment == null) {
            nonceGuard.release(request.actorId(), request.operationId());
            return Result.failure(Status.INSUFFICIENT_VIS);
        }

        UUID payloadId = UUID.nameUUIDFromBytes(
                ("nodejar:" + node.nodeId() + ":" + request.operationId())
                        .getBytes(StandardCharsets.UTF_8)
        );
        NodeJarData data = NodeJarFactory.captured(
                payloadId,
                NodeJarCaptureRules.prepareForJar(node, random)
        );
        boolean worldChanged = false;
        try {
            worldChanged = captureWorld.captureAtomically(
                    request.nodePosition(),
                    NodeJarStructure.materialPositions(request.nodePosition()),
                    data
            );
            if (!worldChanged) {
                payment.rollback();
                nonceGuard.release(request.actorId(), request.operationId());
                return Result.failure(Status.WORLD_TRANSACTION_FAILED);
            }
            if (!ledger.registerCaptured(data)) {
                captureWorld.rollbackCapture(request.nodePosition(), data);
                payment.rollback();
                nonceGuard.release(request.actorId(), request.operationId());
                return Result.failure(Status.DUPLICATE_PAYLOAD);
            }
            if (!ledger.claimPlacement(data, request.placementKey())) {
                ledger.removeCaptured(data);
                captureWorld.rollbackCapture(request.nodePosition(), data);
                payment.rollback();
                nonceGuard.release(request.actorId(), request.operationId());
                return Result.failure(Status.DUPLICATE_PAYLOAD);
            }
            payment.commit();
            captureWorld.commitCapture(request.nodePosition(), data);
            return new Result(Status.CAPTURED, data, structure);
        } catch (RuntimeException exception) {
            if (worldChanged) {
                captureWorld.rollbackCapture(request.nodePosition(), data);
            }
            if (!ledger.releasePlacedNode(data, request.placementKey())) {
                ledger.removeCaptured(data);
            }
            payment.rollback();
            nonceGuard.release(request.actorId(), request.operationId());
            throw exception;
        }
    }

    private static Status validate(Request request, CastingToolPayment wand) {
        if (!request.serverSide()) {
            return Status.NOT_SERVER;
        }
        if (!request.researchCompleted()) {
            return Status.RESEARCH_REQUIRED;
        }
        if (!request.sameDimension()) {
            return Status.WRONG_DIMENSION;
        }
        if (!request.nodeChunkLoaded()) {
            return Status.NODE_NOT_LOADED;
        }
        if (!Double.isFinite(request.distance())
                || request.distance() < 0.0D
                || request.distance() > request.maximumDistance()) {
            return Status.TOO_FAR;
        }
        if (!wand.isStillHeldCastingTool()) {
            return Status.CASTING_TOOL_REQUIRED;
        }
        return Status.CAPTURED;
    }

    public interface CastingToolPayment {
        boolean isStillHeldCastingTool();

        /**
         * Reserves the base cost atomically. The adapter applies the actual
         * wand cap modifier and returns empty without changing NBT when the
         * held casting tool cannot pay.
         */
        Optional<PaymentReservation> reserve(
                Map<PrimalAspect, Integer> baseCost
        );
    }

    public interface PaymentReservation {
        void commit();

        void rollback();
    }

    public interface CaptureWorld {
        Optional<AuraNodeState> snapshotNode(BlockPos position);

        /**
         * Must consume the validated shell and replace the node with one
         * jarred-node block as one server-thread transaction.
         */
        boolean captureAtomically(
                BlockPos nodePosition,
                java.util.List<BlockPos> consumedMaterials,
                NodeJarData data
        );

        /**
         * Restores the exact shell and node snapshot if a later transaction
         * stage fails.
         */
        void rollbackCapture(BlockPos nodePosition, NodeJarData data);

        default void commitCapture(BlockPos nodePosition, NodeJarData data) {
        }
    }

    public record Request(
            UUID actorId,
            UUID operationId,
            UUID expectedNodeId,
            BlockPos nodePosition,
            String placementKey,
            boolean serverSide,
            boolean researchCompleted,
            boolean sameDimension,
            boolean nodeChunkLoaded,
            double distance,
            double maximumDistance
    ) {
        public Request {
            actorId = Objects.requireNonNull(actorId, "actorId");
            operationId = Objects.requireNonNull(operationId, "operationId");
            expectedNodeId = Objects.requireNonNull(expectedNodeId, "expectedNodeId");
            nodePosition = Objects.requireNonNull(nodePosition, "nodePosition").immutable();
            placementKey = Objects.requireNonNull(placementKey, "placementKey");
            if (!Double.isFinite(maximumDistance) || maximumDistance < 0.0D) {
                throw new IllegalArgumentException("maximumDistance must be finite and non-negative");
            }
        }
    }

    public record Result(
            Status status,
            NodeJarData data,
            NodeJarStructure.Validation structure
    ) {
        public Result {
            status = Objects.requireNonNull(status, "status");
        }

        static Result failure(Status status) {
            return new Result(status, null, null);
        }

        public Optional<NodeJarData> capturedData() {
            return Optional.ofNullable(data);
        }
    }

    public enum Status {
        CAPTURED,
        DUPLICATE_OPERATION,
        DUPLICATE_PAYLOAD,
        NOT_SERVER,
        RESEARCH_REQUIRED,
        WRONG_DIMENSION,
        NODE_NOT_LOADED,
        TOO_FAR,
        CASTING_TOOL_REQUIRED,
        INSUFFICIENT_VIS,
        INVALID_STRUCTURE,
        NODE_CHANGED,
        WORLD_TRANSACTION_FAILED
    }
}
