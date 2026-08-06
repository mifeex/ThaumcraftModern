package com.thaumcraftmodern.entity;

/** Detects work navigation that repeatedly finishes or makes no meaningful progress. */
final class GolemRouteWatchdog {
    static final int FAILED_CHECKS_BEFORE_REBUILD = 3;
    static final int CHECK_INTERVAL_TICKS = 10;
    static final int REBUILDS_BEFORE_TARGET_RELEASE = 4;
    private static final double MIN_MOVEMENT_SQR = .04D;
    private static final double MIN_DISTANCE_PROGRESS = .04D;

    private long targetKey = Long.MIN_VALUE;
    private int nextCheckTick;
    private int failedChecks;
    private int approachIndex;
    private double lastX;
    private double lastY;
    private double lastZ;
    private double lastDistanceSqr = Double.MAX_VALUE;

    boolean shouldRebuild(long newTargetKey, int tick, double x, double y, double z,
            double distanceSqr, boolean navigationDone) {
        if (targetKey != newTargetKey) {
            targetKey = newTargetKey;
            nextCheckTick = tick + CHECK_INTERVAL_TICKS;
            failedChecks = 0;
            approachIndex = 0;
            remember(x, y, z, distanceSqr);
            return true;
        }
        if (tick < nextCheckTick) return false;
        nextCheckTick = tick + CHECK_INTERVAL_TICKS;
        double dx = x - lastX;
        double dy = y - lastY;
        double dz = z - lastZ;
        boolean moved = dx * dx + dy * dy + dz * dz >= MIN_MOVEMENT_SQR;
        boolean approached = lastDistanceSqr - distanceSqr >= MIN_DISTANCE_PROGRESS;
        remember(x, y, z, distanceSqr);
        if (navigationDone || !moved && !approached) failedChecks++;
        else failedChecks = 0;
        if (failedChecks < FAILED_CHECKS_BEFORE_REBUILD) return false;
        failedChecks = 0;
        approachIndex++;
        return true;
    }

    int approachIndex() {
        return approachIndex;
    }

    boolean shouldReleaseTarget() {
        return approachIndex >= REBUILDS_BEFORE_TARGET_RELEASE;
    }

    void arrived() {
        targetKey = Long.MIN_VALUE;
        failedChecks = 0;
        lastDistanceSqr = Double.MAX_VALUE;
    }

    private void remember(double x, double y, double z, double distanceSqr) {
        lastX = x;
        lastY = y;
        lastZ = z;
        lastDistanceSqr = distanceSqr;
    }
}
