# Biomas Helados Servidro

## cordilleras_heladas

Objetivo: bioma de cordillera enorme inspirado en Andes y cadenas montanosas glaciares reales.

- Terreno: montanas nevadas extremas, con picos hasta altura maxima menos 5 bloques.
- Composicion: piedra, tierra, nieve y hielo superficial.
- Clima: tormenta de nieve permanente a nivel de experiencia; el runtime aplica frio ambiental.
- Cuevas: sistemas amplios que conectan con `servidro:profundidades_heladas`.
- Refugios: fuentes hidrotermales con magma block en la base; anulan la congelacion cercana.
- Recursos superficiales: cobre vanilla y estano custom futuro.
- Recursos superficiales: afloraciones de estano en cotas altas y plata en afloraciones menos comunes.
- Mobs: zombies helados, gigantes de hielo, osos polares y esqueletos de hielo.

## profundidades_heladas

Objetivo: red de cavernas gigantes siempre debajo de `cordilleras_heladas`.

- Terreno: cavernas enormes con hielo, tierra, piedra y deepslate.
- Recursos: hierro vanilla y plata custom futura.
- Recursos: hierro y estano abundantes en profundidad; plata concentrada en afloraciones altas.
- Amenaza: aranas de hielo con madrigueras, zombies helados y esqueletos de hielo.
- Restriccion: gigantes de hielo solo en superficie.
- Magma frio: por ahora se modela como bloques de magma normales con dano/congelacion runtime pendiente; visual azul requiere resource pack/Oraxen.

## Estado tecnico

Implementado:

- Registro de ambos biomas en datapack.
- Dimension POC `servidro:poc_helado`.
- Dimension POC usando noise setting `minecraft:amplified` para validar cordilleras altas.
- Congelacion ambiental en runtime para `cordilleras_heladas`.
- Proteccion hidrotermal si el jugador esta cerca de `MAGMA_BLOCK`.
- Remapeo runtime de chunks frios/montanosos del overworld a `servidro:cordilleras_heladas`.
- Remapeo runtime subterraneo del mismo cluster a `servidro:profundidades_heladas`.
- Pasada runtime de menas heladas:
  - estano custom (`tin_ore`) comun en afloraciones altas
  - plata custom (`silver_ore`) menos comun en cumbres altas
  - hierro abundante en profundidad
  - estano abundante en profundidad

Pendiente:

- Noise settings propio para picos hasta Y maxima - 5 de forma garantizada.
- Integracion del contrato de biomas/manifiesto para que el plugin deje de depender de heuristicas internas.
- Features de estano/plata como bloques Oraxen o stand-ins.
- Estructuras de madrigueras de aranas.
- Magma frio azul con resource pack y dano propio.
