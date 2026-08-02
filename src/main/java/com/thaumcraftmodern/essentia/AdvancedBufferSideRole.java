package com.thaumcraftmodern.essentia;

/** Player-selected role of one improved buffer face. */
public enum AdvancedBufferSideRole {
    BLOCKED(0x8F414B),
    INPUT(0x2B83B6),
    MAIN_OUTPUT(0x159B7E),
    RESERVE_OUTPUT(0xC65D12);

    private final int indicatorColor;

    AdvancedBufferSideRole(int indicatorColor) {
        this.indicatorColor = indicatorColor;
    }

    /** RGB tint of the small centre lamp on the corresponding buffer face. */
    public int indicatorColor() {
        return indicatorColor;
    }

    public AdvancedBufferSideRole next() {
        AdvancedBufferSideRole[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
