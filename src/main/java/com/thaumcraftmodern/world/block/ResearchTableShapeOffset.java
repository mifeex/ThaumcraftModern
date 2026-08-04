package com.thaumcraftmodern.world.block;

import net.minecraft.core.Direction;

final class ResearchTableShapeOffset {
    private ResearchTableShapeOffset() {
    }

    static Offset otherHalf(ResearchTablePart part, Direction facing) {
        Direction direction = part == ResearchTablePart.MAIN
                ? facing
                : facing.getOpposite();
        return new Offset(direction.getStepX(), direction.getStepZ());
    }

    record Offset(int x, int z) {
    }
}
