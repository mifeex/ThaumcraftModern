package com.thaumcraftmodern.essentia;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class JarRedstoneSignalTest {
    @Test
    void usesRequestedJarFillLevels() {
        assertEquals(0, JarRedstoneSignal.forAmount(0));
        assertEquals(5, JarRedstoneSignal.forAmount(7));
        assertEquals(6, JarRedstoneSignal.forAmount(8));
        assertEquals(6, JarRedstoneSignal.forAmount(15));
        assertEquals(7, JarRedstoneSignal.forAmount(16));
        assertEquals(7, JarRedstoneSignal.forAmount(31));
        assertEquals(8, JarRedstoneSignal.forAmount(32));
        assertEquals(8, JarRedstoneSignal.forAmount(63));
        assertEquals(10, JarRedstoneSignal.forAmount(64));
    }

    @Test
    void voidJarUsesElevenOnlyForActiveOverflow() {
        assertEquals(10, JarRedstoneSignal.forVoidJar(64, false));
        assertEquals(11, JarRedstoneSignal.forVoidJar(64, true));
        assertEquals(8, JarRedstoneSignal.forVoidJar(63, true));
    }
}
