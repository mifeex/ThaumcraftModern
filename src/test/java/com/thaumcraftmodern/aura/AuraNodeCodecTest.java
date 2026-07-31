package com.thaumcraftmodern.aura;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuraNodeCodecTest {
    @Test
    void roundTripPreservesIdentityTypeModifierCurrentMaximumAndRevision() {
        EnumMap<PrimalAspect, Integer> current =
                new EnumMap<>(PrimalAspect.class);
        EnumMap<PrimalAspect, Integer> maximum =
                new EnumMap<>(PrimalAspect.class);
        int value = 7;
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            current.put(aspect, value);
            maximum.put(aspect, value + 40);
            value += 3;
        }
        AuraNodeState original = new AuraNodeState(
                UUID.fromString("cfa11e2e-ded4-4b18-8cd7-9fce43b49784"),
                AuraNodeType.NORMAL,
                AuraNodeModifier.NORMAL,
                current,
                maximum,
                12L
        );

        CompoundTag encoded = AuraNodeCodec.encode(original);
        AuraNodeState restored = AuraNodeCodec.decode(encoded);

        assertEquals(AuraNodeCodec.SERIAL_VERSION, encoded.getInt("version"));
        assertEquals(original.snapshot(), restored.snapshot());
    }

    @Test
    void invalidOrFutureDataIsRejectedWithoutPartialState() {
        CompoundTag future = AuraNodeCodec.encode(AuraNodeFactory.newWorldNode());
        future.putInt("version", 99);

        assertThrows(IllegalArgumentException.class, () -> AuraNodeCodec.decode(future));

        AuraNodeState fallback = AuraNodeFactory.ordinary(
                UUID.fromString("015aeb3c-b0ac-4df2-91e7-f8674a9fab43")
        );
        AuraNodeCodec.DecodeResult recovered = AuraNodeCodec.decodeOrRecover(
                future,
                () -> fallback
        );
        assertTrue(recovered.recovered());
        assertFalse(recovered.diagnostic().isBlank());
        assertEquals(fallback.snapshot(), recovered.state().snapshot());
    }

    @Test
    void stateRejectsMissingPrimalsAndCurrentAboveMaximum() {
        Map<PrimalAspect, Integer> incomplete = new EnumMap<>(PrimalAspect.class);
        incomplete.put(PrimalAspect.AER, 1);
        assertThrows(
                IllegalArgumentException.class,
                () -> new AuraNodeState(
                        UUID.randomUUID(),
                        AuraNodeType.NORMAL,
                        AuraNodeModifier.NORMAL,
                        incomplete,
                        PrimalVis.uniform(10),
                        0L
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new AuraNodeState(
                        UUID.randomUUID(),
                        AuraNodeType.NORMAL,
                        AuraNodeModifier.NORMAL,
                        PrimalVis.uniform(11),
                        PrimalVis.uniform(10),
                        0L
                )
        );
    }
}
