package com.thaumcraftmodern.aura;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyUniformDarkNodeMigrationTest {
    @Test
    void exactFormerStructurePlaceholderIsRecognized() {
        AuraNodeState legacy = AuraNodeFactory.typed(
                UUID.randomUUID(),
                AuraNodeType.DARK,
                AuraNodeModifier.NORMAL,
                100
        );

        assertTrue(LegacyUniformDarkNodeMigration.matches(legacy.snapshot()));
        legacy.replaceAspects(
                0L,
                Map.of("aer", 99, "terra", 100, "ignis", 100,
                        "aqua", 100, "ordo", 100, "perditio", 100),
                legacy.snapshot().aspectsMaximum()
        );
        assertFalse(LegacyUniformDarkNodeMigration.matches(legacy.snapshot()));
    }

    @Test
    void replacementPreservesIdentityAndRevisionButUsesRandomPools() {
        UUID id = UUID.randomUUID();
        AuraNodeState legacy = AuraNodeFactory.typed(
                id,
                AuraNodeType.DARK,
                AuraNodeModifier.NORMAL,
                100
        );
        AuraNodeState generated = AuraNodeState.withAspects(
                UUID.randomUUID(),
                AuraNodeType.DARK,
                AuraNodeModifier.PALE,
                Map.of("aer", 31, "tenebrae", 19),
                Map.of("aer", 31, "tenebrae", 19),
                0L
        );

        AuraNodeState.Snapshot replacement =
                LegacyUniformDarkNodeMigration.replacement(
                        legacy.snapshot(),
                        generated.snapshot()
                ).snapshot();

        assertEquals(id, replacement.nodeId());
        assertEquals(legacy.revision(), replacement.revision());
        assertEquals(AuraNodeType.DARK, replacement.type());
        assertEquals(AuraNodeModifier.PALE, replacement.modifier());
        assertEquals(Map.of("aer", 31, "tenebrae", 19),
                replacement.aspectsCurrent());
    }
}
