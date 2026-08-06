package com.thaumcraftmodern.infusion;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class InfusionDurabilityFidelityTest {
    @Test
    void transfersTheCentralItemsAbsoluteDamage() {
        assertEquals(37, InfusionRecipeDefinition.transferredDamage(37, 500));
    }

    @Test
    void neverCreatesAnAlreadyBrokenResult() {
        assertEquals(99, InfusionRecipeDefinition.transferredDamage(140, 100));
    }

    @Test
    void rejectsNegativeOrNonDamageableValues() {
        assertEquals(0, InfusionRecipeDefinition.transferredDamage(-4, 100));
        assertEquals(0, InfusionRecipeDefinition.transferredDamage(12, 0));
    }
}
