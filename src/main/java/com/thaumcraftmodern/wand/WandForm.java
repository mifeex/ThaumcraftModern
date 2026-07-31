package com.thaumcraftmodern.wand;

/**
 * The three classic casting-tool forms.
 *
 * <p>Scepters trade focus support for 50% more storage and an additional
 * 10% vis discount. Staves use their own high-capacity rods, can carry foci,
 * and are intentionally rejected by Arcane Workbench crafting.</p>
 */
public enum WandForm {
    WAND("item.Wand.wand.obj", 100, 0, true, true),
    SCEPTRE("item.Wand.sceptre.obj", 150, 10, true, false),
    STAFF("item.Wand.staff.obj", 100, 0, false, true);

    private final String translationKey;
    private final int capacityPercent;
    private final int inherentDiscountPercent;
    private final boolean craftingTool;
    private final boolean acceptsFocus;

    WandForm(
            String translationKey,
            int capacityPercent,
            int inherentDiscountPercent,
            boolean craftingTool,
            boolean acceptsFocus
    ) {
        this.translationKey = translationKey;
        this.capacityPercent = capacityPercent;
        this.inherentDiscountPercent = inherentDiscountPercent;
        this.craftingTool = craftingTool;
        this.acceptsFocus = acceptsFocus;
    }

    public String translationKey() {
        return translationKey;
    }

    public int applyCapacity(int rodCapacityVis) {
        return Math.multiplyExact(rodCapacityVis, capacityPercent) / 100;
    }

    public int inherentDiscountPercent() {
        return inherentDiscountPercent;
    }

    public boolean isCraftingTool() {
        return craftingTool;
    }

    public boolean acceptsFocus() {
        return acceptsFocus;
    }
}
