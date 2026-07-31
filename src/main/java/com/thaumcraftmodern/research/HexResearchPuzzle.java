package com.thaumcraftmodern.research;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

import com.thaumcraftmodern.aspect.AspectCatalog;
import com.thaumcraftmodern.knowledge.PlayerThaumKnowledge;

/**
 * Authoritative state machine for the first-discovery five-cell research
 * puzzle. All cells lie on r=0 and use q coordinates from -2 through 2.
 */
public final class HexResearchPuzzle {
    public static final int MIN_Q = -2;
    public static final int MAX_Q = 2;
    public static final int R = 0;

    private static final Map<Integer, String> ANCHORS = orderedReadOnlyMap(
            MIN_Q, "aer",
            MAX_Q, "ordo");

    private static final Map<Integer, String> SOLUTION = orderedReadOnlyMap(
            -2, "aer",
            -1, "lux",
            0, "ignis",
            1, "potentia",
            2, "ordo");

    private final AspectCatalog catalog;
    private final TreeMap<Integer, String> placements = new TreeMap<>();

    public HexResearchPuzzle(AspectCatalog catalog) {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        for (String anchor : ANCHORS.values()) {
            if (catalog.lookup(anchor).isEmpty()) {
                throw new IllegalArgumentException("catalog is missing puzzle anchor aspect: " + anchor);
            }
        }
        placements.putAll(ANCHORS);
    }

    public PlacementResult place(int q, String aspectId, PlayerThaumKnowledge knowledge) {
        return place(q, aspectId, knowledge, true);
    }

    /**
     * Places an aspect with a server-selected point-cost policy. Research
     * Mastery may waive the point cost, but the classic rule still requires
     * the player to own at least one point before the roll is applied.
     */
    public PlacementResult place(
            int q,
            String aspectId,
            PlayerThaumKnowledge knowledge,
            boolean consumeAspect
    ) {
        PlacementResult validation = validatePlacement(q, aspectId, knowledge);
        if (validation != PlacementResult.PLACED) {
            return validation;
        }
        if (consumeAspect && !knowledge.tryConsumeAspect(aspectId)) {
            return PlacementResult.ASPECT_DEPLETED;
        }

        placements.put(q, aspectId);
        return isComplete()
                ? PlacementResult.PLACED_AND_COMPLETED
                : PlacementResult.PLACED;
    }

    /**
     * Restores an aspect already persisted on research notes. Loading saved
     * state must never consume the player's live aspect pool.
     */
    public boolean restorePlacement(int q, String aspectId) {
        if (!isCell(q)
                || isAnchor(q)
                || placements.containsKey(q)
                || catalog.lookup(aspectId).isEmpty()) {
            return false;
        }
        placements.put(q, aspectId);
        return true;
    }

    /**
     * Checks a prospective placement without changing the puzzle. The client
     * uses this to highlight the hex that can accept a dragged aspect, while
     * {@link #place(int, String, PlayerThaumKnowledge)} remains authoritative.
     */
    public PlacementResult validatePlacement(int q, String aspectId, PlayerThaumKnowledge knowledge) {
        Objects.requireNonNull(knowledge, "knowledge");
        if (!isCell(q)) {
            return PlacementResult.CELL_OUT_OF_BOUNDS;
        }
        if (isComplete()) {
            return PlacementResult.ALREADY_COMPLETE;
        }
        if (isAnchor(q)) {
            return PlacementResult.ANCHOR_LOCKED;
        }
        if (placements.containsKey(q)) {
            return PlacementResult.CELL_OCCUPIED;
        }
        if (catalog.lookup(aspectId).isEmpty()) {
            return PlacementResult.ASPECT_NOT_REGISTERED;
        }
        if (!knowledge.knowsAspect(aspectId)) {
            return PlacementResult.ASPECT_NOT_KNOWN;
        }
        if (knowledge.aspectAmount(aspectId) < 1) {
            return PlacementResult.ASPECT_DEPLETED;
        }

        return PlacementResult.PLACED;
    }

    public EraseResult erase(int q) {
        if (!isCell(q)) {
            return EraseResult.CELL_OUT_OF_BOUNDS;
        }
        if (isComplete()) {
            return EraseResult.ALREADY_COMPLETE;
        }
        if (isAnchor(q)) {
            return EraseResult.ANCHOR_LOCKED;
        }
        if (!placements.containsKey(q)) {
            return EraseResult.CELL_EMPTY;
        }

        placements.remove(q);
        return EraseResult.ERASED;
    }

    public Optional<String> aspectAt(int q) {
        return Optional.ofNullable(placements.get(q));
    }

    public Map<Integer, String> placements() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(placements));
    }

    public boolean isAnchor(int q) {
        return ANCHORS.containsKey(q);
    }

    public boolean hasRelatedNeighbor(int q) {
        String aspectId = placements.get(q);
        if (aspectId == null) {
            return false;
        }
        for (int neighborQ : new int[] {q - 1, q + 1}) {
            String neighbor = placements.get(neighborQ);
            if (neighbor != null && catalog.related(aspectId, neighbor)) {
                return true;
            }
        }
        return false;
    }

    public boolean isComplete() {
        for (int q = MIN_Q; q <= MAX_Q; q++) {
            if (!placements.containsKey(q)) {
                return false;
            }
            if (q < MAX_Q && !catalog.related(placements.get(q), placements.get(q + 1))) {
                return false;
            }
        }
        return true;
    }

    public static Map<Integer, String> solution() {
        return SOLUTION;
    }

    private static boolean isCell(int q) {
        return q >= MIN_Q && q <= MAX_Q;
    }

    private static Map<Integer, String> orderedReadOnlyMap(Object... entries) {
        LinkedHashMap<Integer, String> result = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            result.put((Integer) entries[index], (String) entries[index + 1]);
        }
        return Collections.unmodifiableMap(result);
    }

    public enum PlacementResult {
        PLACED,
        PLACED_AND_COMPLETED,
        CELL_OUT_OF_BOUNDS,
        ANCHOR_LOCKED,
        CELL_OCCUPIED,
        ASPECT_NOT_REGISTERED,
        ASPECT_NOT_KNOWN,
        ASPECT_DEPLETED,
        NOT_CONNECTED,
        NOT_RELATED_TO_NEIGHBOR,
        ALREADY_COMPLETE
    }

    public enum EraseResult {
        ERASED,
        CELL_OUT_OF_BOUNDS,
        ANCHOR_LOCKED,
        CELL_EMPTY,
        ALREADY_COMPLETE
    }
}
