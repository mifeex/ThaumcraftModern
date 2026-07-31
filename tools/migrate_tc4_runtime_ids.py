#!/usr/bin/env python3
"""Replace TC4/1.7.10 runtime identities with Forge 1.20.1 identities.

Only live fields are rewritten.  The nested ``legacy`` object is immutable
provenance and deliberately keeps the original SRG fields, metadata, Ore
Dictionary names, entity names and source expressions.
"""

from __future__ import annotations

import json
import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
DATA = ROOT / "src/main/resources/data/thaumcraftmodern"
SCANS = DATA / "thaumcraft/scans/legacy"
RESEARCH = DATA / "thaumcraft/research"
RECIPES = DATA / "recipes"
MCP_FIELDS = ROOT / (
    "data/legacy_tc4_4_2_3_5/modern_migration/"
    "mcp_stable_12_1_7_10_fields.json"
)

UNMAPPED_REASON = "legacy item, metadata, or Ore Dictionary target is not mapped"
UNMAPPED_ENTITY_REASON = "legacy entity type is not implemented or mapped"
COLLAPSED_REASON = "multiple legacy registrations collapse to one modern target"
RECIPE_DERIVED_REASON = "recipe-derived base aspects cannot yet be reproduced safely"

SPECIAL_TARGETS = {
    "dyes[i]": ("item_tag", "forge:dyes"),
}

# Ore Dictionary was a group identity.  Keep that behavior through modern
# block/item tags instead of picking an arbitrary member of the group.
ORE_TAGS: dict[str, tuple[str, str]] = {
    "stone": ("block_tag", "forge:stone"),
    "cobblestone": ("block_tag", "forge:cobblestone"),
    "logWood": ("block_tag", "minecraft:logs"),
    "plankWood": ("block_tag", "minecraft:planks"),
    "slabWood": ("block_tag", "minecraft:wooden_slabs"),
    "stairWood": ("block_tag", "minecraft:wooden_stairs"),
    "stickWood": ("item_tag", "forge:rods/wooden"),
    "treeSapling": ("block_tag", "minecraft:saplings"),
    "treeLeaves": ("block_tag", "minecraft:leaves"),
    "oreLapis": ("block_tag", "forge:ores/lapis"),
    "oreDiamond": ("block_tag", "forge:ores/diamond"),
    "gemDiamond": ("item_tag", "forge:gems/diamond"),
    "oreRedstone": ("block_tag", "forge:ores/redstone"),
    "oreEmerald": ("block_tag", "forge:ores/emerald"),
    "gemEmerald": ("item_tag", "forge:gems/emerald"),
    "oreQuartz": ("block_tag", "forge:ores/quartz"),
    "nuggetIron": ("item_tag", "forge:nuggets/iron"),
    "oreIron": ("block_tag", "forge:ores/iron"),
    "dustIron": ("item_tag", "forge:dusts/iron"),
    "oreGold": ("block_tag", "forge:ores/gold"),
    "dustGold": ("item_tag", "forge:dusts/gold"),
    "dustRedstone": ("item_tag", "forge:dusts/redstone"),
    "dustGlowstone": ("item_tag", "forge:dusts/glowstone"),
    "glowstone": ("block", "minecraft:glowstone"),
    "dustCopper": ("item_tag", "forge:dusts/copper"),
    "nuggetTin": ("item_tag", "forge:nuggets/tin"),
    "ingotTin": ("item_tag", "forge:ingots/tin"),
    "dustTin": ("item_tag", "forge:dusts/tin"),
    "oreTin": ("block_tag", "forge:ores/tin"),
    "nuggetSilver": ("item_tag", "forge:nuggets/silver"),
    "ingotSilver": ("item_tag", "forge:ingots/silver"),
    "dustSilver": ("item_tag", "forge:dusts/silver"),
    "oreSilver": ("block_tag", "forge:ores/silver"),
    "nuggetLead": ("item_tag", "forge:nuggets/lead"),
    "ingotLead": ("item_tag", "forge:ingots/lead"),
    "dustLead": ("item_tag", "forge:dusts/lead"),
    "oreLead": ("block_tag", "forge:ores/lead"),
}

