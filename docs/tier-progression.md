# Progresion de tiers

## Limite tecnico actual

Hoy el servidor puede escalar bien:

- niveles de clase
- niveles de profesiones
- drops por rareza
- mejoras de forja
- restricciones por material vanilla

Lo que **todavia no puede existir como item real distinto** sin Oraxen o un
sistema equivalente es esto:

- armadura de madera real
- escudo de piedra/cobre/bronce/etc. con skin propia
- varitas y staffs con identidad visual propia
- minerales nuevos como plata, oricalco o mithril con bloque e item unicos

Por eso la estrategia correcta es trabajar en dos capas:

1. **Ahora**: usar cobre vanilla donde ya exista y stand-ins vanilla con
   nombre, lore, stats y gating para lo que aun no exista.
2. **Despues**: sustituir esos stand-ins por items Oraxen y recetas reales.

## Regla general de progresion

La progresion debe sentirse clara, no saturada:

- cada tier dura entre 8 y 12 niveles
- el tier nuevo supera al anterior de forma visible
- la mejor forja de un tier **no supera** al tier base siguiente
- los drops de mobs siguen la misma escalera que la herreria

Regla de cap de forja recomendada:

- el bonus maximo de un item nunca puede cerrar mas del `80%` de la brecha hacia
  su siguiente tier
- ejemplo: una espada de madera excelente puede acercarse a piedra mala, pero no
  superarla

## Escalera recomendada a 100 niveles

| Tier | Usar/equipar | Herrero | Minero | Fuente |
| --- | ---: | ---: | ---: | --- |
| Madera | 1 | 1 | 1 | Bosques, arranque |
| Piedra | 3 | 3 | 3 | Cuevas tempranas |
| Cobre | 5 | 5 | 5 | Overworld alto |
| Bronce | 10 | 10 | 10 | Aleacion cobre + estano |
| Hierro | 18 | 16 | 16 | Overworld medio |
| Plata | 30 | 26 | 26 | Cerca de diamante |
| Diamante | 45 | 40 | 40 | Overworld profundo |
| Oricalco | 60 | 54 | 54 | Nether y corrupcion |
| Mithril | 78 | 72 | 72 | End y dungeons finales |
| Legendario | 94 | 88 | 88 | Bosses y materiales unicos |

## Stand-ins mientras no haya skins nuevas

| Tipo | Stand-in temporal |
| --- | --- |
| Armadura de madera/piedra | `LEATHER_*` |
| Armadura de cobre | cobre vanilla |
| Armadura de bronce | `CHAINMAIL_*` |
| Armadura de hierro/plata | `IRON_*` |
| Armadura de diamante/oricalco | `DIAMOND_*` |
| Armadura de mithril/legendario | `NETHERITE_*` |
| Escudos | `SHIELD` con nombre/stats por tier |
| Arcos | `BOW` con stats por tier |
| Varitas | `STICK` o `BLAZE_ROD` hasta Oraxen |
| Staff clerigo | `STICK` o `BLAZE_ROD` hasta Oraxen |

## Progresion de stats

No conviene que cada tier explote en numeritos. Lo sano es que cada salto se
sienta en cuatro ejes:

- durabilidad
- dano base
- velocidad de ataque
- defensa o vida adicional

Tabla guia:

| Tier | Durabilidad | Dano/arma | Vel. ataque | Armadura |
| --- | --- | --- | --- | --- |
| Madera | muy baja | base 1 | base | base 1 |
| Piedra | baja | +1 escalon | lenta-media | +1 |
| Cobre | baja-media | +1 | media | +1 a +2 |
| Bronce | media | +2 | media | +2 |
| Hierro | media-alta | +3 | media | +3 |
| Plata | media-alta | +3 | algo mas rapida | +3 y afinidad |
| Diamante | alta | +4 | media-alta | +4 |
| Oricalco | alta | +5 | alta | +4 y bonus magico |
| Mithril | muy alta | +6 | alta | +5 |
| Legendario | endgame | +7 | alta | +6 y bonus unico |

## Herreria y valor de recursos

Bronce no deberia ser mineral directo si queremos una progresion logica. Lo
mas limpio es:

- `Cobre` minable
- `Estano` minable en capa de hierro
- `Bronce = cobre + estano`

Esto hace que el tier intermedio se sienta ganado por proceso, no por azar.

XP sugerida por unidad de material principal:

| Material | XP por unidad |
| --- | ---: |
| wood | 4 |
| stone | 8 |
| copper | 12 |
| bronze | 16 |
| iron | 18 |
| silver | 24 |
| gold | 28 |
| diamond | 48 |
| oricalco | 64 |
| mithril | 80 |
| legendary | 120 |

Las piezas pesadas deben seguir dando bono:

- pecho: bono alto
- piernas: bono medio-alto
- escudo: bono medio

## Spawn de materiales

No recomiendo crear un mundo nuevo solo para minerales. La progresion ya tiene
tres dimensiones y conviene usarlas:

### Overworld

- Madera
- Piedra
- Cobre
- Estano para bronce
- Hierro
- Plata
- Diamante

Distribucion sugerida:

- cobre: amplio, capas medias-altas, usando el material vanilla ya existente
- estano: similar a hierro, algo menos comun
- hierro: normal
- plata: rara, capas cercanas a diamante
- diamante: vanilla o ligeramente reducido

### Nether

- Oricalco

Tiene sentido como metal magico o conductor del reino corrompido.

### End

- Mithril

Reservado para el tramo final del progreso.

### Legendario

No deberia ser mineral normal. Mejor:

- fragmentos de bosses
- drop de raids
- recompensa de dungeons
- combinacion de mithril + oricalco + nucleo de boss

## Drops de mobs

Los mobs deben seguir la misma escalera:

- `Normal`: piezas del tier del jugador sin stats o con loot simple
- `Rare`: tier del jugador con bonus bajo
- `Elite`: tier del jugador con bonus medio
- `MiniBoss`: tier del jugador con bonus alto o materiales especiales

La regla importante:

- el drop sigue el **nivel del jugador**
- no debe saltarse tiers por matar un mob raro temprano

## Implementacion por fases

### Fase 1

- caps a 100
- roadmap de tiers en config
- cobre vanilla + stand-ins vanilla para tiers sin item propio
- gates nuevas por tier

### Fase 2

- recipes custom por tier
- drops por tier
- materiales nuevos del overworld/nether/end

### Fase 3

- Oraxen
- modelos reales
- minerales e items con skin propia

## Recomendacion final

La pieza mas sana para el siguiente paso no es intentar meter ya mithril,
oricalco y legendario como items falsos a medias. Lo correcto es:

1. cablear primero `wood -> stone -> copper -> bronze -> iron -> silver -> diamond`
2. dejar `oricalco`, `mithril` y `legendario` para la fase de custom items
3. usar `Nether` y `End` como hogar natural de tiers altos

Asi mantenemos una progresion clara, comprobable y sin inflar el sistema antes
de tener los assets.
