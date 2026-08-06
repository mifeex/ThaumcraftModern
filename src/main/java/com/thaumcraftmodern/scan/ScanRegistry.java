package com.thaumcraftmodern.scan;

import java.util.Collection;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import com.thaumcraftmodern.config.ThaumcraftModernServerConfig;

public final class ScanRegistry {
    private static volatile Map<String, ScanDefinition> definitions = Map.of();
    private static volatile Map<String, ScanDefinition> explicitDefinitions = Map.of();
    private static final Map<String, Optional<ScanDefinition>> AUTOMATIC_DEFINITIONS =
            new ConcurrentHashMap<>();
    private static final Map<String, String> BLOCK_SCAN_ALIASES = Map.of(
            "thaumcraftmodern:deepslate_air_infused_stone",
            "thaumcraftmodern:air_infused_stone",
            "thaumcraftmodern:deepslate_fire_infused_stone",
            "thaumcraftmodern:fire_infused_stone",
            "thaumcraftmodern:deepslate_water_infused_stone",
            "thaumcraftmodern:water_infused_stone",
            "thaumcraftmodern:deepslate_earth_infused_stone",
            "thaumcraftmodern:earth_infused_stone",
            "thaumcraftmodern:deepslate_order_infused_stone",
            "thaumcraftmodern:order_infused_stone",
            "thaumcraftmodern:deepslate_entropy_infused_stone",
            "thaumcraftmodern:entropy_infused_stone"
    );

    private ScanRegistry() {
    }

    public static synchronized void replace(Collection<ScanDefinition> values) {
        Map<String, ScanDefinition> next = new LinkedHashMap<>();
        values.stream()
                .sorted(Comparator.comparing(ScanDefinition::scanKey))
                .forEach(definition -> {
                    if (next.put(definition.scanKey(), definition) != null) {
                        throw new IllegalArgumentException(
                                "Duplicate scan definition: " + definition.scanKey()
                        );
                    }
                });
        definitions = Map.copyOf(next);
        explicitDefinitions = definitions;
        AUTOMATIC_DEFINITIONS.clear();
    }

    /** Replaces only the runtime recipe-derived layer; datapack definitions win. */
    public static synchronized void replaceGenerated(
            Collection<ScanDefinition> generated
    ) {
        Map<String, ScanDefinition> next = new LinkedHashMap<>(explicitDefinitions);
        generated.stream()
                .sorted(Comparator.comparing(ScanDefinition::scanKey))
                .forEach(definition -> next.putIfAbsent(
                        definition.scanKey(), definition));
        definitions = Map.copyOf(next);
        AUTOMATIC_DEFINITIONS.clear();
    }

    public static Optional<ScanDefinition> find(ScanTargetType type, String targetId) {
        return find(
                type,
                targetId,
                ThaumcraftModernServerConfig.automaticScanFallback(),
                AutomaticScanDefinitionFactory::create
        );
    }

    static Optional<ScanDefinition> find(
            ScanTargetType type,
            String targetId,
            boolean allowAutomaticFallback,
            BiFunction<ScanTargetType, String, Optional<ScanDefinition>> automaticFactory
    ) {
        String key = scanKey(type, targetId);
        ScanDefinition explicit = definitions.get(key);
        if (explicit != null) {
            return Optional.of(explicit);
        }
        Optional<ScanDefinition> tagged = findTagDefinition(type, targetId);
        if (tagged.isPresent()) {
            return tagged;
        }
        if (!allowAutomaticFallback) {
            return Optional.empty();
        }
        return AUTOMATIC_DEFINITIONS.computeIfAbsent(
                key,
                ignored -> automaticFactory.apply(type, targetId)
        );
    }

