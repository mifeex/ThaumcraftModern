package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EssentiaVisualFidelityTest {
    @Test
    void phialUsesClassicBottleAndAnimatedTintedEssenceLayers()
            throws Exception {
        String model = Files.readString(Path.of(
                "src/main/resources/assets/thaumcraftmodern/models/item/"
                        + "essentia_phial.json"));
        String animation = Files.readString(Path.of(
                "src/main/resources/assets/thaumcraftmodern/textures/item/"
                        + "essence.png.mcmeta"));

        assertTrue(model.contains("thaumcraftmodern:item/phial"));
        assertTrue(model.contains("thaumcraftmodern:item/essence"));
        assertTrue(animation.contains("\"animation\""));
    }

    @Test
    void jarLiquidAndGogglesReadoutUseItsSynchronizedAspect() throws Exception {
        String renderer = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/"
                        + "EssentiaJarBlockEntityRenderer.java"));
        String readout = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/"
                        + "ClientAspectContainerReadout.java"));
        String jarBlock = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/"
                        + "EssentiaJarBlock.java"));
        String itemRenderer = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/"
                        + "WardedJarItemRenderer.java"));
        String itemModel = Files.readString(Path.of(
                "src/main/resources/assets/thaumcraftmodern/models/item/"
                        + "warded_jar.json"));

        assertTrue(renderer.contains("renderLiquid(jar.aspect(), jar.amount()"));
        assertTrue(renderer.contains("AspectRegistryRuntime.find(aspect)"));
        assertTrue(renderer.contains("int red = (color >> 16) & 255"));
        assertTrue(renderer.contains("TextureAtlas.LOCATION_BLOCKS"));
        assertTrue(renderer.contains("sprite.getU0()"));
        assertTrue(renderer.contains("sprite.getU1()"));
        assertTrue(renderer.contains("sprite.getV0()"));
        assertTrue(renderer.contains("sprite.getV1()"));
        assertTrue(renderer.contains(".color(red, green, blue, 255)"));
        assertTrue(renderer.contains("LightTexture.pack(12, 12)"));
        String hudRegistry = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/"
                        + "AspectContainerHudRegistry.java"));
        assertTrue(readout.contains("AspectContainerHudRegistry.resolve"));
        assertTrue(hudRegistry.contains("register(EssentiaJarBlockEntity.class"));
        assertTrue(hudRegistry.contains("jar.aspect(), jar.amount()"));
        assertTrue(hudRegistry.contains("CLASSIC_FACE_OFFSET = 0.6D"));
        assertTrue(jarBlock.contains("box(3, 0, 3, 13, 12, 13)"));
        assertTrue(jarBlock.contains("return SHAPE;"));
        assertTrue(itemModel.contains("minecraft:builtin/entity"));
        assertTrue(itemRenderer.contains("WardedJarItem.contents(stack)"));
        assertTrue(itemRenderer.contains("EssentiaJarBlockEntityRenderer.renderLiquid("));
        assertTrue(itemRenderer.contains("ModBlocks.WARDED_JAR.get().defaultBlockState()"));
    }
}
