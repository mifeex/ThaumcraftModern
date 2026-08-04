package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class ResearchTableUnknownAspectMaskTest {
    private static final Path SCREEN = Path.of(
            "src/main/java/com/thaumcraftmodern/client/screen/ResearchTableScreen.java"
    );
    private static final Path ORIGINAL = Path.of(
            "reference/Thaumcraft-4.2-FOREVA-master/src/main/java/"
                    + "thaumcraft/client/gui/GuiResearchTable.java"
    );

    @Test
    void puzzleMasksUnknownIconsAndTheirConnectionsLikeTc4() throws Exception {
        String screen = Files.readString(SCREEN);
        String original = Files.readString(ORIGINAL);

        assertTrue(original.contains("textures/aspects/_unknown.png"));
        assertTrue(original.contains("knowledge.hasDiscoveredAspect"));
        assertTrue(screen.contains("textures/aspects/_unknown.png"));
        assertTrue(screen.contains("!knowledge.knowsAspect(aspectId)"));
        assertTrue(screen.contains("renderConnections(graphics, puzzle, knowledge)"));
        assertTrue(screen.contains(
                "!knowledge.knowsAspect(\n"
                        + "                                puzzle.aspectAt(cell).orElseThrow()"
        ));
        assertTrue(screen.contains(
                "!knowledge.knowsAspect(\n"
                        + "                                puzzle.aspectAt(neighbor).orElseThrow()"
        ));
    }
}
