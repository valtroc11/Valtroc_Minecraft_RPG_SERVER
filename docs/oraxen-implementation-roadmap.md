# Roadmap de implementacion Oraxen

Este documento baja a tierra que piezas vamos a convertir primero en contenido
Oraxen mas integrado, que base vanilla usan y que mecanica necesitan.

## Estado

- **Listo**: ya existe asset e item Oraxen usable
- **Parcial**: existe asset/item, pero falta integracion fuerte
- **Pendiente**: no existe o falta casi todo

## Fase 1 - Bloques y menas base

| ID | Tipo | Tier | Base vanilla | Mecanica Oraxen | Drop objetivo | Estado |
| --- | --- | --- | --- | --- | --- | --- |
| `tin_ore` | mena | bronce | `IRON_ORE` | `noteblock` | `tin_chunk` | Parcial |
| `tin_block` | bloque | bronce | `IRON_BLOCK` | `noteblock` | self | Parcial |
| `silver_ore` | mena | plata | `DEEPSLATE_GOLD_ORE` | `noteblock` | `silver_chunk` | Parcial |
| `silver_block` | bloque | plata | `QUARTZ_BLOCK` | `noteblock` | self | Parcial |

## Fase 2 - Expansion de bloques altos

| ID | Tipo | Tier | Base vanilla | Mecanica Oraxen | Drop objetivo | Estado |
| --- | --- | --- | --- | --- | --- | --- |
| `oricalco_ore` | mena | oricalco | `NETHER_GOLD_ORE` | `noteblock` | `oricalco_fragment` | Parcial |
| `oricalco_block` | bloque | oricalco | `GLOWSTONE` | `noteblock` | self | Parcial |
| `mithril_ore` | mena | mithril | `DIAMOND_ORE` | `noteblock` | `mithril_shard` | Parcial |
| `mithril_block` | bloque | mithril | `DIAMOND_BLOCK` | `noteblock` | self | Parcial |

## Fase 3 - Escudos y equipo defensivo

| ID | Tipo | Tier | Base vanilla | Mecanica Oraxen | Uso | Estado |
| --- | --- | --- | --- | --- | --- | --- |
| `wood_shield` | escudo | madera | `SHIELD` | item/model | guerrero/clerigo | Listo |
| `stone_shield` | escudo | piedra | `SHIELD` | item/model | guerrero/clerigo | Listo |
| `copper_shield` | escudo | cobre | `SHIELD` | item/model | guerrero/clerigo | Listo |
| `bronze_shield` | escudo | bronce | `SHIELD` | item/model | guerrero/clerigo | Listo |
| `iron_shield` | escudo | hierro | `SHIELD` | item/model | guerrero/clerigo | Listo |
| `silver_shield` | escudo | plata | `SHIELD` | item/model | guerrero/clerigo | Listo |
| `diamond_shield` | escudo | diamante | `SHIELD` | item/model | guerrero/clerigo | Pendiente |
| `oricalco_shield` | escudo | oricalco | `SHIELD` | item/model | guerrero/clerigo | Pendiente |
| `mithril_shield` | escudo | mithril | `SHIELD` | item/model | guerrero/clerigo | Pendiente |

## Fase 4 - Varitas y staffs

| ID | Tipo | Tier | Base vanilla | Mecanica Oraxen | Uso | Estado |
| --- | --- | --- | --- | --- | --- | --- |
| `wood_wand` | varita | madera | `STICK` | item/model | mago | Listo |
| `stone_wand` | varita | piedra | `STICK` | item/model | mago | Listo |
| `copper_wand` | varita | cobre | `STICK` | item/model | mago | Listo |
| `bronze_wand` | varita | bronce | `STICK` | item/model | mago | Listo |
| `iron_wand` | varita | hierro | `STICK` | item/model | mago | Pendiente |
| `silver_wand` | varita | plata | `STICK` | item/model | mago | Listo |
| `wood_staff` | staff | madera | `BLAZE_ROD` | item/model | clerigo | Listo |
| `stone_staff` | staff | piedra | `BLAZE_ROD` | item/model | clerigo | Listo |
| `copper_staff` | staff | cobre | `BLAZE_ROD` | item/model | clerigo | Listo |
| `bronze_staff` | staff | bronce | `BLAZE_ROD` | item/model | clerigo | Listo |
| `iron_staff` | staff | hierro | `BLAZE_ROD` | item/model | clerigo | Pendiente |
| `silver_staff` | staff | plata | `BLAZE_ROD` | item/model | clerigo | Listo |

