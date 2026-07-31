package com.thaumcraftmodern.nodejar;

import com.thaumcraftmodern.aura.AuraNodeModifier;
import com.thaumcraftmodern.aura.AuraNodeState;

import java.util.Objects;
import java.util.function.DoubleSupplier;

/**
 * Classic modifier degradation applied while enclosing a node.
 */
public final class NodeJarCaptureRules {
    public static final double MODIFIER_DEGRADATION_CHANCE = 0.75D;

    private NodeJarCaptureRules() {
    }

    public static AuraNodeState prepareForJar(
            AuraNodeState source,
            DoubleSupplier random
    ) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(random, "random");
        AuraNodeState.Snapshot snapshot = source.snapshot();
        double roll = random.getAsDouble();
        if (!Double.isFinite(roll) || roll < 0.0D || roll >= 1.0D) {
            throw new IllegalArgumentException("random roll must be in [0, 1)");
        }
        AuraNodeModifier modifier = roll < MODIFIER_DEGRADATION_CHANCE
                ? degrade(snapshot.modifier())
                : snapshot.modifier();
        return AuraNodeState.withAspects(
                snapshot.nodeId(),
                snapshot.type(),
                modifier,
                snapshot.aspectsCurrent(),
                snapshot.aspectsMaximum(),
                snapshot.revision()
        );
    }

    static AuraNodeModifier degrade(AuraNodeModifier modifier) {
        return switch (Objects.requireNonNull(modifier, "modifier")) {
            case BRIGHT -> AuraNodeModifier.NORMAL;
            case NORMAL -> AuraNodeModifier.PALE;
            case PALE, FADING -> AuraNodeModifier.FADING;
        };
    }
}
