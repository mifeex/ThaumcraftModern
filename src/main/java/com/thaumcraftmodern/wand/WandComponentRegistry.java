package com.thaumcraftmodern.wand;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Runtime holder replaced as one unit after a successful data-pack reload.
 */
public final class WandComponentRegistry {
    public static final int SERIAL_VERSION = 1;

    private static volatile WandComponentCatalog catalog;

    private WandComponentRegistry() {
    }

    public static synchronized void replace(
            Collection<WandRodDefinition> rods,
            Collection<WandCapDefinition> caps
    ) {
        catalog = new WandComponentCatalog(rods, caps);
    }

    public static WandComponentCatalog catalog() {
        WandComponentCatalog current = catalog;
        if (current == null) {
            throw new IllegalStateException(
                    "Wand component registry has not been loaded"
            );
        }
        return current;
    }

    public static Optional<WandComponentCatalog> current() {
        return Optional.ofNullable(catalog);
    }

    public static Optional<WandRodDefinition> rod(String id) {
        WandComponentCatalog current = catalog;
        return current == null ? Optional.empty() : current.rod(id);
    }

    public static Optional<WandCapDefinition> cap(String id) {
        WandComponentCatalog current = catalog;
        return current == null ? Optional.empty() : current.cap(id);
    }

    /**
     * Server-to-client data-definition sync payload. Wand ItemStack NBT is
     * synchronized by vanilla inventory packets; this catalog supplies its
     * data-driven capacity and cap properties on a dedicated client.
     */
    public static CompoundTag serialize() {
        CompoundTag root = new CompoundTag();
        root.putInt("version", SERIAL_VERSION);
        ListTag rods = new ListTag();
        ListTag caps = new ListTag();
        WandComponentCatalog current = catalog;
        if (current != null) {
            for (WandRodDefinition rod : current.rods()) {
                CompoundTag entry = new CompoundTag();
                entry.putString("id", rod.id());
                entry.putInt("capacity_vis", rod.capacityVis());
                entry.putString("translation_key", rod.translationKey());
                entry.putInt(
                        "recharge_interval_ticks",
                        rod.rechargeIntervalTicks()
                );
                entry.putInt("recharge_centivis", rod.rechargeCentivis());
                entry.putBoolean("staff", rod.staff());
                entry.putBoolean("runes", rod.runes());
                ListTag rechargeAspects = new ListTag();
                for (String aspect : rod.rechargeAspects()) {
                    rechargeAspects.add(
                            net.minecraft.nbt.StringTag.valueOf(aspect)
                    );
                }
                entry.put("recharge_aspects", rechargeAspects);
                rods.add(entry);
            }
            for (WandCapDefinition cap : current.caps()) {
                CompoundTag entry = new CompoundTag();
                entry.putString("id", cap.id());
                entry.putFloat("cost_modifier", cap.costModifier());
                entry.putString("translation_key", cap.translationKey());
                entry.putFloat(
                        "special_cost_modifier",
                        cap.specialCostModifier()
                );
                ListTag specialAspects = new ListTag();
                for (String aspect : cap.specialAspects()) {
                    specialAspects.add(
                            net.minecraft.nbt.StringTag.valueOf(aspect)
                    );
                }
                entry.put("special_aspects", specialAspects);
                caps.add(entry);
            }
        }
        root.put("rods", rods);
        root.put("caps", caps);
        return root;
    }

    public static WandComponentCatalog deserialize(CompoundTag root) {
        if (root.getInt("version") != SERIAL_VERSION) {
            throw new IllegalArgumentException(
                    "unsupported wand catalog version "
                            + root.getInt("version")
            );
        }
        List<WandRodDefinition> rods = new ArrayList<>();
        for (Tag raw : root.getList("rods", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) raw;
            List<String> rechargeAspects = new ArrayList<>();
            for (Tag aspect : entry.getList(
                    "recharge_aspects",
                    Tag.TAG_STRING
            )) {
                rechargeAspects.add(aspect.getAsString());
            }
            rods.add(new WandRodDefinition(
                    entry.getString("id"),
                    entry.getInt("capacity_vis"),
                    entry.getString("translation_key"),
                    rechargeAspects,
                    entry.getInt("recharge_interval_ticks"),
                    entry.getInt("recharge_centivis"),
                    entry.getBoolean("staff"),
                    entry.getBoolean("runes")
            ));
        }
        List<WandCapDefinition> caps = new ArrayList<>();
        for (Tag raw : root.getList("caps", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) raw;
            List<String> specialAspects = new ArrayList<>();
            for (Tag aspect : entry.getList(
                    "special_aspects",
                    Tag.TAG_STRING
            )) {
                specialAspects.add(aspect.getAsString());
            }
            caps.add(new WandCapDefinition(
                    entry.getString("id"),
                    entry.getFloat("cost_modifier"),
                    entry.getString("translation_key"),
                    specialAspects,
                    entry.contains("special_cost_modifier")
                            ? entry.getFloat("special_cost_modifier")
                            : entry.getFloat("cost_modifier")
            ));
        }
        return new WandComponentCatalog(rods, caps);
    }

    public static synchronized void replace(WandComponentCatalog next) {
        catalog = java.util.Objects.requireNonNull(next, "next");
    }
}
