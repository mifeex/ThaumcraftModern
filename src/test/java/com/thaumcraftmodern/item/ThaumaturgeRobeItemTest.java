package com.thaumcraftmodern.item;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class ThaumaturgeRobeItemTest {
    @Test
    void robePiecesKeepClassicColorArmorValuesAndVisDiscounts()
            throws Exception {
        String item = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/item/ThaumaturgeRobeItem.java"
        ));
        String material = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/item/ThaumaturgeRobeArmorMaterial.java"
        ));

        assertTrue(item.contains("DEFAULT_COLOR = 0x6A3880"));
        assertTrue(item.contains("getType() == Type.BOOTS ? 1 : 2"));
        assertTrue(item.contains("implements DyeableLeatherItem, VisDiscountGear"));
        assertTrue(material.contains("case BOOTS, HELMET -> 1"));
        assertTrue(material.contains("case LEGGINGS -> 2"));
        assertTrue(material.contains("case CHESTPLATE -> 3"));
    }
}
