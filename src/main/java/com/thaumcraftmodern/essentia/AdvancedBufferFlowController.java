package com.thaumcraftmodern.essentia;

import java.util.Objects;

/**
 * Pure state machine for the improved buffer. World access and transfers stay
 * in the block entity, while transition priority remains deterministic and
 * bootstrap-free for focused tests.
 */
public final class AdvancedBufferFlowController {
    public enum State {
        IDLE,
        SUPPLY,
        COOLDOWN,
        RETURN,
        RESERVE,
        BLOCKED
    }

    public record Snapshot(State state, int timer, int quietTicks) {
        public Snapshot {
            state = Objects.requireNonNull(state, "state");
            timer = Math.max(0, timer);
            quietTicks = Math.max(0, quietTicks);
        }

        public static Snapshot idle() {
            return new Snapshot(State.IDLE, 0, 0);
        }
    }

    public record Signals(
            boolean activeConsumer,
            boolean mainPathReversed,
            boolean returnedEssentia,
            boolean reserveAccepts
    ) {
    }

    private AdvancedBufferFlowController() {
    }

    public static Snapshot advance(
            Snapshot current,
            Signals signals,
            int cooldownTicks
    ) {
        Objects.requireNonNull(current, "current");
        Objects.requireNonNull(signals, "signals");
        if (!signals.mainPathReversed()) {
            return signals.activeConsumer()
                    ? state(State.SUPPLY) : state(State.IDLE);
        }
        if (!signals.returnedEssentia()) return state(State.IDLE);
        return signals.reserveAccepts()
                ? state(State.RESERVE) : state(State.BLOCKED);
    }

    private static Snapshot state(State state) {
        return new Snapshot(state, 0, 0);
    }
}
