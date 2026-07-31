package com.thaumcraftmodern.aspect;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public final class AspectRegistryRuntime {
    private static volatile AspectCatalog catalog;

    private AspectRegistryRuntime() {
    }

    public static synchronized void replace(Collection<AspectDefinition> definitions) {
        catalog = new AspectCatalog(definitions);
    }

    public static AspectCatalog catalog() {
        AspectCatalog current = catalog;
        if (current == null) {
            throw new IllegalStateException("Aspect registry has not been loaded");
        }
        return current;
    }

    public static Optional<AspectDefinition> find(String id) {
        AspectCatalog current = catalog;
        return current == null ? Optional.empty() : current.lookup(id);
    }

    public static CompoundTag serialize() {
        CompoundTag root = new CompoundTag();
        ListTag definitions = new ListTag();
        AspectCatalog current = catalog;
        if (current != null) {
            for (AspectDefinition definition : current.definitions()) {
                CompoundTag entry = new CompoundTag();
                entry.putString("id", definition.id());
                entry.putInt("color", definition.color());
                entry.putString("icon", definition.icon());
                entry.putInt("order", definition.order());
                ListTag components = new ListTag();
                definition.components().stream()
                        .map(StringTag::valueOf)
                        .forEach(components::add);
                entry.put("components", components);
                definitions.add(entry);
            }
        }
        root.put("definitions", definitions);
        return root;
    }

    public static List<AspectDefinition> deserialize(CompoundTag root) {
        List<AspectDefinition> definitions = new ArrayList<>();
        for (Tag raw : root.getList("definitions", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) raw;
            List<String> components = new ArrayList<>();
            ListTag componentTags = entry.getList("components", Tag.TAG_STRING);
            for (int index = 0; index < componentTags.size(); index++) {
                components.add(componentTags.getString(index));
            }
            definitions.add(new AspectDefinition(
                    entry.getString("id"),
                    entry.getInt("color"),
                    entry.getString("icon"),
                    components,
                    entry.getInt("order")
            ));
        }
        return definitions;
    }
}