ENTITY_IDS = {
    "Thaumcraft.Firebat": "thaumcraftmodern:firebat",
    "Thaumcraft.Pech": "thaumcraftmodern:pech",
    "Thaumcraft.ThaumSlime": "thaumcraftmodern:thaumic_slime",
    "Thaumcraft.BrainyZombie": "thaumcraftmodern:angry_zombie",
    "Thaumcraft.GiantBrainyZombie": "thaumcraftmodern:furious_zombie",
    "Thaumcraft.Taintacle": "thaumcraftmodern:taintacle",
    "Thaumcraft.TaintacleTiny": "thaumcraftmodern:taint_tendril",
    "Thaumcraft.TaintSpider": "thaumcraftmodern:tainted_crawler",
    "Thaumcraft.TaintSpore": "thaumcraftmodern:taint_spore",
    "Thaumcraft.TaintSwarmer": "thaumcraftmodern:taint_spore_swarmer",
    "Thaumcraft.TaintSwarm": "thaumcraftmodern:taint_swarm",
    "Thaumcraft.TaintedPig": "thaumcraftmodern:tainted_pig",
    "Thaumcraft.TaintedSheep": "thaumcraftmodern:tainted_sheep",
    "Thaumcraft.TaintedCow": "thaumcraftmodern:tainted_cow",
    "Thaumcraft.TaintedChicken": "thaumcraftmodern:tainted_chicken",
    "Thaumcraft.TaintedVillager": "thaumcraftmodern:tainted_villager",
    "Thaumcraft.TaintedCreeper": "thaumcraftmodern:tainted_creeper",
    "Thaumcraft.MindSpider": "thaumcraftmodern:mind_spider",
    "Thaumcraft.EldritchGuardian": "thaumcraftmodern:eldritch_guardian",
    "Thaumcraft.EldritchOrb": "thaumcraftmodern:eldritch_orb",
    "Thaumcraft.CultistKnight": "thaumcraftmodern:crimson_knight",
    "Thaumcraft.CultistCleric": "thaumcraftmodern:crimson_cleric",
    "Thaumcraft.Wisp": "thaumcraftmodern:wisp",
}

VANILLA_RENAMES = {
    "clay": "clay",
    "double_plant": "sunflower",
    "grass": "grass_block",
    "red_flower": "poppy",
    "yellow_flower": "dandelion",
    "tallgrass": "grass",
    "waterlily": "lily_pad",
    "monster_egg": "infested_stone",
    "portal": "nether_portal",
    "mob_spawner": "spawner",
    "web": "cobweb",
    "reeds": "sugar_cane",
    "deadbush": "dead_bush",
    "noteblock": "note_block",
    "golden_rail": "powered_rail",
    "stonebrick": "stone_bricks",
    "lit_furnace": "furnace",
    "lit_redstone_ore": "redstone_ore",
    "lit_pumpkin": "jack_o_lantern",
    "fireball": "fire_charge",
    "netherbrick": "nether_brick",
    "wooden_door": "oak_door",
    "trapdoor": "oak_trapdoor",
    "fence_gate": "oak_fence_gate",
    "wooden_pressure_plate": "oak_pressure_plate",
    "wooden_button": "oak_button",
    "boat": "oak_boat",
    "potionitem": "potion",
    "fish": "cod",
    "cooked_fish": "cooked_cod",
    "skull": "skeleton_skull",
    "record_13": "music_disc_13",
    "record_cat": "music_disc_cat",
    "record_blocks": "music_disc_blocks",
    "record_chirp": "music_disc_chirp",
    "record_far": "music_disc_far",
    "record_mall": "music_disc_mall",
    "record_mellohi": "music_disc_mellohi",
    "record_stal": "music_disc_stal",
    "record_strad": "music_disc_strad",
    "record_ward": "music_disc_ward",
    "record_11": "music_disc_11",
    "record_wait": "music_disc_wait",
    "nether_brick": "nether_bricks",
    "melon_block": "melon",
    "hardened_clay": "terracotta",
    "stained_hardened_clay": "white_terracotta",
    "stained_glass": "white_stained_glass",
    "wool": "white_wool",
}

