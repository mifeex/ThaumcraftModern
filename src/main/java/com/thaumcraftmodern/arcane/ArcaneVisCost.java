package com.thaumcraftmodern.arcane;

import com.thaumcraftmodern.aspect.AspectCost;
import com.thaumcraftmodern.aspect.AspectCostProvider;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable six-primal vis cost stored by an arcane recipe.
 */
public final class ArcaneVisCost implements AspectCostProvider {
    public static final List<String> PRIMALS = List.of(
            "aer",
            "terra",
            "ignis",
            "aqua",
            "ordo",
            "perditio"
    );
    public static final ArcaneVisCost EMPTY = new ArcaneVisCost(Map.of());

    private final Map<String, Integer> amounts;

    public ArcaneVisCost(Map<String, Integer> amounts) {
        LinkedHashMap<String, Integer> validated = new LinkedHashMap<>();
        for (String primal : PRIMALS) {
            int amount = amounts.getOrDefault(primal, 0);
            if (amount < 0) {
                throw new IllegalArgumentException("vis cost cannot be negative: " + primal);
            }
            validated.put(primal, amount);
        }
        for (String aspectId : amounts.keySet()) {
            if (!PRIMALS.contains(aspectId)) {
                throw new IllegalArgumentException("arcane recipe cost is not primal: " + aspectId);
            }
        }
        this.amounts = Map.copyOf(validated);
    }

    public int amount(String primalId) {
        return amounts.getOrDefault(primalId, 0);
    }

    public Map<String, Integer> amounts() {
        return amounts;
    }

    public boolean isEmpty() {
        return amounts.values().stream().allMatch(amount -> amount == 0);
    }

    @Override
    public List<AspectCost> aspectCosts() {
        return PRIMALS.stream()
                .filter(primal -> amount(primal) > 0)
                .map(primal -> new AspectCost(primal, amount(primal)))
                .toList();
    }

    public static ArcaneVisCost fromJson(JsonObject json) {
        LinkedHashMap<String, Integer> values = new LinkedHashMap<>();
        for (String primal : PRIMALS) {
            if (!json.has(primal)) {
                continue;
            }
            int amount = json.get(primal).getAsInt();
            if (amount < 0) {
                throw new JsonSyntaxException("vis cost cannot be negative: " + primal);
            }
            values.put(primal, amount);
        }
        for (String key : json.keySet()) {
            if (!PRIMALS.contains(key)) {
                throw new JsonSyntaxException("unknown primal vis id: " + key);
            }
        }
        return new ArcaneVisCost(values);
    }

    public void toNetwork(FriendlyByteBuf buffer) {
        for (String primal : PRIMALS) {
            buffer.writeVarInt(amount(primal));
        }
    }

    public static ArcaneVisCost fromNetwork(FriendlyByteBuf buffer) {
        LinkedHashMap<String, Integer> values = new LinkedHashMap<>();
        for (String primal : PRIMALS) {
            values.put(primal, buffer.readVarInt());
        }
        return new ArcaneVisCost(values);
    }
}
