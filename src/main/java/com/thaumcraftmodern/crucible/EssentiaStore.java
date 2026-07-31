package com.thaumcraftmodern.crucible;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Small deterministic aspect multiset used by the Crucible.
 *
 * <p>TC4 permits additions above the nominal 100 essentia limit and removes
 * the overflow one point at a time. The limit is therefore enforced by the
 * Crucible tick, not by this container.</p>
 */
public final class EssentiaStore {
    private final LinkedHashMap<String, Integer> amounts = new LinkedHashMap<>();

    public int amount(String aspect) {
        return amounts.getOrDefault(aspect, 0);
    }

    public int total() {
        return amounts.values().stream().mapToInt(Integer::intValue).sum();
    }

    public boolean isEmpty() {
        return amounts.isEmpty();
    }

    public Map<String, Integer> view() {
        return Collections.unmodifiableMap(amounts);
    }

    public void add(String aspect, int amount) {
        if (aspect == null || aspect.isBlank() || amount <= 0) {
            return;
        }
        amounts.merge(aspect, amount, Integer::sum);
    }

    public boolean contains(Map<String, Integer> required) {
        return required.entrySet().stream().allMatch(entry ->
                entry.getValue() > 0
                        && amount(entry.getKey()) >= entry.getValue()
        );
    }

    public boolean remove(String aspect, int amount) {
        int current = amount(aspect);
        if (amount <= 0 || current < amount) {
            return false;
        }
        int remaining = current - amount;
        if (remaining == 0) {
            amounts.remove(aspect);
        } else {
            amounts.put(aspect, remaining);
        }
        return true;
    }

    public boolean removeAll(Map<String, Integer> required) {
        if (!contains(required)) {
            return false;
        }
        required.forEach(this::remove);
        return true;
    }

    public void clear() {
        amounts.clear();
    }

    public CompoundTag save() {
        CompoundTag root = new CompoundTag();
        ListTag entries = new ListTag();
        amounts.forEach((aspect, amount) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("Aspect", aspect);
            entry.putInt("Amount", amount);
            entries.add(entry);
        });
        root.put("Entries", entries);
        return root;
    }

    public void load(CompoundTag root) {
        amounts.clear();
        for (Tag raw : root.getList("Entries", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) raw;
            add(entry.getString("Aspect"), entry.getInt("Amount"));
        }
    }
}
