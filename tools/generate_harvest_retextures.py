from __future__ import annotations

from pathlib import Path
import sys

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(Path(__file__).resolve().parent))

from generate_future_class_retextures import recolor_dagger
from generate_signature_retextures import (
    TIERS,
    ensure_base_texture,
    finish_color,
    is_brown_pixel,
    is_metal_pixel,
    luminance,
    mix,
    preview_resampling,
    sample_gradient,
)
TEXTURE_DIR = ROOT / "content" / "oraxen" / "pack" / "textures" / "servidorpg"
ORAXEN_TEXTURE_DIR = ROOT / "content" / "oraxen" / "pack" / "assets" / "oraxen" / "textures" / "servidorpg"
PREVIEW_DIR = ROOT / "docs" / "previews"


def save_nested_texture(img: Image.Image, relative_name: str) -> None:
    target = TEXTURE_DIR / relative_name
    target.parent.mkdir(parents=True, exist_ok=True)
    target_oraxen = ORAXEN_TEXTURE_DIR / relative_name
    target_oraxen.parent.mkdir(parents=True, exist_ok=True)
    img.save(target)
    img.save(target_oraxen)


def knife_from_dagger(dagger: Image.Image) -> Image.Image:
    src = dagger.convert("RGBA")
    scaled = src.resize(
        (max(1, int(src.width * 0.78)), max(1, int(src.height * 0.78))),
        preview_resampling(src),
    )
    out = Image.new("RGBA", src.size, (0, 0, 0, 0))
    x = max(0, (src.width - scaled.width) // 2 + 2)
    y = max(0, src.height - scaled.height - 1)
    out.alpha_composite(scaled, (x, y))
    return out


def draw_hide_icon(config: dict[str, object]) -> Image.Image:
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    wood = config["wood"]
    hunter = config["hunter"]
    assert isinstance(wood, list)
    assert isinstance(hunter, list)

    hide_dark = mix(sample_gradient(wood, 0.18), sample_gradient(hunter, 0.12), 0.22)
    hide_mid = mix(sample_gradient(wood, 0.48), sample_gradient(hunter, 0.28), 0.14)
    hide_light = mix(sample_gradient(wood, 0.82), sample_gradient(hunter, 0.46), 0.16)
    seam = mix(hide_dark, (236, 220, 188), 0.34)

    body = [(5, 10), (10, 6), (21, 6), (27, 12), (25, 22), (19, 27), (9, 25), (4, 18)]
    inner = [(7, 11), (11, 8), (20, 8), (24, 12), (22, 21), (18, 24), (10, 23), (6, 17)]

    draw.polygon(body, fill=hide_dark)
    draw.polygon(inner, fill=hide_mid)
    draw.line(body + [body[0]], fill=hide_light, width=1)
    draw.line([(9, 12), (14, 15), (20, 13)], fill=seam, width=1)
    draw.line([(11, 18), (16, 21), (21, 19)], fill=seam, width=1)
    draw.point((8, 15), fill=hide_light)
    draw.point((23, 16), fill=hide_light)
    draw.point((15, 24), fill=seam)
    return img


def draw_cloth_icon(config: dict[str, object]) -> Image.Image:
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    arcane = config["arcane"]
    holy = config["holy"]
    metal = config["metal"]
    assert isinstance(arcane, list)
    assert isinstance(holy, list)
    assert isinstance(metal, list)

    cloth_dark = mix(sample_gradient(arcane, 0.18), sample_gradient(holy, 0.18), 0.26)
    cloth_mid = mix(sample_gradient(arcane, 0.48), sample_gradient(holy, 0.34), 0.22)
    cloth_light = mix(sample_gradient(arcane, 0.78), sample_gradient(holy, 0.68), 0.30)
    trim = sample_gradient(metal, 0.62)
    fold = mix(cloth_dark, cloth_light, 0.36)

    draw.polygon([(6, 8), (22, 8), (26, 13), (25, 24), (9, 24), (5, 18)], fill=cloth_mid)
    draw.polygon([(18, 9), (26, 13), (25, 24), (18, 20)], fill=fold)
    draw.line([(7, 9), (21, 9)], fill=cloth_light, width=1)
    draw.line([(6, 20), (10, 23), (24, 23)], fill=cloth_dark, width=1)
    draw.line([(9, 14), (20, 14)], fill=trim, width=1)
    draw.line([(10, 17), (18, 17)], fill=trim, width=1)
    draw.point((22, 11), fill=cloth_light)
    draw.point((21, 16), fill=trim)
    return img


def draw_scythe_icon(config: dict[str, object]) -> Image.Image:
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    metal = config["metal"]
    wood = config["wood"]
    assert isinstance(metal, list)
    assert isinstance(wood, list)

    wood_dark = sample_gradient(wood, 0.18)
    wood_mid = sample_gradient(wood, 0.44)
    wood_light = sample_gradient(wood, 0.74)
    metal_dark = sample_gradient(metal, 0.20)
    metal_mid = sample_gradient(metal, 0.52)
    metal_light = sample_gradient(metal, 0.84)
    binding = mix(wood_dark, metal_light, 0.28)

    # Mango mas largo y claro para que la silueta de herramienta de cosecha se lea mejor en mano.
    draw.line([(7, 27), (20, 13)], fill=wood_dark, width=5)
    draw.line([(8, 26), (19, 14)], fill=wood_mid, width=3)
    draw.point((11, 23), fill=wood_light)
    draw.point((14, 20), fill=wood_light)
    draw.point((17, 17), fill=wood_light)
    draw.rectangle([6, 26, 8, 28], fill=wood_dark)

    # Hoja en media luna clasica, bastante mas notoria.
    outer_box = (13, 2, 31, 20)
    inner_box = (20, 5, 31, 17)
    draw.ellipse(outer_box, fill=metal_mid)
    draw.ellipse(inner_box, fill=(0, 0, 0, 0))
    draw.arc(outer_box, start=145, end=338, fill=metal_light, width=1)
    draw.arc(inner_box, start=140, end=335, fill=metal_dark, width=1)

    # Gancho frontal para reforzar la lectura de la silueta.
    hook = [(14, 8), (10, 7), (9, 8), (11, 10), (14, 11)]
    draw.polygon(hook, fill=metal_mid)
    draw.line(hook + [hook[0]], fill=metal_dark, width=1)
    draw.point((11, 8), fill=metal_light)

    # Union mango-hoja.
    draw.rectangle([18, 11, 22, 14], fill=binding)
    draw.line([(18, 11), (21, 11)], fill=metal_light, width=1)
    draw.line([(18, 14), (21, 14)], fill=wood_dark, width=1)
    draw.point((23, 9), fill=metal_light)
    draw.point((25, 6), fill=metal_light)
    return img


def is_hoe_head_zone(x: int, y: int) -> bool:
    return x >= 6 and y <= 9


def recolor_excalibur_hoe(source: Image.Image, config: dict[str, object]) -> Image.Image:
    out = Image.new("RGBA", source.size, (0, 0, 0, 0))
    src = source.convert("RGBA")
    metal = config["metal"]
    wood = config["wood"]
    assert isinstance(metal, list)
    assert isinstance(wood, list)

    for y in range(src.height):
        for x in range(src.width):
            r, g, b, a = src.getpixel((x, y))
            if a == 0:
                continue
            shade = luminance(r, g, b)
            if is_hoe_head_zone(x, y):
                color = sample_gradient(metal, min(1.0, shade * 1.04 + 0.02))
            elif is_brown_pixel(r, g, b):
                color = sample_gradient(wood, min(1.0, shade * 0.95))
            else:
                color = mix(sample_gradient(metal, shade), sample_gradient(wood, shade), 0.18)
            out.putpixel((x, y), (*finish_color(color, shade), a))
    return out


def create_preview(rows: list[tuple[str, list[Path]]], output_name: str) -> None:
    PREVIEW_DIR.mkdir(parents=True, exist_ok=True)
    width = 1440
    row_height = 220
    img = Image.new("RGBA", (width, 60 + row_height * len(rows) + 20), (12, 11, 14, 255))
    draw = ImageDraw.Draw(img)
    tier_labels = ["wood", "stone", "copper", "bronze", "iron", "silver"]
    xs = [150 + i * 210 for i in range(len(tier_labels))]

    for i, tier in enumerate(tier_labels):
        draw.text((xs[i] + 58, 16), tier, fill=(235, 229, 218, 255))

    for row_index, (title, files) in enumerate(rows):
        y = 58 + row_index * row_height
        draw.text((20, y + 80), title, fill=(235, 229, 218, 255))
        for i, path in enumerate(files):
            tex = Image.open(path).convert("RGBA")
            scale = 150 / max(tex.width, tex.height)
            resized = tex.resize(
                (max(1, int(tex.width * scale)), max(1, int(tex.height * scale))),
                preview_resampling(tex),
            )
            x = xs[i] + (120 - resized.width) // 2
            img.alpha_composite(resized, (x, y + 26))

    img.save(PREVIEW_DIR / output_name)


def main() -> None:
    dagger_base = Image.open(
        ensure_base_texture(
            "assets/minecraft/textures/item/wooden_sword.png",
            "_base_excalibur_dagger.png",
        )
    ).convert("RGBA")
    hoe_base = Image.open(
        ensure_base_texture(
            "assets/minecraft/textures/item/wooden_hoe.png",
            "_base_excalibur_wooden_hoe.png",
        )
    ).convert("RGBA")

    knife_paths: list[Path] = []
    hoe_paths: list[Path] = []
    hide_paths: list[Path] = []
    cloth_paths: list[Path] = []

    for tier, config in TIERS.items():
        dagger_path = TEXTURE_DIR / f"{tier}_dagger.png"
        if dagger_path.exists():
            dagger_img = Image.open(dagger_path).convert("RGBA")
        else:
            dagger_img = recolor_dagger(dagger_base, config)
        knife_img = knife_from_dagger(dagger_img)
        save_nested_texture(knife_img, f"{tier}_knife.png")
        knife_paths.append(TEXTURE_DIR / f"{tier}_knife.png")

        hoe_img = recolor_excalibur_hoe(hoe_base, config)
        save_nested_texture(hoe_img, f"{tier}_hoe.png")
        hoe_paths.append(TEXTURE_DIR / f"{tier}_hoe.png")

        scythe_img = draw_scythe_icon(config)
        save_nested_texture(scythe_img, f"{tier}_scythe.png")

        hide_img = draw_hide_icon(config)
        save_nested_texture(hide_img, f"materials/{tier}_hide.png")
        hide_paths.append(TEXTURE_DIR / "materials" / f"{tier}_hide.png")

        cloth_img = draw_cloth_icon(config)
        save_nested_texture(cloth_img, f"materials/{tier}_cloth.png")
        cloth_paths.append(TEXTURE_DIR / "materials" / f"{tier}_cloth.png")

    create_preview(
        [
            ("Cuchillos", knife_paths),
            ("Azadas", hoe_paths),
            ("Cueros", hide_paths),
            ("Telas", cloth_paths),
        ],
        "preview_harvest_families.png",
    )


if __name__ == "__main__":
    main()
