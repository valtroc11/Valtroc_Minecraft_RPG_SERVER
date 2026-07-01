from __future__ import annotations

from pathlib import Path
import sys

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(Path(__file__).resolve().parent))

from generate_signature_retextures import finish_color, luminance, mix, preview_resampling, sample_gradient


TEXTURE_DIR = ROOT / "content" / "oraxen" / "pack" / "textures" / "servidorpg"
ORAXEN_TEXTURE_DIR = ROOT / "content" / "oraxen" / "pack" / "assets" / "oraxen" / "textures" / "servidorpg"
MOB_TEXTURE_DIR = TEXTURE_DIR / "mobs"
ORAXEN_MOB_TEXTURE_DIR = ORAXEN_TEXTURE_DIR / "mobs"
PREVIEW_DIR = ROOT / "docs" / "previews"


PALETTES: dict[str, dict[str, list[tuple[int, int, int]]]] = {
    "summoner": {
        "primary": [(42, 30, 58), (86, 54, 124), (151, 91, 203), (226, 199, 255)],
        "accent": [(42, 82, 48), (76, 147, 80), (138, 222, 117), (226, 255, 198)],
        "dark": [(18, 16, 24), (35, 29, 44), (70, 55, 84), (125, 103, 146)],
    },
    "brutal": {
        "primary": [(56, 29, 22), (106, 54, 37), (174, 84, 54), (241, 151, 96)],
        "accent": [(82, 14, 16), (145, 25, 29), (218, 62, 53), (255, 141, 105)],
        "dark": [(19, 17, 17), (39, 35, 33), (77, 66, 59), (126, 103, 88)],
    },
    "bastion": {
        "primary": [(43, 47, 53), (91, 98, 108), (148, 158, 170), (230, 236, 243)],
        "accent": [(79, 68, 45), (136, 114, 69), (203, 173, 103), (255, 232, 170)],
        "dark": [(17, 19, 22), (36, 40, 45), (69, 77, 86), (119, 131, 143)],
    },
    "swift": {
        "primary": [(30, 47, 36), (53, 91, 65), (83, 151, 99), (164, 232, 165)],
        "accent": [(38, 68, 62), (55, 129, 113), (88, 202, 173), (189, 255, 232)],
        "dark": [(16, 22, 18), (29, 43, 34), (52, 78, 58), (88, 123, 91)],
    },
    "frost": {
        "primary": [(49, 74, 94), (78, 128, 160), (129, 198, 229), (227, 250, 255)],
        "accent": [(70, 101, 156), (101, 159, 220), (160, 224, 255), (240, 255, 255)],
        "dark": [(18, 24, 31), (35, 49, 62), (63, 92, 114), (105, 145, 169)],
    },
    "blood": {
        "primary": [(47, 25, 31), (91, 39, 50), (151, 62, 78), (235, 135, 146)],
        "accent": [(88, 11, 22), (151, 25, 42), (222, 57, 78), (255, 141, 153)],
        "dark": [(18, 15, 17), (35, 27, 31), (70, 49, 57), (119, 82, 94)],
    },
}


SOURCE_BY_ROLE = {
    "summoner": ["summoner_hood.png", "summoner_staff.png"],
    "brutal": ["brutal_helm.png", "brutal_axe.png"],
    "bastion": ["bastion_shield.png"],
    "frost": ["frost_focus.png"],
    "blood": ["blood_mask.png"],
}


FAMILIES = {
    "zombie": {"dark_mix": 0.24, "accent_mix": 0.08},
    "skeleton": {"dark_mix": 0.08, "accent_mix": 0.12},
    "piglin": {"dark_mix": 0.18, "accent_mix": 0.18},
    "creeper": {"dark_mix": 0.05, "accent_mix": 0.28},
}


def save_nested_texture(img: Image.Image, relative_name: str) -> Path:
    for base in (MOB_TEXTURE_DIR, ORAXEN_MOB_TEXTURE_DIR):
        base.mkdir(parents=True, exist_ok=True)
        img.save(base / relative_name)
    return MOB_TEXTURE_DIR / relative_name


