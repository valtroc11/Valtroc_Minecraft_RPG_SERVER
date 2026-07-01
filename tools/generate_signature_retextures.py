from __future__ import annotations

import colorsys
import math
from pathlib import Path

from PIL import Image, ImageDraw

from excalibur_sources import ensure_excalibur_texture


ROOT = Path(__file__).resolve().parents[1]
TEXTURE_DIR = ROOT / "content" / "oraxen" / "pack" / "textures" / "servidorpg"
ORAXEN_TEXTURE_DIR = (
    ROOT / "content" / "oraxen" / "pack" / "assets" / "oraxen" / "textures" / "servidorpg"
)
PREVIEW_DIR = ROOT / "docs" / "previews"


def lerp(a: float, b: float, t: float) -> float:
    return a + (b - a) * t


def mix(c1: tuple[int, int, int], c2: tuple[int, int, int], t: float) -> tuple[int, int, int]:
    t = max(0.0, min(1.0, t))
    return (
        round(lerp(c1[0], c2[0], t)),
        round(lerp(c1[1], c2[1], t)),
        round(lerp(c1[2], c2[2], t)),
    )


def sample_gradient(stops: list[tuple[int, int, int]], value: float) -> tuple[int, int, int]:
    value = max(0.0, min(1.0, value))
    if value <= 0:
        return stops[0]
    if value >= 1:
        return stops[-1]
    scaled = value * (len(stops) - 1)
    idx = int(math.floor(scaled))
    frac = scaled - idx
    return mix(stops[idx], stops[idx + 1], frac)


def ensure_base_texture(zip_member: str, base_name: str) -> Path:
    TEXTURE_DIR.mkdir(parents=True, exist_ok=True)
    ORAXEN_TEXTURE_DIR.mkdir(parents=True, exist_ok=True)
    return ensure_excalibur_texture(zip_member, TEXTURE_DIR / base_name)


def save_texture(img: Image.Image, filename: str) -> None:
    TEXTURE_DIR.mkdir(parents=True, exist_ok=True)
    ORAXEN_TEXTURE_DIR.mkdir(parents=True, exist_ok=True)
    img.save(TEXTURE_DIR / filename)
    img.save(ORAXEN_TEXTURE_DIR / filename)


def hsv(r: int, g: int, b: int) -> tuple[float, float, float]:
    return colorsys.rgb_to_hsv(r / 255.0, g / 255.0, b / 255.0)


def luminance(r: int, g: int, b: int) -> float:
    return (0.2126 * r + 0.7152 * g + 0.0722 * b) / 255.0


def finish_color(color: tuple[int, int, int], shade: float) -> tuple[int, int, int]:
    if shade > 0.80:
        return mix(color, (255, 255, 255), (shade - 0.80) / 0.20 * 0.18)
    if shade < 0.18:
        return mix(color, (18, 18, 22), (0.18 - shade) / 0.18 * 0.28)
    return color


def preview_resampling(img: Image.Image) -> Image.Resampling:
    return Image.Resampling.NEAREST if max(img.size) <= 64 else Image.Resampling.LANCZOS


def is_metal_pixel(r: int, g: int, b: int) -> bool:
    h, s, v = hsv(r, g, b)
    return s <= 0.28 and v >= 0.12


def is_brown_pixel(r: int, g: int, b: int) -> bool:
    h, s, v = hsv(r, g, b)
    return 0.02 <= h <= 0.15 and s >= 0.12 and v >= 0.05


def is_string_pixel(r: int, g: int, b: int, x: int, width: int) -> bool:
    _, s, v = hsv(r, g, b)
    return x >= width * 0.64 and s <= 0.35 and v <= 0.45


def is_hunter_accent_zone(x: int, y: int, width: int, height: int) -> bool:
    return x <= width * 0.28 and height * 0.22 <= y <= height * 0.90


