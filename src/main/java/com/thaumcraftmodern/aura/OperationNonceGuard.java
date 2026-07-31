package com.thaumcraftmodern.aura;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Bounded server-side replay guard for mutating gameplay requests.
 */
public final class OperationNonceGuard {
    public static final int DEFAULT_CAPACITY = 2_048;

    private final int capacity;
    private final LinkedHashMap<Key, Boolean> completed;

    public OperationNonceGuard() {
        this(DEFAULT_CAPACITY);
    }

    public OperationNonceGuard(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.capacity = capacity;
        this.completed = new LinkedHashMap<>();
    }

    public synchronized boolean tryBegin(UUID actorId, UUID operationId) {
        Key key = new Key(
                Objects.requireNonNull(actorId, "actorId"),
                Objects.requireNonNull(operationId, "operationId")
        );
        if (completed.putIfAbsent(key, Boolean.TRUE) != null) {
            return false;
        }
        trim();
        return true;
    }

    /**
     * Validation/transaction failures may be retried with the same nonce.
     */
    public synchronized void release(UUID actorId, UUID operationId) {
        completed.remove(new Key(actorId, operationId));
    }

    public synchronized int size() {
        return completed.size();
    }

    private void trim() {
        while (completed.size() > capacity) {
            Map.Entry<Key, Boolean> eldest = completed.entrySet().iterator().next();
            completed.remove(eldest.getKey());
        }
    }

    private record Key(UUID actorId, UUID operationId) {
    }
}