## Fase 5 - Armas de progresion

| ID | Tipo | Tier | Base vanilla | Mecanica Oraxen | Uso | Estado |
| --- | --- | --- | --- | --- | --- | --- |
| `bronze_sword` | arma | bronce | `IRON_SWORD` | item/model | guerrero | Listo |
| `bronze_axe` | arma | bronce | `IRON_AXE` | item/model | guerrero | Listo |
| `bronze_bow` | arma | bronce | `BOW` | item/model | explorador | Listo |
| `silver_sword` | arma | plata | `IRON_SWORD` | item/model | guerrero | Listo |
| `silver_axe` | arma | plata | `IRON_AXE` | item/model | guerrero | Listo |
| `silver_bow` | arma | plata | `BOW` | item/model | explorador | Listo |

## Fase 6 - Materiales y componentes

| ID | Tipo | Tier | Base vanilla | Uso | Estado |
| --- | --- | --- | --- | --- | --- |
| `tin_chunk` | material | bronce | `RAW_IRON` | fundicion | Listo |
| `tin_ingot` | material | bronce | `IRON_INGOT` | crafting | Listo |
| `bronze_nugget` | material | bronce | `GOLD_NUGGET` | crafting | Listo |
| `bronze_ingot` | material | bronce | `COPPER_INGOT` | crafting | Listo |
| `silver_chunk` | material | plata | `RAW_GOLD` | fundicion | Listo |
| `silver_ingot` | material | plata | `GOLD_INGOT` | crafting | Listo |
| `oricalco_fragment` | material | oricalco | `BLAZE_POWDER` | crafting | Listo |
| `oricalco_ingot` | material | oricalco | `BLAZE_ROD` | crafting | Listo |
| `mithril_shard` | material | mithril | `PRISMARINE_CRYSTALS` | crafting | Listo |
| `mithril_ingot` | material | mithril | `PRISMARINE_SHARD` | crafting | Listo |
| `legendary_core` | material | legendario | `NETHER_STAR` | crafting | Listo |
| `bronze_plate` | componente | bronce | por definir | recipes avanzadas | Pendiente |
| `silver_plate` | componente | plata | por definir | recipes avanzadas | Pendiente |
| `oricalco_plate` | componente | oricalco | por definir | recipes avanzadas | Pendiente |
| `mithril_plate` | componente | mithril | por definir | recipes avanzadas | Pendiente |

## Fase 7 - Visuales de mobs

| ID | Tipo | Rol visual | Base vanilla | Integracion | Estado |
| --- | --- | --- | --- | --- | --- |
| `summoner_hood` | casco | invocador | `LEATHER_HELMET` | elite/miniboss | Listo |
| `summoner_staff` | arma | invocador | `BLAZE_ROD` | elite/miniboss | Listo |
| `brutal_helm` | casco | brutal | `IRON_HELMET` | elite/miniboss | Listo |
| `brutal_axe` | arma | brutal | `IRON_AXE` | elite/miniboss | Listo |
| `bastion_shield` | escudo | robusto/bastion | `SHIELD` | elite/miniboss | Listo |
| `frost_focus` | foco | congelante | `AMETHYST_SHARD` | elite/miniboss | Listo |
| `blood_mask` | mascara | vampirico | `GOLDEN_HELMET` | elite/miniboss | Listo |

## Orden recomendado de trabajo

1. `tin_ore`, `tin_block`, `silver_ore`, `silver_block`
2. `bronze_wand`, `bronze_staff`, `bronze_sword`, `bronze_axe`, `bronze_bow`
3. `silver_shield`, `silver_wand`, `silver_staff`, `silver_sword`, `silver_axe`, `silver_bow`
4. Integracion de visuales de mobs por afinidad
5. `oricalco` y `mithril`

## Regla general

Siempre que sea posible:

- reutilizar silueta y lectura vanilla
- recolorear o ajustar detalles
- usar Oraxen para la capa custom
- dejar la complejidad 3D real para una fase posterior
