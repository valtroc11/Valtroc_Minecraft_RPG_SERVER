# Familias de clase por recolor

## Objetivo

Convertir las piezas visuales de clase del pack en familias limpias de tier:

- sin geometria nueva
- sin adornos nuevos
- una base chica aprobada por familia
- recolor puro por material y acento de clase

## Bases aprobadas

Estas familias ahora salen desde bases pixel-art compactas del pack:

- `assets/minecraft/textures/item/cit/custom/diamond_battle_axe.png`
- `assets/minecraft/textures/item/excalibur/bow/infinity.png`
- `assets/minecraft/textures/item/amethyst_shard.png`
- `assets/minecraft/textures/item/blaze_rod.png`

## Generador

Archivo:

- `tools/generate_signature_retextures.py`

## Salidas actuales

### Guerrero

- `wood_berserker_war_axe.png`
- `stone_berserker_war_axe.png`
- `copper_berserker_war_axe.png`
- `bronze_berserker_war_axe.png`
- `iron_berserker_war_axe.png`
- `silver_berserker_war_axe.png`

### Explorador

- `wood_hunter_bow.png`
- `stone_hunter_bow.png`
- `copper_hunter_bow.png`
- `bronze_hunter_bow.png`
- `iron_hunter_bow.png`
- `silver_hunter_bow.png`

### Mago

- `wood_mage_focus.png`
- `stone_mage_focus.png`
- `copper_mage_focus.png`
- `bronze_mage_focus.png`
- `iron_mage_focus.png`
- `silver_mage_focus.png`

### Clerigo

- `wood_cleric_relic.png`
- `stone_cleric_relic.png`
- `copper_cleric_relic.png`
- `bronze_cleric_relic.png`
- `iron_cleric_relic.png`
- `silver_cleric_relic.png`

## Previews

- `docs/previews/preview_berserker_war_axe_tiers.png`
- `docs/previews/preview_hunter_bow_tiers.png`
- `docs/previews/preview_mage_focus_tiers.png`
- `docs/previews/preview_cleric_relic_tiers.png`
- `docs/previews/preview_signature_class_families.png`

## Regla visual

Cada familia mantiene su identidad de clase por acento:

- berserker: acento de furia rojizo
- hunter: acento verde de explorador
- mage: nucleo arcano violeta
- clerigo: nucleo santo dorado/ivorio

El tier cambia la lectura del material base, no la silueta.
La silueta queda ahora en escala tipo Minecraft para que no desentone con el
resto del pack.

## IDs Oraxen disponibles

### Berserker

- `wood_berserker_war_axe`
- `stone_berserker_war_axe`
- `copper_berserker_war_axe`
- `bronze_berserker_war_axe`
- `iron_berserker_war_axe`
- `silver_berserker_war_axe`

### Hunter

- `wood_hunter_bow`
- `stone_hunter_bow`
- `copper_hunter_bow`
- `bronze_hunter_bow`
- `iron_hunter_bow`
- `silver_hunter_bow`

### Mage

- `wood_mage_focus`
- `stone_mage_focus`
- `copper_mage_focus`
- `bronze_mage_focus`
- `iron_mage_focus`
- `silver_mage_focus`

### Clerigo

- `wood_cleric_relic`
- `stone_cleric_relic`
- `copper_cleric_relic`
- `bronze_cleric_relic`
- `iron_cleric_relic`
- `silver_cleric_relic`

## Recipes registrados

Quedaron registradas recetas Oraxen para los tiers:

- `wood`
- `stone`
- `copper`
- `bronze`
- `iron`
- `silver`

Familias cubiertas:

- Hachas Berserker
- Arcos del Cazador
- Focos Arcanos
- Reliquias del Clerigo

## Starter kit

El starter kit ahora intenta usar items Oraxen por tier cuando existen:

- escudos por tier para `guerrero` y `clerigo`
- arco del cazador por tier para `explorador`
- foco arcano por tier para `mago`
- reliquia del clerigo por tier para `clerigo`

Si Oraxen no esta disponible o un ID falla, el plugin cae a item vanilla sin
romper el flujo de eleccion de clase.
