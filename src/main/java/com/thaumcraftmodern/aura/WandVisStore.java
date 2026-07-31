package com.thaumcraftmodern.aura;

import java.util.Map;

/**
 * Narrow adapter implemented by the wand subsystem. It lets node charging
 * remain independent from the concrete wand item/NBT implementation.
 */
public interface WandVisStore {
    /**
     * Number of store units represented by one whole node-vis point. TC4 wand
     * NBT uses centivis, so its adapter returns {@code 100}.
     */
    default int unitsPerNodeVis() {
        return 1;
    }

    Snapshot snapshot();

    boolean replaceCurrent(
            long expectedRevision,
            Map<PrimalAspect, Integer> nextCurrent
    );

    boolean restore(Snapshot snapshot, long expectedRevision);

    /**
     * The integration must return the object guarding snapshot/replace/restore.
     */
    default Object transactionLock() {
        return this;
    }

    record Snapshot(
            Map<PrimalAspect, Integer> current,
            Map<PrimalAspect, Integer> capacity,
            long revision
    ) {
        public Snapshot {
            current = PrimalVis.exact(current, "wand current");
            capacity = PrimalVis.exact(capacity, "wand capacity");
            if (revision < 0L) {
                throw new IllegalArgumentException("wand revision cannot be negative");
            }
            for (PrimalAspect aspect : PrimalAspect.ordered()) {
                if (current.get(aspect) > capacity.get(aspect)) {
                    throw new IllegalArgumentException(
                            aspect.id() + " wand vis exceeds capacity"
                    );
                }
            }
        }
    }
}