VANILLA_META = {
    ("Blocks", "dirt", 2): ("block", "minecraft:podzol"),
    ("Blocks", "sandstone", 1): ("block", "minecraft:chiseled_sandstone"),
    ("Blocks", "sandstone", 2): ("block", "minecraft:cut_sandstone"),
    ("Blocks", "stonebrick", 1): ("block", "minecraft:mossy_stone_bricks"),
    ("Blocks", "stonebrick", 2): ("block", "minecraft:cracked_stone_bricks"),
    ("Blocks", "stonebrick", 3): ("block", "minecraft:chiseled_stone_bricks"),
    ("Items", "golden_apple", 1): ("item", "minecraft:enchanted_golden_apple"),
    ("Items", "skull", 0): ("item", "minecraft:skeleton_skull"),
    ("Items", "skull", 1): ("item", "minecraft:wither_skeleton_skull"),
    ("Items", "skull", 2): ("item", "minecraft:zombie_head"),
    ("Items", "skull", 3): ("item", "minecraft:player_head"),
    ("Items", "skull", 4): ("item", "minecraft:creeper_head"),
}

RESEARCH_RESOURCE_ICONS = {
    "ASPECTS": "thaumcraftmodern:textures/misc/r_aspects.png",
    "ELDRITCHMAJOR": "thaumcraftmodern:textures/misc/r_eldritchmajor.png",
    "RESEARCHER1": "thaumcraftmodern:textures/misc/r_researcher1.png",
    "RESEARCHER2": "thaumcraftmodern:textures/misc/r_researcher2.png",
    "WARP": "thaumcraftmodern:textures/misc/r_warp.png",
}

