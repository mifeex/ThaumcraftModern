package com.thaumcraftmodern.scan;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Supplies a conservative aspect composition for every registered target that
 * does not have an explicit datapack definition. Explicit scan JSON always
 * wins, so pack authors can replace any automatically inferred composition.
 */
final class AutomaticScanDefinitionFactory {
    private AutomaticScanDefinitionFactory() {
    }

    static Optional<ScanDefinition> create(ScanTargetType type, String targetId) {
        ResourceLocation id = ResourceLocation.tryParse(targetId);
        if (id == null) {
            return Optional.empty();
        }
        return switch (type) {
            case BLOCK -> BuiltInRegistries.BLOCK.getOptional(id)
                    .map(block -> definition(type, targetId, aspectsForBlock(block, id)));
            case ITEM -> BuiltInRegistries.ITEM.getOptional(id)
                    .map(item -> definition(type, targetId, aspectsForItem(item, id)));
            case ENTITY -> BuiltInRegistries.ENTITY_TYPE.getOptional(id)
                    .map(entityType -> definition(
                            type,
                            targetId,
                            aspectsForEntity(entityType, id)
                    ));
            case BLOCK_TAG, ITEM_TAG, PHENOMENON -> Optional.empty();
        };
    }

    private static ScanDefinition definition(
            ScanTargetType type,
            String targetId,
            List<AspectReward> aspects
    ) {
        return new ScanDefinition(type, targetId, "", aspects);
    }

    private static List<AspectReward> aspectsForBlock(Block block, ResourceLocation id) {
        String path = id.getPath().toLowerCase(Locale.ROOT);
        BlockState state = block.defaultBlockState();
        Rewards rewards = new Rewards();

        if (path.equals("water")) {
            return rewards.add("aqua", 4).build();
        }
        if (path.equals("lava")) {
            return rewards.add("ignis", 4).add("terra", 1).build();
        }
        if (state.is(BlockTags.FIRE)) {
            return rewards.add("ignis", 3).add("perditio", 1).build();
        }
        if (state.is(BlockTags.ICE) || state.is(BlockTags.SNOW)) {
            return rewards.add("aqua", 2).add("ordo", 1).build();
        }
        if (state.is(BlockTags.PORTALS)) {
            return rewards.add("potentia", 2).add("perditio", 1).build();
        }
        if (state.is(BlockTags.CAMPFIRES)
                || state.is(BlockTags.CANDLES)
                || path.contains("torch")
                || path.contains("lantern")) {
            return rewards.add("lux", 1).add("ignis", 1).build();
        }
        if (state.is(BlockTags.REDSTONE_ORES) || path.contains("redstone")) {
            return rewards.add("potentia", 2).add("terra", 1).build();
        }
        if (state.is(BlockTags.COAL_ORES)) {
            return rewards.add("terra", 2).add("ignis", 1).build();
        }
        if (isMetalOre(state) || containsAny(path, "iron", "gold", "copper")) {
            return rewards.add("terra", 2).add("ordo", 1).build();
        }
        if (isLivingPlant(state, path)) {
            return rewards.add("victus", 1).add("terra", 1).build();
        }
        if (state.is(BlockTags.LOGS)
                || state.is(BlockTags.PLANKS)
                || containsAny(path, "wood", "stem", "hyphae")) {
            return rewards.add("terra", 2).add("victus", 1).build();
        }
        if (state.is(BlockTags.SAND)) {
            return rewards.add("terra", 2).add("perditio", 1).build();
        }
        if (state.is(BlockTags.DIRT)
                || state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(BlockTags.BASE_STONE_NETHER)
                || state.is(BlockTags.TERRACOTTA)) {
            return rewards.add("terra", 2).build();
        }
        if (containsAny(path, "glass", "crystal")) {
            return rewards.add("ordo", 1).add("aer", 1).build();
        }
        if (containsAny(path, "tnt", "explosive")) {
            return rewards.add("perditio", 3).add("ignis", 1).build();
        }
        if (state.liquid()) {
            return rewards.add("aqua", 2).build();
        }

        int substance = block.defaultDestroyTime() >= 5.0F ? 2 : 1;
        return rewards.add("terra", substance).build();
    }

