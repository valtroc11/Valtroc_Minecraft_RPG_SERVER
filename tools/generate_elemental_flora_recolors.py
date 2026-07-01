from __future__ import annotations

from io import BytesIO
from pathlib import Path
from zipfile import ZipFile
import colorsys
import sys

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(Path(__file__).resolve().parent))

from generate_signature_retextures import mix, preview_resampling


EXCALIBUR_ZIP = ROOT / "Excalibur_V26.1_01.zip"
TEXTURE_DIR = ROOT / "content" / "oraxen" / "pack" / "textures" / "servidorpg" / "flora"
ORAXEN_TEXTURE_DIR = ROOT / "content" / "oraxen" / "pack" / "assets" / "oraxen" / "textures" / "servidorpg" / "flora"
PREVIEW_DIR = ROOT / "docs" / "previews"


WOODS = {
    "oak": [
        "oak_log.png",
        "oak_log_top.png",
        "stripped_oak_log.png",
        "stripped_oak_log_top.png",
        "oak_planks.png",
        "oak_trapdoor.png",
        "oak_door_bottom.png",
        "oak_door_top.png",
        "oak_sapling.png",
        "oak_leaves.png",
        "oak_leaves_tips.png",
    ],
    "spruce": [
        "spruce_log.png",
        "spruce_log_top.png",
        "stripped_spruce_log.png",
        "stripped_spruce_log_top.png",
        "spruce_planks.png",
        "spruce_trapdoor.png",
        "spruce_door_bottom.png",
        "spruce_door_top.png",
        "spruce_sapling.png",
        "spruce_leaves.png",
        "spruce_leaves_tips.png",
    ],
    "birch": [
        "birch_log.png",
        "birch_log_top.png",
        "stripped_birch_log.png",
        "stripped_birch_log_top.png",
        "birch_planks.png",
        "birch_trapdoor.png",
        "birch_door_bottom.png",
        "birch_door_top.png",
        "birch_sapling.png",
        "birch_leaves.png",
        "birch_leaves_tips.png",
    ],
    "jungle": [
        "jungle_log.png",
        "jungle_log_top.png",
        "stripped_jungle_log.png",
        "stripped_jungle_log_top.png",
        "jungle_planks.png",
        "jungle_trapdoor.png",
        "jungle_door_bottom.png",
        "jungle_door_top.png",
        "jungle_sapling.png",
        "jungle_leaves.png",
        "jungle_leaves_tips.png",
    ],
    "acacia": [
        "acacia_log.png",
        "acacia_log_top.png",
        "stripped_acacia_log.png",
        "stripped_acacia_log_top.png",
        "acacia_planks.png",
        "acacia_trapdoor.png",
        "acacia_door_bottom.png",
        "acacia_door_top.png",
        "acacia_sapling.png",
        "acacia_leaves.png",
        "acacia_leaves_tips.png",
    ],
    "dark_oak": [
        "dark_oak_log.png",
        "dark_oak_log_top.png",
        "stripped_dark_oak_log.png",
        "stripped_dark_oak_log_top.png",
        "dark_oak_planks.png",
        "dark_oak_planks_slab.png",
        "dark_oak_trapdoor.png",
        "dark_oak_door_bottom.png",
        "dark_oak_door_top.png",
        "dark_oak_sapling.png",
        "dark_oak_leaves.png",
        "dark_oak_leaves_tips.png",
    ],
    "mangrove": [
        "mangrove_log.png",
        "mangrove_log_top.png",
        "stripped_mangrove_log.png",
        "stripped_mangrove_log_top.png",
        "mangrove_planks.png",
        "mangrove_trapdoor.png",
        "mangrove_door_bottom.png",
        "mangrove_door_top.png",
        "mangrove_leaves.png",
        "mangrove_leaves_tips.png",
    ],
    "cherry": [
        "cherry_log.png",
        "cherry_log_top.png",
        "stripped_cherry_log.png",
        "stripped_cherry_log_top.png",
        "cherry_planks.png",
        "cherry_trapdoor.png",
        "cherry_door_bottom.png",
        "cherry_door_top.png",
        "cherry_sapling.png",
        "cherry_leaves.png",
        "cherry_leaves_tips.png",
    ],
    "pale_oak": [
        "pale_oak_log.png",
        "pale_oak_log_top.png",
        "stripped_pale_oak_log.png",
        "stripped_pale_oak_log_top.png",
        "pale_oak_planks.png",
        "pale_oak_trapdoor.png",
        "pale_oak_door_bottom.png",
        "pale_oak_door_top.png",
        "pale_oak_sapling.png",
        "pale_oak_leaves.png",
        "pale_oak_leaves_tips.png",
    ],
}


FLOWERS = [
    "dandelion.png",
    "poppy.png",
    "blue_orchid.png",
    "allium.png",
    "azure_bluet.png",
    "red_tulip.png",
    "orange_tulip.png",
    "white_tulip.png",
    "pink_tulip.png",
    "oxeye_daisy.png",
    "cornflower.png",
    "lily_of_the_valley.png",
    "sunflower_front.png",
    "sunflower_top.png",
    "rose_bush_bottom.png",
    "rose_bush_top.png",
    "peony_bottom.png",
    "peony_top.png",
    "lilac_bottom.png",
    "lilac_top.png",
    "torchflower.png",
    "wildflowers.png",
]


