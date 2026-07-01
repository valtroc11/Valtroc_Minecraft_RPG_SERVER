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


TIERS: dict[str, dict[str, object]] = {
    "wood": {
        "metal": [(76, 45, 19), (128, 79, 36), (183, 126, 71), (238, 196, 128)],
        "grip": [(45, 26, 15), (73, 45, 26), (108, 72, 44), (159, 120, 81)],
    },
    "stone": {
        "metal": [(49, 58, 73), (86, 102, 126), (129, 151, 183), (201, 221, 240)],
        "grip": [(44, 42, 47), (72, 72, 78), (109, 112, 119), (162, 168, 177)],
    },
    "copper": {
        "metal": [(107, 48, 14), (173, 88, 25), (228, 134, 43), (255, 202, 103)],
        "grip": [(58, 27, 15), (96, 49, 25), (139, 80, 42), (194, 131, 78)],
    },
    "bronze": {
        "metal": [(117, 67, 15), (181, 113, 24), (229, 164, 53), (255, 223, 117)],
        "grip": [(61, 35, 14), (101, 63, 23), (148, 100, 43), (206, 156, 88)],
    },
    "iron": {
        "metal": [(67, 76, 91), (111, 125, 150), (165, 184, 211), (232, 242, 255)],
        "grip": [(52, 35, 28), (84, 58, 48), (121, 90, 77), (176, 141, 121)],
    },
    "silver": {
        "metal": [(116, 128, 151), (170, 187, 214), (221, 235, 250), (255, 255, 255)],
        "grip": [(58, 45, 43), (92, 76, 73), (134, 121, 117), (190, 181, 175)],
    },
}

APPRENTICE = {
    "metal": [(86, 95, 112), (132, 145, 170), (187, 202, 228), (243, 249, 255)],
    "grip": [(55, 38, 30), (84, 59, 45), (120, 89, 72), (170, 139, 116)],
}


def ensure_base_texture() -> Path:
    TEXTURE_DIR.mkdir(parents=True, exist_ok=True)
    ORAXEN_TEXTURE_DIR.mkdir(parents=True, exist_ok=True)
    return ensure_excalibur_texture(
        "assets/minecraft/textures/item/cit/custom/diamond_master_sword.png",
        TEXTURE_DIR / "_base_excalibur_master_sword.png",
    )


def save_texture(img: Image.Image, filename: str) -> None:
    TEXTURE_DIR.mkdir(parents=True, exist_ok=True)
    ORAXEN_TEXTURE_DIR.mkdir(parents=True, exist_ok=True)
    img.save(TEXTURE_DIR / filename)
    img.save(ORAXEN_TEXTURE_DIR / filename)


def luminance(r: int, g: int, b: int) -> float:
    return (0.2126 * r + 0.7152 * g + 0.0722 * b) / 255.0


def is_grip_pixel(r: int, g: int, b: int, x: int, y: int, width: int, height: int) -> bool:
    h, s, v = colorsys.rgb_to_hsv(r / 255.0, g / 255.0, b / 255.0)
    center = width / 2.0
    return (
        y >= height * 0.56
        and abs(x - center) <= max(1.0, width * 0.12)
        and 0.02 <= h <= 0.14
        and s >= 0.10
        and v >= 0.05
    )


def recolor_sword(source: Image.Image, config: dict[str, object]) -> Image.Image:
    out = Image.new("RGBA", source.size, (0, 0, 0, 0))
    src = source.convert("RGBA")
    width, height = src.size
    metal = config["metal"]
    grip = config["grip"]
    assert isinstance(metal, list)
    assert isinstance(grip, list)

    for y in range(height):
        for x in range(width):
            r, g, b, a = src.getpixel((x, y))
            if a == 0:
                continue

            shade = luminance(r, g, b)
            if is_grip_pixel(r, g, b, x, y, width, height):
                color = sample_gradient(grip, shade)
            else:
                color = sample_gradient(metal, shade)

            if shade > 0.80:
                color = mix(color, (255, 255, 255), (shade - 0.80) / 0.20 * 0.18)
            elif shade < 0.18:
                color = mix(color, (18, 18, 22), (0.18 - shade) / 0.18 * 0.28)

            out.putpixel((x, y), (*color, a))
    return out


def create_preview(filenames: list[str]) -> None:
    PREVIEW_DIR.mkdir(parents=True, exist_ok=True)
    cards: list[Image.Image] = []
    labels: list[str] = []

    for filename in filenames:
        img = Image.open(TEXTURE_DIR / filename).convert("RGBA")
        scale = 280 / img.height
        resized = img.resize((max(1, int(img.width * scale)), 280), Image.Resampling.NEAREST)
        card = Image.new("RGBA", (220, 340), (18, 16, 20, 255))
        x = (card.width - resized.width) // 2
        y = 18
        card.alpha_composite(resized, (x, y))
        cards.append(card)
        labels.append(filename.replace("_sword.png", ""))

    preview = Image.new("RGBA", (len(cards) * 220 + 32, 360), (12, 11, 14, 255))
    draw = ImageDraw.Draw(preview)
    for idx, (label, card) in enumerate(zip(labels, cards)):
        x = 16 + idx * 220
        preview.alpha_composite(card, (x, 16))
        draw.text((x + 74, 316), label, fill=(232, 226, 215, 255))

    preview.save(PREVIEW_DIR / "preview_sword_tiers.png")


def main() -> None:
    source = Image.open(ensure_base_texture()).convert("RGBA")
    generated: list[str] = []
    for tier, config in TIERS.items():
        filename = f"{tier}_sword.png"
        save_texture(recolor_sword(source, config), filename)
        generated.append(filename)
    save_texture(recolor_sword(source, APPRENTICE), "apprentice_sword.png")

    create_preview(generated)
    print("Generated vivid recolor sword textures:", ", ".join(generated), "and apprentice_sword.png")


if __name__ == "__main__":
    main()
