package com.thaumcraftmodern.scan;

public enum ScanTargetType {
    BLOCK,
    BLOCK_TAG,
    ITEM,
    ITEM_TAG,
    ENTITY,
    /**
     * A world phenomenon with its own persistent identity. Aura nodes use
     * this target kind instead of pretending to be an ordinary block.
     */
    PHENOMENON
}
