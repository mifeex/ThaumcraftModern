package com.thaumcraftmodern.aura;

import java.util.Map;
import java.util.Objects;

/**
 * Recognizes and replaces only the former modern structure-node placeholder:
 * DARK + NORMAL with exactly six untouched 100/100 primal pools.
 */
public final class LegacyUniformDarkNodeMigration {
    private LegacyUniformDarkNodeMigration() {
    }

    public static boolean matches(AuraNodeState.Snapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        if (snapshot.type() != AuraNodeType.DARK
                || snapshot.modifier() != AuraNodeModifier.NORMAL
                || snapshot.aspectsCurrent().size()
                        != PrimalAspect.ordered().size()
                || !snapshot.aspectsCurrent().keySet().equals(
                        snapshot.aspectsMaximum().keySet()
                )) {
            return false;
        }
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            if (snapshot.aspectsCurrent().getOrDefault(aspect.id(), -1) != 100
                    || snapshot.aspectsMaximum().getOrDefault(
                            aspect.id(),
                            -1
                    ) != 100) {
                return false;
            }
        }
        return true;
    }

    public static AuraNodeState replacement(
            AuraNodeState.Snapshot legacy,
            AuraNodeState.Snapshot generated
    ) {
        if (!matches(legacy)) {
            throw new IllegalArgumentException(
                    "legacy node does not match the uniform DARK signature"
            );
        }
        Objects.requireNonNull(generated, "generated");
        if (generated.type() != AuraNodeType.DARK) {
            throw new IllegalArgumentException(
                    "generated replacement must remain DARK"
            );
        }
        return AuraNodeState.withAspects(
                legacy.nodeId(),
                AuraNodeType.DARK,
                generated.modifier(),
                generated.aspectsCurrent(),
                generated.aspectsMaximum(),
                legacy.revision()
        );
    }
}
