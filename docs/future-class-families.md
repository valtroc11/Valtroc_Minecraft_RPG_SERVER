# Familias futuras por tier

## Objetivo

Completar la siguiente tanda de armas/clases sin romper la regla visual actual:

- una base medieval por familia
- recolor puro por tier
- sin geometria nueva
- lectura clara en inventario y en mano

## Familias agregadas

- `greatsword`: espada ancha del guerrero
- `dagger`: daga del explorador
- `spellbook`: grimorio del mago

## Bases aprobadas

- espada ancha:
  - `assets/minecraft/textures/item/excalibur/custom_name/dragon_slayer_greatsword.png`
- daga:
  - `assets/minecraft/textures/item/wooden_sword.png`
- grimorio:
  - `assets/minecraft/textures/item/book.png`

## Script

Archivo:

- `tools/generate_future_class_retextures.py`

Genera:

- `wood_greatsword.png`
- `stone_greatsword.png`
- `copper_greatsword.png`
- `bronze_greatsword.png`
- `iron_greatsword.png`
- `silver_greatsword.png`
- `wood_dagger.png`
- `stone_dagger.png`
- `copper_dagger.png`
- `bronze_dagger.png`
- `iron_dagger.png`
- `silver_dagger.png`
- `wood_spellbook.png`
- `stone_spellbook.png`
- `copper_spellbook.png`
- `bronze_spellbook.png`
- `iron_spellbook.png`
- `silver_spellbook.png`

## Lectura de gameplay actual

Mientras no metamos una capa propia de stats por familia:

- `greatsword` usa materiales base de hacha para sentirse pesada
- `dagger` usa materiales base de espada como version ligera de explorador
- `spellbook` usa `BOOK` como soporte visual/caster
- `spellbook` ahora usa un modelo manual 2D de grimorio abierto para que se lea
  mejor en la mano izquierda

## Previews

- `docs/previews/preview_greatsword_tiers.png`
- `docs/previews/preview_dagger_tiers.png`
- `docs/previews/preview_spellbook_tiers.png`
- `docs/previews/preview_future_class_families.png`
