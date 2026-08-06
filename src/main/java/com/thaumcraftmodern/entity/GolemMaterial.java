package com.thaumcraftmodern.entity;

/** Exact values from TC4 EnumGolemType (4.2.3.5). */
public enum GolemMaterial {
    STRAW("straw", 10, 1, 0, 0, .38D, false, 1, 75, 0, true),
    WOOD("wood", 20, 4, 1, 6, .35D, false, 1, 75, 1, true),
    TALLOW("tallow", 20, 8, 2, 9, .33D, false, 2, 75, 2, true),
    CLAY("clay", 25, 8, 2, 9, .33D, true, 1, 100, 2, false),
    FLESH("flesh", 15, 4, 1, 6, .35D, false, 2, 40, 1, true),
    STONE("stone", 30, 16, 3, 12, .32D, true, 1, 100, 3, false),
    IRON("iron", 35, 32, 4, 15, .31D, true, 1, 125, 4, false),
    THAUMIUM("thaumium", 40, 32, 4, 15, .32D, true, 2, 100, 4, false);

    private final String id;
    private final int health;
    private final int carry;
    private final int strength;
    private final int armor;
    private final double speed;
    private final boolean fireResistant;
    private final int upgradeSlots;
    private final int regenerationDelay;
    private final int visCost;
    private final boolean light;

    GolemMaterial(String id, int health, int carry, int strength, int armor, double speed,
            boolean fireResistant, int upgradeSlots, int regenerationDelay, int visCost, boolean light) {
        this.id = id;
        this.health = health;
        this.carry = carry;
        this.strength = strength;
        this.armor = armor;
        this.speed = speed;
        this.fireResistant = fireResistant;
        this.upgradeSlots = upgradeSlots;
        this.regenerationDelay = regenerationDelay;
        this.visCost = visCost;
        this.light = light;
    }

    public String id() { return id; }
    public int health() { return health; }
    public int carry() { return carry; }
    public int strength() { return strength; }
    public int armor() { return armor; }
    public double speed() { return speed; }
    public boolean fireResistant() { return fireResistant; }
    public int upgradeSlots() { return upgradeSlots; }
    public int regenerationDelay() { return regenerationDelay; }
    public int visCost() { return visCost; }
    public boolean light() { return light; }
    public double attackDamage() { return 2D + strength; }
}