TC_STACKS: dict[tuple[str, int | None], tuple[str, str]] = {
    ("ConfigItems.itemThaumonomicon", None): ("item", "thaumcraftmodern:thaumonomicon"),
    ("ConfigItems.itemZombieBrain", None): ("item", "thaumcraftmodern:zombie_brain"),
    ("ConfigItems.itemGoggles", None): ("item", "thaumcraftmodern:goggles_of_revealing"),
    ("ConfigItems.itemWispEssence", 0): ("item", "thaumcraftmodern:ethereal_essence"),
    ("ConfigItems.itemCrystalEssence", 0): ("item", "thaumcraftmodern:ethereal_essence"),
    ("ConfigItems.itemEssence", 0): ("item", "thaumcraftmodern:essentia_phial"),
    ("ConfigItems.itemEssence", 1): ("item", "thaumcraftmodern:ethereal_essence"),
    ("ConfigItems.itemPrimalArrow", None): (
        "item_tag",
        "thaumcraftmodern:primal_arrows",
    ),
    ("ConfigItems.itemResource", 3): ("item", "thaumcraftmodern:quicksilver"),
    ("ConfigItems.itemResource", 6): ("item", "thaumcraftmodern:amber"),
    ("ConfigItems.itemResource", 11): ("item", "thaumcraftmodern:tainted_goo"),
    ("ConfigItems.itemResource", 12): ("item", "thaumcraftmodern:taint_tendril"),
    ("ConfigItems.itemResource", 18): ("item", "thaumcraftmodern:gold_coin"),
    ("ConfigItems.itemNugget", 0): ("item", "minecraft:iron_nugget"),
    ("ConfigItems.itemNugget", 1): ("item", "thaumcraftmodern:copper_nugget"),
    ("ConfigItems.itemNugget", 3): ("item", "thaumcraftmodern:silver_nugget"),
    ("ConfigItems.itemNugget", 5): ("item", "thaumcraftmodern:quicksilver"),
    ("ConfigItems.itemNugget", 6): ("item", "thaumcraftmodern:thaumium_nugget"),
    ("ConfigItems.itemEldritchObject", 0): ("item", "thaumcraftmodern:eldritch_eye"),
    ("ConfigItems.itemEldritchObject", 1): ("item", "thaumcraftmodern:crimson_rites"),
    ("ConfigItems.itemEldritchObject", 2): ("item", "thaumcraftmodern:void_seed"),
    ("ConfigItems.itemEldritchObject", 3): ("item", "thaumcraftmodern:primordial_pearl"),
    ("ConfigItems.itemLootbag", 0): ("item", "thaumcraftmodern:common_loot_bag"),
    ("ConfigItems.itemLootbag", 1): ("item", "thaumcraftmodern:uncommon_loot_bag"),
    ("ConfigItems.itemLootbag", 2): ("item", "thaumcraftmodern:rare_loot_bag"),
    ("ConfigBlocks.blockCustomOre", 0): ("block", "thaumcraftmodern:cinnabar_ore"),
    ("ConfigBlocks.blockCustomOre", 1): ("block", "thaumcraftmodern:air_infused_stone"),
    ("ConfigBlocks.blockCustomOre", 2): ("block", "thaumcraftmodern:fire_infused_stone"),
    ("ConfigBlocks.blockCustomOre", 3): ("block", "thaumcraftmodern:water_infused_stone"),
    ("ConfigBlocks.blockCustomOre", 4): ("block", "thaumcraftmodern:earth_infused_stone"),
    ("ConfigBlocks.blockCustomOre", 5): ("block", "thaumcraftmodern:order_infused_stone"),
    ("ConfigBlocks.blockCustomOre", 6): ("block", "thaumcraftmodern:entropy_infused_stone"),
    ("ConfigBlocks.blockCustomOre", 7): ("block", "thaumcraftmodern:amber_ore"),
    ("ConfigBlocks.blockMagicalLog", 0): ("block", "thaumcraftmodern:greatwood_log"),
    ("ConfigBlocks.blockMagicalLog", 1): ("block", "thaumcraftmodern:silverwood_log"),
    ("ConfigBlocks.blockMagicalLeaves", 0): ("block", "thaumcraftmodern:greatwood_leaves"),
    ("ConfigBlocks.blockMagicalLeaves", 1): ("block", "thaumcraftmodern:silverwood_leaves"),
    ("ConfigBlocks.blockCustomPlant", 0): ("block", "thaumcraftmodern:greatwood_sapling"),
    ("ConfigBlocks.blockCustomPlant", 1): ("block", "thaumcraftmodern:silverwood_sapling"),
    ("ConfigBlocks.blockCustomPlant", 2): ("block", "thaumcraftmodern:shimmerleaf"),
    ("ConfigBlocks.blockCustomPlant", 3): ("block", "thaumcraftmodern:cinderpearl"),
    ("ConfigBlocks.blockCustomPlant", 4): ("block", "thaumcraftmodern:ethereal_bloom"),
    ("ConfigBlocks.blockCustomPlant", 5): ("block", "thaumcraftmodern:vishroom"),
    ("ConfigBlocks.blockTaint", 0): ("block", "thaumcraftmodern:crusted_taint"),
    ("ConfigBlocks.blockTaint", 1): ("block", "thaumcraftmodern:tainted_soil"),
    ("ConfigBlocks.blockTaintFibres", 0): ("block", "thaumcraftmodern:taint_fibres"),
    ("ConfigBlocks.blockTaintFibres", 1): ("block", "thaumcraftmodern:short_tainted_grass"),
    ("ConfigBlocks.blockTaintFibres", 2): ("block", "thaumcraftmodern:tall_tainted_grass"),
    ("ConfigBlocks.blockTaintFibres", 3): ("block", "thaumcraftmodern:spore_stalk"),
    ("ConfigBlocks.blockTaintFibres", 4): ("block", "thaumcraftmodern:mature_spore_stalk"),
    ("ConfigBlocks.blockCosmeticSolid", 0): ("block", "thaumcraftmodern:obsidian_totem"),
    ("ConfigBlocks.blockCosmeticSolid", None): ("block", "thaumcraftmodern:obsidian_totem"),
    ("ConfigBlocks.blockCosmeticSolid", 1): ("block", "thaumcraftmodern:obsidian_tile"),
    ("ConfigBlocks.blockCosmeticSolid", 6): ("block", "thaumcraftmodern:arcane_stone"),
    ("ConfigBlocks.blockCosmeticSolid", 7): ("block", "thaumcraftmodern:arcane_stone_brick"),
    ("ConfigBlocks.blockCosmeticSolid", 11): ("block", "thaumcraftmodern:ancient_stone"),
    ("ConfigBlocks.blockCosmeticSolid", 12): ("block", "thaumcraftmodern:ancient_stone"),
    ("ConfigBlocks.blockTable", None): ("block", "thaumcraftmodern:thaumcraft_table"),
    ("ConfigBlocks.blockTable", 15): ("block", "thaumcraftmodern:arcane_workbench"),
    ("ConfigBlocks.blockAlchemyFurnace", None): ("block", "thaumcraftmodern:alchemical_furnace"),
    ("ConfigBlocks.blockMetalDevice", None): ("block", "thaumcraftmodern:crucible"),
    ("ConfigBlocks.blockEldritch", 3): ("block", "thaumcraftmodern:eldritch_altar_part"),
    ("ConfigBlocks.blockEldritch", 4): ("block", "thaumcraftmodern:ancient_stone"),
    ("ConfigBlocks.blockEldritch", 5): ("block", "thaumcraftmodern:ancient_stone"),
    ("ConfigBlocks.blockEldritch", 6): ("block", "thaumcraftmodern:ancient_stone"),
}


