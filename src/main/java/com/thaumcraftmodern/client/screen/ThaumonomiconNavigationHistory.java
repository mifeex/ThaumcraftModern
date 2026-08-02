package com.thaumcraftmodern.client.screen;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

/** Exact nested-research return locations, independent of page arrows. */
final class ThaumonomiconNavigationHistory {
    private final Deque<Location> entries = new ArrayDeque<>();

    void push(String researchId, int pagePair, String categoryId) {
        entries.push(new Location(researchId, pagePair, categoryId));
    }

    Optional<Location> pop() {
        return entries.isEmpty() ? Optional.empty() : Optional.of(entries.pop());
    }

    boolean isEmpty() { return entries.isEmpty(); }
    int depth() { return entries.size(); }
    void clear() { entries.clear(); }

    record Location(String researchId, int pagePair, String categoryId) {
    }
}
