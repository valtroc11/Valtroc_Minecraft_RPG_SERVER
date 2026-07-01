from __future__ import annotations

import colorsys
import math
from pathlib import Path
from shutil import copyfile

from PIL import Image, ImageDraw


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


def luminance(r: int, g: int, b: int) -> float:
    return (0.2126 * r + 0.7152 * g + 0.0722 * b) / 255.0


def hsv(r: int, g: int, b: int) -> tuple[float, float, float]:
    return colorsys.rgb_to_hsv(r / 255.0, g / 255.0, b / 255.0)


def ensure_base_texture() -> Path:
    TEXTURE_DIR.mkdir(parents=True, exist_ok=True)
    ORAXEN_TEXTURE_DIR.mkdir(parents=True, exist_ok=True)
    base = TEXTURE_DIR / "_base_wood_shield_tower.png"
    if not base.exists():
        copyfile(TEXTURE_DIR / "wood_shield.png", base)
    return base


def save_texture(img: Image.Image, filename: str) -> None:
    TEXTURE_DIR.mkdir(parents=True, exist_ok=True)
    ORAXEN_TEXTURE_DIR.mkdir(parents=True, exist_ok=True)
    img.save(TEXTURE_DIR / filename)
    img.save(ORAXEN_TEXTURE_DIR / filename)


def is_metal_pixel(r: int, g: int, b: int) -> bool:
    h, s, v = hsv(r, g, b)
    return s <= 0.24 and v >= 0.12


def is_panel_pixel(r: int, g: int, b: int) -> bool:
    h, s, v = hsv(r, g, b)
    return 0.03 <= h <= 0.15 and s >= 0.20 and v >= 0.22


def is_strap_pixel(r: int, g: int, b: int) -> bool:
    h, s, v = hsv(r, g, b)
    return 0.02 <= h <= 0.12 and s >= 0.18 and v < 0.22


TIERS: dict[str, dict[str, object]] = {
    "wood": {
        "panel": [(74, 49, 26), (111, 78, 47), (149, 110, 71), (197, 156, 109)],
        "strap": [(45, 28, 19), (71, 47, 33), (102, 72, 52), (145, 111, 83)],
        "metal": [(43, 45, 50), (88, 93, 100), (138, 145, 154), (224, 229, 235)],
    },
    "stone": {
        "panel": [(38, 41, 46), (74, 80, 88), (117, 124, 134), (181, 188, 198)],
        "strap": [(31, 33, 37), (58, 63, 70), (92, 99, 108), (139, 146, 156)],
        "metal": [(52, 56, 63), (98, 105, 114), (151, 159, 170), (231, 236, 242)],
    },
    "copper": {
        "panel": [(69, 41, 21), (124, 73, 42), (185, 112, 66), (241, 176, 108)],
        "strap": [(49, 30, 19), (83, 51, 31), (121, 80, 49), (170, 124, 79)],
        "metal": [(60, 35, 18), (109, 63, 36), (164, 97, 57), (226, 153, 92)],
    },
    "bronze": {
        "panel": [(62, 39, 18), (104, 66, 31), (152, 98, 50), (212, 156, 90)],
        "strap": [(48, 29, 17), (78, 49, 29), (114, 75, 46), (161, 118, 74)],
        "metal": [(74, 48, 24), (124, 79, 41), (180, 119, 64), (233, 182, 118)],
    },
    "iron": {
        "panel": [(42, 46, 52), (87, 93, 102), (136, 144, 155), (220, 227, 236)],
        "strap": [(34, 36, 41), (62, 67, 75), (96, 103, 113), (144, 151, 162)],
        "metal": [(58, 63, 70), (105, 112, 122), (157, 165, 176), (237, 242, 248)],
    },
    "silver": {
        "panel": [(66, 74, 85), (115, 126, 141), (172, 183, 200), (240, 245, 252)],
        "strap": [(45, 50, 58), (81, 89, 99), (121, 131, 144), (172, 181, 193)],
        "metal": [(82, 91, 103), (136, 147, 163), (196, 207, 222), (250, 252, 255)],
    },
}


def recolor_tower_shield(source: Image.Image, config: dict[str, object]) -> Image.Image:
    out = Image.new("RGBA", source.size, (0, 0, 0, 0))
    src = source.convert("RGBA")
    panel = config["panel"]
    strap = config["strap"]
    metal = config["metal"]
    assert isinstance(panel, list)
    assert isinstance(strap, list)
    assert isinstance(metal, list)

    for y in range(src.height):
        for x in range(src.width):
            r, g, b, a = src.getpixel((x, y))
            if a == 0:
                continue

            shade = luminance(r, g, b)
            if is_metal_pixel(r, g, b):
                color = sample_gradient(metal, shade)
            elif is_strap_pixel(r, g, b):
                color = sample_gradient(strap, shade)
            elif is_panel_pixel(r, g, b):
                color = sample_gradient(panel, shade)
            else:
                color = sample_gradient(panel, shade)
            out.putpixel((x, y), (*color, a))
    return out


def create_preview(filenames: list[str]) -> None:
    PREVIEW_DIR.mkdir(parents=True, exist_ok=True)
    cards: list[Image.Image] = []
    labels: list[str] = []

    for filename in filenames:
        img = Image.open(TEXTURE_DIR / filename).convert("RGBA")
        scale = 300 / img.height
        resized = img.resize((max(1, int(img.width * scale)), 300), Image.Resampling.LANCZOS)
        card = Image.new("RGBA", (220, 360), (18, 16, 20, 255))
        x = (card.width - resized.width) // 2
        card.alpha_composite(resized, (x, 12))
        cards.append(card)
        labels.append(filename.replace("_shield.png", ""))

    preview = Image.new("RGBA", (len(cards) * 220 + 32, 388), (12, 11, 14, 255))
    draw = ImageDraw.Draw(preview)
    for idx, (label, card) in enumerate(zip(labels, cards)):
        x = 16 + idx * 220
        preview.alpha_composite(card, (x, 12))
        draw.text((x + 68, 340), label, fill=(232, 226, 215, 255))

    preview.save(PREVIEW_DIR / "preview_tower_shield_tiers.png")


def main() -> None:
    source = Image.open(ensure_base_texture()).convert("RGBA")
    generated: list[str] = []

    for tier, config in TIERS.items():
        texture = recolor_tower_shield(source, config)
        save_texture(texture, f"{tier}_shield.png")
        save_texture(texture, f"{tier}_shield_tower.png")
        generated.append(f"{tier}_shield.png")

    create_preview(generated)
    print("Generated tower shield textures:", ", ".join(generated))


if __name__ == "__main__":
    main()
