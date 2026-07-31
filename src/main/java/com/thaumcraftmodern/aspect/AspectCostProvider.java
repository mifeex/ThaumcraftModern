package com.thaumcraftmodern.aspect;

import java.util.List;

/**
 * Common Thaumonomicon handoff for any mechanic that consumes aspects:
 * Arcane Workbench recipes, infusion/matrix recipes and wand actions.
 */
public interface AspectCostProvider {
    List<AspectCost> aspectCosts();
}
