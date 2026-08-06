package com.thaumcraftmodern.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.thaumcraftmodern.entity.GolemCoreType;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

final class ClassicGolemItemTooltipTest {
    @Test
    void blankGolemDoesNotPretendToHaveAHeart() {
        assertNull(PortableGolemCore.read(new CompoundTag()));
    }

    @Test
    void pickedUpGolemReportsItsExactStoredHeart() {
        for (GolemCoreType core : GolemCoreType.values()) {
            CompoundTag golem = portableGolemWithCore(core.legacyId());
            assertEquals(core, PortableGolemCore.read(golem));
        }
    }

    @Test
    void invalidLegacyCoreDoesNotProduceABogusTooltip() {
        assertNull(PortableGolemCore.read(portableGolemWithCore(127)));
    }

    private static CompoundTag portableGolemWithCore(int legacyId) {
        CompoundTag golem = new CompoundTag();
        CompoundTag data = new CompoundTag();
        data.putInt("Core", legacyId);
        golem.put("GolemData", data);
        return golem;
    }
}