TIERS: dict[str, dict[str, object]] = {
    "wood": {
        "metal": [(46, 31, 18), (88, 60, 37), (132, 96, 63), (189, 154, 111)],
        "wood": [(43, 27, 18), (71, 48, 31), (104, 74, 52), (149, 116, 84)],
        "holy": [(131, 88, 22), (191, 138, 36), (238, 191, 85), (255, 239, 180)],
        "arcane": [(90, 63, 122), (127, 89, 175), (175, 129, 228), (224, 204, 255)],
        "rage": [(101, 24, 24), (152, 35, 34), (205, 61, 54), (247, 118, 92)],
        "hunter": [(44, 82, 27), (67, 134, 45), (105, 186, 77), (180, 236, 143)],
        "string": (76, 60, 42),
    },
    "stone": {
        "metal": [(39, 43, 49), (77, 83, 92), (121, 128, 137), (182, 188, 198)],
        "wood": [(39, 33, 31), (61, 56, 57), (97, 94, 97), (146, 143, 148)],
        "holy": [(139, 145, 113), (183, 192, 150), (224, 230, 193), (248, 251, 228)],
        "arcane": [(76, 92, 120), (110, 133, 174), (151, 178, 221), (215, 230, 255)],
        "rage": [(81, 34, 42), (126, 52, 62), (178, 82, 95), (229, 142, 156)],
        "hunter": [(62, 100, 70), (94, 145, 104), (141, 194, 151), (213, 241, 208)],
        "string": (95, 99, 108),
    },
    "copper": {
        "metal": [(65, 38, 21), (117, 68, 39), (177, 106, 63), (238, 174, 107)],
        "wood": [(43, 27, 18), (71, 47, 31), (102, 72, 51), (146, 112, 83)],
        "holy": [(156, 86, 14), (218, 131, 26), (255, 182, 69), (255, 232, 164)],
        "arcane": [(106, 52, 86), (156, 78, 128), (211, 115, 176), (252, 192, 232)],
        "rage": [(120, 32, 21), (173, 51, 33), (221, 88, 55), (255, 151, 102)],
        "hunter": [(24, 102, 83), (39, 155, 124), (77, 204, 171), (158, 243, 221)],
        "string": (83, 58, 39),
    },
    "bronze": {
        "metal": [(74, 48, 24), (124, 79, 41), (180, 119, 64), (233, 181, 118)],
        "wood": [(45, 28, 18), (74, 48, 31), (107, 74, 50), (150, 114, 82)],
        "holy": [(159, 101, 22), (215, 151, 42), (247, 198, 90), (255, 235, 182)],
        "arcane": [(106, 55, 121), (155, 82, 175), (200, 121, 224), (242, 197, 255)],
        "rage": [(104, 28, 20), (153, 47, 31), (206, 79, 50), (249, 140, 91)],
        "hunter": [(93, 95, 22), (135, 145, 34), (185, 194, 69), (232, 238, 136)],
        "string": (86, 64, 44),
    },
    "iron": {
        "metal": [(40, 43, 49), (84, 90, 98), (134, 143, 154), (220, 227, 236)],
        "wood": [(43, 27, 18), (70, 47, 31), (103, 73, 52), (146, 113, 84)],
        "holy": [(181, 167, 82), (219, 204, 114), (243, 228, 165), (255, 249, 220)],
        "arcane": [(86, 66, 135), (122, 97, 189), (170, 142, 234), (223, 212, 255)],
        "rage": [(104, 22, 24), (156, 32, 36), (208, 58, 59), (250, 111, 109)],
        "hunter": [(26, 107, 57), (44, 157, 86), (80, 205, 126), (164, 241, 187)],
        "string": (89, 96, 107),
    },
    "silver": {
        "metal": [(84, 93, 106), (137, 149, 166), (196, 207, 223), (250, 252, 255)],
        "wood": [(49, 37, 34), (79, 66, 62), (119, 107, 104), (171, 162, 159)],
        "holy": [(193, 194, 151), (228, 226, 185), (248, 244, 218), (255, 253, 241)],
        "arcane": [(101, 88, 158), (141, 127, 208), (191, 179, 244), (241, 235, 255)],
        "rage": [(118, 52, 66), (171, 77, 95), (220, 119, 140), (255, 188, 200)],
        "hunter": [(88, 118, 111), (126, 173, 160), (174, 217, 204), (228, 248, 241)],
        "string": (116, 122, 132),
    },
}


def recolor_berserker_axe(source: Image.Image, config: dict[str, object]) -> Image.Image:
    out = Image.new("RGBA", source.size, (0, 0, 0, 0))
    src = source.convert("RGBA")
    metal = config["metal"]
    wood = config["wood"]
    rage = config["rage"]
    assert isinstance(metal, list)
    assert isinstance(wood, list)
    assert isinstance(rage, list)

    for y in range(src.height):
        for x in range(src.width):
            r, g, b, a = src.getpixel((x, y))
            if a == 0:
                continue
            shade = luminance(r, g, b)
            if is_metal_pixel(r, g, b):
                metal_color = sample_gradient(metal, shade)
                rage_color = sample_gradient(rage, min(1.0, shade * 1.35))
                color = mix(metal_color, rage_color, 0.42 if shade < 0.45 else 0.18)
            else:
                color = sample_gradient(wood, shade * 0.92)
            out.putpixel((x, y), (*finish_color(color, shade), a))
    return out


