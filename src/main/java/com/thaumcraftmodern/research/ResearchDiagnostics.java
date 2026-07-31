package com.thaumcraftmodern.research;

import com.thaumcraftmodern.ThaumcraftModern;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Structured trace used to diagnose the complete Research Table and
 * Thaumonomicon pipelines from a single latest.log file.
 */
public final class ResearchDiagnostics {
    public static final String MARKER = "TCM-RESEARCH";
    private static final AtomicLong SEQUENCE = new AtomicLong();

    private ResearchDiagnostics() {
    }

    public static void log(String stage, String message, Object... arguments) {
        Object[] values = new Object[arguments.length + 2];
        values[0] = SEQUENCE.incrementAndGet();
        values[1] = stage;
        System.arraycopy(arguments, 0, values, 2, arguments.length);
        ThaumcraftModern.LOGGER.info(
                "[{} #{}][{}] " + message,
                reorder(values)
        );
    }

    /*
     * Logger placeholders above expect marker, sequence, stage, then the
     * caller's arguments. Keeping that arrangement here makes every line easy
     * to grep while preserving SLF4J's structured formatting.
     */
    private static Object[] reorder(Object[] values) {
        Object[] result = new Object[values.length + 1];
        result[0] = MARKER;
        result[1] = values[0];
        result[2] = values[1];
        System.arraycopy(values, 2, result, 3, values.length - 2);
        return result;
    }
}
