package com.thaumcraftmodern.world.block;

import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

final class ResearchTableFullShapeTest {
    @Test
    void eitherHalfPointsAtTheOtherBlockAlongX() {
        assertEquals(
                new ResearchTableShapeOffset.Offset(1, 0),
                ResearchTableShapeOffset.otherHalf(
                        ResearchTablePart.MAIN,
                        Direction.EAST
                )
        );
        assertEquals(
                new ResearchTableShapeOffset.Offset(-1, 0),
                ResearchTableShapeOffset.otherHalf(
                        ResearchTablePart.COMPANION,
                        Direction.EAST
                )
        );
    }

    @Test
    void eitherHalfPointsAtTheOtherBlockAlongZ() {
        assertEquals(
                new ResearchTableShapeOffset.Offset(0, -1),
                ResearchTableShapeOffset.otherHalf(
                        ResearchTablePart.MAIN,
                        Direction.NORTH
                )
        );
        assertEquals(
                new ResearchTableShapeOffset.Offset(0, 1),
                ResearchTableShapeOffset.otherHalf(
                        ResearchTablePart.COMPANION,
                        Direction.NORTH
                )
        );
    }

    @Test
    void selectionShapeIsOneFullTwoBlockCuboid() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/ResearchTableBlock.java"
        ));
        assertTrue(source.contains("return fullTableShape("));
        int start = source.indexOf("static VoxelShape fullTableShape(");
        int end = source.indexOf("@Override\n    public InteractionResult use(", start);
        String method = source.substring(start, end);
        assertTrue(method.contains("otherHalf.x() * 16"));
        assertTrue(method.contains("otherHalf.z() * 16"));
        assertTrue(method.contains("(otherHalf.x() + 1) * 16"));
        assertTrue(method.contains("(otherHalf.z() + 1) * 16"));
        assertTrue(method.contains("return box("));
        assertTrue(method.contains("maximumX,\n                16,\n                maximumZ"));
        assertFalse(method.contains("Shapes.or("));
        assertFalse(method.contains("SHAPE.move("));
    }
}
