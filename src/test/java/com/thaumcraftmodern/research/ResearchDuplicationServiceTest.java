package com.thaumcraftmodern.research;

import com.thaumcraftmodern.aspect.AspectCost;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResearchDuplicationServiceTest {
    @Test
    void everyExistingCopyAddsOneToEveryOriginalAspectCost() {
        ResearchDefinition research = new ResearchDefinition(
                "target",
                "basics",
                "minecraft:book",
                "",
                "research.target",
                "",
                false,
                false,
                false,
                false,
                "",
                List.of(),
                List.of(),
                ResearchCondition.ALWAYS,
                ResearchCondition.ALWAYS,
                0,
                0,
                List.of(),
                0,
                ResearchDefinition.NodeFrame.PRIMARY,
                false,
                List.of(
                        new AspectCost("cognitio", 6),
                        new AspectCost("ordo", 3)
                ),
                List.of(),
                List.of()
        );

        assertEquals(
                List.of(
                        new AspectCost("cognitio", 8),
                        new AspectCost("ordo", 5)
                ),
                ResearchDuplicationService.cost(research, 2)
        );
    }
}
