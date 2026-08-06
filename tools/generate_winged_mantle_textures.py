#!/usr/bin/env python3
"""Build the Winged Mantle's hard-edged 4096x4096 UV atlas and item icons."""

from __future__ import annotations

from collections import deque
from math import ceil
from pathlib import Path

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/thaumcraftmodern/textures"
SCALE = 16

INK = (12, 6, 26, 255)
EDGE = (25, 13, 47, 255)
VIOLET = (39, 22, 68, 255)
MID = (55, 32, 88, 255)
LIGHT = (76, 46, 111, 255)
GLYPH = (127, 79, 174, 255)
GOLD_DARK = (101, 59, 9, 255)
GOLD = (190, 126, 20, 255)
GOLD_LIGHT = (238, 184, 51, 255)
EMERALD_DARK = (12, 73, 49, 255)
EMERALD = (23, 145, 75, 255)
LEATHER_DARK = (55, 29, 22, 255)
LEATHER = (105, 57, 35, 255)
PARCHMENT = (226, 203, 139, 255)


def _rect(draw: ImageDraw.ImageDraw, xy: tuple[int, int, int, int], color) -> None:
    x0, y0, x1, y1 = xy
    if x1 >= x0 and y1 >= y0:
        draw.rectangle(xy, fill=color)


