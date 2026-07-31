package com.thaumcraftmodern.aura;

/**
 * Classic TC4 modifiers required to preserve and jar an ordinary node.
 *
 * <p>TC4 represented an unmodified node with {@code null}; {@link #NORMAL} is
 * the explicit modern serialization value for that state.</p>
 */
public enum AuraNodeModifier {
    NORMAL,
    BRIGHT,
    PALE,
    FADING
}
