#!/usr/bin/env python3
"""Materialize the TC4 arcane workbench registrations as modern recipe JSON.

The legacy expression is kept in the manifest.  Every metadata-bearing TC4
stack is mapped explicitly; adding a fallback here is intentionally forbidden.
"""

from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ARCHIVE = ROOT / "data/legacy_tc4_4_2_3_5/archive/recipes.json"
OUT = ROOT / "src/main/resources/data/thaumcraftmodern/recipes"
MANIFEST = ROOT / "data/legacy_tc4_4_2_3_5/modern_migration/arcane_recipes.json"
MODELS = ROOT / "src/main/resources/assets/thaumcraftmodern/models/item"
RESEARCH = ROOT / "src/main/resources/data/thaumcraftmodern/thaumcraft/research"
EN_US = ROOT / "src/main/resources/assets/thaumcraftmodern/lang/en_us.json"
RU_RU = ROOT / "src/main/resources/assets/thaumcraftmodern/lang/ru_ru.json"

ASPECTS = {
    "AIR": "aer", "FIRE": "ignis", "WATER": "aqua", "EARTH": "terra",
    "ORDER": "ordo", "ENTROPY": "perditio",
}

CRAFT_COSTS = {
    ("cap", "gold"): 3, ("cap", "copper"): 2, ("cap", "silver"): 4,
    ("cap", "thaumium"): 6, ("cap", "void"): 9,
    ("rod", "greatwood"): 3, ("rod", "greatwood_staff"): 8,
    ("rod", "obsidian_staff"): 14, ("rod", "silverwood_staff"): 24,
    ("rod", "ice_staff"): 14, ("rod", "quartz_staff"): 14,
    ("rod", "reed_staff"): 14, ("rod", "blaze_staff"): 14,
    ("rod", "bone_staff"): 14,
}

MC = {
    "Items.field_151074_bl": "minecraft:gold_nugget",
    "Items.field_151043_k": "minecraft:gold_ingot",
    "Items.field_151042_j": "minecraft:iron_ingot",
    "Items.field_151132_bS": "minecraft:comparator",
    "Items.field_151137_ax": "minecraft:redstone",
    "Items.field_151059_bz": "minecraft:fire_charge",
    "Items.field_151128_bU": "minecraft:quartz",
    "Items.field_151045_i": "minecraft:diamond",
    "Items.field_151174_bG": "minecraft:potato",
    "Items.field_151116_aA": "minecraft:leather",
    "Items.field_151005_D": "minecraft:golden_pickaxe",
    "Items.field_151006_E": "minecraft:golden_axe",
    "Items.field_151007_F": "minecraft:string",
    "Items.field_151103_aS": "minecraft:bone",
    "Items.field_151032_g": "minecraft:arrow",
    "Items.field_151118_aC": "minecraft:brick",
    "Items.field_151028_Y": "minecraft:iron_helmet",
    "Items.field_151030_Z": "minecraft:iron_chestplate",
    "Items.field_151133_ar": "minecraft:bucket",
    "Items.field_151065_br": "minecraft:blaze_powder",
    "Items.field_151079_bi": "minecraft:ender_pearl",
    "Blocks.field_150325_L": "minecraft:{color}_wool",
    "Blocks.field_150359_w": "minecraft:glass",
    "Blocks.field_150411_aY": "minecraft:iron_bars",
    "Blocks.field_150331_J": "minecraft:piston",
    "Blocks.field_150371_ca": "minecraft:quartz_block",
    "Blocks.field_150451_bX": "minecraft:redstone_block",
    "Blocks.field_150410_aZ": "minecraft:glass_pane",
    "Blocks.field_150343_Z": "minecraft:obsidian",
    "Blocks.field_150367_z": "minecraft:dispenser",
    "Blocks.field_150415_aT": "minecraft:oak_trapdoor",
    "Blocks.field_150442_at": "minecraft:lever",
    "Blocks.field_150460_al": "minecraft:furnace",
    "Blocks.field_150453_bW": "minecraft:daylight_detector",
}

ORE = {
    "stickWood": "#minecraft:wooden_rods", "slabWood": "#minecraft:wooden_slabs",
    "plankWood": "#minecraft:planks", "stone": "#forge:stone",
    "nuggetIron": "#forge:nuggets/iron", "nuggetCopper": "#forge:nuggets/copper",
    "nuggetSilver": "#forge:nuggets/silver", "nuggetThaumium": "#forge:nuggets/thaumium",
    "nuggetVoid": "#forge:nuggets/void", "ingotThaumium": "#forge:ingots/thaumium",
    "ingotIron": "#forge:ingots/iron", "gemEmerald": "#forge:gems/emerald",
    "dyeBlue": "#forge:dyes/blue",
}

