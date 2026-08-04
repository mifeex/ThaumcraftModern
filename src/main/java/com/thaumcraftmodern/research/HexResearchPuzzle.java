package com.thaumcraftmodern.research;

import com.thaumcraftmodern.aspect.AspectCatalog;
import com.thaumcraftmodern.aspect.AspectCost;
import com.thaumcraftmodern.knowledge.PlayerThaumKnowledge;
import net.minecraft.util.RandomSource;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** Server-authoritative TC4-style aspect-connection research puzzle. */
public final class HexResearchPuzzle {
    public static final int MIN_Q = -2;
    public static final int MAX_Q = 2;
    public static final int MAX_RADIUS = 4;
    private static final int[][] DIRECTIONS = {
            {1, 0}, {1, -1}, {0, -1}, {-1, 0}, {-1, 1}, {0, 1}
    };

    private final AspectCatalog catalog;
    private final Set<Cell> cells;
    private final Map<Cell, String> anchors;
    private final Map<Cell, String> placements = new LinkedHashMap<>();

    public HexResearchPuzzle(
            AspectCatalog catalog,
            Set<Cell> cells,
            Map<Cell, String> anchors
    ) {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        this.cells = Collections.unmodifiableSet(new LinkedHashSet<>(cells));
        this.anchors = Collections.unmodifiableMap(new LinkedHashMap<>(anchors));
        if (this.cells.isEmpty() || this.anchors.isEmpty()
                || !this.cells.containsAll(this.anchors.keySet())) {
            throw new IllegalArgumentException("puzzle needs cells containing every anchor");
        }
        for (String aspect : this.anchors.values()) {
            if (catalog.lookup(aspect).isEmpty()) {
                throw new IllegalArgumentException("catalog is missing anchor aspect: " + aspect);
            }
        }
        placements.putAll(this.anchors);
    }

    /** Compatibility fallback for old notes made before per-research recipes. */
    public HexResearchPuzzle(AspectCatalog catalog) {
        this(catalog, legacyCells(), Map.of(new Cell(-2, 0), "aer", new Cell(2, 0), "ordo"));
    }

    public static Layout classicLayout(
            int complexity,
            List<AspectCost> researchAspects,
            RandomSource random
    ) {
        Objects.requireNonNull(researchAspects, "researchAspects");
        Objects.requireNonNull(random, "random");
        if (researchAspects.isEmpty()) {
            return new Layout(legacyCells(), Map.of(
                    new Cell(-2, 0), "aer",
                    new Cell(2, 0), "ordo"
            ));
        }

        int radius = 1 + Math.min(3, Math.max(0, complexity));
        LinkedHashSet<Cell> cells = generateHexes(radius);
        List<Cell> ring = ring(radius);
        LinkedHashMap<Cell, String> anchors = new LinkedHashMap<>();
        float step = ring.size() / (float) researchAspects.size();
        float cursor = 0.0F;
        for (AspectCost cost : researchAspects) {
            anchors.put(ring.get(Math.round(cursor) % ring.size()), cost.aspectId());
            cursor += step;
        }

        int removals = complexity > 1 ? complexity * 2 : 0;
        for (int attempts = 0; removals > 0 && attempts < 10_000; attempts++) {
            List<Cell> candidates = cells.stream()
                    .filter(cell -> !anchors.containsKey(cell))
                    .toList();
            if (candidates.isEmpty()) break;
            Cell candidate = candidates.get(random.nextInt(candidates.size()));
            if (!canRemove(candidate, cells, anchors.keySet())) continue;
            cells.remove(candidate);
            removals--;
        }
        return new Layout(cells, anchors);
    }

    public PlacementResult place(
            Cell cell,
            String aspectId,
            PlayerThaumKnowledge knowledge,
            boolean consumeAspect
    ) {
        PlacementResult validation = validatePlacement(cell, aspectId, knowledge);
        if (validation != PlacementResult.PLACED) return validation;
        if (consumeAspect && !knowledge.tryConsumeAspect(aspectId)) {
            return PlacementResult.ASPECT_DEPLETED;
        }
        placements.put(cell, aspectId);
        return isComplete() ? PlacementResult.PLACED_AND_COMPLETED : PlacementResult.PLACED;
    }

