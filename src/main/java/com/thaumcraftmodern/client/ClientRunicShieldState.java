package com.thaumcraftmodern.client;

public final class ClientRunicShieldState {
    private static int charge;
    private static int maximum;
    private ClientRunicShieldState() { }
    public static void set(int value, int max) {
        charge = Math.max(0, value); maximum = Math.max(0, max);
    }
    public static int charge() { return charge; }
    public static int maximum() { return maximum; }
}
