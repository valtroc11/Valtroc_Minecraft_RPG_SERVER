from __future__ import annotations

from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "content" / "oraxen" / "recipes" / "servidro_elemental_flora_recipes.yml"


WOODS = [
    "oak",
    "spruce",
    "birch",
    "jungle",
    "acacia",
    "dark_oak",
    "mangrove",
    "cherry",
    "pale_oak",
]

ELEMENTS = ["ice", "fire"]

FLOWER_DYES = {
    "dandelion": "YELLOW_DYE",
    "poppy": "RED_DYE",
    "blue_orchid": "LIGHT_BLUE_DYE",
    "allium": "MAGENTA_DYE",
    "azure_bluet": "LIGHT_GRAY_DYE",
    "red_tulip": "RED_DYE",
    "orange_tulip": "ORANGE_DYE",
    "white_tulip": "LIGHT_GRAY_DYE",
    "pink_tulip": "PINK_DYE",
    "oxeye_daisy": "LIGHT_GRAY_DYE",
    "cornflower": "BLUE_DYE",
    "lily_of_the_valley": "WHITE_DYE",
    "sunflower_front": "YELLOW_DYE",
    "rose_bush_top": "RED_DYE",
    "peony_top": "PINK_DYE",
    "lilac_top": "MAGENTA_DYE",
    "torchflower": "ORANGE_DYE",
    "wildflowers": "YELLOW_DYE",
}


def shaped_single(recipe_id: str, source: str, result: str, amount: int) -> str:
    return f"""{recipe_id}:
  result:
    oraxen_item: {result}
    amount: {amount}
  ingredients:
    A:
      oraxen_item: {source}
  shape:
    - "A"
"""


def shaped_single_vanilla(recipe_id: str, source: str, result: str, amount: int) -> str:
    return f"""{recipe_id}:
  result:
    minecraft_type: {result}
    amount: {amount}
  ingredients:
    A:
      oraxen_item: {source}
  shape:
    - "A"
"""


def main() -> None:
    chunks = [
        "# Generado por tools/generate_elemental_flora_recipes.py",
        "# Recetas para que las variantes elementales mantengan su cadena de madera.",
        "",
    ]

    for element in ELEMENTS:
        for wood in WOODS:
            log = f"{element}_{wood}_log"
            stripped = f"{element}_{wood}_stripped_log"
            planks = f"{element}_{wood}_planks"

            chunks.append(shaped_single(f"{log}_to_planks", log, planks, 4))
            chunks.append(shaped_single(f"{stripped}_to_planks", stripped, planks, 4))

        for flower, dye in FLOWER_DYES.items():
            flower_id = f"{element}_{flower}"
            chunks.append(shaped_single_vanilla(f"{flower_id}_to_dye", flower_id, dye, 1))

    OUT.write_text("\n".join(chunks), encoding="utf-8")
    print(f"Wrote {OUT}")


if __name__ == "__main__":
    main()
