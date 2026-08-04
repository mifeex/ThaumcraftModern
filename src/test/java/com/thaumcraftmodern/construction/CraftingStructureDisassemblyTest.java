package com.thaumcraftmodern.construction;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CraftingStructureDisassemblyTest {
    private static String source(String relative) throws IOException {
        return Files.readString(Path.of("src/main/java/com/thaumcraftmodern/", relative));
    }

    @Test
    void everyWandBuiltCraftingStructureHooksTheSharedDisassemblyPath() throws IOException {
        assertTrue(source("world/block/ClassicPartBlock.java")
                .contains("CraftingStructureDisassembly.partRemoved"));
        assertTrue(source("world/block/InfusionPillarBlock.java")
                .contains("CraftingStructureDisassembly.partRemoved"));
        assertTrue(source("world/block/RunicMatrixBlock.java")
                .contains("CraftingStructureDisassembly.matrixRemoved"));
        assertTrue(source("world/block/ThaumatoriumBlock.java")
                .contains("CraftingStructureDisassembly.partRemoved"));
    }

    @Test
    void survivingPartsRestoreToTheirExactPreAssemblyBlocks() throws IOException {
        String source = source("construction/CraftingStructureDisassembly.java");
        for (String required : new String[]{
                "ARCANE_STONE.get().defaultBlockState()",
                "ARCANE_STONE_BRICK.get().defaultBlockState()",
                "Blocks.NETHERRACK.defaultBlockState()",
                "Blocks.OBSIDIAN.defaultBlockState()",
                "Blocks.LAVA.defaultBlockState()",
                "Blocks.IRON_BARS.defaultBlockState()",
                "ALCHEMICAL_FURNACE.get().defaultBlockState()",
                "ADVANCED_ALCHEMICAL_CONSTRUCT.get().defaultBlockState()",
                "ARCANE_ALEMBIC.get().defaultBlockState()",
                "ALCHEMICAL_CONSTRUCT.get().defaultBlockState()"
        }) assertTrue(source.contains(required), required);
    }

    @Test
    void idleActiveMatrixAlsoDetectsAFieldBlockBeingBroken() throws IOException {
        String source = source("world/block/entity/RunicMatrixBlockEntity.java");
        int validation = source.indexOf("CraftingStructureDisassembly.invalidInfusionMatrix");
        int idleReturn = source.indexOf("if (!matrix.crafting) return;", validation);
        assertTrue(validation >= 0 && idleReturn > validation,
                "active idle matrices must disassemble before the crafting-only return");
    }
}
