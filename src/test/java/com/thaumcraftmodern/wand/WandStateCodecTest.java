package com.thaumcraftmodern.wand;

import com.thaumcraftmodern.aura.PrimalAspect;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WandStateCodecTest {
    @Test
    void versionedRoundTripPreservesCompositionAndAllSixPrimals() {
        WandState original = state("wood", 100, 200, 300, 400, 500, 600);

        CompoundTag encoded = WandStateCodec.encode(original);
        WandState restored = WandStateCodec.decode(
                encoded,
                WandComponentCatalogTest.fixtures()
        );

        assertEquals(WandStateCodec.SERIAL_VERSION, encoded.getInt("version"));
        assertEquals(original, restored);
        assertEquals(6, restored.visCentivis().size());
    }

    @Test
    void copiedNbtDoesNotShareMutableVisState() {
        CompoundTag original = WandStateCodec.encode(
                state("wood", 100, 200, 300, 400, 500, 600)
        );
        CompoundTag copy = original.copy();

        copy.getCompound("vis").putInt("aer", 900);

        assertEquals(100, original.getCompound("vis").getInt("aer"));
        assertEquals(900, copy.getCompound("vis").getInt("aer"));
    }

    @Test
    void unknownVersionComponentAndIncompletePrimalsAreRejected() {
        WandComponentCatalog catalog = WandComponentCatalogTest.fixtures();
        CompoundTag future = WandStateCodec.encode(
                state("wood", 0, 0, 0, 0, 0, 0)
        );
        future.putInt("version", 99);
        assertThrows(
                IllegalArgumentException.class,
                () -> WandStateCodec.decode(future, catalog)
        );

        CompoundTag unknownRod = WandStateCodec.encode(
                state("wood", 0, 0, 0, 0, 0, 0)
        );
        unknownRod.putString("rod", "invented");
        assertThrows(
                IllegalArgumentException.class,
                () -> WandStateCodec.decode(unknownRod, catalog)
        );

        CompoundTag incomplete = WandStateCodec.encode(
                state("wood", 0, 0, 0, 0, 0, 0)
        );
        incomplete.getCompound("vis").remove("ordo");
        assertThrows(
                IllegalArgumentException.class,
                () -> WandStateCodec.decode(incomplete, catalog)
        );
    }

    @Test
    void codecEnforcesTheSelectedRodsCapacityWithoutClamping() {
        WandComponentCatalog catalog = WandComponentCatalogTest.fixtures();
        CompoundTag woodOverflow = WandStateCodec.encode(
                state("wood", 2501, 0, 0, 0, 0, 0)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> WandStateCodec.decode(woodOverflow, catalog)
        );

        CompoundTag silverwood = WandStateCodec.encode(
                state("silverwood", 10000, 10000, 10000, 10000, 10000, 10000)
        );
        assertEquals(
                10000,
                WandStateCodec.decode(silverwood, catalog)
                        .visCentivis(PrimalAspect.AER)
        );
    }

    private static WandState state(
            String rod,
            int aer,
            int terra,
            int ignis,
            int aqua,
            int ordo,
            int perditio
    ) {
        EnumMap<PrimalAspect, Integer> vis =
                new EnumMap<>(PrimalAspect.class);
        vis.put(PrimalAspect.AER, aer);
        vis.put(PrimalAspect.TERRA, terra);
        vis.put(PrimalAspect.IGNIS, ignis);
        vis.put(PrimalAspect.AQUA, aqua);
        vis.put(PrimalAspect.ORDO, ordo);
        vis.put(PrimalAspect.PERDITIO, perditio);
        return new WandState(
                WandStateCodec.SERIAL_VERSION,
                rod,
                "iron",
                vis
        );
    }
}
