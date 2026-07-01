from __future__ import annotations

import colorsys
import json
import math
from pathlib import Path

from PIL import Image, ImageDraw

from excalibur_sources import ensure_excalibur_texture


ROOT = Path(__file__).resolve().parents[1]
TEXTURE_DIR = ROOT / "content" / "oraxen" / "pack" / "textures" / "servidorpg"
MODEL_DIR = ROOT / "content" / "oraxen" / "pack" / "models" / "servidorpg"
ORAXEN_TEXTURE_DIR = (
    ROOT / "content" / "oraxen" / "pack" / "assets" / "oraxen" / "textures" / "servidorpg"
)
ORAXEN_MODEL_DIR = (
    ROOT / "content" / "oraxen" / "pack" / "assets" / "oraxen" / "models" / "servidorpg"
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


def is_wood_pixel(r: int, g: int, b: int) -> bool:
    h, s, v = hsv(r, g, b)
    return 0.02 <= h <= 0.15 and s >= 0.12 and v >= 0.05


def is_string_pixel(r: int, g: int, b: int, x: int, width: int) -> bool:
    _, s, v = hsv(r, g, b)
    return x >= width * 0.64 and s <= 0.35 and v <= 0.45


AXE_TIERS: dict[str, dict[str, object]] = {
    "wood": {
        "metal": [(42, 28, 18), (71, 49, 31), (112, 82, 55), (176, 142, 102)],
        "grip": [(42, 26, 17), (72, 48, 31), (107, 75, 50), (150, 116, 81)],
    },
    "stone": {
        "metal": [(35, 38, 42), (71, 76, 84), (111, 117, 126), (173, 179, 188)],
        "grip": [(42, 28, 20), (69, 47, 34), (101, 74, 55), (142, 112, 87)],
    },
    "copper": {
        "metal": [(60, 36, 20), (113, 67, 38), (171, 103, 61), (232, 170, 104)],
        "grip": [(42, 26, 19), (69, 47, 34), (102, 73, 55), (144, 109, 83)],
    },
    "bronze": {
        "metal": [(74, 48, 24), (124, 79, 41), (180, 119, 64), (233, 181, 118)],
        "grip": [(44, 28, 18), (73, 50, 32), (106, 77, 53), (149, 114, 84)],
    },
    "iron": {
        "metal": [(34, 38, 43), (79, 85, 94), (129, 137, 148), (214, 222, 232)],
        "grip": [(42, 26, 19), (70, 48, 34), (103, 74, 54), (146, 112, 85)],
    },
    "silver": {
        "metal": [(84, 93, 106), (137, 149, 166), (196, 207, 223), (250, 252, 255)],
        "grip": [(52, 41, 39), (83, 71, 68), (123, 113, 109), (176, 167, 161)],
    },
}


BOW_TIERS: dict[str, dict[str, object]] = {
    "wood": {
        "metal": [(71, 49, 31), (106, 78, 54), (144, 113, 82), (186, 154, 118)],
        "wood": [(45, 28, 18), (74, 49, 31), (109, 80, 56), (152, 120, 87)],
        "string": (58, 44, 32),
    },
    "stone": {
        "metal": [(54, 58, 66), (96, 102, 112), (136, 143, 153), (188, 194, 205)],
        "wood": [(39, 27, 18), (65, 46, 31), (96, 70, 50), (136, 104, 76)],
        "string": (82, 86, 95),
    },
    "copper": {
        "metal": [(69, 41, 22), (125, 73, 42), (185, 112, 66), (241, 177, 108)],
        "wood": [(42, 27, 19), (69, 47, 32), (100, 72, 52), (142, 109, 82)],
        "string": (78, 55, 38),
    },
    "bronze": {
        "metal": [(78, 49, 25), (126, 81, 42), (181, 119, 65), (233, 180, 118)],
        "wood": [(44, 28, 19), (72, 48, 31), (104, 74, 53), (146, 112, 83)],
        "string": (87, 65, 44),
    },
    "iron": {
        "metal": [(39, 43, 49), (84, 90, 98), (134, 143, 154), (221, 228, 236)],
        "wood": [(41, 27, 18), (67, 45, 31), (99, 70, 50), (139, 106, 77)],
        "string": (85, 93, 106),
    },
    "silver": {
        "metal": [(84, 93, 106), (137, 149, 166), (196, 207, 223), (250, 252, 255)],
        "wood": [(48, 39, 37), (77, 68, 65), (114, 107, 103), (163, 156, 150)],
        "string": (118, 124, 136),
    },
}


WAND_TIERS: dict[str, dict[str, object]] = {
    "wood": {
        "wood": [(49, 29, 18), (84, 55, 33), (124, 87, 56), (179, 139, 95)],
        "tip": [(148, 87, 25), (211, 139, 43), (248, 194, 93), (255, 239, 182)],
    },
    "stone": {
        "wood": [(44, 43, 48), (72, 74, 83), (110, 114, 123), (164, 170, 180)],
        "tip": [(133, 156, 181), (177, 202, 226), (220, 237, 248), (251, 254, 255)],
    },
    "copper": {
        "wood": [(56, 28, 17), (95, 51, 28), (141, 85, 48), (201, 141, 88)],
        "tip": [(176, 77, 14), (235, 124, 24), (255, 175, 65), (255, 232, 165)],
    },
    "bronze": {
        "wood": [(63, 35, 15), (104, 64, 24), (152, 101, 41), (210, 157, 87)],
        "tip": [(189, 113, 25), (232, 163, 46), (252, 210, 103), (255, 239, 182)],
    },
    "iron": {
        "wood": [(52, 35, 28), (84, 58, 48), (121, 90, 77), (176, 141, 121)],
        "tip": [(132, 160, 189), (180, 208, 233), (223, 240, 251), (252, 255, 255)],
    },
    "silver": {
        "wood": [(57, 46, 42), (91, 78, 73), (132, 120, 114), (188, 179, 172)],
        "tip": [(187, 190, 211), (218, 224, 239), (240, 244, 251), (255, 255, 255)],
    },
}


STAFF_TIERS: dict[str, dict[str, object]] = {
    "wood": {
        "wood": [(41, 26, 17), (70, 46, 30), (104, 74, 50), (151, 116, 82)],
        "metal": [(80, 52, 23), (123, 84, 38), (170, 126, 66), (223, 182, 120)],
        "glow": [(156, 101, 29), (221, 152, 50), (251, 205, 101), (255, 241, 190)],
    },
    "stone": {
        "wood": [(42, 31, 23), (67, 55, 47), (99, 87, 80), (145, 136, 130)],
        "metal": [(66, 72, 81), (108, 118, 130), (151, 164, 178), (210, 221, 234)],
        "glow": [(136, 162, 186), (183, 208, 231), (224, 240, 250), (252, 255, 255)],
    },
    "copper": {
        "wood": [(45, 27, 17), (73, 46, 29), (106, 74, 50), (151, 114, 81)],
        "metal": [(78, 45, 22), (130, 78, 43), (186, 117, 68), (243, 184, 111)],
        "glow": [(181, 88, 14), (237, 132, 24), (255, 183, 67), (255, 235, 173)],
    },
    "bronze": {
        "wood": [(46, 28, 17), (75, 49, 31), (109, 78, 52), (155, 118, 84)],
        "metal": [(88, 56, 23), (138, 92, 44), (190, 134, 69), (240, 191, 122)],
        "glow": [(189, 120, 28), (235, 170, 52), (252, 215, 111), (255, 242, 191)],
    },
    "iron": {
        "wood": [(43, 27, 18), (70, 47, 31), (103, 73, 52), (146, 113, 84)],
        "metal": [(50, 58, 69), (95, 106, 121), (146, 160, 178), (215, 227, 239)],
        "glow": [(145, 175, 198), (193, 219, 239), (229, 244, 252), (254, 255, 255)],
    },
    "silver": {
        "wood": [(50, 41, 39), (80, 72, 69), (118, 110, 106), (170, 162, 156)],
        "metal": [(94, 105, 121), (146, 160, 179), (201, 213, 228), (250, 252, 255)],
        "glow": [(202, 205, 220), (229, 234, 245), (246, 249, 253), (255, 255, 255)],
    },
}


STAFF_MODEL_TEXTURES: dict[str, str] = {
    "wood_staff": "servidorpg/wood_staff",
    "stone_staff": "servidorpg/stone_staff",
    "copper_staff": "servidorpg/copper_staff",
    "bronze_staff": "servidorpg/bronze_staff",
    "iron_staff": "servidorpg/iron_staff",
    "silver_staff": "servidorpg/silver_staff",
    "summoner_staff": "servidorpg/mobs/summoner_staff",
}

WAND_MODEL_TEXTURES: dict[str, str] = {
    "wood_wand": "servidorpg/wood_wand",
    "stone_wand": "servidorpg/stone_wand",
    "copper_wand": "servidorpg/copper_wand",
    "bronze_wand": "servidorpg/bronze_wand",
    "iron_wand": "servidorpg/iron_wand",
    "silver_wand": "servidorpg/silver_wand",
}

RELIC_MODEL_TEXTURES: dict[str, str] = {
    "cleric_relic": "servidorpg/cleric_relic",
    "wood_cleric_relic": "servidorpg/wood_cleric_relic",
    "stone_cleric_relic": "servidorpg/stone_cleric_relic",
    "copper_cleric_relic": "servidorpg/copper_cleric_relic",
    "bronze_cleric_relic": "servidorpg/bronze_cleric_relic",
    "iron_cleric_relic": "servidorpg/iron_cleric_relic",
    "silver_cleric_relic": "servidorpg/silver_cleric_relic",
}

FOCUS_MODEL_TEXTURES: dict[str, str] = {
    "mage_focus": "servidorpg/mage_focus",
    "wood_mage_focus": "servidorpg/wood_mage_focus",
    "stone_mage_focus": "servidorpg/stone_mage_focus",
    "copper_mage_focus": "servidorpg/copper_mage_focus",
    "bronze_mage_focus": "servidorpg/bronze_mage_focus",
    "iron_mage_focus": "servidorpg/iron_mage_focus",
    "silver_mage_focus": "servidorpg/silver_mage_focus",
}


def recolor_axe(source: Image.Image, config: dict[str, object]) -> Image.Image:
    out = Image.new("RGBA", source.size, (0, 0, 0, 0))
    src = source.convert("RGBA")
    metal = config["metal"]
    grip = config["grip"]
    assert isinstance(metal, list)
    assert isinstance(grip, list)

    for y in range(src.height):
        for x in range(src.width):
            r, g, b, a = src.getpixel((x, y))
            if a == 0:
                continue
            shade = luminance(r, g, b)
            if is_metal_pixel(r, g, b):
                color = sample_gradient(metal, shade)
            else:
                color = sample_gradient(grip, shade * 0.9)
            out.putpixel((x, y), (*finish_color(color, shade), a))
    return out


def recolor_bow(source: Image.Image, config: dict[str, object]) -> Image.Image:
    out = Image.new("RGBA", source.size, (0, 0, 0, 0))
    src = source.convert("RGBA")
    metal = config["metal"]
    wood = config["wood"]
    string = config["string"]
    assert isinstance(metal, list)
    assert isinstance(wood, list)
    assert isinstance(string, tuple)

    for y in range(src.height):
        for x in range(src.width):
            r, g, b, a = src.getpixel((x, y))
            if a == 0:
                continue
            shade = luminance(r, g, b)
            if is_string_pixel(r, g, b, x, src.width):
                color = mix(string, (255, 255, 255), shade * 0.08)
            elif is_metal_pixel(r, g, b):
                color = sample_gradient(metal, shade)
            elif is_wood_pixel(r, g, b):
                color = sample_gradient(wood, shade)
            else:
                color = sample_gradient(wood, shade * 0.9)
            out.putpixel((x, y), (*finish_color(color, shade), a))
    return out


def recolor_wand(source: Image.Image, config: dict[str, object]) -> Image.Image:
    out = Image.new("RGBA", source.size, (0, 0, 0, 0))
    src = source.convert("RGBA")
    wood = config["wood"]
    tip = config["tip"]
    assert isinstance(wood, list)
    assert isinstance(tip, list)

    for y in range(src.height):
        for x in range(src.width):
            r, g, b, a = src.getpixel((x, y))
            if a == 0:
                continue
            shade = luminance(r, g, b)
            if x >= src.width * 0.62 and y <= src.height * 0.55:
                color = sample_gradient(tip, min(1.0, shade * 1.08 + 0.08))
            elif 0.32 * src.width <= x <= 0.52 * src.width and 0.40 * src.height <= y <= 0.62 * src.height:
                color = mix(sample_gradient(wood, shade), sample_gradient(tip, shade), 0.18)
            else:
                color = sample_gradient(wood, shade)
            out.putpixel((x, y), (*finish_color(color, shade), a))
    return out


def recolor_staff(source: Image.Image, config: dict[str, object]) -> Image.Image:
    out = Image.new("RGBA", source.size, (0, 0, 0, 0))
    src = source.convert("RGBA")
    wood = config["wood"]
    metal = config["metal"]
    glow = config["glow"]
    assert isinstance(wood, list)
    assert isinstance(metal, list)
    assert isinstance(glow, list)

    for y in range(src.height):
        for x in range(src.width):
            r, g, b, a = src.getpixel((x, y))
            if a == 0:
                continue
            shade = luminance(r, g, b)
            if x >= src.width * 0.66 and y <= src.height * 0.38:
                color = sample_gradient(glow, min(1.0, shade * 1.10 + 0.08))
            elif shade >= 0.72:
                color = sample_gradient(glow, min(1.0, shade * 1.06))
            elif shade >= 0.46 or (x >= src.width * 0.56 and y <= src.height * 0.48):
                color = sample_gradient(metal, shade)
            else:
                color = sample_gradient(wood, shade)
            out.putpixel((x, y), (*finish_color(color, shade), a))
    return out


def recolor_trident_staff(source: Image.Image, config: dict[str, object]) -> Image.Image:
    out = Image.new("RGBA", source.size, (0, 0, 0, 0))
    src = source.convert("RGBA")
    wood = config["wood"]
    metal = config["metal"]
    glow = config["glow"]
    assert isinstance(wood, list)
    assert isinstance(metal, list)
    assert isinstance(glow, list)
    gem_pixels = {
        (9, 0): 0.90,
        (10, 0): 0.96,
        (12, 0): 0.92,
        (13, 0): 0.86,
        (8, 1): 0.82,
        (12, 1): 1.00,
        (9, 1): 0.88,
        (7, 2): 0.72,
        (11, 2): 0.84,
        (8, 2): 0.78,
        (7, 3): 0.66,
        (10, 3): 0.66,
        (7, 4): 0.62,
        (13, 4): 0.92,
        (6, 5): 0.56,
        (12, 5): 0.70,
        (6, 6): 0.48,
        (6, 7): 0.42,
        (5, 8): 0.36,
    }
    trim_pixels = {
        (15, 4),
        (15, 5),
        (14, 6),
    }

    for y in range(src.height):
        for x in range(src.width):
            r, g, b, a = src.getpixel((x, y))
            if (x, y) in trim_pixels:
                continue
            if a == 0 and (x, y) in gem_pixels:
                color = sample_gradient(glow, gem_pixels[(x, y)])
                out.putpixel((x, y), (*finish_color(color, gem_pixels[(x, y)]), 255))
                continue
            if a == 0:
                continue
            shade = luminance(r, g, b)

            if x >= src.width * 0.56 and y <= src.height * 0.42:
                color = sample_gradient(glow, min(1.0, shade * 1.10 + 0.08))
            elif x >= src.width * 0.44 and y <= src.height * 0.58:
                color = sample_gradient(metal, shade)
            else:
                color = sample_gradient(wood, shade)

            out.putpixel((x, y), (*finish_color(color, shade), a))
    return out


def write_model(item_id: str, texture_path: str, parent: str, display: dict[str, dict[str, list[float]]]) -> None:
    MODEL_DIR.mkdir(parents=True, exist_ok=True)
    ORAXEN_MODEL_DIR.mkdir(parents=True, exist_ok=True)
    model = {
        "parent": parent,
        "textures": {
            "layer0": texture_path,
        },
        "display": display,
    }
    model_json = json.dumps(model, indent=2) + "\n"
    (MODEL_DIR / f"{item_id}.json").write_text(model_json, encoding="utf-8")
    (ORAXEN_MODEL_DIR / f"{item_id}.json").write_text(model_json, encoding="utf-8")


def write_staff_model(item_id: str, texture_path: str) -> None:
    write_model(
        item_id,
        texture_path,
        "item/handheld",
        {
            "thirdperson_righthand": {
                "rotation": [0, -90, 55],
                "translation": [0, 4.2, 0.5],
                "scale": [0.88, 1.55, 0.88],
            },
            "firstperson_righthand": {
                "rotation": [0, -90, 25],
                "translation": [1.13, 3.2, 1.13],
                "scale": [0.82, 1.3, 0.82],
            },
        },
    )


def write_wand_model(item_id: str, texture_path: str) -> None:
    write_model(
        item_id,
        texture_path,
        "item/handheld",
        {
            "thirdperson_righthand": {
                "rotation": [0, -90, 42],
                "translation": [0, 2.8, 0.5],
                "scale": [0.72, 1.08, 0.72],
            },
            "firstperson_righthand": {
                "rotation": [0, -90, 18],
                "translation": [0.96, 2.5, 1.08],
                "scale": [0.68, 1.02, 0.68],
            },
        },
    )


def write_relic_model(item_id: str, texture_path: str) -> None:
    write_model(
        item_id,
        texture_path,
        "item/handheld",
        {
            "thirdperson_righthand": {
                "rotation": [0, -90, 46],
                "translation": [0, 3.1, 0.5],
                "scale": [0.8, 1.16, 0.8],
            },
            "firstperson_righthand": {
                "rotation": [0, -90, 20],
                "translation": [1.02, 2.7, 1.1],
                "scale": [0.76, 1.08, 0.76],
            },
        },
    )


def write_focus_model(item_id: str, texture_path: str) -> None:
    write_model(
        item_id,
        texture_path,
        "item/generated",
        {
            "thirdperson_righthand": {
                "rotation": [0, 92, -34],
                "translation": [0.0, 1.3, -2.0],
                "scale": [1.08, 1.08, 1.08],
            },
            "firstperson_righthand": {
                "rotation": [0, 92, -16],
                "translation": [0.9, 2.0, 0.82],
                "scale": [1.02, 1.02, 1.02],
            },
        },
    )


def create_row_preview(filenames: list[str], output_name: str, height: int = 280) -> None:
    PREVIEW_DIR.mkdir(parents=True, exist_ok=True)
    cards: list[Image.Image] = []
    labels: list[str] = []
    for filename in filenames:
        img = Image.open(TEXTURE_DIR / filename).convert("RGBA")
        scale = height / img.height
        resized = img.resize((max(1, int(img.width * scale)), height), preview_resampling(img))
        card = Image.new("RGBA", (220, height + 48), (18, 16, 20, 255))
        x = (card.width - resized.width) // 2
        card.alpha_composite(resized, (x, 12))
        cards.append(card)
        label = filename
        for suffix in ("_axe.png", "_bow.png", "_wand.png", "_staff.png"):
            label = label.replace(suffix, "")
        labels.append(label)

    preview = Image.new("RGBA", (len(cards) * 220 + 32, height + 80), (12, 11, 14, 255))
    draw = ImageDraw.Draw(preview)
    for idx, (card, label) in enumerate(zip(cards, labels)):
        x = 16 + idx * 220
        preview.alpha_composite(card, (x, 12))
        draw.text((x + 76, height + 26), label, fill=(232, 226, 215, 255))
    preview.save(PREVIEW_DIR / output_name)


def create_focus_preview(wand_files: list[str], staff_files: list[str]) -> None:
    PREVIEW_DIR.mkdir(parents=True, exist_ok=True)
    bg = Image.new("RGBA", (1360, 760), (12, 11, 14, 255))
    draw = ImageDraw.Draw(bg)
    draw.text((24, 20), "wands", fill=(232, 226, 215, 255))
    draw.text((24, 390), "staffs", fill=(232, 226, 215, 255))

    def paste_row(files: list[str], y_offset: int, target_height: int) -> None:
        for idx, filename in enumerate(files):
            img = Image.open(TEXTURE_DIR / filename).convert("RGBA")
            scale = target_height / img.height
            resized = img.resize((max(1, int(img.width * scale)), target_height), preview_resampling(img))
            x = 40 + idx * 210 + (180 - resized.width) // 2
            bg.alpha_composite(resized, (x, y_offset))
            label = filename
            for suffix in ("_wand.png", "_staff.png"):
                label = label.replace(suffix, "")
            draw.text((40 + idx * 210 + 68, y_offset + target_height + 12), label, fill=(232, 226, 215, 255))

    paste_row(wand_files, 50, 240)
    paste_row(staff_files, 420, 240)
    bg.save(PREVIEW_DIR / "preview_focus_families.png")


def main() -> None:
    axe_base = Image.open(
        ensure_base_texture(
            "assets/minecraft/textures/item/cit/custom/diamond_battle_axe.png",
            "_base_excalibur_battle_axe.png",
        )
    ).convert("RGBA")
    bow_base = Image.open(
        ensure_base_texture(
            "assets/minecraft/textures/item/excalibur/bow/infinity.png",
            "_base_excalibur_infinity_bow.png",
        )
    ).convert("RGBA")
    wand_base = Image.open(
        ensure_base_texture(
            "assets/minecraft/textures/item/blaze_rod.png",
            "_base_excalibur_blaze_rod.png",
        )
    ).convert("RGBA")
    staff_base = Image.open(
        ensure_base_texture(
            "assets/minecraft/textures/item/trident.png",
            "_base_excalibur_trident.png",
        )
    ).convert("RGBA")

    for tier, config in AXE_TIERS.items():
        save_texture(recolor_axe(axe_base, config), f"{tier}_axe.png")

    for tier, config in BOW_TIERS.items():
        save_texture(recolor_bow(bow_base, config), f"{tier}_bow.png")

    for tier, config in STAFF_TIERS.items():
        save_texture(recolor_staff(wand_base, config), f"{tier}_wand.png")
        save_texture(recolor_trident_staff(staff_base, config), f"{tier}_staff.png")

    for item_id, texture_path in STAFF_MODEL_TEXTURES.items():
        write_staff_model(item_id, texture_path)
    for item_id, texture_path in WAND_MODEL_TEXTURES.items():
        write_wand_model(item_id, texture_path)
    for item_id, texture_path in RELIC_MODEL_TEXTURES.items():
        write_relic_model(item_id, texture_path)
    for item_id, texture_path in FOCUS_MODEL_TEXTURES.items():
        write_focus_model(item_id, texture_path)

    create_row_preview(
        ["wood_axe.png", "stone_axe.png", "copper_axe.png", "bronze_axe.png", "iron_axe.png", "silver_axe.png"],
        "preview_axe_tiers.png",
    )
    create_row_preview(
        ["wood_bow.png", "stone_bow.png", "copper_bow.png", "bronze_bow.png", "iron_bow.png", "silver_bow.png"],
        "preview_bow_tiers.png",
    )
    create_focus_preview(
        ["wood_wand.png", "stone_wand.png", "copper_wand.png", "bronze_wand.png", "iron_wand.png", "silver_wand.png"],
        ["wood_staff.png", "stone_staff.png", "copper_staff.png", "bronze_staff.png", "iron_staff.png", "silver_staff.png"],
    )
    print("Generated Excalibur-sized recolors and staff display models.")


if __name__ == "__main__":
    main()
