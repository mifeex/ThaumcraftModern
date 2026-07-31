package com.thaumcraftmodern.client.render;

/**
 * The original Forge OBJ root matrix was evaluated around the center of a
 * block. Inventory display transforms are now calibrated against direct model
 * coordinates, while the custom first-person presentation still uses the
 * legacy centered coordinates.
 */
final class ThaumometerModelCoordinates {
    private static final float SCALE = 0.32F;

    private ThaumometerModelCoordinates() {
    }

    static Position transform(
            float sourceX,
            float sourceY,
            float sourceZ,
            boolean legacyBlockCentered
    ) {
        float centerX = legacyBlockCentered ? 0.84F : 0.5F;
        float centerY = legacyBlockCentered ? 0.84F : 0.5F;
        float centerZ = legacyBlockCentered ? 1.16F : 0.5F;
        return new Position(
                sourceX * SCALE + centerX,
                sourceZ * SCALE + centerY,
                -sourceY * SCALE + centerZ
        );
    }

    record Position(float x, float y, float z) {
    }
}
