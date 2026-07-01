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
TEXTURE_DIR = ROOT / "content" / "oraxen" / "pack" / "textures" / "servidorpg" / "mobs" / "entities"
ORAXEN_TEXTURE_DIR = (
    ROOT / "content" / "oraxen" / "pack" / "assets" / "oraxen" / "textures" / "servidorpg" / "mobs" / "entities"
)
PREVIEW_DIR = ROOT / "docs" / "previews"


MOBS = {
    "zombie": {
        "display": "Zombie",
        "type": "ZOMBIE",
        "texture": "assets/minecraft/textures/entity/zombie/zombie.png",
    },
    "skeleton": {
        "display": "Esqueleto",
        "type": "SKELETON",
        "texture": "assets/minecraft/textures/entity/skeleton/skeleton.png",
    },
    "spider": {
        "display": "Arana",
        "type": "SPIDER",
        "texture": "assets/minecraft/textures/entity/spider/spider.png",
    },
    "cave_spider": {
        "display": "Arana de cueva",
        "type": "CAVE_SPIDER",
        "texture": "assets/minecraft/textures/entity/spider/cave_spider.png",
    },
    "creeper": {
        "display": "Creeper",
        "type": "CREEPER",
        "texture": "assets/minecraft/textures/entity/creeper/creeper.png",
    },
    "enderman": {
        "display": "Enderman",
        "type": "ENDERMAN",
        "texture": "assets/minecraft/textures/entity/enderman/enderman.png",
    },
    "witch": {
        "display": "Bruja",
        "type": "WITCH",
        "texture": "assets/minecraft/textures/entity/witch/witch.png",
    },
    "piglin": {
        "display": "Piglin",
        "type": "PIGLIN",
        "texture": "assets/minecraft/textures/entity/piglin/piglin.png",
    },
    "wolf": {
        "display": "Lobo",
        "type": "WOLF",
        "texture": "assets/minecraft/textures/entity/wolf/wolf.png",
    },
    "bee": {
        "display": "Abeja",
        "type": "BEE",
        "texture": "assets/minecraft/textures/entity/bee/bee.png",
    },
    "iron_golem": {
        "display": "Golem de hierro",
        "type": "IRON_GOLEM",
        "texture": "assets/minecraft/textures/entity/iron_golem/iron_golem.png",
    },
    "llama": {
        "display": "Llama",
        "type": "LLAMA",
        "texture": "assets/minecraft/textures/entity/llama/llama_creamy.png",
    },
}


ELEMENTS = {
    "ice": {
        "display_prefix": "de Hielo",
        "display_color": "&b",
        "palette": [(22, 50, 77), (57, 128, 171), (128, 216, 241), (235, 253, 255)],
        "accent": (198, 245, 255),
    },
    "fire": {
        "display_prefix": "de Fuego",
        "display_color": "&6",
        "palette": [(72, 22, 13), (157, 56, 20), (237, 126, 37), (255, 226, 112)],
        "accent": (255, 184, 75),
    },
}


def luminance(r: int, g: int, b: int) -> float:
    return (0.2126 * r + 0.7152 * g + 0.0722 * b) / 255.0


def saturation(r: int, g: int, b: int) -> float:
    return colorsys.rgb_to_hsv(r / 255.0, g / 255.0, b / 255.0)[1]


def sample_gradient(stops: list[tuple[int, int, int]], value: float) -> tuple[int, int, int]:
    value = max(0.0, min(1.0, value))
    scaled = value * (len(stops) - 1)
    idx = min(len(stops) - 2, int(scaled))
    frac = scaled - idx
    return mix(stops[idx], stops[idx + 1], frac)


def load_texture(zip_file: ZipFile, member: str) -> Image.Image:
    with zip_file.open(member) as fh:
        return Image.open(BytesIO(fh.read())).convert("RGBA")


def elemental_recolor(source: Image.Image, element: str) -> Image.Image:
    src = source.convert("RGBA")
    out = Image.new("RGBA", src.size, (0, 0, 0, 0))
    palette = ELEMENTS[element]["palette"]
    accent = ELEMENTS[element]["accent"]
    assert isinstance(palette, list)
    assert isinstance(accent, tuple)

    for y in range(src.height):
        for x in range(src.width):
            r, g, b, a = src.getpixel((x, y))
            if a == 0:
                continue
            lum = luminance(r, g, b)
            sat = saturation(r, g, b)
            color = sample_gradient(palette, min(1.0, lum * 1.08 + 0.03))
            keep_detail = 0.22 if sat > 0.35 else 0.12
            color = mix(color, (r, g, b), keep_detail)
            if (x * 3 + y * 5) % 37 == 0 and lum > 0.28:
                color = mix(color, accent, 0.35)
            out.putpixel((x, y), (*color, a))
    return out


def save_texture(img: Image.Image, filename: str) -> Path:
    for base in (TEXTURE_DIR, ORAXEN_TEXTURE_DIR):
        base.mkdir(parents=True, exist_ok=True)
        img.save(base / filename)
    return TEXTURE_DIR / filename


def create_preview(files: list[tuple[str, Path]]) -> None:
    PREVIEW_DIR.mkdir(parents=True, exist_ok=True)
    cell_w = 178
    cell_h = 154
    cols = 4
    rows = (len(files) + cols - 1) // cols
    preview = Image.new("RGBA", (cols * cell_w + 32, rows * cell_h + 64), (12, 11, 14, 255))
    draw = ImageDraw.Draw(preview)
    draw.text((18, 18), "Classic mob elemental recolors from Excalibur", fill=(235, 229, 218, 255))
    for idx, (label, path) in enumerate(files):
        x = 16 + (idx % cols) * cell_w
        y = 52 + (idx // cols) * cell_h
        tex = Image.open(path).convert("RGBA")
        scale = min(96 / tex.width, 96 / tex.height)
        resized = tex.resize((max(1, int(tex.width * scale)), max(1, int(tex.height * scale))), preview_resampling(tex))
        preview.alpha_composite(resized, (x + (120 - resized.width) // 2, y))
        draw.text((x + 4, y + 104), label.replace("_", " ")[:25], fill=(220, 214, 204, 255))
    preview.save(PREVIEW_DIR / "preview_classic_mob_elemental_recolors.png")


def main() -> None:
    generated: list[tuple[str, Path]] = []
    with ZipFile(EXCALIBUR_ZIP) as zip_file:
        for mob_id, spec in MOBS.items():
            source = load_texture(zip_file, str(spec["texture"]))
            save_texture(source, f"{mob_id}_excalibur_base.png")
            for element in ELEMENTS:
                filename = f"{mob_id}_{element}.png"
                path = save_texture(elemental_recolor(source, element), filename)
                generated.append((filename.removesuffix(".png"), path))
    create_preview(generated)
    print(f"Generated {len(generated)} elemental mob textures from Excalibur.")


if __name__ == "__main__":
    main()
