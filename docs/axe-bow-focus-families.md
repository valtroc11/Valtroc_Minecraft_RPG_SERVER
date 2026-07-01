# Familias visuales: hachas, arcos y focos

## Objetivo

Completar la segunda tanda de armas y focos del servidor bajo la regla actual:

- no crear modelos nuevos
- no tocar geometria
- retexturizar bases ya existentes

## Generador

Archivo:

- `tools/generate_weapon_retextures.py`

Este script usa bases chicas del ZIP de Excalibur:

- `assets/minecraft/textures/item/cit/custom/diamond_battle_axe.png`
- `assets/minecraft/textures/item/excalibur/bow/infinity.png`
- `assets/minecraft/textures/item/blaze_rod.png`
- `assets/minecraft/textures/item/trident.png`

Desde ahi genera o actualiza:

### Hachas

- `wood_axe.png`
- `stone_axe.png`
- `copper_axe.png`
- `bronze_axe.png`
- `iron_axe.png`
- `silver_axe.png`

### Arcos

- `wood_bow.png`
- `stone_bow.png`
- `copper_bow.png`
- `bronze_bow.png`
- `iron_bow.png`
- `silver_bow.png`

### Varitas

- `wood_wand.png`
- `stone_wand.png`
- `copper_wand.png`
- `bronze_wand.png`
- `iron_wand.png`
- `silver_wand.png`

### Staffs

- `wood_staff.png`
- `stone_staff.png`
- `copper_staff.png`
- `bronze_staff.png`
- `iron_staff.png`
- `silver_staff.png`

## Previews

- `docs/previews/preview_axe_tiers.png`
- `docs/previews/preview_bow_tiers.png`
- `docs/previews/preview_focus_families.png`

## IDs listos para probar

### Hachas

- `wood_axe`
- `stone_axe`
- `copper_axe`
- `bronze_axe`
- `iron_axe`
- `silver_axe`

### Arcos

- `wood_bow`
- `stone_bow`
- `copper_bow`
- `bronze_bow`
- `iron_bow`
- `silver_bow`

### Varitas

- `wood_wand`
- `stone_wand`
- `copper_wand`
- `bronze_wand`
- `iron_wand`
- `silver_wand`

### Staffs

- `wood_staff`
- `stone_staff`
- `copper_staff`
- `bronze_staff`
- `iron_staff`
- `silver_staff`

## Recetas nuevas agregadas

Para esta fase solo se agregaron recetas que no pisan directamente la salida
vanilla:

- `copper_axe_recipe`
- `copper_bow_recipe`
- `iron_bow_recipe`
- `iron_wand_recipe`
- `iron_staff_recipe`

## Nota visual

Todas estas familias quedan ahora en lectura pixel-art compacta:

- sin renders gigantes
- sin arte ilustrado externo
- solo recolor sobre siluetas 16x16 ya existentes
- las varitas heredan la lectura visual del antiguo staff
- los staffs ahora usan silueta de tridente para leerse mas como baston

## Nota de gameplay

Las familias siguen apoyandose en materiales vanilla para su base mecanica:

- hachas tempranas: `WOODEN_AXE` / `STONE_AXE`
- cobre: `STONE_AXE` como stand-in
- hierro, bronce, plata: `IRON_AXE`
- arcos: `BOW`
- varitas: `STICK`
- staffs: `BLAZE_ROD`