    public PlacementResult place(Cell cell, String aspectId, PlayerThaumKnowledge knowledge) {
        return place(cell, aspectId, knowledge, true);
    }

    public PlacementResult place(
            int q, String aspectId, PlayerThaumKnowledge knowledge,
            boolean consumeAspect
    ) {
        return place(new Cell(q, 0), aspectId, knowledge, consumeAspect);
    }

    public PlacementResult place(int q, String aspectId, PlayerThaumKnowledge knowledge) {
        return place(new Cell(q, 0), aspectId, knowledge, true);
    }

    public boolean restorePlacement(Cell cell, String aspectId) {
        if (!cells.contains(cell) || isAnchor(cell) || placements.containsKey(cell)
                || catalog.lookup(aspectId).isEmpty()) return false;
        placements.put(cell, aspectId);
        return true;
    }

    public boolean restorePlacement(int q, String aspectId) {
        return restorePlacement(new Cell(q, 0), aspectId);
    }

    public PlacementResult validatePlacement(
            Cell cell,
            String aspectId,
            PlayerThaumKnowledge knowledge
    ) {
        Objects.requireNonNull(knowledge, "knowledge");
        if (!cells.contains(cell)) return PlacementResult.CELL_OUT_OF_BOUNDS;
        if (isComplete()) return PlacementResult.ALREADY_COMPLETE;
        if (isAnchor(cell)) return PlacementResult.ANCHOR_LOCKED;
        if (placements.containsKey(cell)) return PlacementResult.CELL_OCCUPIED;
        if (catalog.lookup(aspectId).isEmpty()) return PlacementResult.ASPECT_NOT_REGISTERED;
        if (!knowledge.knowsAspect(aspectId)) return PlacementResult.ASPECT_NOT_KNOWN;
        if (knowledge.aspectAmount(aspectId) < 1) return PlacementResult.ASPECT_DEPLETED;
        return PlacementResult.PLACED;
    }

    public PlacementResult validatePlacement(
            int q, String aspectId, PlayerThaumKnowledge knowledge
    ) {
        return validatePlacement(new Cell(q, 0), aspectId, knowledge);
    }

    public EraseResult erase(Cell cell) {
        if (!cells.contains(cell)) return EraseResult.CELL_OUT_OF_BOUNDS;
        if (isComplete()) return EraseResult.ALREADY_COMPLETE;
        if (isAnchor(cell)) return EraseResult.ANCHOR_LOCKED;
        if (!placements.containsKey(cell)) return EraseResult.CELL_EMPTY;
        placements.remove(cell);
        return EraseResult.ERASED;
    }

    public EraseResult erase(int q) {
        return erase(new Cell(q, 0));
    }

    public Optional<String> aspectAt(Cell cell) {
        return Optional.ofNullable(placements.get(cell));
    }

    public Optional<String> aspectAt(int q) {
        return aspectAt(new Cell(q, 0));
    }

    public Set<Cell> cells() {
        return cells;
    }

    public Map<Cell, String> anchors() {
        return anchors;
    }