    private static List<AspectReward> aspectsForItem(Item item, ResourceLocation id) {
        String path = id.getPath().toLowerCase(Locale.ROOT);
        Rewards rewards = new Rewards();

        if (path.contains("water_bucket")) {
            return rewards.add("aqua", 4).add("ordo", 1).build();
        }
        if (path.contains("lava_bucket")) {
            return rewards.add("ignis", 4).add("terra", 1).add("ordo", 1).build();
        }
        if (item.isEdible()) {
            return rewards.add("victus", 2)
                    .add(containsAny(path, "fish", "cod", "salmon", "kelp") ? "aqua" : "terra", 1)
                    .build();
        }
        if (path.contains("redstone")) {
            return rewards.add("potentia", 2).build();
        }
        if (containsAny(path, "coal", "charcoal", "blaze")) {
            return rewards.add("ignis", 2).add("potentia", 1).build();
        }
        if (containsAny(path, "feather", "elytra")) {
            return rewards.add("aer", 2).build();
        }
        if (containsAny(path, "potion", "bottle")) {
            return rewards.add("aqua", 1).add("ordo", 1).build();
        }
        if (containsAny(path, "book", "paper", "map")) {
            return rewards.add("ordo", 2).add("aer", 1).build();
        }
        if (item instanceof TieredItem
                || item instanceof ArmorItem
                || item instanceof ProjectileWeaponItem) {
            return rewards.add("ordo", 2).add("terra", 1).build();
        }
        if (containsAny(path, "gunpowder", "firework", "fire_charge")) {
            return rewards.add("perditio", 2).add("ignis", 1).build();
        }
        return rewards.add("ordo", 1).build();
    }

    private static List<AspectReward> aspectsForEntity(
            EntityType<?> entityType,
            ResourceLocation id
    ) {
        String path = id.getPath().toLowerCase(Locale.ROOT);
        MobCategory category = entityType.getCategory();
        Rewards rewards = new Rewards();

        if (category == MobCategory.WATER_CREATURE
                || category == MobCategory.WATER_AMBIENT
                || category == MobCategory.UNDERGROUND_WATER_CREATURE
                || category == MobCategory.AXOLOTLS) {
            return rewards.add("victus", 2).add("aqua", 2).build();
        }
        if (category == MobCategory.CREATURE) {
            return rewards.add("victus", 3).build();
        }
        if (category == MobCategory.MONSTER) {
            return rewards.add("perditio", 2).add("victus", 1).build();
        }
        if (category == MobCategory.AMBIENT) {
            return rewards.add("aer", 1).add("victus", 1).build();
        }
        if (containsAny(path, "lightning", "experience_orb")) {
            return rewards.add("potentia", 3).add("aer", 1).build();
        }
        if (containsAny(path, "fireball", "firework", "tnt")) {
            return rewards.add("ignis", 2).add("perditio", 2).build();
        }
        if (containsAny(path, "arrow", "projectile", "snowball", "trident")) {
            return rewards.add("aer", 2).add("ordo", 1).build();
        }
        if (containsAny(path, "boat", "minecart")) {
            return rewards.add("ordo", 2).add("terra", 1).build();
        }
        return rewards.add("ordo", 1).build();
    }

    private static boolean isMetalOre(BlockState state) {
        return state.is(BlockTags.IRON_ORES)
                || state.is(BlockTags.GOLD_ORES)
                || state.is(BlockTags.COPPER_ORES);
    }

    private static boolean isLivingPlant(BlockState state, String path) {
        return state.is(BlockTags.LEAVES)
                || state.is(BlockTags.SAPLINGS)
                || state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.CROPS)
                || containsAny(path, "grass", "vine", "moss", "cactus", "mushroom");
    }

    private static boolean containsAny(String value, String... fragments) {
        for (String fragment : fragments) {
            if (value.contains(fragment)) {
                return true;
            }
        }
        return false;
    }

    private static final class Rewards {
        private final Map<String, Integer> amounts = new LinkedHashMap<>();

        private Rewards add(String aspectId, int amount) {
            amounts.merge(aspectId, amount, Integer::sum);
            return this;
        }

        private List<AspectReward> build() {
            List<AspectReward> result = new ArrayList<>(amounts.size());
            amounts.forEach((id, amount) -> result.add(new AspectReward(id, amount)));
            return List.copyOf(result);
        }
    }
}