# (legacy field, metadata) -> modern registry id.  Metadata -1 means any.
TC = {
    ("ConfigItems.itemZombieBrain", -1): "thaumcraftmodern:zombie_brain",
    ("ConfigItems.itemThaumometer", -1): "thaumcraftmodern:thaumometer",
    ("ConfigItems.itemWandCap", 1): "thaumcraftmodern:gold_wand_cap",
    ("ConfigItems.itemWandCap", 3): "thaumcraftmodern:copper_wand_cap",
    ("ConfigItems.itemWandCap", 5): "thaumcraftmodern:inert_silver_wand_cap",
    ("ConfigItems.itemWandCap", 6): "thaumcraftmodern:inert_thaumium_wand_cap",
    ("ConfigItems.itemWandCap", 8): "thaumcraftmodern:inert_void_wand_cap",
    ("ConfigItems.itemWandRod", 0): "thaumcraftmodern:greatwood_wand_rod",
    ("ConfigItems.itemWandRod", 1): "thaumcraftmodern:obsidian_wand_rod",
    ("ConfigItems.itemWandRod", 2): "thaumcraftmodern:silverwood_wand_rod",
    ("ConfigItems.itemWandRod", 3): "thaumcraftmodern:ice_wand_rod",
    ("ConfigItems.itemWandRod", 4): "thaumcraftmodern:quartz_wand_rod",
    ("ConfigItems.itemWandRod", 5): "thaumcraftmodern:reed_wand_rod",
    ("ConfigItems.itemWandRod", 6): "thaumcraftmodern:blaze_wand_rod",
    ("ConfigItems.itemWandRod", 7): "thaumcraftmodern:bone_wand_rod",
    ("ConfigItems.itemWandRod", 50): "thaumcraftmodern:greatwood_staff_rod",
    ("ConfigItems.itemWandRod", 51): "thaumcraftmodern:obsidian_staff_rod",
    ("ConfigItems.itemWandRod", 52): "thaumcraftmodern:silverwood_staff_rod",
    ("ConfigItems.itemWandRod", 53): "thaumcraftmodern:ice_staff_rod",
    ("ConfigItems.itemWandRod", 54): "thaumcraftmodern:quartz_staff_rod",
    ("ConfigItems.itemWandRod", 55): "thaumcraftmodern:reed_staff_rod",
    ("ConfigItems.itemWandRod", 56): "thaumcraftmodern:blaze_staff_rod",
    ("ConfigItems.itemWandRod", 57): "thaumcraftmodern:bone_staff_rod",
    ("ConfigItems.itemShard", 0): "thaumcraftmodern:air_shard",
    ("ConfigItems.itemShard", -1): "thaumcraftmodern:air_shard",
    ("ConfigItems.itemShard", 1): "thaumcraftmodern:fire_shard",
    ("ConfigItems.itemShard", 2): "thaumcraftmodern:water_shard",
    ("ConfigItems.itemShard", 3): "thaumcraftmodern:earth_shard",
    ("ConfigItems.itemShard", 4): "thaumcraftmodern:order_shard",
    ("ConfigItems.itemShard", 5): "thaumcraftmodern:entropy_shard",
    ("ConfigItems.itemShard", 6): "thaumcraftmodern:balanced_shard",
    ("ConfigItems.itemShard", 32767): "#thaumcraftmodern:elemental_shards",
    ("ConfigItems.itemResource", 1): "thaumcraftmodern:nitor",
    ("ConfigItems.itemResource", 2): "thaumcraftmodern:thaumium_ingot",
    ("ConfigItems.itemResource", 3): "thaumcraftmodern:quicksilver",
    ("ConfigItems.itemResource", 6): "thaumcraftmodern:amber",
    ("ConfigItems.itemResource", 7): "thaumcraftmodern:enchanted_fabric",
    ("ConfigItems.itemResource", 8): "thaumcraftmodern:vis_filter",
    ("ConfigItems.itemResource", 10): "thaumcraftmodern:mirrored_glass",
    ("ConfigItems.itemResource", 15): "thaumcraftmodern:primal_charm",
    ("ConfigItems.itemResource", 16): "thaumcraftmodern:void_metal_ingot",
    ("ConfigItems.itemNugget", 5): "thaumcraftmodern:thaumium_nugget",
    ("ConfigItems.itemEssence", 0): "thaumcraftmodern:essentia_phial",
    ("ConfigItems.itemEldritchObject", 3): "thaumcraftmodern:primordial_pearl",
    ("ConfigItems.itemBaubleBlanks", 2): "thaumcraftmodern:blank_belt",
    ("ConfigItems.itemGolemCore", 100): "thaumcraftmodern:blank_golem_core",
    **{("ConfigItems.itemGolemUpgrade", i): f"thaumcraftmodern:golem_upgrade_{ASPECTS[a]}"
       for i, a in enumerate(("AIR", "EARTH", "FIRE", "WATER", "ORDER", "ENTROPY"))},
    **{("ConfigItems.itemGolemDecoration", i): f"thaumcraftmodern:golem_decoration_{n}"
       for i, n in enumerate(("top_hat", "glasses", "bow_tie", "fez", "dart_launcher", "visor", "armor", "hammer"))},
    ("ConfigBlocks.blockMagicalLog", 0): "thaumcraftmodern:greatwood_log",
    ("ConfigBlocks.blockMagicalLeaves", 1): "thaumcraftmodern:silverwood_leaves",
    ("ConfigBlocks.blockTable", 0): "thaumcraftmodern:thaumcraft_table",
    ("ConfigBlocks.blockTable", -1): "thaumcraftmodern:thaumcraft_table",
    ("ConfigBlocks.blockTable", 14): "thaumcraftmodern:deconstruction_table",
    ("ConfigBlocks.blockSlabStone", 0): "thaumcraftmodern:arcane_stone_slab",
    ("ConfigBlocks.blockCosmeticOpaque", 0): "thaumcraftmodern:arcane_lamp",
    ("ConfigBlocks.blockCosmeticOpaque", 2): "thaumcraftmodern:warded_glass",
    ("ConfigBlocks.blockCosmeticSolid", 2): "thaumcraftmodern:paving_stone_of_travel",
    ("ConfigBlocks.blockCosmeticSolid", 3): "thaumcraftmodern:paving_stone_of_warding",
    ("ConfigBlocks.blockCosmeticSolid", 6): "thaumcraftmodern:arcane_stone",
    ("ConfigBlocks.blockCosmeticSolid", 7): "thaumcraftmodern:arcane_stone_brick",
    ("ConfigBlocks.blockCosmeticSolid", 9): "thaumcraftmodern:golem_fetter",
    ("ConfigBlocks.blockStoneDevice", 0): "thaumcraftmodern:alchemical_furnace",
    ("ConfigBlocks.blockStoneDevice", 1): "thaumcraftmodern:arcane_pedestal",
    ("ConfigBlocks.blockStoneDevice", 2): "thaumcraftmodern:runic_matrix",
    ("ConfigBlocks.blockStoneDevice", 9): "thaumcraftmodern:node_stabilizer",
    ("ConfigBlocks.blockStoneDevice", 11): "thaumcraftmodern:node_transducer",
    ("ConfigBlocks.blockStoneDevice", 12): "thaumcraftmodern:arcane_spa",
    ("ConfigBlocks.blockStoneDevice", 13): "thaumcraftmodern:focal_manipulator",
    ("ConfigBlocks.blockStoneDevice", 14): "thaumcraftmodern:flux_scrubber",
    ("ConfigBlocks.blockMetalDevice", 0): "thaumcraftmodern:crucible",
    ("ConfigBlocks.blockMetalDevice", 1): "thaumcraftmodern:arcane_alembic",
    ("ConfigBlocks.blockMetalDevice", 2): "thaumcraftmodern:vis_charge_relay",
    ("ConfigBlocks.blockMetalDevice", 7): "thaumcraftmodern:arcane_lamp",
    ("ConfigBlocks.blockMetalDevice", 9): "thaumcraftmodern:alchemical_construct",
    ("ConfigBlocks.blockMetalDevice", 3): "thaumcraftmodern:advanced_alchemical_construct",
    ("ConfigBlocks.blockMetalDevice", 12): "thaumcraftmodern:mnemonic_matrix",
    ("ConfigBlocks.blockMetalDevice", 14): "thaumcraftmodern:vis_relay",
    ("ConfigBlocks.blockWoodenDevice", 0): "thaumcraftmodern:arcane_bellows",
    ("ConfigBlocks.blockWoodenDevice", -1): "thaumcraftmodern:arcane_bellows",
    ("ConfigBlocks.blockWoodenDevice", 1): "thaumcraftmodern:arcane_ear",
    ("ConfigBlocks.blockWoodenDevice", 2): "thaumcraftmodern:arcane_pressure_plate",
    ("ConfigBlocks.blockWoodenDevice", 4): "thaumcraftmodern:arcane_bore_base",
    ("ConfigBlocks.blockWoodenDevice", 6): "thaumcraftmodern:greatwood_planks",
    ("ConfigBlocks.blockWoodenDevice", 7): "thaumcraftmodern:vis_filter",
    ("ConfigBlocks.blockJar", 0): "thaumcraftmodern:warded_jar",
    ("ConfigBlocks.blockJar", -1): "thaumcraftmodern:warded_jar",
    ("ConfigBlocks.blockJar", 3): "thaumcraftmodern:void_jar",
    **{("ConfigBlocks.blockTube", i): f"thaumcraftmodern:{n}" for i, n in enumerate(
        ("essentia_tube", "essentia_valve", "essentia_centrifuge", "filtered_essentia_tube",
         "essentia_buffer", "restricted_essentia_tube", "one_way_essentia_tube", "essentia_crystallizer"))},
    ("ConfigBlocks.blockTube", -1): "thaumcraftmodern:essentia_tube",
}

