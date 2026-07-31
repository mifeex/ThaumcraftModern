package com.thaumcraftmodern.worldgen;

import net.minecraft.util.RandomSource;

final class GreatwoodClassicSettings {
    static final double HEIGHT_ATTENUATION = 0.618D;
    static final double BRANCH_SLOPE = 0.38D;
    static final double INITIAL_SCALE_WIDTH = 1.2D;
    static final double SECOND_PASS_SCALE_WIDTH = 1.66D;
    static final double LEAF_DENSITY = 0.9D;
    static final int TRUNK_SIZE = 2;
    static final int HEIGHT_LIMIT_BASE = 11;
    static final int LEAF_DISTANCE_LIMIT = 4;
    static final int SAPLING_SPIDER_DENOMINATOR = 8;
    static final int WORLDGEN_SPIDER_DENOMINATOR = 16;
    static final int WEB_ATTEMPTS = 50;

    private GreatwoodClassicSettings() {
    }

    static int sampledHeightLimit(RandomSource random) {
        return HEIGHT_LIMIT_BASE + random.nextInt(HEIGHT_LIMIT_BASE);
    }

    static int attenuatedHeight(int heightLimit) {
        int height = (int) (heightLimit * HEIGHT_ATTENUATION);
        return height >= heightLimit ? heightLimit - 1 : height;
    }

    static int maximumGeneratedY(int baseY, int heightLimit) {
        return baseY + attenuatedHeight(heightLimit) + heightLimit - 1;
    }
}
