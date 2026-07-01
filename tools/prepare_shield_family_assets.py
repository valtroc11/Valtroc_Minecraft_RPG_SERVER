from __future__ import annotations

from pathlib import Path
from shutil import copyfile

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
TEXTURE_DIR = ROOT / "content" / "oraxen" / "pack" / "textures" / "servidorpg"
ASSET_TEXTURE_DIR = (
    ROOT / "content" / "oraxen" / "pack" / "assets" / "oraxen" / "textures" / "servidorpg"
)
PREVIEW_DIR = ROOT / "docs" / "previews"

TIERS = ["wood", "stone", "copper", "bronze", "iron", "silver"]


def copy_tower_aliases() -> None:
    ASSET_TEXTURE_DIR.mkdir(parents=True, exist_ok=True)
    for tier in TIERS:
        source = TEXTURE_DIR / f"{tier}_shield.png"
        target = TEXTURE_DIR / f"{tier}_shield_tower.png"
        asset_target = ASSET_TEXTURE_DIR / f"{tier}_shield_tower.png"
        copyfile(source, target)
        copyfile(source, asset_target)


def make_preview() -> None:
    PREVIEW_DIR.mkdir(parents=True, exist_ok=True)
    cell_w = 188
    cell_h = 232
    padding = 20
    header_h = 42
    cols = 3
    rows = len(TIERS) + 1
    canvas = Image.new(
        "RGBA",
        (padding * 2 + cols * cell_w, padding * 2 + header_h + rows * cell_h),
        (16, 14, 18, 255),
    )
    draw = ImageDraw.Draw(canvas)

    headers = ["tier", "rondela", "torre"]
    for col, header in enumerate(headers):
        x = padding + col * cell_w + 8
        draw.text((x, padding + 8), header, fill=(232, 224, 210, 255))

    for row, tier in enumerate(TIERS, start=1):
        y = padding + header_h + row * cell_h - cell_h + 8
        draw.text((padding + 8, y), tier, fill=(210, 201, 186, 255))

        round_img = Image.open(TEXTURE_DIR / f"{tier}_shield_round.png").convert("RGBA")
        round_img = round_img.resize((128, 128), Image.Resampling.NEAREST)
        round_x = padding + cell_w + 22
        round_y = y + 34
        canvas.alpha_composite(round_img, (round_x, round_y))

        tower_img = Image.open(TEXTURE_DIR / f"{tier}_shield_tower.png").convert("RGBA")
        tower_img.thumbnail((156, 196), Image.Resampling.LANCZOS)
        tower_x = padding + cell_w * 2 + (cell_w - tower_img.width) // 2
        tower_y = y + 12
        canvas.alpha_composite(tower_img, (tower_x, tower_y))

    canvas.save(PREVIEW_DIR / "preview_shield_families.png")


def main() -> None:
    copy_tower_aliases()
    make_preview()
    print("Prepared shield families for tiers:", ", ".join(TIERS))


if __name__ == "__main__":
    main()