    public Map<Cell, String> placements() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(placements));
    }

    public boolean isAnchor(Cell cell) {
        return anchors.containsKey(cell);
    }

    public boolean isAnchor(int q) {
        return isAnchor(new Cell(q, 0));
    }

    public boolean hasRelatedNeighbor(Cell cell) {
        String aspect = placements.get(cell);
        if (aspect == null) return false;
        return neighbors(cell).stream()
                .map(placements::get)
                .filter(Objects::nonNull)
                .anyMatch(neighbor -> catalog.related(aspect, neighbor));
    }

    public boolean hasRelatedNeighbor(int q) {
        return hasRelatedNeighbor(new Cell(q, 0));
    }

    public static Map<Cell, String> solution() {
        return Map.of(
                new Cell(-2, 0), "aer",
                new Cell(-1, 0), "lux",
                new Cell(0, 0), "ignis",
                new Cell(1, 0), "potentia",
                new Cell(2, 0), "ordo"
        );
    }

    public boolean isComplete() {
        Cell first = anchors.keySet().iterator().next();
        Set<Cell> connected = new LinkedHashSet<>();
        ArrayDeque<Cell> pending = new ArrayDeque<>();
        connected.add(first);
        pending.add(first);
        while (!pending.isEmpty()) {
            Cell current = pending.removeFirst();
            String currentAspect = placements.get(current);
            for (Cell neighbor : neighbors(current)) {
                String neighborAspect = placements.get(neighbor);
                if (neighborAspect != null && !connected.contains(neighbor)
                        && catalog.related(currentAspect, neighborAspect)) {
                    connected.add(neighbor);
                    pending.addLast(neighbor);
                }
            }
        }
        return connected.containsAll(anchors.keySet());
    }

    public static List<Cell> neighbors(Cell cell) {
        List<Cell> result = new ArrayList<>(6);
        for (int[] direction : DIRECTIONS) {
            result.add(new Cell(cell.q + direction[0], cell.r + direction[1]));
        }
        return result;
    }

    private static boolean canRemove(Cell candidate, Set<Cell> cells, Set<Cell> anchors) {
        for (Cell neighbor : neighbors(candidate)) {
            if (!anchors.contains(neighbor)) continue;
            long existingNeighbors = neighbors(neighbor).stream().filter(cells::contains).count();
            if (existingNeighbors < 2) return false;
        }
        return true;
    }

    private static LinkedHashSet<Cell> generateHexes(int radius) {
        LinkedHashSet<Cell> result = new LinkedHashSet<>();
        for (int q = -radius; q <= radius; q++) {
            int minimumR = Math.max(-radius, -q - radius);
            int maximumR = Math.min(radius, -q + radius);
            for (int r = minimumR; r <= maximumR; r++) result.add(new Cell(q, r));
        }
        return result;
    }

    private static List<Cell> ring(int radius) {
        Cell cursor = new Cell(0, 0);
        for (int i = 0; i < radius; i++) cursor = neighbors(cursor).get(4);
        List<Cell> result = new ArrayList<>(radius * 6);
        for (int direction = 0; direction < 6; direction++) {
            for (int step = 0; step < radius; step++) {
                result.add(cursor);
                cursor = neighbors(cursor).get(direction);
            }
        }
        return result;
    }

    private static Set<Cell> legacyCells() {
        LinkedHashSet<Cell> result = new LinkedHashSet<>();
        for (int q = -2; q <= 2; q++) result.add(new Cell(q, 0));
        return result;
    }

    public record Cell(int q, int r) implements Comparable<Cell> {
        @Override public int compareTo(Cell other) {
            int byQ = Integer.compare(q, other.q);
            return byQ != 0 ? byQ : Integer.compare(r, other.r);
        }
    }

    public record Layout(Set<Cell> cells, Map<Cell, String> anchors) {
        public Layout {
            cells = Collections.unmodifiableSet(new LinkedHashSet<>(cells));
            anchors = Collections.unmodifiableMap(new LinkedHashMap<>(anchors));
        }
    }

    public enum PlacementResult {
        PLACED, PLACED_AND_COMPLETED, CELL_OUT_OF_BOUNDS, ANCHOR_LOCKED,
        CELL_OCCUPIED, ASPECT_NOT_REGISTERED, ASPECT_NOT_KNOWN,
        ASPECT_DEPLETED, NOT_CONNECTED, NOT_RELATED_TO_NEIGHBOR, ALREADY_COMPLETE
    }

    public enum EraseResult {
        ERASED, CELL_OUT_OF_BOUNDS, ANCHOR_LOCKED, CELL_EMPTY, ALREADY_COMPLETE
    }
}
