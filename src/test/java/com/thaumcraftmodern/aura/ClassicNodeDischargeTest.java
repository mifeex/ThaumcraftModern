package com.thaumcraftmodern.aura;

import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassicNodeDischargeTest {
    @Test
    void strongerNodeStealsCurrentVisFromWeakerNode() {
        AuraNodeState predator = node(
                Map.of("aer", 99),
                Map.of("aer", 100)
        );
        AuraNodeState victim = node(
                Map.of("aer", 10),
                Map.of("aer", 10)
        );

        ClassicNodeDischarge.Result result =
                ClassicNodeDischarge.tryTransfer(
                        predator.snapshot(),
                        victim.snapshot(),
                        RandomSource.create(1L),
                        false
                ).orElseThrow();

        assertEquals(100, result.predatorCurrent().get("aer"));
        assertEquals(9, result.victimCurrent().get("aer"));
        assertEquals(100, result.predatorMaximum().get("aer"));
        assertEquals(10, result.victimMaximum().get("aer"));
    }

    @Test
    void stolenUnknownAspectCanGrowPredatorBaseAndErodeVictim() {
        AuraNodeState predator = node(
                Map.of("aer", 100),
                Map.of("aer", 100)
        );
        AuraNodeState victim = node(
                Map.of("fames", 10),
                Map.of("fames", 10)
        );

        ClassicNodeDischarge.Result result =
                ClassicNodeDischarge.tryTransfer(
                        predator.snapshot(),
                        victim.snapshot(),
                        new ZeroRandomSource(),
                        true
                ).orElseThrow();

        assertEquals(1, result.predatorMaximum().get("fames"));
        assertEquals(0, result.predatorCurrent().get("fames"));
        assertEquals(9, result.victimCurrent().get("fames"));
        assertEquals(9, result.victimMaximum().get("fames"));
    }

    @Test
    void equalOrStrongerVictimCannotBeDrained() {
        AuraNodeState predator = node(
                Map.of("aer", 10),
                Map.of("aer", 10)
        );
        AuraNodeState victim = node(
                Map.of("aer", 10),
                Map.of("aer", 10)
        );
        assertTrue(ClassicNodeDischarge.tryTransfer(
                predator.snapshot(),
                victim.snapshot(),
                RandomSource.create(2L),
                false
        ).isEmpty());
    }

    private static AuraNodeState node(
            Map<String, Integer> current,
            Map<String, Integer> maximum
    ) {
        return AuraNodeState.withAspects(
                UUID.randomUUID(),
                AuraNodeType.NORMAL,
                AuraNodeModifier.NORMAL,
                current,
                maximum,
                0L
        );
    }

    private static final class ZeroRandomSource implements RandomSource {
        @Override
        public RandomSource fork() {
            return this;
        }

        @Override
        public net.minecraft.world.level.levelgen.PositionalRandomFactory
                forkPositional() {
            return new net.minecraft.world.level.levelgen.PositionalRandomFactory() {
                @Override
                public RandomSource at(int x, int y, int z) {
                    return ZeroRandomSource.this;
                }

                @Override
                public RandomSource fromHashOf(String value) {
                    return ZeroRandomSource.this;
                }

                @Override
                public void parityConfigString(
                        StringBuilder builder
                ) {
                }
            };
        }

        @Override
        public void setSeed(long seed) {
        }

        @Override
        public int nextInt() {
            return 0;
        }

        @Override
        public int nextInt(int bound) {
            return 0;
        }

        @Override
        public long nextLong() {
            return 0L;
        }

        @Override
        public boolean nextBoolean() {
            return false;
        }

        @Override
        public float nextFloat() {
            return 0.0F;
        }

        @Override
        public double nextDouble() {
            return 0.0D;
        }

        @Override
        public double nextGaussian() {
            return 0.0D;
        }
    }
}
