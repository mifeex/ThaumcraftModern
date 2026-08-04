package com.thaumcraftmodern.client;

import com.thaumcraftmodern.scan.AspectReward;
import com.thaumcraftmodern.aura.PrimalAspect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientAspectContainerReadoutTest {
    @Test
    void crucibleUsesTheSameAspectValuesAsTheNodeHud() {
        LinkedHashMap<String, Integer> essentia = new LinkedHashMap<>();
        essentia.put("aer", 3);
        essentia.put("ignis", 7);
        essentia.put("aqua", 0);

        assertEquals(
                List.of(
                        new AspectReward("aer", 3),
                        new AspectReward("ignis", 7)
                ),
                ClientAspectContainerReadout.crucibleContents(essentia)
        );
    }

    @Test
    void jarAndAlembicSingleAspectContentsUseTheSharedGogglesHud() {
        assertEquals(List.of(new AspectReward("aqua", 24)),
                ClientAspectContainerReadout.singleAspectContents("aqua", 24));
        assertEquals(List.of(),
                ClientAspectContainerReadout.singleAspectContents("aqua", 0));
    }

    @Test
    void energizedNodeHudUsesItsReducedPrimalVisBase() {
        assertEquals(
                List.of(
                        new AspectReward("aer", 4),
                        new AspectReward("ignis", 2)
                ),
                ClientAspectContainerReadout.energizedNodeContents(Map.of(
                        PrimalAspect.AER, 4,
                        PrimalAspect.IGNIS, 2,
                        PrimalAspect.AQUA, 0
                ))
        );
    }

    @Test
    void classicContainerAnchorSitsOnTheActuallyHitBlockFace() {
        BlockPos position = new BlockPos(4, 7, 9);
        Vec3 north = AspectContainerHudRegistry.onHitFace(
                new BlockHitResult(Vec3.atCenterOf(position), Direction.NORTH,
                        position, false));
        Vec3 up = AspectContainerHudRegistry.onHitFace(
                new BlockHitResult(Vec3.atCenterOf(position), Direction.UP,
                        position, false));

        assertEquals(4.5D, north.x());
        assertEquals(7.5D, north.y());
        assertEquals(8.9D, north.z());
        assertEquals(8.1D, up.y());
    }
}
