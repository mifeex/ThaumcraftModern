package com.thaumcraftmodern.nodejar;

import com.thaumcraftmodern.aura.OperationNonceGuard;
import net.minecraft.core.BlockPos;

import java.util.Objects;
import java.util.UUID;

/**
 * Shared placement path for survival and deterministic creative jar stacks.
 */
public final class NodeJarPlacementService {
    private final OperationNonceGuard nonceGuard;

    public NodeJarPlacementService(OperationNonceGuard nonceGuard) {
        this.nonceGuard = Objects.requireNonNull(nonceGuard, "nonceGuard");
    }

    public Status place(
            Request request,
            JarStackReservation stack,
            NodeJarSavedData savedData,
            PlacementWorld world
    ) {
        Objects.requireNonNull(savedData, "savedData");
        Status status = place(request, stack, savedData.ledger(), world);
        if (status == Status.PLACED) {
            savedData.markLedgerChanged();
        }
        return status;
    }

    public Status place(
            Request request,
            JarStackReservation stack,
            NodeJarLedger ledger,
            PlacementWorld world
    ) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(stack, "stack");
        Objects.requireNonNull(ledger, "ledger");
        Objects.requireNonNull(world, "world");

        Status validation = validate(request, stack);
        if (validation != Status.PLACED) {
            return validation;
        }
        if (!nonceGuard.tryBegin(request.actorId(), request.operationId())) {
            return Status.DUPLICATE_OPERATION;
        }

        NodeJarData data = stack.data();
        String placementKey = request.placementKey();
        if (!ledger.claimPlacement(data, placementKey)) {
            nonceGuard.release(request.actorId(), request.operationId());
            return Status.DUPLICATE_PAYLOAD;
        }

        boolean worldChanged = false;
        try {
            worldChanged = world.placeAtomically(request.position(), data);
            if (!worldChanged) {
                ledger.rollbackPlacement(data, placementKey);
                nonceGuard.release(request.actorId(), request.operationId());
                return Status.WORLD_TRANSACTION_FAILED;
            }
            if (!stack.consumeOne()) {
                world.rollbackPlacement(request.position(), data);
                ledger.rollbackPlacement(data, placementKey);
                nonceGuard.release(request.actorId(), request.operationId());
                return Status.STACK_CHANGED;
            }
            world.commitPlacement(request.position(), data);
            return Status.PLACED;
        } catch (RuntimeException exception) {
            if (worldChanged) {
                world.rollbackPlacement(request.position(), data);
            }
            ledger.rollbackPlacement(data, placementKey);
            nonceGuard.release(request.actorId(), request.operationId());
            throw exception;
        }
    }

    private static Status validate(Request request, JarStackReservation stack) {
        if (!request.serverSide()) {
            return Status.NOT_SERVER;
        }
        if (!request.targetChunkLoaded()) {
            return Status.CHUNK_NOT_LOADED;
        }
        if (!request.targetReplaceable()) {
            return Status.TARGET_BLOCKED;
        }
        if (!Double.isFinite(request.distance())
                || request.distance() < 0.0D
                || request.distance() > request.maximumDistance()) {
            return Status.TOO_FAR;
        }
        if (!stack.stillMatchesHeldStack()) {
            return Status.STACK_CHANGED;
        }
        return Status.PLACED;
    }

    public interface JarStackReservation {
        NodeJarData data();

        boolean stillMatchesHeldStack();

        /**
         * Survival consumes one exact matching stack. Creative returns true
         * without decrementing, but follows this same method.
         */
        boolean consumeOne();
    }

    public interface PlacementWorld {
        boolean placeAtomically(BlockPos position, NodeJarData data);

        void rollbackPlacement(BlockPos position, NodeJarData data);

        default void commitPlacement(BlockPos position, NodeJarData data) {
        }
    }

    public record Request(
            UUID actorId,
            UUID operationId,
            BlockPos position,
            String placementKey,
            boolean serverSide,
            boolean targetChunkLoaded,
            boolean targetReplaceable,
            double distance,
            double maximumDistance
    ) {
        public Request {
            actorId = Objects.requireNonNull(actorId, "actorId");
            operationId = Objects.requireNonNull(operationId, "operationId");
            position = Objects.requireNonNull(position, "position").immutable();
            placementKey = Objects.requireNonNull(placementKey, "placementKey").trim();
            if (placementKey.isEmpty()) {
                throw new IllegalArgumentException("placementKey cannot be blank");
            }
            if (!Double.isFinite(maximumDistance) || maximumDistance < 0.0D) {
                throw new IllegalArgumentException("maximumDistance must be finite and non-negative");
            }
        }
    }

    public enum Status {
        PLACED,
        DUPLICATE_OPERATION,
        DUPLICATE_PAYLOAD,
        NOT_SERVER,
        CHUNK_NOT_LOADED,
        TARGET_BLOCKED,
        TOO_FAR,
        STACK_CHANGED,
        WORLD_TRANSACTION_FAILED
    }
}
