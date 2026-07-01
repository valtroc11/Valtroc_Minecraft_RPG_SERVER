from __future__ import annotations

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


def recolor_texture(source: Image.Image, gradient: list[tuple[int, int, int]]) -> Image.Image:
    out = Image.new("RGBA", source.size, (0, 0, 0, 0))
    src = source.convert("RGBA")

    for y in range(src.height):
        for x in range(src.width):
            r, g, b, a = src.getpixel((x, y))
            if a == 0:
                continue
            luminance = (0.2126 * r + 0.7152 * g + 0.0722 * b) / 255.0
            color = sample_gradient(gradient, luminance)
            out.putpixel((x, y), (*color, a))
    return out


def save_texture(img: Image.Image, filename: str) -> None:
    TEXTURE_DIR.mkdir(parents=True, exist_ok=True)
    ORAXEN_TEXTURE_DIR.mkdir(parents=True, exist_ok=True)
    img.save(TEXTURE_DIR / filename)
    img.save(ORAXEN_TEXTURE_DIR / filename)


TIERS = {
    "wood": {
        "face": [(37, 26, 18), (68, 54, 44), (104, 80, 62), (155, 123, 86)],
        "handle": [(20, 19, 20), (40, 39, 41), (72, 70, 74), (145, 144, 151)],
    },
    "stone": {
        "face": [(33, 36, 41), (63, 67, 74), (100, 106, 115), (149, 155, 164)],
        "handle": [(24, 26, 30), (54, 58, 66), (104, 111, 122), (176, 184, 194)],
    },
    "copper": {
        "face": [(53, 34, 20), (95, 61, 36), (151, 95, 56), (218, 151, 90)],
        "handle": [(50, 31, 18), (103, 62, 35), (174, 108, 62), (236, 174, 101)],
    },
    "bronze": {
        "face": [(46, 28, 14), (82, 51, 24), (130, 79, 38), (191, 132, 68)],
        "handle": [(44, 27, 14), (93, 57, 27), (154, 97, 46), (216, 158, 89)],
    },
    "iron": {
        "face": [(29, 31, 35), (62, 67, 75), (105, 111, 121), (169, 176, 186)],
        "handle": [(25, 27, 31), (57, 61, 69), (111, 118, 129), (192, 199, 209)],
    },
    "silver": {
        "face": [(61, 69, 80), (103, 114, 129), (163, 175, 193), (227, 236, 248)],
        "handle": [(70, 78, 91), (121, 133, 149), (182, 194, 212), (245, 249, 255)],
    },
}


def ensure_base_textures() -> tuple[Path, Path, Path]:
    TEXTURE_DIR.mkdir(parents=True, exist_ok=True)
    ORAXEN_TEXTURE_DIR.mkdir(parents=True, exist_ok=True)
    base_face = ensure_excalibur_texture(
        "assets/minecraft/textures/item/excalibur/custom_name/shield/round.png",
        TEXTURE_DIR / "_base_excalibur_round_shield.png",
    )
    base_handle = ensure_excalibur_texture(
        "assets/minecraft/textures/item/excalibur/custom_name/shield/handle.png",
        TEXTURE_DIR / "_base_excalibur_round_shield_handle.png",
    )
    base_particle = ensure_excalibur_texture(
        "assets/minecraft/textures/item/excalibur/custom_name/shield/shield_particle.png",
        TEXTURE_DIR / "_base_excalibur_round_shield_particle.png",
    )
    return base_face, base_handle, base_particle


def create_preview(filenames: list[str]) -> None:
    PREVIEW_DIR.mkdir(parents=True, exist_ok=True)
    tiles: list[Image.Image] = []
    labels: list[str] = []
    for filename in filenames:
        img = Image.open(TEXTURE_DIR / filename).convert("RGBA")
        tiles.append(img.resize((img.width * 8, img.height * 8), Image.Resampling.NEAREST))
        labels.append(filename.replace("_shield_round.png", ""))

    tile_w, tile_h = tiles[0].size
    padding = 24
    label_h = 18
    preview = Image.new(
        "RGBA",
        (padding + len(tiles) * (tile_w + padding), tile_h + padding * 2 + label_h),
        (17, 15, 18, 255),
    )
    draw = ImageDraw.Draw(preview)
    for idx, (label, tile) in enumerate(zip(labels, tiles)):
        x = padding + idx * (tile_w + padding)
        y = padding
        preview.alpha_composite(tile, (x, y))
        draw.text((x, y + tile_h + 4), label, fill=(228, 223, 214, 255))
    preview.save(PREVIEW_DIR / "preview_round_shield_tiers.png")


def main() -> None:
    base_face_path, base_handle_path, base_particle_path = ensure_base_textures()
    face_src = Image.open(base_face_path).convert("RGBA")
    handle_src = Image.open(base_handle_path).convert("RGBA")
    particle_src = Image.open(base_particle_path).convert("RGBA")

    preview_faces: list[str] = []

    for tier, config in TIERS.items():
        save_texture(recolor_texture(face_src, config["face"]), f"{tier}_shield_round.png")
        save_texture(recolor_texture(handle_src, config["handle"]), f"{tier}_shield_round_handle.png")
        save_texture(recolor_texture(particle_src, config["face"]), f"{tier}_shield_round_particle.png")
        preview_faces.append(f"{tier}_shield_round.png")

    create_preview(preview_faces)
    print("Generated round shield textures from Excalibur base:", ", ".join(TIERS))


if __name__ == "__main__":
    main()
