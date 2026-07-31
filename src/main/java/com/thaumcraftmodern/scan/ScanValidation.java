package com.thaumcraftmodern.scan;

import java.util.Objects;

/**
 * Pure validation policy for an authoritative server-side scan attempt.
 */
public final class ScanValidation {
    private ScanValidation() {
    }

    public static Reason validate(Request request) {
        Objects.requireNonNull(request, "request");

        if (!request.holdingThaumometer()) {
            return Reason.THAUMOMETER_NOT_HELD;
        }
        if (!request.playerDimension().equals(request.targetDimension())) {
            return Reason.DIMENSION_MISMATCH;
        }
        if (!request.targetChunkLoaded()) {
            return Reason.TARGET_CHUNK_UNLOADED;
        }
        if (request.distance() > request.maximumDistance()) {
            return Reason.TARGET_OUT_OF_RANGE;
        }
        if (!request.lineOfSight()) {
            return Reason.LINE_OF_SIGHT_BLOCKED;
        }
        if (request.stableDurationTicks() < request.requiredStableDurationTicks()) {
            return Reason.STABLE_DURATION_NOT_REACHED;
        }
        if (!request.registeredTarget()) {
            return Reason.TARGET_NOT_REGISTERED;
        }
        return Reason.VALID;
    }

    public record Request(
            boolean holdingThaumometer,
            String playerDimension,
            String targetDimension,
            boolean targetChunkLoaded,
            double distance,
            double maximumDistance,
            boolean lineOfSight,
            boolean registeredTarget,
            int stableDurationTicks,
            int requiredStableDurationTicks) {
        public Request {
            playerDimension = requireId(playerDimension, "playerDimension");
            targetDimension = requireId(targetDimension, "targetDimension");
            if (!Double.isFinite(distance) || distance < 0.0D) {
                throw new IllegalArgumentException("distance must be finite and non-negative");
            }
            if (!Double.isFinite(maximumDistance) || maximumDistance <= 0.0D) {
                throw new IllegalArgumentException("maximumDistance must be finite and positive");
            }
            if (stableDurationTicks < 0) {
                throw new IllegalArgumentException("stableDurationTicks cannot be negative");
            }
            if (requiredStableDurationTicks <= 0) {
                throw new IllegalArgumentException("requiredStableDurationTicks must be positive");
            }
        }

        private static String requireId(String value, String fieldName) {
            Objects.requireNonNull(value, fieldName);
            if (value.isBlank() || !value.equals(value.trim())) {
                throw new IllegalArgumentException(fieldName + " must be non-blank and trimmed");
            }
            return value;
        }
    }

    public enum Reason {
        VALID,
        THAUMOMETER_NOT_HELD,
        DIMENSION_MISMATCH,
        TARGET_CHUNK_UNLOADED,
        TARGET_OUT_OF_RANGE,
        LINE_OF_SIGHT_BLOCKED,
        TARGET_NOT_REGISTERED,
        STABLE_DURATION_NOT_REACHED
    }
}
