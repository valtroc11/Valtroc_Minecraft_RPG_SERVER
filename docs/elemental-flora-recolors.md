# Recolors elementales de arboles y flores

Assets generados desde el resource pack de Excalibur para crear variantes de
hielo/congelacion y calor/fuego.

## Resultado

Se generaron `242` texturas en:

- `content/oraxen/pack/textures/servidorpg/flora/trees/ice/`
- `content/oraxen/pack/textures/servidorpg/flora/trees/fire/`
- `content/oraxen/pack/textures/servidorpg/flora/flowers/ice/`
- `content/oraxen/pack/textures/servidorpg/flora/flowers/fire/`

Tambien se duplican para Oraxen en:

- `content/oraxen/pack/assets/oraxen/textures/servidorpg/flora/`

Previews:

- `docs/previews/preview_elemental_tree_recolors.png`
- `docs/previews/preview_elemental_flower_recolors.png`

Regenerar:

```powershell
python tools\generate_elemental_flora_recolors.py
```

## Arboles

La madera y derivados tienen una recoloracion marcada:

- `ice`: madera mas azul/gris fria.
- `fire`: madera mas rojiza/calida.

Familias generadas:

| Familia | Hielo | Fuego |
| --- | --- | --- |
| Oak | `trees/ice/oak/` | `trees/fire/oak/` |
| Spruce | `trees/ice/spruce/` | `trees/fire/spruce/` |
| Birch | `trees/ice/birch/` | `trees/fire/birch/` |
| Jungle | `trees/ice/jungle/` | `trees/fire/jungle/` |
| Acacia | `trees/ice/acacia/` | `trees/fire/acacia/` |
| Dark Oak | `trees/ice/dark_oak/` | `trees/fire/dark_oak/` |
| Mangrove | `trees/ice/mangrove/` | `trees/fire/mangrove/` |
| Cherry | `trees/ice/cherry/` | `trees/fire/cherry/` |
| Pale Oak | `trees/ice/pale_oak/` | `trees/fire/pale_oak/` |

Texturas cubiertas segun disponibilidad en Excalibur:

- logs
- log tops
- stripped logs
- stripped log tops
- planks
- trapdoors
- door bottom/top
- saplings
- leaves
- leaves tips
- `dark_oak_planks_slab`

Nota para derivados:

- Las escaleras, losas, cercas, puertas de cerca, botones y pressure plates de
  Minecraft normalmente reutilizan la textura de `*_planks`.
- Por eso, para esas piezas se debe apuntar el modelo al `*_planks.png`
  elemental correspondiente.

## Flores

Las flores tienen solo un tinte leve de la tematica:

- `ice`: toque frio/celeste suave.
- `fire`: toque calido/rojizo suave.

Mantienen la misma funcion y deben dar los mismos drops que su flor base.

Flores generadas:

| Flor base | Hielo | Fuego |
| --- | --- | --- |
| Dandelion | `flowers/ice/dandelion.png` | `flowers/fire/dandelion.png` |
| Poppy | `flowers/ice/poppy.png` | `flowers/fire/poppy.png` |
| Blue Orchid | `flowers/ice/blue_orchid.png` | `flowers/fire/blue_orchid.png` |
| Allium | `flowers/ice/allium.png` | `flowers/fire/allium.png` |
| Azure Bluet | `flowers/ice/azure_bluet.png` | `flowers/fire/azure_bluet.png` |
| Red Tulip | `flowers/ice/red_tulip.png` | `flowers/fire/red_tulip.png` |
| Orange Tulip | `flowers/ice/orange_tulip.png` | `flowers/fire/orange_tulip.png` |
| White Tulip | `flowers/ice/white_tulip.png` | `flowers/fire/white_tulip.png` |
| Pink Tulip | `flowers/ice/pink_tulip.png` | `flowers/fire/pink_tulip.png` |
| Oxeye Daisy | `flowers/ice/oxeye_daisy.png` | `flowers/fire/oxeye_daisy.png` |
| Cornflower | `flowers/ice/cornflower.png` | `flowers/fire/cornflower.png` |
| Lily of the Valley | `flowers/ice/lily_of_the_valley.png` | `flowers/fire/lily_of_the_valley.png` |
| Sunflower | `flowers/ice/sunflower_front.png` | `flowers/fire/sunflower_front.png` |
| Rose Bush | `flowers/ice/rose_bush_top.png` | `flowers/fire/rose_bush_top.png` |
| Peony | `flowers/ice/peony_top.png` | `flowers/fire/peony_top.png` |
| Lilac | `flowers/ice/lilac_top.png` | `flowers/fire/lilac_top.png` |
| Torchflower | `flowers/ice/torchflower.png` | `flowers/fire/torchflower.png` |
| Wildflowers | `flowers/ice/wildflowers.png` | `flowers/fire/wildflowers.png` |

## Siguiente paso para usarlos en mundo

Estas PNG ya estan listas como assets del pack. Para colocarlas como bloques
custom se debe crear el mapeo Oraxen de cada bloque/flor:

- bloques de madera: usar `Mechanics.noteblock` o el sistema de furniture/block
  que prefieras para el servidor.
- flores: crear variantes custom con el mismo drop que la flor vanilla base.

Convencion sugerida de IDs:

- `ice_oak_log`, `fire_oak_log`
- `ice_oak_planks`, `fire_oak_planks`
- `ice_oak_slab`, `fire_oak_slab`
- `ice_poppy`, `fire_poppy`
- `ice_blue_orchid`, `fire_blue_orchid`