def recolor_hunter_bow(source: Image.Image, config: dict[str, object]) -> Image.Image:
    out = Image.new("RGBA", source.size, (0, 0, 0, 0))
    src = source.convert("RGBA")
    metal = config["metal"]
    wood = config["wood"]
    hunter = config["hunter"]
    string = config["string"]
    assert isinstance(metal, list)
    assert isinstance(wood, list)
    assert isinstance(hunter, list)
    assert isinstance(string, tuple)

    for y in range(src.height):
        for x in range(src.width):
            r, g, b, a = src.getpixel((x, y))
            if a == 0:
                continue
            shade = luminance(r, g, b)
            if is_string_pixel(r, g, b, x, src.width):
                color = mix(string, (255, 255, 255), shade * 0.08)
            elif is_hunter_accent_zone(x, y, src.width, src.height):
                hunter_color = sample_gradient(hunter, min(1.0, shade * 1.08 + 0.08))
                if is_brown_pixel(r, g, b):
                    color = mix(sample_gradient(wood, shade), hunter_color, 0.48)
                else:
                    color = mix(sample_gradient(metal, shade), hunter_color, 0.58)
            elif is_metal_pixel(r, g, b):
                metal_color = sample_gradient(metal, shade)
                hunter_color = sample_gradient(hunter, shade)
                color = mix(metal_color, hunter_color, 0.38 if shade > 0.42 else 0.22)
            elif is_brown_pixel(r, g, b):
                wood_color = sample_gradient(wood, shade)
                hunter_color = sample_gradient(hunter, shade)
                color = mix(wood_color, hunter_color, 0.14 if shade > 0.55 else 0.08)
            else:
                color = sample_gradient(wood, shade * 0.9)
            out.putpixel((x, y), (*finish_color(color, shade), a))
    return out


def recolor_mage_focus(source: Image.Image, config: dict[str, object]) -> Image.Image:
    out = Image.new("RGBA", source.size, (0, 0, 0, 0))
    src = source.convert("RGBA")
    metal = config["metal"]
    arcane = config["arcane"]
    assert isinstance(metal, list)
    assert isinstance(arcane, list)

    for y in range(src.height):
        for x in range(src.width):
            r, g, b, a = src.getpixel((x, y))
            if a == 0:
                continue
            shade = luminance(r, g, b)
            if shade >= 0.66:
                color = sample_gradient(arcane, shade)
            elif shade >= 0.34:
                color = mix(sample_gradient(metal, shade), sample_gradient(arcane, shade), 0.45)
            else:
                color = sample_gradient(metal, shade)
            out.putpixel((x, y), (*finish_color(color, shade), a))
    return out


def recolor_cleric_relic(source: Image.Image, config: dict[str, object]) -> Image.Image:
    out = Image.new("RGBA", source.size, (0, 0, 0, 0))
    src = source.convert("RGBA")
    metal = config["metal"]
    holy = config["holy"]
    wood = config["wood"]
    assert isinstance(metal, list)
    assert isinstance(holy, list)
    assert isinstance(wood, list)

    for y in range(src.height):
        for x in range(src.width):
            r, g, b, a = src.getpixel((x, y))
            if a == 0:
                continue
            shade = luminance(r, g, b)
            if x >= src.width * 0.72 and y <= src.height * 0.30:
                color = sample_gradient(holy, min(1.0, shade * 1.10 + 0.08))
            elif shade >= 0.74:
                color = sample_gradient(holy, shade)
            elif shade >= 0.46 or (x >= src.width * 0.58 and y <= src.height * 0.46):
                color = sample_gradient(metal, shade)
            else:
                color = sample_gradient(wood, shade)
            out.putpixel((x, y), (*finish_color(color, shade), a))
    return out


def create_family_preview(files: list[str], output_name: str, label_suffix: str, height: int = 280) -> None:
    PREVIEW_DIR.mkdir(parents=True, exist_ok=True)
    cards: list[Image.Image] = []
    labels: list[str] = []

    for filename in files:
        img = Image.open(TEXTURE_DIR / filename).convert("RGBA")
        scale = height / img.height
        resized = img.resize((max(1, int(img.width * scale)), height), preview_resampling(img))
        card = Image.new("RGBA", (220, height + 54), (18, 16, 20, 255))
        x = (card.width - resized.width) // 2
        card.alpha_composite(resized, (x, 10))
        cards.append(card)
        labels.append(filename.replace(label_suffix, ""))

    preview = Image.new("RGBA", (len(cards) * 220 + 32, height + 86), (12, 11, 14, 255))
    draw = ImageDraw.Draw(preview)
    for idx, (card, label) in enumerate(zip(cards, labels)):
        x = 16 + idx * 220
        preview.alpha_composite(card, (x, 12))
        draw.text((x + 64, height + 28), label, fill=(232, 226, 215, 255))
    preview.save(PREVIEW_DIR / output_name)


