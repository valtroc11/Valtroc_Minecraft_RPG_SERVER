# Materiales y menas - Fase 1

## Objetivo

Avanzar con materiales nuevos **sin** meternos todavia en una generacion de
menas que luego tengamos que deshacer cuando entre `Oraxen`.

La fase 1 correcta no es llenar el mundo de bloques falsos a ciegas. La fase 1
correcta es:

1. fijar que materiales existen
2. fijar que tier ocupan
3. fijar donde deberian aparecer
4. volverlos tangibles como items del servidor
5. dejar lista la base para recipes, drops y economia

## Materiales iniciales recomendados

### Ruta temprana-media

- `tin_chunk`
- `tin_ingot`
- `bronze_ingot`
- `silver_chunk`
- `silver_ingot`

### Ruta alta

- `oricalco_fragment`
- `oricalco_ingot`
- `mithril_shard`
- `mithril_ingot`
- `legendary_core`

## Regla de progresion

### Bronce

Bronce **no** debe ser mena directa final.

La progresion buena es:

- cobre vanilla
- estano como nueva mena
- bronce como aleacion

Eso le da identidad de herreria real.

### Plata

Plata si puede ser una mena rara del overworld, cercana al diamante.

### Oricalco

Debe sentirse como metal corrompido del Nether, no como "otro hierro".

### Mithril

Debe reservarse para End y contenido final.

### Legendario

No deberia ser una mena normal. Debe ser un catalizador de endgame.

## Spawn sugerido por mundo

### Overworld

#### Estano

- tier: `bronze`
- y: `32` a `72`
- rol: alimentar la primera gran aleacion del servidor

#### Plata

- tier: `silver`
- y: `-32` a `16`
- rol: metal refinado de mid game

### Nether

#### Oricalco

- tier: `oricalco`
- y: `8` a `48`
- rol: metal magico y corrompido

### End

#### Mithril

- tier: `mithril`
- y: `0` a `80`
- rol: metal de late game

## Stand-ins temporales

Mientras no haya worldgen dedicado o bloques custom, estos son los candidatos
mas razonables:

- plata:
  - `EMERALD_ORE`
  - `DEEPSLATE_EMERALD_ORE`
- oricalco:
  - `NETHER_QUARTZ_ORE`
  - o `ANCIENT_DEBRIS` si queremos que sea muy raro

No recomiendo activar aun stand-ins para:

- estano
- mithril

porque merecen mejor identidad visual y de capa.

## Que si esta listo en fase 1

En esta fase ya podemos:

- spawnear materiales custom por comando admin
- definir recipes futuras alrededor de esos materiales
- usarlos en economia, drops y crafting gated
- probar nombres, tiers y rareza visual del loot

## Que no conviene activar aun

- worldgen real de nuevas menas en todos los mundos
- reemplazo agresivo de ores vanilla sin decidir consecuencias
- mithril/oricalco como "ore comun"

## Recomendacion final

La mejor siguiente implementacion real del mundo no es "todas las menas".
Es esta:

1. activar primero `estano` y `plata`
2. dejar `oricalco` y `mithril` como fase 2
3. introducir worldgen custom cuando:
   - tengamos el look decidido
   - sepamos si va por datapack, plugin o Oraxen
   - y no pisemos recursos vanilla que luego vayamos a extrañar
