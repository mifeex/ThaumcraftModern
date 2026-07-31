package com.thaumcraftmodern.nodejar;

import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.aura.PrimalVis;

import java.util.Map;

/**
 * Exact TC4 NODEJAR base cost: 70 of every primal. Wand cap consumption
 * modifiers belong to the wand subsystem and must be applied there.
 */
public final class NodeJarCost {
    public static final int BASE_PER_PRIMAL = 70;
    public static final Map<PrimalAspect, Integer> BASE =
            PrimalVis.uniform(BASE_PER_PRIMAL);

    private NodeJarCost() {
    }
}
