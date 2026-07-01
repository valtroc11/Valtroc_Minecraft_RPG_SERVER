# Mobs clasicos elementales

Primera tanda de mobs clasicos/enemigos y neutrales recoloreados con paletas de
hielo/congelacion y calor/fuego usando como base las texturas de Excalibur.

## Nota tecnica

Las texturas completas de entidad generadas quedan como assets base para una
fase con ModelEngine, OptiFine/CIT o un sistema de modelos por entidad. Con solo
MythicMobs + Oraxen, el servidor puede spawnear el comportamiento, nombre,
particulas, drops y equipo, pero no puede cambiar la textura corporal completa
por instancia sin afectar a todos los mobs vanilla del mismo tipo.

Texturas generadas:

- `content/oraxen/pack/textures/servidorpg/mobs/entities/*_ice.png`
- `content/oraxen/pack/textures/servidorpg/mobs/entities/*_fire.png`
- `content/oraxen/pack/assets/oraxen/textures/servidorpg/mobs/entities/*_ice.png`
- `content/oraxen/pack/assets/oraxen/textures/servidorpg/mobs/entities/*_fire.png`

Preview:

- `docs/previews/preview_classic_mob_elemental_recolors.png`

Regenerar:

```powershell
python tools\generate_classic_mob_elemental_recolors.py
```

## Lista de IDs y comandos

| ID MythicMobs | Nombre | Base | Afinidad | Textura |
| --- | --- | --- | --- | --- |
| `srv_ice_zombie` | Zombie de Hielo | Zombie | Hielo | `zombie_ice.png` |
| `srv_fire_zombie` | Zombie de Fuego | Zombie | Fuego | `zombie_fire.png` |
| `srv_ice_skeleton` | Esqueleto de Hielo | Skeleton | Hielo | `skeleton_ice.png` |
| `srv_fire_skeleton` | Esqueleto de Fuego | Skeleton | Fuego | `skeleton_fire.png` |
| `srv_ice_spider` | Arana de Hielo | Spider | Hielo | `spider_ice.png` |
| `srv_fire_spider` | Arana de Fuego | Spider | Fuego | `spider_fire.png` |
| `srv_ice_cave_spider` | Arana de Cueva de Hielo | Cave Spider | Hielo | `cave_spider_ice.png` |
| `srv_fire_cave_spider` | Arana de Cueva de Fuego | Cave Spider | Fuego | `cave_spider_fire.png` |
| `srv_ice_creeper` | Creeper de Hielo | Creeper | Hielo | `creeper_ice.png` |
| `srv_fire_creeper` | Creeper de Fuego | Creeper | Fuego | `creeper_fire.png` |
| `srv_ice_enderman` | Enderman de Hielo | Enderman | Hielo | `enderman_ice.png` |
| `srv_fire_enderman` | Enderman de Fuego | Enderman | Fuego | `enderman_fire.png` |
| `srv_ice_witch` | Bruja de Hielo | Witch | Hielo | `witch_ice.png` |
| `srv_fire_witch` | Bruja de Fuego | Witch | Fuego | `witch_fire.png` |
| `srv_ice_piglin` | Piglin de Hielo | Piglin | Hielo | `piglin_ice.png` |
| `srv_fire_piglin` | Piglin de Fuego | Piglin | Fuego | `piglin_fire.png` |
| `srv_ice_wolf` | Lobo de Hielo | Wolf | Hielo | `wolf_ice.png` |
| `srv_fire_wolf` | Lobo de Fuego | Wolf | Fuego | `wolf_fire.png` |
| `srv_ice_bee` | Abeja de Hielo | Bee | Hielo | `bee_ice.png` |
| `srv_fire_bee` | Abeja de Fuego | Bee | Fuego | `bee_fire.png` |
| `srv_ice_iron_golem` | Golem de Hierro de Hielo | Iron Golem | Hielo | `iron_golem_ice.png` |
| `srv_fire_iron_golem` | Golem de Hierro de Fuego | Iron Golem | Fuego | `iron_golem_fire.png` |
| `srv_ice_llama` | Llama de Hielo | Llama | Hielo | `llama_ice.png` |
| `srv_fire_llama` | Llama de Fuego | Llama | Fuego | `llama_fire.png` |

## Comandos de spawn

Usa estos comandos dentro del servidor:

```text
/mm mobs spawn srv_ice_zombie 1
/mm mobs spawn srv_fire_zombie 1
/mm mobs spawn srv_ice_skeleton 1
/mm mobs spawn srv_fire_skeleton 1
/mm mobs spawn srv_ice_spider 1
/mm mobs spawn srv_fire_spider 1
/mm mobs spawn srv_ice_cave_spider 1
/mm mobs spawn srv_fire_cave_spider 1
/mm mobs spawn srv_ice_creeper 1
/mm mobs spawn srv_fire_creeper 1
/mm mobs spawn srv_ice_enderman 1
/mm mobs spawn srv_fire_enderman 1
/mm mobs spawn srv_ice_witch 1
/mm mobs spawn srv_fire_witch 1
/mm mobs spawn srv_ice_piglin 1
/mm mobs spawn srv_fire_piglin 1
/mm mobs spawn srv_ice_wolf 1
/mm mobs spawn srv_fire_wolf 1
/mm mobs spawn srv_ice_bee 1
/mm mobs spawn srv_fire_bee 1
/mm mobs spawn srv_ice_iron_golem 1
/mm mobs spawn srv_fire_iron_golem 1
/mm mobs spawn srv_ice_llama 1
/mm mobs spawn srv_fire_llama 1
```

Para spawnear 5 unidades cambia el ultimo numero:

```text
/mm mobs spawn srv_ice_spider 5
/mm mobs spawn srv_fire_cave_spider 5
```
