package com.thaumcraftmodern.essentia;

import com.thaumcraftmodern.world.block.entity.AlchemicalFurnaceBlockEntity;
import com.thaumcraftmodern.world.block.entity.ArcaneAlembicBlockEntity;
import com.thaumcraftmodern.world.block.entity.EssentiaJarBlockEntity;
import com.thaumcraftmodern.world.block.entity.EssentiaTubeBlockEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClassicEssentiaValuesTest {
    @Test
    void capacitiesAndCadenceMatchTc4235() {
        assertEquals(50, AlchemicalFurnaceBlockEntity.MAX_ESSENTIA);
        assertEquals(4, AlchemicalFurnaceBlockEntity.MAX_ALEMBIC_STACK);
        assertEquals(32, ArcaneAlembicBlockEntity.CAPACITY);
        assertEquals(64, EssentiaJarBlockEntity.CAPACITY);
        assertEquals(32, EssentiaJarBlockEntity.SUCTION);
        assertEquals(64, EssentiaJarBlockEntity.FILTERED_SUCTION);
        assertEquals(2, EssentiaTubeBlockEntity.SUCTION_INTERVAL);
        assertEquals(5, EssentiaTubeBlockEntity.TRANSFER_INTERVAL);
        assertEquals(40, EssentiaTubeBlockEntity.VENT_TICKS);
    }
}
