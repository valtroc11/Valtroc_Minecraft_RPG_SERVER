# Mapa visual: Medieval Items + Oraxen

## Objetivo

Usar un pack gratuito y util para cubrir el mayor terreno posible de la
progresion medieval **sin** esperar a tener todos los modelos propios listos.

La estrategia correcta es:

1. aprovechar `Medieval Items!` para lo que ya resuelve bien
2. usar items vanilla/stand-ins donde no haya hueco visual grave
3. cubrir con `Oraxen` propio lo que el pack no trae o no puede resolver

## Lo que si trae Medieval Items!

Tras revisar el contenido del pack, estas son las piezas mas utiles para
nosotros:

- armas vanilla medievalizadas:
  - madera
  - piedra
  - hierro
  - oro
  - diamante
  - netherite
- armaduras vanilla medievalizadas:
  - cuero
  - hierro
  - oro
  - diamante
  - netherite
  - chainmail
- arco y ballesta
- mucho UI medievalizado:
  - inventario
  - crafting table
  - smithing
  - enchanting
  - libro
  - hud de vida/comida
- set visual de cobre:
  - espada
  - hacha
  - pala
  - pico
  - azada
  - casco
  - pechera
  - grebas
  - botas
  - lingote
  - nugget

## Lo que no resuelve Medieval Items!

No es una libreria de items funcionales nuevos. Es un resource pack que
overridea texturas vanilla.

Huecos actuales:

- no vi escudos medievales dedicados
- no vi varitas
- no vi staffs clericales
- no vi reliquias
- no vi tiers propios:
  - bronce
  - plata
  - oricalco
  - mithril
  - legendario
- no vi monstruos custom ni modelos de elites/miniboss

Por eso no debe ser la columna central del sistema RPG; debe ser una **capa de
apoyo**.

## Regla de uso

### Cubierto por Medieval Items

Usar directamente cuando:

- el item es vanilla
- el tier existe visualmente en el pack
- no necesitamos modelo nuevo ni comportamiento especial

### Cubierto por Oraxen

Usar Oraxen cuando:

- el item no existe en vanilla
- el tier es propio del servidor
- el item tiene identidad de clase
- necesitamos distinguirlo con claridad del resto del loot

## Mapeo por tier

| Tier | Fuente visual principal | Observacion |
| --- | --- | --- |
| Madera | Medieval Items + vanilla | suficiente para arrancar |
| Piedra | Medieval Items + vanilla | suficiente para early |
| Cobre | Medieval Items | gran candidato, ya trae set visible |
| Bronce | Oraxen propio | no existe como set claro en el pack |
| Hierro | Medieval Items | suficiente para tier medio |
| Plata | Oraxen propio | no existe como tier real del pack |
| Diamante | Medieval Items | suficiente para tier alto base |
| Oricalco | Oraxen propio | tier fantastico, requiere identidad |
| Mithril | Oraxen propio | tier fantastico, requiere identidad |
| Legendario | Oraxen propio | debe sentirse unico |

## Mapeo por clase

### Guerrero

### Cubierto ya

- espada
- hacha
- armadura

### Falta

- escudo por tier
- espadon futuro
- lanza futura

### Explorador

### Cubierto ya

- arco
- ballesta
- armadura ligera temporal

### Falta

- daga real
- arco por tier custom si queremos mas identidad
- capa o pieza distintiva futura

### Mago

### Cubierto ya

- solo stand-in temporal

### Falta

- varita real por tier
- staff real por tier
- foco/orbe futuro

### Clerigo

### Cubierto ya

- solo stand-in temporal

### Falta

- staff clerical real
- escudo clerical por tier
- reliquia futura
- maza futura

## Primera tanda recomendada para Oraxen

Si queremos impacto visual rapido sin bloquearnos, esta seria mi primera tanda:

### Prioridad 1

- `wood_shield`
- `stone_shield`
- `copper_shield`
- `bronze_shield`
- `iron_shield`

Porque el escudo ya tiene mecanica propia en el plugin y ahora mismo es la
pieza mas importante que no tiene identidad visual.

### Prioridad 2

- `wood_wand`
- `stone_wand`
- `copper_wand`
- `bronze_wand`
- `iron_wand`

- `wood_staff`
- `stone_staff`
- `copper_staff`
- `bronze_staff`
- `iron_staff`

Con eso `Mago` y `Clerigo` dejan de sentirse como "stick/blaze rod con lore".

### Prioridad 3

- `bronze_sword`
- `bronze_axe`
- `silver_sword`
- `silver_axe`
- `silver_bow`
- `bronze_bow`

Con esto ya cerramos los primeros tiers propios que no cubre Medieval Items.

## Reparto practico de trabajo

### Etapa 1

Usar `Medieval Items!` para:

- gear vanilla
- cobre
- GUI medieval
- armas comunes

### Etapa 2

Usar `Oraxen` para:

- escudos
- varitas
- staffs
- bronze
- silver

### Etapa 3

Usar `Oraxen` + `MythicMobs` para:

- drops de elite/miniboss
- materiales raros
- trofeos visuales

### Etapa 4

Si luego queremos monstruos realmente unicos:

- `ModelEngine`

## Recomendacion final

La decision buena hoy no es intentar que `Medieval Items!` resuelva todo.
La decision buena es:

1. dejar que cubra `wood/stone/copper/iron/diamond` y la UI
2. reservar para `Oraxen` lo que el pack no trae:
   - escudos
   - varitas
   - staffs
   - bronze
   - silver
   - tiers fantasticos
3. construir el resto del sistema visual encima de esa base

Asi avanzamos rapido, con licencia utilizable y sin obligarnos a rehacer luego
todo desde cero.
