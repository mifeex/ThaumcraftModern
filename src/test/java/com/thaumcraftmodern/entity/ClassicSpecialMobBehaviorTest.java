package com.thaumcraftmodern.entity;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ClassicSpecialMobBehaviorTest {
    private static final Path MOB_SOURCE = Path.of(
            "src/main/java/com/thaumcraftmodern/entity/"
                    + "LegacyThaumcraftMob.java"
    );

    @Test
    void fixedAttributesMatchTc4() {
        assertKind(LegacyMobKind.WISP, 22.0D, 3.0D, 0.20D);
        assertKind(LegacyMobKind.FIREBAT, 5.0D, 1.0D, 0.35D);
        assertKind(LegacyMobKind.MIND_SPIDER, 1.0D, 1.0D, 0.30D);
        assertKind(LegacyMobKind.THAUMIC_SLIME, 1.0D, 1.0D, 0.24D);
    }

    @Test
    void dynamicAndAttackBehaviorsRemainPresent() throws Exception {
        String source = Files.readString(MOB_SOURCE);
        assertTrue(source.contains("setThaumicSlimeSize"));
        assertTrue(source.contains("spitThaumicSlime"));
        assertTrue(source.contains("mergeWithNearbySlime"));
        assertTrue(source.contains(
                "case THAUMIC_SLIME -> SoundEvents.SLIME_SQUISH"
        ));
        assertTrue(source.contains("class WispZapGoal"));
        assertTrue(source.contains("new WispZapPacket("));
        assertTrue(!source.substring(
                source.indexOf("private void renderWispZap"),
                source.indexOf(
                        "private void spawnFirebatParticle",
                        source.indexOf("private void renderWispZap")
                )
        ).contains("sendParticles"));
        assertTrue(source.contains("class FirebatAttackGoal"));
        assertTrue(source.contains("target.setSecondsOnFire(2)"));
        assertTrue(source.contains("tickCount > 1200"));
        assertTrue(source.contains("DamageTypeTags.IS_EXPLOSION"));
    }

    private static void assertKind(
            LegacyMobKind kind,
            double health,
            double damage,
            double speed
    ) {
        assertEquals(health, kind.health());
        assertEquals(damage, kind.damage());
        assertEquals(speed, kind.speed());
    }
}