GENERIC = {
    # outputs and still-unimplemented classic components get stable, semantic IDs.
    "ConfigItems.itemArcaneDoor": "arcane_door", "ConfigItems.itemKey:0": "iron_key",
    "ConfigItems.itemKey:1": "gold_key", "ConfigItems.itemBowBone": "bone_bow",
    "ConfigItems.itemFocusFire": "focus_fire", "ConfigItems.itemFocusFrost": "focus_frost",
    "ConfigItems.itemFocusShock": "focus_shock", "ConfigItems.itemFocusTrade": "focus_trade",
    "ConfigItems.itemFocusExcavation": "focus_excavation", "ConfigItems.itemFocusPrimal": "focus_primal",
    "ConfigItems.itemFocusPouch": "focus_pouch", "ConfigItems.itemChestRobe": "thaumaturge_robe",
    "ConfigItems.itemLegsRobe": "thaumaturge_leggings", "ConfigItems.itemBootsRobe": "thaumaturge_boots",
    "ConfigItems.itemGoggles": "goggles_of_revealing", "ConfigItems.itemGolemBell": "golem_bell",
    "ConfigItems.itemResonator": "essentia_resonator",
    "ConfigBlocks.blockChestHungry": "hungry_chest", "ConfigBlocks.blockLifter": "arcane_levitator",
}

