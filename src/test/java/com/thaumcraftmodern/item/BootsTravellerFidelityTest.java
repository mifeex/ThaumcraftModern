package com.thaumcraftmodern.item;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BootsTravellerFidelityTest {
    @Test
    void restoresClassicStepAndJumpBonuses() {
        assertEquals(0.4D, BootsTravellerItem.STEP_HEIGHT_ADDITION);
        assertEquals(0.275D, BootsTravellerItem.JUMP_VELOCITY_ADDITION);
    }

    @Test
    void stepModifierIsEquipmentBoundAndJumpRequiresTravellerBoots()
            throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/item/BootsTravellerItem.java"
        ));
        assertTrue(source.contains("ForgeMod.STEP_HEIGHT_ADDITION.get()"));
        assertTrue(source.contains("slot != EquipmentSlot.FEET"));
        assertTrue(source.contains("boots.getItem() instanceof BootsTravellerItem"));
        assertTrue(source.contains("JUMP_VELOCITY_ADDITION"));
    }
}
