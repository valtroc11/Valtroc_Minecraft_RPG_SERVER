# Familias de escudos por tier

## Objetivo

Separar visualmente los escudos del RPG en dos familias:

- `rondela`
- `torre`

Por ahora **no hay diferencia mecanica** entre una rondela y un escudo torre del
mismo tier. La diferencia es solo visual y de fantasia del item.

## Tiers cubiertos

- `wood`
- `stone`
- `copper`
- `bronze`
- `iron`
- `silver`

## Convencion de nombres

### Rondela

- textura principal: `{tier}_shield_round.png`
- textura del asa: `{tier}_shield_round_handle.png`
- textura de particula: `{tier}_shield_round_particle.png`

### Torre

- textura principal: `{tier}_shield_tower.png`

## Estado actual

### Rondela

La familia `round` ya no debe evolucionar como rama de geometria propia.

La decision actual del proyecto es:

- conservar la base visual/silueta que ya funciona
- trabajar solo con retexturizado
- no abrir nuevas variantes de modelo para escudos por tier

Base directa actual desde Excalibur:

- `assets/minecraft/textures/item/excalibur/custom_name/shield/round.png`
- `assets/minecraft/textures/item/excalibur/custom_name/shield/handle.png`
- `assets/minecraft/textures/item/excalibur/custom_name/shield/shield_particle.png`

### Torre

La familia `tower` ahora sigue la misma regla que las demas familias limpias:

- una sola base aprobada
- recolor puro
- sin adornos nuevos

Base actual:

- `_base_wood_shield_tower.png`

Salidas:

- `wood_shield.png`
- `stone_shield.png`
- `copper_shield.png`
- `bronze_shield.png`
- `iron_shield.png`
- `silver_shield.png`
- alias: `{tier}_shield_tower.png`

## Nota tecnica sobre Excalibur

El escudo cuadrado de Excalibur existe, pero usa el renderer especial vanilla
de `minecraft:shield`. Eso sirve bien como base global del shield vanilla, pero
no queremos seguir desviandonos a una rama de modelos propios.

Por eso la estrategia recomendada es:

- conservar la geometria/silueta base existente
- retexturizar por tier
- no pintar adornos sinteticos encima de la base aprobada
- usar `tower` y `round` como familias visuales, no como familias de modelo

## Preview

Se puede regenerar la preview con:

```powershell
python .\tools\prepare_shield_family_assets.py
python .\tools\generate_tower_shield_textures.py
```

Salida:

- `docs/previews/preview_shield_families.png`
- `docs/previews/preview_tower_shield_tiers.png`