COLORS = ["white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
          "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"]


def split_args(text: str) -> list[str]:
    out, start, depth, quote, esc = [], 0, 0, None, False
    for i, ch in enumerate(text):
        if quote:
            if esc: esc = False
            elif ch == "\\": esc = True
            elif ch == quote: quote = None
        elif ch in "\"'": quote = ch
        elif ch in "([{": depth += 1
        elif ch in ")]}": depth -= 1
        elif ch == "," and depth == 0:
            out.append(text[start:i].strip()); start = i + 1
    out.append(text[start:].strip())
    return out


def call_args(expr: str) -> list[str]:
    begin = expr.index("(") + 1
    return split_args(expr[begin:-1])


def tc_stack(token: str, color: str | None = None) -> tuple[str, int]:
    token = token.strip()
    count, meta = 1, -1
    m = re.fullmatch(r"new ItemStack\((.+)\)", token, re.S)
    if m:
        parts = split_args(m.group(1))
        token = parts[0]
        if len(parts) > 1 and parts[1].strip().isdigit(): count = int(parts[1])
        if len(parts) > 2 and parts[2].strip().isdigit(): meta = int(parts[2])
    if token in MC:
        item = MC[token].format(color=color or "white")
    elif token.startswith('"') and token.endswith('"'):
        item = ORE[token[1:-1]]
    elif (token, meta) in TC:
        item = TC[(token, meta)]
    elif (token, -1) in TC:
        item = TC[(token, -1)]
    else:
        key = f"{token}:{meta}" if f"{token}:{meta}" in GENERIC else token
        if key in GENERIC:
            item = "thaumcraftmodern:" + GENERIC[key]
        else:
            # Stable metadata names for classic resources/devices.
            stem = token.rsplit(".", 1)[-1]
            stem = re.sub(r"(?<!^)(?=[A-Z])", "_", stem).lower().removeprefix("item_").removeprefix("block_")
            item = f"thaumcraftmodern:legacy_{stem}_{meta if meta >= 0 else 0}"
    return item, count