def recolor_role_texture(source: Image.Image, role: str, family: str) -> Image.Image:
    palette = PALETTES[role]
    family_tuning = FAMILIES[family]
    src = source.convert("RGBA")
    out = Image.new("RGBA", src.size, (0, 0, 0, 0))

    for y in range(src.height):
        for x in range(src.width):
            r, g, b, a = src.getpixel((x, y))
            if a == 0:
                continue
            shade = luminance(r, g, b)
            if shade < 0.30:
                color = sample_gradient(palette["dark"], min(1.0, shade * 1.35))
            elif shade > 0.70:
                color = sample_gradient(palette["accent"], min(1.0, shade * 1.05))
                color = mix(color, sample_gradient(palette["primary"], shade), 0.20)
            else:
                color = sample_gradient(palette["primary"], shade)

            color = mix(color, sample_gradient(palette["dark"], shade), float(family_tuning["dark_mix"]))
            if (x + y) % 5 == 0 or shade > 0.74:
                color = mix(color, sample_gradient(palette["accent"], shade), float(family_tuning["accent_mix"]))
            out.putpixel((x, y), (*finish_color(color, shade), a))
    return out


def draw_swift_blade() -> Image.Image:
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    metal = PALETTES["swift"]["accent"]
    grip = PALETTES["swift"]["dark"]
    draw.line([(8, 26), (22, 8)], fill=sample_gradient(grip, 0.35), width=4)
    draw.line([(9, 25), (22, 8)], fill=sample_gradient(metal, 0.70), width=2)
    draw.polygon([(21, 5), (25, 4), (23, 10), (13, 22), (11, 20)], fill=sample_gradient(metal, 0.78))
    draw.line([(21, 5), (25, 4), (23, 10)], fill=sample_gradient(metal, 0.95), width=1)
    draw.rectangle([7, 23, 11, 27], fill=sample_gradient(grip, 0.22))
    draw.point((18, 13), fill=sample_gradient(metal, 1.0))
    return img


def create_preview(files: list[tuple[str, Path]]) -> None:
    PREVIEW_DIR.mkdir(parents=True, exist_ok=True)
    cell = 180
    label_h = 34
    width = cell * 4 + 48
    height = 80 + (len(files) // 4 + 1) * (cell + label_h)
    preview = Image.new("RGBA", (width, height), (12, 11, 14, 255))
    draw = ImageDraw.Draw(preview)
    draw.text((18, 18), "Mob recolors: role + family overlays", fill=(235, 229, 218, 255))

    for idx, (label, path) in enumerate(files):
        row = idx // 4
        col = idx % 4
        x = 24 + col * cell
        y = 58 + row * (cell + label_h)
        tex = Image.open(path).convert("RGBA")
        resized = tex.resize((96, 96), preview_resampling(tex))
        preview.alpha_composite(resized, (x + 42, y))
        label = label.replace("_", " ")
        draw.text((x + 8, y + 100), label[:24], fill=(220, 214, 204, 255))

    preview.save(PREVIEW_DIR / "preview_mob_recolors.png")


def main() -> None:
    generated: list[tuple[str, Path]] = []
    for role, filenames in SOURCE_BY_ROLE.items():
        for filename in filenames:
            source = Image.open(MOB_TEXTURE_DIR / filename).convert("RGBA")
            stem = Path(filename).stem
            for family in FAMILIES:
                target_name = f"{family}_{role}_{stem.removeprefix(role + '_')}.png"
                path = save_nested_texture(recolor_role_texture(source, role, family), target_name)
                generated.append((target_name.removesuffix(".png"), path))

    swift_hood = recolor_role_texture(Image.open(MOB_TEXTURE_DIR / "summoner_hood.png").convert("RGBA"), "swift", "zombie")
    path = save_nested_texture(swift_hood, "swift_hood.png")
    generated.append(("swift_hood", path))
    for family in FAMILIES:
        path = save_nested_texture(recolor_role_texture(swift_hood, "swift", family), f"{family}_swift_hood.png")
        generated.append((f"{family}_swift_hood", path))

    swift_blade = draw_swift_blade()
    path = save_nested_texture(swift_blade, "swift_blade.png")
    generated.append(("swift_blade", path))
    for family in FAMILIES:
        path = save_nested_texture(recolor_role_texture(swift_blade, "swift", family), f"{family}_swift_blade.png")
        generated.append((f"{family}_swift_blade", path))

    create_preview(generated)
    print(f"Generated {len(generated)} mob recolor textures.")


if __name__ == "__main__":
    main()
