package com.thaumcraftmodern.nodejar;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public final class NodeJarKeys {
    private NodeJarKeys() {
    }

    public static String placement(ServerLevel level, BlockPos position) {
        return level.dimension().location() + "@"
                + position.getX() + ","
                + position.getY() + ","
                + position.getZ();
    }
}