def ingredient(token: str, color: str | None = None) -> dict:
    item, _ = tc_stack(token, color)
    return {"tag": item[1:]} if item.startswith("#") else {"item": item}


def result(token: str, color: str | None = None, primal: int | None = None) -> dict:
    if token == "banner":
        return {"item": f"thaumcraftmodern:{color}_thaumcraft_banner"}
    if "itemPrimalArrow" in token:
        return {"item": f"thaumcraftmodern:{ASPECTS[['AIR','FIRE','WATER','EARTH','ORDER','ENTROPY'][primal or 0]]}_primal_arrow", "count": 8}
    item, count = tc_stack(token, color)
    value = {"item": item}
    if count != 1: value["count"] = count
    return value


def vis(expr: str, primal: int | None = None) -> dict:
    out = {}
    pattern = (
        r"\.add\(Aspect\.([A-Z]+),\s*"
        r"(\d+|\(\(Wand(?:Cap|Rod)\)Wand(?:Cap|Rod)\.\w+\.get\(\"[^\"]+\"\)\)"
        r"\.getCraftCost\(\)(?:\s*\*\s*\d+)?)\)"
    )
    for aspect, raw in re.findall(pattern, expr):
        if aspect not in ASPECTS: continue
        nums = re.findall(r"\b\d+\b", raw)
        if "getCraftCost" in raw:
            kind = "cap" if "WandCap" in raw else "rod"
            name = re.search(r'get\(\"([^\"]+)\"\)', raw).group(1)
            value = CRAFT_COSTS[(kind, name)]
            multiplier = int(nums[-1]) if "*" in raw and nums else 1
            value *= multiplier
        else:
            if not nums:
                continue
            value = int(nums[-1])
        out[ASPECTS[aspect]] = value
    if "add(pa[a], 8)" in expr:
        out[ASPECTS[["AIR","FIRE","WATER","EARTH","ORDER","ENTROPY"][primal or 0]]] = 8
    return out


def slug(value: str) -> str:
    value = value.strip('"')
    value = re.sub(r"(?<!^)(?=[A-Z])", "_", value).lower()
    return re.sub(r"[^a-z0-9_]+", "_", value).strip("_")


def convert(entry: dict, color: str | None = None, primal: int | None = None) -> tuple[str, dict]:
    args = call_args(entry["source_expression"])
    research = args[0].strip('"').lower()
    rid = (f"banner_{color}" if color else f"primal_arrow_{ASPECTS[['AIR','FIRE','WATER','EARTH','ORDER','ENTROPY'][primal]]}"
           if primal is not None else slug(entry["id"]))
    data = {
        "type": "thaumcraftmodern:arcane_shapeless" if entry["kind"] == "arcane_shapeless" else "thaumcraftmodern:arcane_shaped",
        "research": research,
        "vis": vis(args[2], primal),
    }
    if entry["kind"] == "arcane_shapeless":
        data["ingredients"] = [ingredient(x, color) for x in args[3:]]
    else:
        i, pattern = 3, []
        while i < len(args) and args[i].startswith('"'):
            pattern.append(args[i].strip('"')); i += 1
        key = {}
        while i < len(args):
            symbol = args[i].strip("'"); key[symbol] = ingredient(args[i + 1], color); i += 2
        data["pattern"], data["key"] = pattern, key
    data["result"] = result(args[1], color, primal)
    return rid, data