def create_overview_preview(rows: list[tuple[str, list[str]]]) -> None:
    PREVIEW_DIR.mkdir(parents=True, exist_ok=True)
    width = 1390
    height = 1440
    bg = Image.new("RGBA", (width, height), (12, 11, 14, 255))
    draw = ImageDraw.Draw(bg)

    tier_labels = ["wood", "stone", "copper", "bronze", "iron", "silver"]
    col_x = [40 + i * 220 for i in range(len(tier_labels))]
    for idx, label in enumerate(tier_labels):
        draw.text((col_x[idx] + 78, 16), label, fill=(232, 226, 215, 255))

    row_y = [70, 390, 710, 1030]
    for row_index, (title, files) in enumerate(rows):
        y = row_y[row_index]
        draw.text((16, y + 120), title, fill=(232, 226, 215, 255))
        for idx, filename in enumerate(files):
            img = Image.open(TEXTURE_DIR / filename).convert("RGBA")
            scale = 240 / img.height
            resized = img.resize((max(1, int(img.width * scale)), 240), preview_resampling(img))
            x = col_x[idx] + (180 - resized.width) // 2
            bg.alpha_composite(resized, (x, y))

    bg.save(PREVIEW_DIR / "preview_signature_class_families.png")


def main() -> None:
    berserker_base = Image.open(
        ensure_base_texture(
            "assets/minecraft/textures/item/cit/custom/diamond_battle_axe.png",
            "_base_excalibur_battle_axe.png",
        )
    ).convert("RGBA")
    hunter_base = Image.open(
        ensure_base_texture(
            "assets/minecraft/textures/item/excalibur/bow/infinity.png",
            "_base_excalibur_infinity_bow.png",
        )
    ).convert("RGBA")
    mage_base = Image.open(
        ensure_base_texture(
            "assets/minecraft/textures/item/amethyst_shard.png",
            "_base_excalibur_amethyst_shard.png",
        )
    ).convert("RGBA")
    cleric_base = Image.open(
        ensure_base_texture(
            "assets/minecraft/textures/item/blaze_rod.png",
            "_base_excalibur_blaze_rod.png",
        )
    ).convert("RGBA")

    berserker_files: list[str] = []
    hunter_files: list[str] = []
    mage_files: list[str] = []
    cleric_files: list[str] = []

    for tier, config in TIERS.items():
        berserker_name = f"{tier}_berserker_war_axe.png"
        hunter_name = "iron_hunter_bow.png" if tier == "iron" else f"{tier}_hunter_bow.png"
        mage_name = f"{tier}_mage_focus.png"
        cleric_name = f"{tier}_cleric_relic.png"

        save_texture(recolor_berserker_axe(berserker_base, config), berserker_name)
        save_texture(recolor_hunter_bow(hunter_base, config), hunter_name)
        save_texture(recolor_mage_focus(mage_base, config), mage_name)
        save_texture(recolor_cleric_relic(cleric_base, config), cleric_name)

        berserker_files.append(berserker_name)
        hunter_files.append(hunter_name)
        mage_files.append(mage_name)
        cleric_files.append(cleric_name)

    iron_config = TIERS["iron"]
    save_texture(recolor_berserker_axe(berserker_base, iron_config), "berserker_war_axe.png")
    save_texture(recolor_mage_focus(mage_base, iron_config), "mage_focus.png")
    save_texture(recolor_cleric_relic(cleric_base, iron_config), "cleric_relic.png")

    create_family_preview(berserker_files, "preview_berserker_war_axe_tiers.png", "_berserker_war_axe.png")
    create_family_preview(hunter_files, "preview_hunter_bow_tiers.png", "_hunter_bow.png")
    create_family_preview(mage_files, "preview_mage_focus_tiers.png", "_mage_focus.png")
    create_family_preview(cleric_files, "preview_cleric_relic_tiers.png", "_cleric_relic.png")
    create_overview_preview(
        [
            ("berserker axe", berserker_files),
            ("hunter bow", hunter_files),
            ("mage focus", mage_files),
            ("cleric relic", cleric_files),
        ]
    )
    print("Generated compact class-family recolors for berserker axe, hunter bow, mage focus, and cleric relic.")


if __name__ == "__main__":
    main()