ELEMENTS = {
    "ice": {
        "wood_target": (82, 151, 191),
        "wood_shadow": (28, 60, 84),
        "flower_target": (142, 217, 243),
    },
    "fire": {
        "wood_target": (184, 75, 38),
        "wood_shadow": (79, 28, 18),
        "flower_target": (239, 116, 48),
    },
}


def lum(rgb: tuple[int, int, int]) -> float:
    r, g, b = rgb
    return (0.2126 * r + 0.7152 * g + 0.0722 * b) / 255.0


def sat(rgb: tuple[int, int, int]) -> float:
    r, g, b = rgb
    return colorsys.rgb_to_hsv(r / 255.0, g / 255.0, b / 255.0)[1]


def load(zip_file: ZipFile, filename: str) -> Image.Image | None:
    member = f"assets/minecraft/textures/block/{filename}"
    try:
        with zip_file.open(member) as fh:
            return Image.open(BytesIO(fh.read())).convert("RGBA")
    except KeyError:
        return None


def recolor_wood(img: Image.Image, element: str, is_leaf_or_sapling: bool) -> Image.Image:
    target = ELEMENTS[element]["wood_target"]
    shadow = ELEMENTS[element]["wood_shadow"]
    assert isinstance(target, tuple)
    assert isinstance(shadow, tuple)
    out = Image.new("RGBA", img.size, (0, 0, 0, 0))
    strength = 0.34 if is_leaf_or_sapling else 0.58
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = img.getpixel((x, y))
            if a == 0:
                continue
            source = (r, g, b)
            shade = lum(source)
            themed = mix(shadow, target, min(1.0, shade * 1.18 + 0.04))
            keep = 0.22 if sat(source) > 0.45 else 0.14
            color = mix(source, themed, strength)
            color = mix(color, source, keep)
            out.putpixel((x, y), (*color, a))
    return out


def recolor_flower(img: Image.Image, element: str) -> Image.Image:
    target = ELEMENTS[element]["flower_target"]
    assert isinstance(target, tuple)
    out = Image.new("RGBA", img.size, (0, 0, 0, 0))
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = img.getpixel((x, y))
            if a == 0:
                continue
            source = (r, g, b)
            strength = 0.16 if sat(source) > 0.25 else 0.08
            color = mix(source, target, strength)
            out.putpixel((x, y), (*color, a))
    return out


def save(img: Image.Image, relative_name: str) -> Path:
    for base in (TEXTURE_DIR, ORAXEN_TEXTURE_DIR):
        target = base / relative_name
        target.parent.mkdir(parents=True, exist_ok=True)
        img.save(target)
    return TEXTURE_DIR / relative_name


def create_preview(samples: list[tuple[str, Path]], name: str) -> None:
    PREVIEW_DIR.mkdir(parents=True, exist_ok=True)
    cols = 6
    cell_w = 132
    cell_h = 122
    rows = (len(samples) + cols - 1) // cols
    preview = Image.new("RGBA", (cols * cell_w + 28, rows * cell_h + 56), (12, 11, 14, 255))
    draw = ImageDraw.Draw(preview)
    draw.text((16, 16), name.replace("_", " "), fill=(235, 229, 218, 255))
    for idx, (label, path) in enumerate(samples):
        x = 14 + (idx % cols) * cell_w
        y = 46 + (idx // cols) * cell_h
        tex = Image.open(path).convert("RGBA")
        scale = min(72 / tex.width, 72 / tex.height)
        resized = tex.resize((max(1, int(tex.width * scale)), max(1, int(tex.height * scale))), preview_resampling(tex))
        preview.alpha_composite(resized, (x + 28, y))
        draw.text((x + 4, y + 78), label[:18], fill=(220, 214, 204, 255))
    preview.save(PREVIEW_DIR / f"{name}.png")


def main() -> None:
    wood_samples: list[tuple[str, Path]] = []
    flower_samples: list[tuple[str, Path]] = []
    with ZipFile(EXCALIBUR_ZIP) as zip_file:
        for wood, filenames in WOODS.items():
            for filename in filenames:
                source = load(zip_file, filename)
                if source is None:
                    continue
                stem = Path(filename).stem
                is_leaf_or_sapling = "leaves" in stem or "sapling" in stem
                for element in ELEMENTS:
                    path = save(recolor_wood(source, element, is_leaf_or_sapling), f"trees/{element}/{wood}/{stem}.png")
                    if stem in {f"{wood}_log", f"{wood}_planks", f"{wood}_leaves"}:
                        wood_samples.append((f"{wood} {element}", path))

        for filename in FLOWERS:
            source = load(zip_file, filename)
            if source is None:
                continue
            stem = Path(filename).stem
            for element in ELEMENTS:
                path = save(recolor_flower(source, element), f"flowers/{element}/{stem}.png")
                flower_samples.append((f"{stem} {element}", path))

    create_preview(wood_samples, "preview_elemental_tree_recolors")
    create_preview(flower_samples, "preview_elemental_flower_recolors")
    print(f"Generated {len(wood_samples)} tree preview samples and {len(flower_samples)} flower textures.")


if __name__ == "__main__":
    main()