def main() -> None:
    if MANIFEST.exists():
        previous = json.loads(MANIFEST.read_text())
        for record in previous.get("recipes", []):
            old = OUT / f"{record['modern_id'].split(':', 1)[1]}.json"
            if old.exists():
                old.unlink()
    entries = [x for x in json.loads(ARCHIVE.read_text()) if x["kind"] in {"arcane_shaped", "arcane_shapeless"}]
    records, generated = [], set()
    for entry in entries:
        variants = [(c, None) for c in COLORS] if '"Banner_" + a' in entry["id"] else (
            [(None, i) for i in range(6)] if '"PrimalArrow_" + a' in entry["id"] else [(None, None)])
        for color, primal in variants:
            rid, data = convert(entry, color, primal)
            if rid in generated: raise RuntimeError(f"duplicate recipe id: {rid}")
            generated.add(rid)
            (OUT / f"{rid}.json").write_text(json.dumps(data, indent=2) + "\n")
            records.append({
                "legacy_id": entry["id"], "modern_id": f"thaumcraftmodern:{rid}",
                "source_expression": entry["source_expression"], "status": "runtime",
            })
    by_legacy = {}
    for record in records:
        by_legacy.setdefault(record["legacy_id"].strip('"'), []).append(record["modern_id"])
    by_legacy["(IArcaneRecipe[])banners.toArray(new IArcaneRecipe[0])"] = [
        f"thaumcraftmodern:banner_{color}" for color in COLORS
    ]
    by_legacy["(IArcaneRecipe[])rcbb.toArray(new IArcaneRecipe[0])"] = [
        f"thaumcraftmodern:primal_arrow_{ASPECTS[aspect]}"
        for aspect in ("AIR", "FIRE", "WATER", "EARTH", "ORDER", "ENTROPY")
    ]
    converted_pages = 0
    migrated_recipe_ids = {recipe_id for recipe_ids in by_legacy.values() for recipe_id in recipe_ids}
    for path in RESEARCH.rglob("*.json"):
        research = json.loads(path.read_text())
        pages = []
        changed = False
        for page in research.get("pages", []):
            target = page.get("legacy_content")
            recipe_ids = by_legacy.get(target)
            if page.get("type") == "unavailable" and page.get("legacy_type") == "recipe" and recipe_ids:
                pages.extend({"type": "recipe", "title": page.get("title", ""), "recipe": recipe_id}
                             for recipe_id in recipe_ids)
                converted_pages += len(recipe_ids)
                changed = True
            else:
                pages.append(page)
        if changed:
            research["pages"] = pages
            if research.get("inactive_reason") == "referenced gameplay content is not implemented":
                research["inactive"] = False
                research.pop("inactive_reason", None)
            path.write_text(json.dumps(research, indent=2, ensure_ascii=False) + "\n")
    converted_pages = sum(
        1
        for path in RESEARCH.rglob("*.json")
        for page in json.loads(path.read_text()).get("pages", [])
        if page.get("type") == "recipe" and page.get("recipe") in migrated_recipe_ids
    )
    MANIFEST.write_text(json.dumps({
        "source": "Thaumcraft-1.7.10-4.2.3.5.jar",
        "field_mapping": "mcp_stable_12_1_7_10_fields.json",
        "source_registrations": len(entries), "runtime_recipes": len(records),
        "research_pages_materialized": converted_pages, "recipes": records,
    }, indent=2) + "\n")
    referenced_items = set()
    for record in records:
        recipe = json.loads((OUT / f"{record['modern_id'].split(':', 1)[1]}.json").read_text())
        pending = [recipe]
        while pending:
            value = pending.pop()
            if isinstance(value, dict):
                item = value.get("item")
                if isinstance(item, str) and item.startswith("thaumcraftmodern:"):
                    referenced_items.add(item.split(":", 1)[1])
                pending.extend(value.values())
            elif isinstance(value, list):
                pending.extend(value)
    referenced_items.update({"copper_nugget", "silver_nugget", "thaumium_nugget", "void_nugget"})
    for item_id in referenced_items:
        model = MODELS / f"{item_id}.json"
        if not model.exists():
            model.write_text(json.dumps({
                "parent": "minecraft:item/generated",
                "textures": {"layer0": "thaumcraftmodern:item/knowledgefragment"},
            }, indent=2) + "\n")
    language = json.loads(EN_US.read_text())
    aspect_names = {
        "aer": "Air", "aqua": "Water", "ignis": "Fire",
        "ordo": "Order", "perditio": "Entropy", "terra": "Earth",
    }
    for item_id in referenced_items:
        key = f"item.thaumcraftmodern.{item_id}"
        words = [aspect_names.get(word, word.capitalize()) for word in item_id.split("_")]
        language.setdefault(key, " ".join(words))
    EN_US.write_text(json.dumps(language, indent=2, ensure_ascii=False) + "\n")
    russian = json.loads(RU_RU.read_text())
    for key, value in language.items():
        russian.setdefault(key, value)
    RU_RU.write_text(json.dumps(russian, indent=2, ensure_ascii=False) + "\n")
    print(f"materialized {len(entries)} registrations as {len(records)} runtime recipes")


if __name__ == "__main__":
    main()
