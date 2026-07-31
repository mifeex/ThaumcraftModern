package com.thaumcraftmodern.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FuriousZombieBehaviorTest {
    @Test
    void damageRaisesAngerByClassicIncrementUpToTwo() {
        assertEquals(1.1F, FuriousZombieBehavior.afterHit(1.0F), 0.0001F);
        assertEquals(2.0F, FuriousZombieBehavior.afterHit(1.95F), 0.0001F);
        assertEquals(2.0F, FuriousZombieBehavior.afterHit(2.0F), 0.0001F);
    }

    @Test
    void angerDecaysByClassicAmountButNeverBelowOne() {
        assertEquals(
                1.998F,
                FuriousZombieBehavior.afterTick(2.0F),
                0.0001F
        );
        assertEquals(
                1.0F,
                FuriousZombieBehavior.afterTick(1.001F),
                0.0001F
        );
    }

    @Test
    void angerAddsFiveAttackDamageAtMaximumSize() {
        assertEquals(7.0D, FuriousZombieBehavior.attackDamage(1.0F));
        assertEquals(12.0D, FuriousZombieBehavior.attackDamage(2.0F));
    }
}
