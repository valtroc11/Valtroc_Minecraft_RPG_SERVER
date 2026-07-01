from __future__ import annotations

import json
from pathlib import Path

from PIL import Image, ImageDraw

from generate_signature_retextures import (
    TIERS,
    ensure_base_texture,
    finish_color,
    luminance,
    mix,
    preview_resampling,
    sample_gradient,
    save_texture,
)


ROOT = Path(__file__).resolve().parents[1]
TEXTURE_DIR = ROOT / "content" / "oraxen" / "pack" / "textures" / "servidorpg"
MODEL_DIR = ROOT / "content" / "oraxen" / "pack" / "models" / "servidorpg"
ORAXEN_TEXTURE_DIR = ROOT / "content" / "oraxen" / "pack" / "assets" / "oraxen" / "textures" / "servidorpg"
ORAXEN_MODEL_DIR = ROOT / "content" / "oraxen" / "pack" / "assets" / "oraxen" / "models" / "servidorpg"
PREVIEW_DIR = ROOT / "docs" / "previews"


def is_grip_pixel(x: int, y: int, width: int, height: int) -> bool:
    center = width / 2.0
    return y >= height * 0.54 and abs(x - center) <= max(1.0, width * 0.12)


def recolor_greatsword(source: Image.Image, config: dict[str, object]) -> Image.Image:
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
            if is_grip_pixel(x, y, src.width, src.height):
                color = sample_gradient(wood, shade)
            else:
                color = sample_gradient(metal, shade)
            out.putpixel((x, y), (*finish_color(color, shade), a))
    return out


def recolor_dagger(source: Image.Image, config: dict[str, object]) -> Image.Image:
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
            if y >= src.height * 0.62:
                color = sample_gradient(wood, shade)
            else:
                color = sample_gradient(metal, shade)
            out.putpixel((x, y), (*finish_color(color, shade), a))
    return out


def recolor_spellbook(source: Image.Image, config: dict[str, object]) -> Image.Image:
    _ = source
    out = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(out)
    metal = config["metal"]
    wood = config["wood"]
    holy = config["holy"]
    arcane = config["arcane"]
    assert isinstance(metal, list)
    assert isinstance(wood, list)
    assert isinstance(holy, list)
    assert isinstance(arcane, list)

    cover_dark = mix(sample_gradient(wood, 0.26), sample_gradient(arcane, 0.24), 0.68)
    cover_mid = mix(sample_gradient(wood, 0.48), sample_gradient(arcane, 0.40), 0.58)
    cover_light = mix(sample_gradient(wood, 0.70), sample_gradient(arcane, 0.64), 0.36)
    edge_dark = sample_gradient(metal, 0.34)
    edge_light = sample_gradient(metal, 0.70)
    page_shadow = mix(sample_gradient(holy, 0.34), (210, 201, 184), 0.55)
    page_mid = mix(sample_gradient(holy, 0.58), (238, 231, 214), 0.62)
    page_light = mix(sample_gradient(holy, 0.90), (255, 250, 241), 0.76)
    page_line = mix(page_shadow, cover_dark, 0.26)
    spine_dark = mix(sample_gradient(wood, 0.22), sample_gradient(metal, 0.18), 0.28)
    spine_mid = mix(sample_gradient(wood, 0.44), sample_gradient(holy, 0.34), 0.12)
    ribbon = mix(sample_gradient(arcane, 0.74), sample_gradient(metal, 0.22), 0.16)

    left_cover = [(2, 10), (9, 8), (15, 10), (15, 22), (9, 24), (2, 22)]
    right_cover = [(17, 10), (23, 8), (30, 10), (30, 22), (23, 24), (17, 22)]
    left_page = [(4, 10), (11, 9), (15, 10), (15, 22), (10, 23), (4, 21)]
    right_page = [(17, 10), (21, 9), (28, 10), (28, 21), (22, 23), (17, 22)]
    left_page_edge = [(3, 10), (5, 10), (5, 21), (3, 20)]
    right_page_edge = [(27, 10), (29, 10), (29, 20), (27, 21)]
    spine = [(15, 9), (17, 9), (17, 23), (15, 23)]

    draw.polygon(left_cover, fill=cover_mid)
    draw.polygon(right_cover, fill=cover_mid)
    draw.polygon(left_page, fill=page_mid)
    draw.polygon(right_page, fill=page_mid)
    draw.polygon(left_page_edge, fill=page_light)
    draw.polygon(right_page_edge, fill=page_light)
    draw.polygon(spine, fill=spine_dark)

    draw.line([(3, 11), (9, 9)], fill=cover_light, width=1)
    draw.line([(23, 9), (29, 11)], fill=cover_light, width=1)
    draw.line([(2, 21), (9, 23)], fill=edge_dark, width=1)
    draw.line([(23, 23), (30, 21)], fill=edge_dark, width=1)
    draw.line([(15, 9), (15, 23)], fill=spine_mid, width=1)
    draw.line([(16, 9), (16, 23)], fill=edge_light, width=1)
    draw.line([(17, 9), (17, 23)], fill=edge_dark, width=1)
    draw.line([(14, 10), (15, 11)], fill=page_light, width=1)
    draw.line([(17, 11), (18, 10)], fill=page_light, width=1)

    left_lines = [
        ((6, 11), (12, 11)),
        ((6, 13), (11, 13)),
        ((6, 15), (10, 15)),
        ((6, 17), (10, 17)),
        ((7, 19), (10, 19)),
    ]
    right_lines = [
        ((20, 11), (26, 11)),
        ((21, 13), (26, 13)),
        ((22, 15), (26, 15)),
        ((22, 17), (26, 17)),
        ((21, 19), (25, 19)),
    ]
    for start, end in left_lines:
        draw.line([start, end], fill=page_line, width=1)
    for start, end in right_lines:
        draw.line([start, end], fill=page_line, width=1)

    draw.line([(15, 23), (15, 27)], fill=ribbon, width=1)
    draw.line([(16, 23), (16, 26)], fill=mix(ribbon, page_light, 0.18), width=1)
    draw.point((14, 22), fill=page_shadow)
    draw.point((17, 22), fill=page_shadow)
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