def _face(draw: ImageDraw.ImageDraw, x: int, y: int, w: int, h: int,
          base=VIOLET, edge=INK, highlight=MID) -> None:
    if w <= 0 or h <= 0:
        return
    _rect(draw, (x, y, x + w - 1, y + h - 1), edge)
    if w > 2 and h > 2:
        _rect(draw, (x + 1, y + 1, x + w - 2, y + h - 2), base)
        draw.line((x + 1, y + 1, x + w - 2, y + 1), fill=highlight)
    if w >= 6 and h >= 5:
        draw.point((x + w // 2, y + 2), fill=LIGHT)
    if w >= 10 and h >= 8:
        draw.line((x + 3, y + 2, x + 3, y + h - 3), fill=EDGE)
        draw.line((x + w - 4, y + 2, x + w - 4, y + h - 3), fill=LIGHT)


def box_faces(u: int, v: int, width: float, height: float,
              depth: float) -> tuple[tuple[int, int, int, int], ...]:
    """Return Minecraft box UV faces as x, y, width, height in atlas pixels."""
    u, v = u * SCALE, v * SCALE
    w, h, d = ceil(width * SCALE), ceil(height * SCALE), max(1, ceil(depth * SCALE))
    return (
        (u + d, v, w, d),
        (u + d + w, v, w, d),
        (u, v + d, d, h),
        (u + d, v + d, w, h),
        (u + d + w, v + d, d, h),
        (u + d + w + d, v + d, w, h),
    )


def box(draw: ImageDraw.ImageDraw, u: int, v: int, width: float,
        height: float, depth: float, *, base=VIOLET, edge=INK,
        highlight=MID, decorate=None) -> None:
    """Paint all six faces using Minecraft's conventional box UV layout."""
    faces = box_faces(u, v, width, height, depth)
    for face in faces:
        _face(draw, *face, base=base, edge=edge, highlight=highlight)
    if decorate is not None:
        # North and south are the two broad faces visible on thin panels.
        decorate(draw, faces[3])
        decorate(draw, faces[5])


def _fortress_fabric_palette() -> tuple[tuple[int, int, int, int], ...]:
    """Derive the cloth value range from the mod's shipped Fortress armor."""
    source = Image.open(ASSETS / "entity/models/fortress_armor.png").convert("RGBA")
    pixels = (source.get_flattened_data() if hasattr(source, "get_flattened_data")
              else source.getdata())
    candidates = sorted({pixel for pixel in pixels
                         if pixel[3] > 0 and pixel[2] > pixel[0]
                         and pixel[2] > pixel[1] and sum(pixel[:3]) > 30},
                        key=lambda color: sum(color[:3]))
    if len(candidates) < 5:
        return INK, EDGE, VIOLET, MID, LIGHT
    positions = (0.08, 0.27, 0.48, 0.70, 0.92)
    return tuple(candidates[min(len(candidates) - 1,
                                int((len(candidates) - 1) * position))]
                 for position in positions)


def _paint_fabric(target: ImageDraw.ImageDraw, width: int, height: int,
                  palette: tuple[tuple[int, int, int, int], ...],
                  seed: int = 0) -> None:
    """Hard-pixel vertical weave based on the original Fortress armor palette."""
    target.rectangle((0, 0, width - 1, height - 1), fill=palette[1])
    center = max(1, width // 2)
    for x in range(width):
        distance = abs(x - center) / center
        band = (x // max(2, SCALE)) % 5
        value = 3 if distance < 0.35 else 2
        if band in (0, 4):
            value -= 1
        value = max(0, min(4, value))
        target.line((x, 1, x, height - 2), fill=palette[value])
        if (x + seed) % 7 == 0:
            target.point((x, 2 + (x * 5 + seed) % max(1, height - 4)),
                         fill=palette[min(4, value + 1)])


def _rune(target: ImageDraw.ImageDraw, cx: int, cy: int, scale: int = 1) -> None:
    color = GLYPH
    bright = (168, 113, 214, 255)
    width = max(1, scale)
    target.line((cx - 4 * scale, cy - 3 * scale,
                 cx + 3 * scale, cy + 4 * scale), fill=color, width=width)
    target.line((cx - 5 * scale, cy + scale,
                 cx - scale, cy + 5 * scale), fill=color, width=width)
    target.line((cx + scale, cy - 4 * scale,
                 cx + 5 * scale, cy), fill=bright, width=width)


def continuous_wing(image: Image.Image, u: int, v: int, width: float,
                    height: float, depth: float, seed: int) -> None:
    """Transfer the approved concept wing instead of procedurally redrawing it."""
    for face_index in (3, 5):
        x, y, w, h = box_faces(u, v, width, height, depth)[face_index]
        image.alpha_composite(_approved_wing_face(w, h), (x, y))


def _approved_wing_face(width: int, height: int) -> Image.Image:
    """Crop the large left wing from concept v3 and remove only outer black."""
    concept = Image.open(
        ROOT / "docs/concepts/winged_mantle_chest_wings_reference_v3.png"
    ).convert("RGBA")
    # Fixed bounds of the isolated lower-left approved wing on the sheet.
    crop = concept.crop((55, 515, 905, 948))
    pixels = crop.load()
    transparent = set()
    queue: deque[tuple[int, int]] = deque()
    for px in range(crop.width):
        queue.append((px, 0))
        queue.append((px, crop.height - 1))
    for py in range(crop.height):
        queue.append((0, py))
        queue.append((crop.width - 1, py))
    while queue:
        px, py = queue.popleft()
        if (px, py) in transparent or not (0 <= px < crop.width and 0 <= py < crop.height):
            continue
        red, green, blue, _ = pixels[px, py]
        if max(red, green, blue) > 24:
            continue
        transparent.add((px, py))
        queue.extend(((px - 1, py), (px + 1, py),
                      (px, py - 1), (px, py + 1)))
    for px, py in transparent:
        red, green, blue, _ = pixels[px, py]
        pixels[px, py] = (red, green, blue, 0)
    bounds = crop.getbbox()
    if bounds is None:
        raise ValueError("approved wing crop unexpectedly became empty")
    crop = crop.crop(bounds)
    # Deterministic nearest-neighbour sampling only: no redraw, smoothing,
    # recoloring, invented seams or generated ornaments.
    return crop.resize((width, height), Image.Resampling.NEAREST)


def generate_elytra_texture() -> None:
    """Write a standalone 64x32 texture with the exact vanilla Elytra UV."""
    vanilla = Image.open(
        ROOT / "docs/concepts/minecraft_elytra_1_20_1.png"
    ).convert("RGBA")
    if vanilla.size != (64, 32):
        raise ValueError(f"vanilla Elytra UV must remain 64x32, got {vanilla.size}")

    # Direct nearest-neighbour transfer from the approved concept wing onto
    # the complete vanilla UV bounds. Vanilla alpha remains the final mask,
    # preserving its exact silhouette, edge thickness and mirrored layout.
    concept = _approved_wing_face(24, 22)
    recolored = Image.new("RGBA", vanilla.size, (0, 0, 0, 0))
    vanilla_pixels = vanilla.load()
    concept_pixels = concept.load()
    target_pixels = recolored.load()
    for py in range(32):
        for px in range(64):
            alpha = vanilla_pixels[px, py][3]
            if alpha == 0:
                continue
            sx = min(23, max(0, px - 22))
            sy = min(21, py)
            red, green, blue, source_alpha = concept_pixels[sx, sy]
            if source_alpha == 0:
                red, green, blue = INK[:3]
            target_pixels[px, py] = (
                red, green, blue, alpha
            )
    output = ASSETS / "entity/winged_mantle_elytra.png"
    output.parent.mkdir(parents=True, exist_ok=True)
    recolored.save(output, optimize=False)


def paint_chest_front(image: Image.Image, face: tuple[int, int, int, int]) -> None:
    x, y, w, h = face
    grid = _approved_chest_arms_grid()
    # The central 42x44 source pixels contain the complete front panel:
    # shoulder cloth, gold diamond, emerald, crossing straps and upper belt.
    # Start four authored pixels farther left so the complete central ornament
    # lands four source units to the right on the torso UV (two review passes).
    source = grid.crop((17, 0, 59, 44))
    image.alpha_composite(source.resize((w, h), Image.Resampling.NEAREST), (x, y))


def _approved_chest_arms_grid() -> Image.Image:
    """Restore the 84x65 authored pixel grid from the 8x screen reference."""
    reference = Image.open(
        ROOT / "docs/concepts/winged_mantle_chest_arms_reference.png"
    ).convert("RGBA")
    if reference.size != (672, 520):
        raise ValueError(f"chest/arms reference must remain 672x520, got {reference.size}")
    # Every authored pixel occupies an exact 8x8 screen cell. Never blur,
    # sharpen, average, recolor or synthesize pixels.
    return reference.resize((84, 65), Image.Resampling.NEAREST)


def _rectify_sleeve(source: Image.Image) -> Image.Image:
    """Remove only outer black and unskew each row of an angled sleeve."""
    rectified = Image.new("RGBA", source.size, (0, 0, 0, 0))
    for py in range(source.height):
        row = [source.getpixel((px, py)) for px in range(source.width)]
        occupied = [px for px, color in enumerate(row) if max(color[:3]) > 8]
        if not occupied:
            continue
        strip = source.crop((occupied[0], py, occupied[-1] + 1, py + 1))
        rectified.alpha_composite(
            strip.resize((source.width, 1), Image.Resampling.NEAREST), (0, py)
        )
    return rectified


def paint_sleeve(image: Image.Image, face: tuple[int, int, int, int],
                 screen_left: bool) -> None:
    """Rectify one angled reference sleeve into a continuous armor UV face."""
    x, y, w, h = face
    grid = _approved_chest_arms_grid()
    source = grid.crop((0, 0, 23, 65) if screen_left else (61, 0, 84, 65))
    if screen_left:
        # The pale parchment belongs to the belt reference, not the arm. If it
        # remains here it reads as exposed player skin at the wrist.
        ImageDraw.Draw(source).rectangle((15, 46, 22, 64), fill=(0, 0, 0, 0))
    source = _rectify_sleeve(source)
    # Close the wrist with an opaque five-pixel violet cuff. The enlarged cube
    # continues below the vanilla hand, so no skin can peek through its end.
    cuff = ImageDraw.Draw(source)
    cuff.rectangle((0, 59, source.width - 1, 64), fill=VIOLET)
    cuff.line((0, 59, source.width - 1, 59), fill=LIGHT)
    cuff.line((0, 60, source.width - 1, 60), fill=EDGE)
    cuff.line((0, 64, source.width - 1, 64), fill=INK)
    image.alpha_composite(source.resize((w, h), Image.Resampling.NEAREST), (x, y))


def paint_belt_front(image: Image.Image, face: tuple[int, int, int, int]) -> None:
    """Transfer belt, buckle and side stud together from the approved frame."""
    x, y, w, h = face
    source = _approved_chest_arms_grid().crop((18, 43, 66, 58))
    image.alpha_composite(source.resize((w, h), Image.Resampling.NEAREST), (x, y))


def paint_chest_back(image: Image.Image, face: tuple[int, int, int, int]) -> None:
    x, y, w, h = face
    patch = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    draw = ImageDraw.Draw(patch)
    palette = _fortress_fabric_palette()
    _paint_fabric(draw, w, h, palette, 29)
    draw.rectangle((0, 0, w - 1, h - 1), outline=INK, width=max(2, SCALE))
    cx, cy = w // 2, int(h * 0.27)
    r = max(5, min(w, h) // 6)
    draw.rectangle((cx - r - 2, cy - r - 2, cx + r + 2, cy + r + 2), fill=GOLD_DARK)
    draw.rectangle((cx - r, cy - r, cx + r, cy + r), fill=GOLD_LIGHT)
    draw.rectangle((cx - r // 2, cy - r + 1, cx + r // 2, cy + r - 1), fill=EMERALD)
    image.alpha_composite(patch, (x, y))


def paint_praetor_gorget(draw: ImageDraw.ImageDraw,
                         face: tuple[int, int, int, int]) -> None:
    """Recolor the leader collar and retain its four recessed front slots."""
    x, y, w, h = face
    rim = max(2, SCALE // 2)
    draw.rectangle((x, y, x + w - 1, y + h - 1), fill=EDGE)
    draw.line((x + rim, y + rim, x + w - rim, y + rim),
              fill=LIGHT, width=max(1, SCALE // 3))
    slot_width = max(2, SCALE // 2)
    slot_top = y + int(h * 0.34)
    slot_bottom = y + int(h * 0.83)
    for slot in range(4):
        cx = x + int(w * (0.35 + slot * 0.10))
        draw.rectangle((cx - slot_width // 2, slot_top,
                        cx + slot_width // 2, slot_bottom), fill=INK)
    stud = max(2, SCALE // 3)
    for cx in (x + int(w * 0.13), x + int(w * 0.87)):
        draw.rectangle((cx - stud, y + h // 2 - stud,
                        cx + stud, y + h // 2 + stud), fill=MID)


def paint_raised_focus(image: Image.Image,
                       face: tuple[int, int, int, int]) -> None:
    """Pixel-copy the approved focus into the original Praetor crest bounds."""
    x, y, w, h = face
    source = _approved_chest_arms_grid().crop((23, 5, 53, 35))
    image.alpha_composite(source.resize((w, h), Image.Resampling.NEAREST), (x, y))


def _palette_color(value: int, low, middle, high):
    normalized = value / 255.0
    if normalized < 0.5:
        blend, start, end = normalized * 2.0, low, middle
    else:
        blend, start, end = (normalized - 0.5) * 2.0, middle, high
    return tuple(round(start[i] + (end[i] - start[i]) * blend) for i in range(3))


def paste_recolored_praetor_armor(atlas: Image.Image) -> None:
    """Transfer every original Praetor texture pixel, changing only material hues."""
    source = Image.open(
        ASSETS / "entity/models/cultist_leader_armor.png"
    ).convert("RGBA")
    if source.size != (256, 128):
        raise ValueError(f"Praetor atlas must remain 256x128, got {source.size}")
    recolored = Image.new("RGBA", source.size, (0, 0, 0, 0))
    src, dst = source.load(), recolored.load()
    for py in range(source.height):
        for px in range(source.width):
            red, green, blue, alpha = src[px, py]
            if alpha == 0:
                continue
            value = (red * 30 + green * 59 + blue * 11) // 100
            # Golden/brown pixels retain their material distinction; crimson
            # cloth and neutral plate are shifted into the mantle violet range.
            if red > green * 1.12 and green > blue * 1.28:
                color = _palette_color(value, LEATHER_DARK[:3], LEATHER[:3], GOLD_LIGHT[:3])
            else:
                color = _palette_color(value, INK[:3], VIOLET[:3], LIGHT[:3])
            dst[px, py] = color + (alpha,)
    atlas.alpha_composite(
        recolored.resize((128 * SCALE, 64 * SCALE), Image.Resampling.NEAREST),
        (128 * SCALE, 128 * SCALE),
    )
    focus_faces = box_faces(204, 181, 5.0, 5.0, 1.0)
    paint_raised_focus(atlas, focus_faces[3])
    paint_raised_focus(atlas, focus_faces[5])


def generate_atlas() -> None:
    image = Image.new("RGBA", (4096, 4096), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    # Exact TC4 Fortress texture block used by the original four-piece
    # shoulder geometry. It is already in the approved violet/brown palette.
    fortress = Image.open(ASSETS / "entity/models/fortress_armor.png").convert("RGBA")
    image.alpha_composite(
        fortress.resize((128 * SCALE, 64 * SCALE), Image.Resampling.NEAREST),
        (128 * SCALE, 0),
    )
    for values in (
        (0, 50, 4.25, 13.5, 1.2),
        (14, 50, 4.25, 13.5, 1.2), (28, 50, 8.8, 13.5, 1.1),
    ):
        box(draw, *values)

    box(draw, 0, 34, 9.0, 9.5, 5.0)
    chest_faces = box_faces(0, 34, 9.0, 9.5, 5.0)
    paint_chest_front(image, chest_faces[3])
    paint_chest_back(image, chest_faces[5])
    paste_recolored_praetor_armor(image)
    box(draw, 72, 34, 9.4, 2.0, 5.8, base=LEATHER,
        edge=LEATHER_DARK, highlight=(139, 79, 43, 255))
    paint_belt_front(image, box_faces(72, 34, 9.4, 2.0, 5.8)[3])

    def book_cover(target: ImageDraw.ImageDraw, face) -> None:
        x, y, w, h = face
        cx, cy = x + w // 2, y + h // 2
        target.line((cx, y + 2, x + w - 3, cy), fill=GOLD_LIGHT, width=2)
        target.line((x + w - 3, cy, cx, y + h - 3), fill=GOLD, width=2)
        target.line((cx, y + h - 3, x + 2, cy), fill=GOLD_DARK, width=2)
        target.line((x + 2, cy, cx, y + 2), fill=GOLD, width=2)
        target.rectangle((cx - 1, cy - 1, cx + 1, cy + 1), fill=EMERALD)

    box(draw, 64, 50, 5.2, 7.0, 1.1, base=LEATHER, edge=LEATHER_DARK,
        highlight=(139, 79, 43, 255), decorate=book_cover)
    box(draw, 78, 60, 0.8, 5.2, 0.55, base=GOLD,
        edge=GOLD_DARK, highlight=GOLD_LIGHT)
    box(draw, 84, 50, 2.0, 5.5, 2.0, base=PARCHMENT,
        edge=(113, 82, 43, 255), highlight=(250, 234, 179, 255))
    box(draw, 96, 50, 2.8, 3.4, 2.2, base=LEATHER,
        edge=LEATHER_DARK, highlight=(139, 79, 43, 255))
    box(draw, 112, 80, 8.4, 3.5, 1.6, base=VIOLET,
        edge=INK, highlight=LIGHT)

    for u, screen_left in ((0, True), (64, False)):
        box(draw, u, 192, 5.0, 13.0, 5.0)
        sleeve_faces = box_faces(u, 192, 5.0, 13.0, 5.0)
        paint_sleeve(image, sleeve_faces[3], screen_left)
        paint_sleeve(image, sleeve_faces[5], screen_left)

    for u in (128, 176):
        box(draw, u, 192, 4.7, 10.8, 4.7)
        box(draw, u, 216, 5.0, 4.1, 5.55)

    paste_recolored_cultist_hood(image)

    output = ASSETS / "entity/models/winged_mantle_armor.png"
    output.parent.mkdir(parents=True, exist_ok=True)
    image.save(output, optimize=False)


def paste_recolored_cultist_hood(atlas: Image.Image) -> None:
    """Copy every ModelRobe hood face, grading light front to dark tail."""
    source = Image.open(
        ASSETS / "entity/models/cultist_robe_armor.png"
    ).convert("RGBA")
    source_scale = source.width // 128
    if source.size != (128 * source_scale, 64 * source_scale):
        raise ValueError(f"unexpected ModelRobe atlas size {source.size}")
    # (source UV, destination UV, dimensions, dark/base/light palette).
    parts = (
        ((16, 7), (0, 96), (9, 9, 9),
         ((19, 9, 37), (49, 28, 82), (91, 57, 126))),
        ((52, 13), (44, 96), (8, 9, 3),
         ((15, 7, 31), (42, 23, 73), (75, 44, 108))),
        ((52, 14), (74, 96), (7, 8, 3),
         ((12, 5, 26), (34, 18, 61), (62, 35, 94))),
        ((53, 15), (102, 96), (6, 7, 3),
         ((8, 3, 19), (25, 12, 47), (48, 25, 76))),
    )
    for source_uv, target_uv, dimensions, palette in parts:
        source_faces = box_faces(*source_uv, *dimensions)
        target_faces = box_faces(*target_uv, *dimensions)
        for source_face, target_face in zip(source_faces, target_faces):
            sx, sy, sw, sh = source_face
            tx, ty, tw, th = target_face
            # box_faces is expressed in the high-density atlas units.
            # Convert source bounds back to original TC4 128x64 pixels.
            sx = sx // SCALE * source_scale
            sy = sy // SCALE * source_scale
            sw = max(1, sw // SCALE * source_scale)
            sh = max(1, sh // SCALE * source_scale)
            face = source.crop((sx, sy, sx + sw, sy + sh))
            pixels = face.load()
            for py in range(face.height):
                for px in range(face.width):
                    red, green, blue, alpha = pixels[px, py]
                    if alpha == 0:
                        continue
                    value = (red * 30 + green * 59 + blue * 11) // 100
                    normalized = value / 255.0
                    if normalized < 0.5:
                        blend = normalized * 2.0
                        low, high = palette[0], palette[1]
                    else:
                        blend = (normalized - 0.5) * 2.0
                        low, high = palette[1], palette[2]
                    pixels[px, py] = tuple(
                        round(low[channel] + (high[channel] - low[channel]) * blend)
                        for channel in range(3)
                    ) + (alpha,)
            atlas.alpha_composite(
                face.resize((tw, th), Image.Resampling.NEAREST), (tx, ty)
            )


def icon(kind: str) -> Image.Image:
    image = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    if kind == "hood":
        draw.polygon(((3, 3), (5, 1), (11, 1), (13, 3), (13, 12),
                      (10, 14), (6, 14), (3, 12)), fill=EDGE)
        _rect(draw, (4, 4, 11, 11), VIOLET)
        _rect(draw, (5, 5, 10, 10), INK)
        draw.line((4, 4, 11, 4), fill=LIGHT)
    elif kind == "chestplate":
        draw.polygon(((2, 3), (5, 1), (11, 1), (14, 3), (13, 14),
                      (9, 13), (8, 15), (7, 13), (3, 14)), fill=EDGE)
        draw.polygon(((3, 4), (6, 2), (10, 2), (13, 4), (12, 12),
                      (9, 11), (8, 13), (7, 11), (4, 12)), fill=VIOLET)
        _rect(draw, (6, 3, 9, 7), GOLD_DARK)
        _rect(draw, (7, 3, 8, 6), EMERALD)
        draw.point((7, 3), fill=GOLD_LIGHT)
        draw.point((2, 5), fill=GOLD_LIGHT)
        draw.point((13, 5), fill=GOLD_LIGHT)
    elif kind == "leggings":
        draw.polygon(((3, 2), (13, 2), (12, 8), (10, 14), (7, 14),
                      (7, 8), (6, 14), (3, 14), (4, 8)), fill=EDGE)
        draw.polygon(((4, 3), (12, 3), (11, 8), (9, 13), (8, 13),
                      (8, 7), (6, 13), (4, 13), (5, 8)), fill=VIOLET)
        draw.line((4, 4, 11, 4), fill=LIGHT)
    elif kind == "boots":
        _rect(draw, (2, 3, 6, 12), EDGE)
        _rect(draw, (9, 3, 13, 12), EDGE)
        _rect(draw, (1, 11, 6, 14), INK)
        _rect(draw, (9, 11, 14, 14), INK)
        _rect(draw, (3, 4, 5, 10), VIOLET)
        _rect(draw, (10, 4, 12, 10), VIOLET)
        draw.line((2, 11, 6, 11), fill=GOLD)
        draw.line((9, 11, 13, 11), fill=GOLD)
    return image


def generate_icons() -> None:
    output_dir = ASSETS / "item"
    output_dir.mkdir(parents=True, exist_ok=True)
    for kind in ("hood", "chestplate", "leggings", "boots"):
        icon(kind).save(output_dir / f"winged_mantle_{kind}.png", optimize=False)


if __name__ == "__main__":
    generate_atlas()
    generate_elytra_texture()
    generate_icons()
