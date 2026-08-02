package com.thaumcraftmodern.essentia;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrystallizerAndMnemonicVisualFidelityTest {
    @Test
    void crystallizerUsesCenteredClassicObjAndItemTransforms() throws Exception {
        String block = read("src/main/resources/assets/thaumcraftmodern/models/block/essentia_crystallizer.json");
        String item = read("src/main/resources/assets/thaumcraftmodern/models/item/essentia_crystallizer.json");
        String implementation = read("src/main/java/com/thaumcraftmodern/world/block/EssentiaCrystallizerBlock.java");
        String machine = read("src/main/java/com/thaumcraftmodern/world/block/entity/EssentiaCrystallizerBlockEntity.java");
        String renderer = read("src/main/java/com/thaumcraftmodern/client/render/EssentiaCrystallizerBlockEntityRenderer.java");
        String state = read("src/main/resources/assets/thaumcraftmodern/blockstates/essentia_crystallizer.json");
        String animatedCrystal = read("src/main/resources/assets/thaumcraftmodern/models/block/crystallizer_crystal.json");
        assertTrue(block.contains("crystalizer.obj"));
        assertTrue(block.contains("[0.5,0.5,0]"));
        assertTrue(state.contains("\"facing=north\": {\"model\": \"thaumcraftmodern:block/essentia_crystallizer\"}"));
        assertTrue(state.contains("\"facing=down\": {\"model\": \"thaumcraftmodern:block/essentia_crystallizer\", \"x\": 90}"));
        assertTrue(state.contains("\"facing=up\": {\"model\": \"thaumcraftmodern:block/essentia_crystallizer\", \"x\": 270}"));
        assertTrue(item.contains("\"gui\""));
        assertTrue(item.contains("[30, 225, 0]"));
        assertTrue(implementation.contains("findConnectedInput"));
        assertTrue(implementation.contains("remote.canOutputTo(side.getOpposite())"));
        assertTrue(machine.contains("alignToConnectedTransport"));
        assertTrue(machine.contains("SoundEvents.FIRE_EXTINGUISH"));
        assertTrue(machine.contains("0.25F"));
        assertTrue(machine.contains("2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F"));
        assertTrue(renderer.contains("pose.translate(0.0D, 0.0D, -0.5D)"));
        assertTrue(renderer.contains("pose.scale(.75F, .75F, .75F)"));
        assertTrue(renderer.contains("pose.translate(.34F, 0.0F, 1.2125F)"));
        assertFalse(renderer.contains("getItemRenderer().renderStatic"));
        assertTrue(animatedCrystal.contains("vis_relay.obj"));
        assertTrue(animatedCrystal.contains("\"RingFloat\": false"));
        assertTrue(Arrays.equals(
                Files.readAllBytes(Path.of("reference/Thaumcraft-4.2-FOREVA-master/src/main/resources/assets/thaumcraft/textures/models/crystalizer.obj")),
                Files.readAllBytes(Path.of("src/main/resources/assets/thaumcraftmodern/textures/models/crystalizer.obj"))));
    }

    @Test
    void mnemonicMatrixIsClassicPlaceableBrainboxNotKnowledgeFragment() throws Exception {
        String item = read("src/main/resources/assets/thaumcraftmodern/models/item/mnemonic_matrix.json");
        String block = read("src/main/resources/assets/thaumcraftmodern/models/block/mnemonic_matrix.json");
        String blockstate = read("src/main/resources/assets/thaumcraftmodern/blockstates/mnemonic_matrix.json");
        String registration = read("src/main/java/com/thaumcraftmodern/registry/ModItems.java");
        String implementation = read("src/main/java/com/thaumcraftmodern/world/block/MnemonicMatrixBlock.java");
        assertFalse(item.contains("knowledgefragment"));
        assertFalse(item.contains("block/mnemonic_matrix"));
        assertFalse(item.contains("[6, 6, 0]"));
        assertTrue(item.contains("[3, 3, 3]"));
        assertTrue(block.contains("[3, 3, 3]"));
        assertTrue(block.contains("[13, 13, 13]"));
        assertTrue(block.contains("[6, 6, 0]"));
        assertTrue(blockstate.contains(
                "\"facing=down\": {\"model\": \"thaumcraftmodern:block/mnemonic_matrix\", \"x\": 90}"
        ));
        assertTrue(blockstate.contains(
                "\"facing=up\": {\"model\": \"thaumcraftmodern:block/mnemonic_matrix\", \"x\": 270}"
        ));
        assertTrue(registration.contains("blockItem(name, ModBlocks.MNEMONIC_MATRIX)"));
        assertTrue(implementation.contains("adjacentConnector"));
        assertTrue(implementation.contains("nearestAlchemyInput"));
        assertTrue(implementation.contains("ALCHEMY_INPUT_SEARCH_RADIUS = 2"));
        assertTrue(implementation.contains("for (Direction inputFace : Direction.values())"));
        assertTrue(implementation.contains("Direction.getNearest("));
        assertTrue(implementation.contains("ModBlocks.ALCHEMICAL_CONSTRUCT.get()"));
        assertTrue(implementation.contains(
                "ModBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get()"
        ));
        assertTrue(implementation.contains("ModBlocks.THAUMATORIUM.get()"));
        assertTrue(implementation.contains("instanceof EssentiaTransport transport"));
        assertTrue(implementation.contains("transport.isConnectable(direction.getOpposite())"));
        assertTrue(implementation.contains("updateShape"));
    }

    private static String read(String path) throws Exception {
        return Files.readString(Path.of(path));
    }
}
