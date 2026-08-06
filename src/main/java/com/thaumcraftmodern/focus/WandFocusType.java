package com.thaumcraftmodern.focus;

import java.util.LinkedHashMap;
import java.util.Map;

/** Complete TC4 player-facing wand-focus catalogue. */
public enum WandFocusType {
    FIRE("fire", 0xFF4500, true, 0, cost("ignis", 10)),
    FROST("frost", 0x4F69CC, false, 4,
            cost("aqua", 5, "ignis", 2, "perditio", 2)),
    SHOCK("shock", 0xFFFF7E, true, 5, cost("aer", 25)),
    TRADE("trade", 0x00CED1, false, 0,
            cost("perditio", 5, "terra", 5, "ordo", 5)),
    EXCAVATION("excavation", 0x064006, true, 0, cost("terra", 15)),
    PRIMAL("primal", 0xFFFFFF, false, 10, Map.of()),
    HELLBAT("hellbat", 0xFF0000, false, 20,
            cost("ignis", 200, "perditio", 100, "aer", 100)),
    PORTABLE_HOLE("portable_hole", 0x091429, false, 0,
            cost("perditio", 10, "aer", 10)),
    WARDING("warding", 0xFFEFAF, false, 0,
            cost("terra", 25, "ordo", 25, "aqua", 10));

    private final String id;
    private final int color;
    private final boolean continuous;
    private final int cooldownTicks;
    private final Map<String, Integer> centivisCost;

    WandFocusType(String id, int color, boolean continuous, int cooldownTicks,
                  Map<String, Integer> centivisCost) {
        this.id = id;
        this.color = color;
        this.continuous = continuous;
        this.cooldownTicks = cooldownTicks;
        this.centivisCost = centivisCost;
    }

    public String id() { return id; }
    public String itemId() { return "focus_" + id; }
    public int color() { return color; }
    public boolean continuous() { return continuous; }
    public boolean perTickCost() { return this == FIRE || this == EXCAVATION; }
    public int cooldownTicks() { return cooldownTicks; }
    public Map<String, Integer> centivisCost() { return centivisCost; }

    public java.util.List<FocusUpgradeType> upgradesAtRank(int rank) {
        if (rank < 1 || rank > 5) return java.util.List.of();
        return switch (this) {
            case FIRE -> switch (rank) {
                case 1, 5 -> list(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY);
                case 2, 4 -> list(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY,
                        FocusUpgradeType.ALCHEMISTS_FIRE);
                case 3 -> list(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY,
                        FocusUpgradeType.FIREBALL, FocusUpgradeType.FIRE_BEAM);
                default -> java.util.List.of();
            };
            case FROST -> switch (rank) {
                case 1, 5 -> list(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY,
                        FocusUpgradeType.ALCHEMISTS_FROST);
                case 2, 4 -> list(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY);
                case 3 -> list(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY,
                        FocusUpgradeType.SCATTERSHOT, FocusUpgradeType.ICE_BOULDER,
                        FocusUpgradeType.ALCHEMISTS_FROST);
                default -> java.util.List.of();
            };
            case SHOCK -> switch (rank) {
                case 1, 2 -> list(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY);
                case 3 -> list(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY,
                        FocusUpgradeType.CHAIN_LIGHTNING, FocusUpgradeType.EARTH_SHOCK);
                case 4, 5 -> list(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY,
                        FocusUpgradeType.ENLARGE);
                default -> java.util.List.of();
            };
            case TRADE -> rank == 3
                    ? list(FocusUpgradeType.FRUGAL, FocusUpgradeType.ENLARGE,
                    FocusUpgradeType.TREASURE, FocusUpgradeType.ARCHITECT)
                    : rank == 5
                    ? list(FocusUpgradeType.FRUGAL, FocusUpgradeType.ENLARGE,
                    FocusUpgradeType.SILK_TOUCH)
                    : list(FocusUpgradeType.FRUGAL, FocusUpgradeType.ENLARGE);
            case EXCAVATION -> switch (rank) {
                case 1 -> list(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY,
                        FocusUpgradeType.TREASURE);
                case 2, 4 -> list(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY,
                        FocusUpgradeType.ENLARGE);
                case 3 -> list(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY,
                        FocusUpgradeType.TREASURE, FocusUpgradeType.DOWSING);
                case 5 -> list(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY,
                        FocusUpgradeType.TREASURE, FocusUpgradeType.SILK_TOUCH);
                default -> java.util.List.of();
            };
            case PRIMAL -> rank == 3
                    ? list(FocusUpgradeType.FRUGAL, FocusUpgradeType.SEEKER)
                    : list(FocusUpgradeType.FRUGAL);
            case HELLBAT -> rank == 3
                    ? list(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY,
                    FocusUpgradeType.BAT_BOMBS, FocusUpgradeType.DEVIL_BATS)
                    : rank == 5
                    ? list(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY,
                    FocusUpgradeType.VAMPIRE_BATS)
                    : list(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY);
            case PORTABLE_HOLE -> list(FocusUpgradeType.FRUGAL,
                    FocusUpgradeType.ENLARGE, FocusUpgradeType.EXTEND);
            case WARDING -> rank == 1
                    ? list(FocusUpgradeType.FRUGAL)
                    : rank == 2
                    ? list(FocusUpgradeType.FRUGAL, FocusUpgradeType.ARCHITECT)
                    : list(FocusUpgradeType.FRUGAL, FocusUpgradeType.ENLARGE);
        };
    }

    private static java.util.List<FocusUpgradeType> list(
            FocusUpgradeType... types) {
        return java.util.List.of(types);
    }

    public static WandFocusType fromItemId(String id) {
        for (WandFocusType value : values()) if (value.itemId().equals(id)) return value;
        throw new IllegalArgumentException("unknown wand focus: " + id);
    }

    private static Map<String, Integer> cost(Object... entries) {
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            result.put((String) entries[index], (Integer) entries[index + 1]);
        }
        return Map.copyOf(result);
    }
}
