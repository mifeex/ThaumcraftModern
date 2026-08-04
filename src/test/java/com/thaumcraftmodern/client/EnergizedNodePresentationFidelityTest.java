package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EnergizedNodePresentationFidelityTest {
    private static final Path MAIN = Path.of("src/main/java/com/thaumcraftmodern");

    @Test
    void energizedNodeRemainsTargetableButHasNoDebugOutline() throws Exception {
        String feedback = Files.readString(MAIN.resolve(
                "client/render/ClientAuraNodeHitFeedback.java"));
        assertTrue(feedback.contains("ModBlocks.ENERGIZED_AURA_NODE"));
    }

    @Test
    void energizedNodeKeepsDimAuraLayersBehindProminentLightningRing()
            throws Exception {
        String renderer = Files.readString(MAIN.resolve(
                "client/render/EnergizedAuraNodeBlockEntityRenderer.java"));
        String renderTypes = Files.readString(MAIN.resolve(
                "client/render/ClassicNodeRenderTypes.java"));
        assertTrue(renderTypes.contains("textures/item/lightningringv.png"));
        assertTrue(renderTypes.contains("ADDITIVE_TRANSPARENCY"));
        assertTrue(renderer.contains("ClassicNodeRenderTypes.energizedRing()"));
        assertTrue(renderer.contains("OUTER_RING_HALF_SIZE = 0.40F"));
        assertTrue(renderer.contains("INNER_RING_HALF_SIZE = 0.33F"));
        assertTrue(renderer.contains("Math.floorMod(frame - 1, 16)"));
        assertTrue(renderer.contains("AURA_LAYER_OPACITY = 0.32F"));
        assertTrue(renderer.contains("ClassicAuraNodeRenderer.renderEnergizedNode("));
        assertTrue(renderer.contains("tile.displayState()"));
    }

    @Test
    void gogglesAndThaumometerBothResolveTheEnergizedNode() throws Exception {
        String goggles = Files.readString(MAIN.resolve(
                "client/AspectContainerHudRegistry.java"));
        String thaumometer = Files.readString(MAIN.resolve(
                "client/ClientThaumometerTarget.java"));
        assertTrue(goggles.contains("EnergizedAuraNodeBlockEntity.class"));
        assertTrue(goggles.contains("energizedNodeContents("));
        assertTrue(goggles.contains("node.visBase()"));
        assertTrue(thaumometer.contains(
                "instanceof EnergizedAuraNodeBlockEntity energized"));
        assertTrue(thaumometer.contains("energizedNodeContents("));
        assertTrue(thaumometer.contains("energized.visBase()"));
        assertTrue(thaumometer.contains("discloseNodeAspects(studied)"));

        String sessions = Files.readString(MAIN.resolve(
                "scan/ScanSessionManager.java"));
        String item = Files.readString(MAIN.resolve(
                "item/ThaumometerItem.java"));
        assertTrue(sessions.contains(
                "instanceof EnergizedAuraNodeBlockEntity node"));
        assertTrue(item.contains(
                "instanceof EnergizedAuraNodeBlockEntity node"));
    }

    @Test
    void openingBoltsUseTheClassicEightSegmentTwoPassShape() throws Exception {
        String renderer = Files.readString(MAIN.resolve(
                "client/render/NodeDeviceBlockEntityRenderer.java"));
        assertTrue(renderer.contains("new Vec3[9]"));
        assertTrue(renderer.contains("0.24D * envelope"));
        assertTrue(renderer.contains("0.0375F, fade"));
        assertTrue(renderer.contains("0.03F, fade"));
        assertTrue(renderer.contains("ClassicBoltRenderTypes.bolt"));
        assertTrue(renderer.contains("keep U=0.5 through the body"));
        assertTrue(renderer.contains("Vec3[] offsets = new Vec3[points.length]"));
        assertTrue(renderer.contains("startTip.subtract(offsets[0])"));
        assertTrue(renderer.contains("endTip.add(offsets[last])"));
    }
}