    /**
     * Resolves a definition for a scan key that is already present in saved
     * player knowledge. The compatibility config controls creation of new
     * inferred scans, not interpretation of scans completed while it was
     * enabled.
     */
    public static Optional<ScanDefinition> findByScanKey(String scanKey) {
        if (scanKey == null) {
            return Optional.empty();
        }
        int separator = scanKey.indexOf(':');
        if (separator <= 0 || separator >= scanKey.length() - 1) {
            return Optional.empty();
        }
        try {
            ScanTargetType type = ScanTargetType.valueOf(
                    scanKey.substring(0, separator).toUpperCase(Locale.ROOT)
            );
            return findHistorical(type, scanKey.substring(separator + 1));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    public static Optional<ScanDefinition> findHistorical(
            ScanTargetType type,
            String targetId
    ) {
        return find(
                type,
                targetId,
                true,
                AutomaticScanDefinitionFactory::create
        );
    }

    public static ItemScanIdentity identityForItem(ItemStack stack) {
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        if (findExplicit(ScanTargetType.ITEM, itemId).isPresent()
                || findTagDefinition(ScanTargetType.ITEM, itemId).isPresent()) {
            return new ItemScanIdentity(ScanTargetType.ITEM, itemId);
        }
        if (stack.getItem() instanceof BlockItem blockItem) {
            String blockId = canonicalBlockId(
                    BuiltInRegistries.BLOCK.getKey(blockItem.getBlock()).toString()
            );
            return new ItemScanIdentity(ScanTargetType.BLOCK, blockId);
        }
        return new ItemScanIdentity(ScanTargetType.ITEM, itemId);
    }

    public static Optional<ScanDefinition> findForItem(ItemStack stack) {
        ItemScanIdentity identity = identityForItem(stack);
        return find(identity.type(), identity.targetId());
    }

    /**
     * Resolves only datapack-defined aspects for an item. Unlike
     * {@link #findForItem(ItemStack)}, this never creates an automatically
     * inferred definition. It is used when the aspects have gameplay value,
     * such as dissolving an item in a Crucible.
     */
    public static Optional<ScanDefinition> findExplicitForItem(
            ItemStack stack
    ) {
        ItemScanIdentity identity = identityForItem(stack);
        return findExplicit(identity.type(), identity.targetId())
                .or(() -> findTagDefinition(
                        identity.type(),
                        identity.targetId()
                ));
    }

    public static List<ScanDefinition> all() {
        return List.copyOf(definitions.values());
    }

    private static Optional<ScanDefinition> findExplicit(
            ScanTargetType type,
            String targetId
    ) {
        return Optional.ofNullable(definitions.get(scanKey(type, targetId)));
    }

    private static Optional<ScanDefinition> findTagDefinition(
            ScanTargetType requestedType,
            String targetId
    ) {
        ScanTargetType tagType = requestedType == ScanTargetType.ITEM
                ? ScanTargetType.ITEM_TAG
                : requestedType == ScanTargetType.BLOCK
                ? ScanTargetType.BLOCK_TAG
                : null;
        if (tagType == null || definitions.values().stream()
                .noneMatch(definition -> definition.type() == tagType)) {
            return Optional.empty();
        }
        ResourceLocation id = ResourceLocation.tryParse(targetId);
        if (id == null) {
            return Optional.empty();
        }
        if (requestedType == ScanTargetType.ITEM) {
            return BuiltInRegistries.ITEM.getOptional(id).flatMap(item ->
                    definitions.values().stream()
                            .filter(definition ->
                                    definition.type() == ScanTargetType.ITEM_TAG)
                            .filter(definition -> item.builtInRegistryHolder().is(
                                    TagKey.create(
                                            Registries.ITEM,
                                            new ResourceLocation(
                                                    definition.targetId()
                                            )
                                    )
                            ))
                            .min(Comparator.comparing(ScanDefinition::scanKey))
            );
        }
        if (requestedType == ScanTargetType.BLOCK) {
            return BuiltInRegistries.BLOCK.getOptional(id).flatMap(block ->
                    definitions.values().stream()
                            .filter(definition ->
                                    definition.type() == ScanTargetType.BLOCK_TAG)
                            .filter(definition -> block.builtInRegistryHolder().is(
                                    TagKey.create(
                                            Registries.BLOCK,
                                            new ResourceLocation(
                                                    definition.targetId()
                                            )
                                    )
                            ))
                            .min(Comparator.comparing(ScanDefinition::scanKey))
            );
        }
        return Optional.empty();
    }

    public static String scanKey(ScanTargetType type, String targetId) {
        return type.name().toLowerCase(Locale.ROOT) + ":" + targetId;
    }

    /** Returns the shared player-knowledge key selected by a direct or tag scan. */
    public static String knowledgeKey(ScanTargetType type, String targetId) {
        return find(type, targetId)
                .map(ScanDefinition::knowledgeKey)
                .orElseGet(() -> scanKey(type, targetId));
    }

    /**
     * Compatibility aliases share one scan definition and one player-knowledge
     * key. Deepslate infused stone differs only in host rock, not in essentia.
     */
    public static String canonicalBlockId(String blockId) {
        return BLOCK_SCAN_ALIASES.getOrDefault(blockId, blockId);
    }

    public static CompoundTag serialize() {
        CompoundTag root = new CompoundTag();
        ListTag entries = new ListTag();
        for (ScanDefinition definition : all()) {
            CompoundTag entry = new CompoundTag();
            entry.putString("type", definition.type().name());
            entry.putString("target", definition.targetId());
            entry.putString("display", definition.displayKey());
            entry.putString("knowledge_key", definition.knowledgeKey());
            ListTag aspects = new ListTag();
            for (AspectReward reward : definition.aspects()) {
                CompoundTag aspect = new CompoundTag();
                aspect.putString("id", reward.aspectId());
                aspect.putInt("amount", reward.amount());
                aspects.add(aspect);
            }
            entry.put("aspects", aspects);
            entries.add(entry);
        }
        root.put("entries", entries);
        return root;
    }

    public static List<ScanDefinition> deserialize(CompoundTag root) {
        List<ScanDefinition> result = new ArrayList<>();
        for (Tag raw : root.getList("entries", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) raw;
            List<AspectReward> aspects = new ArrayList<>();
            for (Tag rawAspect : entry.getList("aspects", Tag.TAG_COMPOUND)) {
                CompoundTag aspect = (CompoundTag) rawAspect;
                aspects.add(new AspectReward(
                        aspect.getString("id"),
                        aspect.getInt("amount")
                ));
            }
            result.add(new ScanDefinition(
                    ScanTargetType.valueOf(entry.getString("type")),
                    entry.getString("target"),
                    entry.getString("display"),
                    aspects,
                    entry.contains("knowledge_key", Tag.TAG_STRING)
                            ? entry.getString("knowledge_key")
                            : null
            ));
        }
        return result;
    }

    public record ItemScanIdentity(ScanTargetType type, String targetId) {
    }
}