def write_spellbook_model(item_id: str, texture_path: str) -> None:
    write_model(
        item_id,
        texture_path,
        "item/generated",
        {
            "ground": {
                "rotation": [0, 0, 0],
                "translation": [0, 3.0, 0],
                "scale": [1.2, 1.2, 1.2],
            },
            "thirdperson_righthand": {
                "rotation": [0, -110, 18],
                "translation": [0.0, 1.8, -1.0],
                "scale": [1.1, 1.1, 1.1],
            },
            "thirdperson_lefthand": {
                "rotation": [0, 110, -18],
                "translation": [0.0, 1.8, -1.0],
                "scale": [1.1, 1.1, 1.1],
            },
            "firstperson_righthand": {
                "rotation": [0, -102, 8],
                "translation": [0.78, 1.9, 0.86],
                "scale": [1.28, 1.28, 1.28],
            },
            "firstperson_lefthand": {
                "rotation": [0, 102, -8],
                "translation": [0.78, 1.9, 0.86],
                "scale": [1.28, 1.28, 1.28],
            },
        },
    )


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
        draw.text((x + 54, height + 28), label, fill=(232, 226, 215, 255))
    preview.save(PREVIEW_DIR / output_name)


def create_overview_preview(rows: list[tuple[str, list[str]]], output_name: str) -> None:
    PREVIEW_DIR.mkdir(parents=True, exist_ok=True)
    row_height = 300
    width = 1390
    height = 70 + row_height * len(rows) + 30
    bg = Image.new("RGBA", (width, height), (12, 11, 14, 255))
    draw = ImageDraw.Draw(bg)

    tier_labels = ["wood", "stone", "copper", "bronze", "iron", "silver"]
    col_x = [40 + i * 220 for i in range(len(tier_labels))]
    for idx, label in enumerate(tier_labels):
        draw.text((col_x[idx] + 78, 16), label, fill=(232, 226, 215, 255))

    for row_index, (title, files) in enumerate(rows):
        y = 70 + row_index * row_height
        draw.text((16, y + 120), title, fill=(232, 226, 215, 255))
        for idx, filename in enumerate(files):
            img = Image.open(TEXTURE_DIR / filename).convert("RGBA")
            scale = 220 / img.height
            resized = img.resize((max(1, int(img.width * scale)), 220), preview_resampling(img))
            x = col_x[idx] + (180 - resized.width) // 2
            bg.alpha_composite(resized, (x, y))

    bg.save(PREVIEW_DIR / output_name)


def main() -> None:
    greatsword_base = Image.open(
        ensure_base_texture(
            "assets/minecraft/textures/item/excalibur/custom_name/dragon_slayer_greatsword.png",
            "_base_excalibur_greatsword.png",
        )
    ).convert("RGBA")
    dagger_base = Image.open(
        ensure_base_texture(
            "assets/minecraft/textures/item/wooden_sword.png",
            "_base_excalibur_dagger.png",
        )
    ).convert("RGBA")
    spellbook_base = Image.open(
        ensure_base_texture(
            "assets/minecraft/textures/item/book.png",
            "_base_excalibur_spellbook.png",
        )
    ).convert("RGBA")

    greatsword_files: list[str] = []
    dagger_files: list[str] = []
    spellbook_files: list[str] = []

    for tier, config in TIERS.items():
        greatsword_name = f"{tier}_greatsword.png"
        dagger_name = f"{tier}_dagger.png"
        spellbook_name = f"{tier}_spellbook.png"

        save_texture(recolor_greatsword(greatsword_base, config), greatsword_name)
        save_texture(recolor_dagger(dagger_base, config), dagger_name)
        save_texture(recolor_spellbook(spellbook_base, config), spellbook_name)
        write_spellbook_model(spellbook_name.removesuffix(".png"), f"servidorpg/{spellbook_name.removesuffix('.png')}")

        greatsword_files.append(greatsword_name)
        dagger_files.append(dagger_name)
        spellbook_files.append(spellbook_name)

    create_family_preview(greatsword_files, "preview_greatsword_tiers.png", "_greatsword.png")
    create_family_preview(dagger_files, "preview_dagger_tiers.png", "_dagger.png")
    create_family_preview(spellbook_files, "preview_spellbook_tiers.png", "_spellbook.png")
    create_overview_preview(
        [
            ("greatsword", greatsword_files),
            ("dagger", dagger_files),
            ("spellbook", spellbook_files),
        ],
        "preview_future_class_families.png",
    )
    print("Generated tiered future class families: greatsword, dagger, spellbook.")


if __name__ == "__main__":
    main()
