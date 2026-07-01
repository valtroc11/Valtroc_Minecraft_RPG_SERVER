from __future__ import annotations

from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "content" / "oraxen" / "items" / "servidro_elemental_flora.yml"


WOODS = {
    "oak": "Roble",
    "spruce": "Abeto",
    "birch": "Abedul",
    "jungle": "Jungla",
    "acacia": "Acacia",
    "dark_oak": "Roble Oscuro",
    "mangrove": "Mangle",
    "cherry": "Cerezo",
    "pale_oak": "Roble Palido",
}


WOOD_PARTS = {
    "log": ("Tronco", "OAK_LOG", "block.wood", "AXE", 2.0),
    "stripped_log": ("Tronco Pelado", "STRIPPED_OAK_LOG", "block.wood", "AXE", 2.0),
    "planks": ("Tablones", "OAK_PLANKS", "block.wood", "AXE", 2.0),
    "leaves": ("Hojas", "OAK_LEAVES", "block.grass", "HOE", 0.3),
    "sapling": ("Retoño", "OAK_SAPLING", "block.grass", "AXE", 0.1),
}


FLOWERS = {
    "dandelion": "Diente de Leon",
    "poppy": "Amapola",
    "blue_orchid": "Orquidea Azul",
    "allium": "Allium",
    "azure_bluet": "Azure Bluet",
    "red_tulip": "Tulipan Rojo",
    "orange_tulip": "Tulipan Naranja",
    "white_tulip": "Tulipan Blanco",
    "pink_tulip": "Tulipan Rosa",
    "oxeye_daisy": "Margarita",
    "cornflower": "Aciano",
    "lily_of_the_valley": "Lirio del Valle",
    "sunflower_front": "Girasol",
    "rose_bush_top": "Rosal",
    "peony_top": "Peonia",
    "lilac_top": "Lila",
    "torchflower": "Antorcha Floral",
    "wildflowers": "Flores Silvestres",
}


ELEMENTS = {
    "ice": ("Hielo", "9edfff", "NOTE_BLOCK"),
    "fire": ("Fuego", "df6f32", "NOTE_BLOCK"),
}


def sounds(sound_group: str) -> str:
    return f"""        place_sound: {sound_group}.place
        break_sound: {sound_group}.break
        hit_sound: {sound_group}.hit
        step_sound: {sound_group}.step
        fall_sound: {sound_group}.fall
        volume: 0.85
        pitch: 1.0"""


def block_entry(
    item_id: str,
    display: str,
    material: str,
    texture: str,
    variation: int,
    sound_group: str,
    hardness: float,
    minimal_type: str,
    best_tool: str,
    color: str,
) -> str:
    return f"""{item_id}:
  displayname: "<#{color}>{display}"
  material: {material}
  Pack:
    generate_model: true
    parent_model: "block/cube_all"
    textures:
      - {texture}
  Mechanics:
    noteblock:
      custom_variation: {variation}
      model: {item_id}
      hardness: {hardness:g}
      block_sounds:
{sounds(sound_group)}
      drop:
        silktouch: false
        minimal_type: {minimal_type}
        best_tools:
          - {best_tool}
        loots:
          - {{oraxen_item: {item_id}, probability: 1.0}}
"""


def main() -> None:
    variation = 120
    chunks: list[str] = [
        "# Generado por tools/generate_elemental_flora_oraxen_items.py",
        "# Bloques elementales de arboles y flores.",
        "",
    ]

    for element, (element_name, color, material) in ELEMENTS.items():
        for wood, wood_name in WOODS.items():
            for part, (part_name, base_material, sound_group, best_tool, hardness) in WOOD_PARTS.items():
                stem = {
                    "log": f"{wood}_log",
                    "stripped_log": f"stripped_{wood}_log",
                    "planks": f"{wood}_planks",
                    "leaves": f"{wood}_leaves",
                    "sapling": f"{wood}_sapling",
                }[part]
                texture = f"servidorpg/flora/trees/{element}/{wood}/{stem}"
                texture_file = ROOT / "content" / "oraxen" / "pack" / "textures" / f"{texture}.png"
                if not texture_file.exists():
                    continue
                item_id = f"{element}_{wood}_{part}"
                chunks.append(
                    block_entry(
                        item_id,
                        f"{part_name} de {wood_name} de {element_name}",
                        material,
                        texture,
                        variation,
                        sound_group,
                        float(hardness),
                        "WOOD" if best_tool == "AXE" else "STONE",
                        str(best_tool),
                        str(color),
                    )
                )
                variation += 1

        for flower, flower_name in FLOWERS.items():
            item_id = f"{element}_{flower}"
            texture = f"servidorpg/flora/flowers/{element}/{flower}"
            chunks.append(
                block_entry(
                    item_id,
                    f"{flower_name} de {element_name}",
                    material,
                    texture,
                    variation,
                    "block.grass",
                    0.1,
                    "WOOD",
                    "SHEARS",
                    str(color),
                )
            )
            variation += 1

    OUT.write_text("\n".join(chunks), encoding="utf-8")
    print(f"Wrote {OUT} with custom variations 120-{variation - 1}.")


if __name__ == "__main__":
    main()