def read(path: Path):
    return json.loads(path.read_text(encoding="utf-8"))


def write(path: Path, value) -> None:
    path.write_text(
        json.dumps(value, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )


def stack_parts(expression: str) -> tuple[str, int | None] | None:
    match = re.fullmatch(r"new ItemStack\((.*)\)", expression.strip(), re.S)
    if not match:
        return None
    values = [part.strip() for part in match.group(1).split(",")]
    base = values[0]
    metadata = None
    if len(values) >= 3 and re.fullmatch(r"-?\d+", values[2]):
        metadata = int(values[2])
        if metadata == 32767:
            metadata = None
    return base, metadata


def map_stack(expression: str, fields: dict[str, str]) -> tuple[str, str] | None:
    parsed = stack_parts(expression)
    if parsed is None:
        return None
    base, metadata = parsed
    mapped = TC_STACKS.get((base, metadata))
    if mapped is None:
        mapped = TC_STACKS.get((base, None))
    if mapped is not None:
        return mapped
    vanilla = re.fullmatch(r"(Blocks|Items)\.(field_\w+)", base)
    if vanilla is None:
        return None
    kind, field = vanilla.groups()
    old_name = fields.get(field)
    if old_name is None:
        return None
    special = VANILLA_META.get((kind, old_name, metadata))
    if special is not None:
        return special
    current_name = VANILLA_RENAMES.get(old_name, old_name)
    return (
        "block" if kind == "Blocks" else "item",
        f"minecraft:{current_name}",
    )


def migrate_scan(path: Path, fields: dict[str, str]) -> bool:
    value = read(path)
    legacy = value.get("legacy", {})
    legacy_target = legacy.get("legacy_target", value.get("target", ""))
    mapped = SPECIAL_TARGETS.get(legacy_target)
    if mapped is None and legacy.get("target_kind") == "ore_dictionary":
        mapped = ORE_TAGS.get(legacy_target)
    elif mapped is None and isinstance(legacy_target, str):
        mapped = map_stack(legacy_target, fields)
    if mapped is None and value.get("type") == "legacy_entity":
        entity = ENTITY_IDS.get(legacy.get("legacy_entity_id", legacy_target))
        if entity is not None:
            mapped = ("entity", entity)
    if mapped is None:
        return False
    target_type, target = mapped
    changed = value.get("type") != target_type or value.get("target") != target
    value["type"] = target_type
    value["target"] = target
    if value.get("inactive_reason") in {
        UNMAPPED_REASON,
        UNMAPPED_ENTITY_REASON,
    }:
        value["inactive"] = False
        value.pop("inactive_reason", None)
        changed = True
    if (
        target_type == "block"
        and target.startswith("minecraft:")
        and value.get("inactive_reason") == RECIPE_DERIVED_REASON
        and value.get("aspects")
    ):
        value["inactive"] = False
        value.pop("inactive_reason", None)
        changed = True
    if (
        target_type == "block"
        and not target.startswith("minecraft:")
        and legacy.get("registration_mode") == "recipe_derived_modifier"
        and not value.get("inactive")
    ):
        value["inactive"] = True
        value["inactive_reason"] = RECIPE_DERIVED_REASON
        changed = True
    if changed:
        write(path, value)
    return changed


def recipe_results() -> dict[str, str]:
    results = {}
    for path in RECIPES.glob("*.json"):
        value = read(path)
        result = value.get("result", {})
        if isinstance(result, dict) and isinstance(result.get("item"), str):
            results[f"thaumcraftmodern:{path.stem}"] = result["item"]
    return results


def migrate_research_icons() -> int:
    results = recipe_results()
    changed = 0
    for path in RESEARCH.rglob("*.json"):
        value = read(path)
        resource_icon = RESEARCH_RESOURCE_ICONS.get(value.get("legacy_id"))
        if resource_icon is not None:
            if value.get("icon_resource") != resource_icon or "icon" in value:
                value.pop("icon", None)
                value["icon_resource"] = resource_icon
                write(path, value)
                changed += 1
            continue
        if value.get("icon") != "thaumcraftmodern:thaumonomicon":
            continue
        replacement = None
        for page in value.get("pages", []):
            if page.get("type") == "recipe":
                replacement = results.get(page.get("recipe"))
                if replacement is not None:
                    break
        if replacement is not None:
            value["icon"] = replacement
            write(path, value)
            changed += 1
    return changed


def deactivate_collisions() -> int:
    active: dict[str, Path] = {}
    collisions = 0
    scan_root = SCANS.parent
    for path in sorted(SCANS.glob("*.json")):
        value = read(path)
        if value.get("inactive_reason") != COLLAPSED_REASON:
            continue
        value["inactive"] = False
        value.pop("inactive_reason", None)
        write(path, value)
    for path in sorted(scan_root.rglob("*.json")):
        if path.parent == SCANS:
            continue
        value = read(path)
        if value.get("inactive"):
            continue
        active[f"{value.get('type')}:{value.get('target')}"] = path
    for path in sorted(SCANS.glob("*.json")):
        value = read(path)
        if value.get("inactive"):
            continue
        key = f"{value.get('type')}:{value.get('target')}"
        if key not in active:
            active[key] = path
            continue
        value["inactive"] = True
        value["inactive_reason"] = COLLAPSED_REASON
        write(path, value)
        collisions += 1
    return collisions


def main() -> None:
    fields = read(MCP_FIELDS)
    scan_changes = sum(
        migrate_scan(path, fields)
        for path in sorted(SCANS.glob("*.json"))
    )
    icon_changes = migrate_research_icons()
    collisions = deactivate_collisions()
    unresolved = [
        str(path.relative_to(ROOT))
        for path in sorted(SCANS.glob("*.json"))
        if read(path).get("type") in {"legacy_object", "legacy_entity"}
    ]
    report = {
        "scan_files_updated": scan_changes,
        "research_icons_updated": icon_changes,
        "collapsed_duplicates_deactivated": collisions,
        "unresolved_without_1_20_1_equivalent": unresolved,
    }
    destination = ROOT / "data/legacy_tc4_4_2_3_5/modern_migration/runtime_ids.json"
    write(destination, report)
    print(json.dumps(report, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
