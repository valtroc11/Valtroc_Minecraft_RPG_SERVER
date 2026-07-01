# Familias de espadas por tier

## Objetivo

Mantener la espada como una familia de recolor puro, igual que el enfoque usado
en los escudos por tier:

- misma silueta base
- mismo lenguaje de pixeles
- solo cambio de material/color
- lectura inmediata en inventario y en mano

La idea no es rediseñar la espada, sino darle variaciones por tier con colores
mas vivos y mas faciles de distinguir.

## Base visual

La familia usa de nuevo la base directa de Excalibur:

- `assets/minecraft/textures/item/cit/custom/diamond_master_sword.png`

El resultado final queda mas cercano a:

- escudos rondela
- hachas por tier
- arcos por tier

## Variantes generadas

- `wood_sword.png`
- `stone_sword.png`
- `copper_sword.png`
- `bronze_sword.png`
- `iron_sword.png`
- `silver_sword.png`
- `apprentice_sword.png`

## Script

Archivo:

- `tools/generate_sword_textures.py`

El script:

- preserva `_base_excalibur_master_sword.png`
- recolorea hoja y empunadura
- usa colores mas vivos por tier
- mantiene la misma silueta pequena en estilo pixel-art

## Nota de gameplay

El plugin actual todavia usa el `Material` vanilla para dano/velocidad base.

Por eso hoy:

- `wood_sword` usa `WOODEN_SWORD`
- `stone_sword` usa `STONE_SWORD`
- `copper_sword` usa `STONE_SWORD`
- `bronze_sword` usa `IRON_SWORD`
- `iron_sword` usa `IRON_SWORD`
- `silver_sword` usa `IRON_SWORD`
