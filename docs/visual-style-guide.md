# Guia visual de Servidro MX

Esta guia define la linea visual para los assets del servidor. La referencia
principal es una fantasia medieval sobria inspirada en la lectura cromatica y
el peso visual de Excalibur, pero con assets propios del proyecto.

## Principios generales

### 1. Legibilidad primero

Cada item o bloque debe poder entenderse rapido:

- material
- tier
- funcion
- rareza o afinidad si aplica

La silueta debe leerse bien incluso en inventario pequeno.

Cuando Excalibur ya nos da una silueta buena, no debemos redibujar la
geometria: retexturizar esa base casi siempre es la mejor decision.

### 2. Medieval sobrio

Queremos:

- metal trabajado
- madera envejecida
- cuero funcional
- decoracion contenida

Evitar:

- neones
- brillo sci-fi
- fantasia exagerada sin control
- dorados demasiado limpios en tiers bajos

### 3. Inspiracion, no copia

Usamos Excalibur como referencia de:

- paleta
- clima medieval
- contraste
- peso visual

Pero los assets deben seguir siendo propios de Servidro MX.

## Regla de produccion

Preferimos:

- retexturizar siluetas existentes de Excalibur
- retexturizar bases vanilla ya bien resueltas
- cambiar materiales y lectura visual sin repintar geometria ni adornos nuevos

Evitamos:

- abrir modelos nuevos solo por capricho visual
- tocar geometria si el problema real era de textura
- duplicar familias de display cuando una sola base ya funciona

## Lenguaje de materiales

### Madera

- roble apagado
- marrones medios
- vetas simples
- herrajes minimos

Uso:

- tier inicial
- escudos simples
- varitas y staffs base

### Piedra

- gris cantera
- poco contraste cromatico
- sensacion tosca pero estable

Uso:

- equipo temprano
- herramientas pesadas
- escudos de lectura defensiva

### Cobre

- naranja tostado
- metal calido
- ligera sensacion artesanal

Uso:

- primer metal noble
- magia intermedia temprana
- herreria temprana seria

### Bronce

- marron dorado
- mas militar que cobre
- menos saturado que oro

Uso:

- primer salto de progresion importante
- armas y equipo de oficio serio

### Hierro

- gris templado
- metal estandar confiable
- lectura limpia y funcional

Uso:

- tier medio estable
- armamento clasico
- proteccion comun de guerra

### Plata

- gris frio claro
- noble, limpio, refinado
- sin verse magico por si mismo

Uso:

- tier elegante
- defensa y soporte
- piezas clericales o de prestigio moderado

### Diamante

- azul muy leve
- brillo solido pero controlado
- no cristal chillon

Uso:

- tier alto vanilla
- equipo fuerte pero terrenal

### Oricalco

- dorado solar
- metal raro, antiguo y ritual
- mas sagrado que rico

Uso:

- reliquias
- catalizadores
- equipo de alto rango

### Mithril

- azul plateado frio
- ligero, limpio, casi etereo
- brillante pero no electrico

Uso:

- tiers tardios
- piezas finas
- magia o nobleza avanzada

### Legendario

- oro palido, marfil y luz
- contraste alto y lectura ceremonial
- debe sentirse unico

Uso:

- nucleos
- reliquias finales
- bosses o crafting excepcional

## Paleta por tier

| Tier | Color guia | Hex aproximado | Notas |
| --- | --- | --- | --- |
| Madera | marron roble | `#9a6a3a` | base humilde |
| Piedra | gris cantera | `#8b8b93` | robusto y temprano |
| Cobre | cobre calido | `#c77743` | primer metal con identidad |
| Bronce | dorado marron | `#9f6f33` | marcial y estable |
| Hierro | gris acero | `#c7ccd1` | funcional |
| Plata | plata fria | `#edf2f7` | refinado |
| Diamante | azul mineral | `#8fc7e8` | fuerte, no neon |
| Oricalco | dorado solar | `#e5ab48` | ritual |
| Mithril | azul plateado | `#8ed1ff` | raro y ligero |
| Legendario | oro claro | `#ffd56a` | unico |

## Reglas por familia de item

### Menas

- base de piedra reconocible
- veta visible y concentrada
- color del metal claramente separado del fondo
- nunca perder lectura de "ore"

### Lingotes y fragmentos

- forma simple
- volumen legible
- poco detalle fino
- el color debe vender el tier sin necesitar texto

### Bloques

- textura repetible
- no depender de detalles centrales unicos
- bordes y caras deben soportar colocacion en masa

### Escudos

- silueta clara
- lectura frontal fuerte
- herrajes visibles
- el metal del tier se nota en borde y refuerzos

### Varitas

- finas
- simples en early
- nucleo visual creciendo por tier

### Staffs

- mas largos y rituales que las varitas
- cabeza o remate mas importante
- lectura clerical o magica segun familia

### Armas

- silhouette vanilla compatible
- peso visual medieval
- evitar exceso de puntas o adornos en tiers bajos

## Rarezas y afinidades

### Rareza

- `Normal`: lectura limpia, sin ruido extra
- `Raro`: pequeno acento visual
- `Elite`: 2 a 4 piezas mas notorias
- `MiniBoss`: set fuerte y firma propia

### Afinidades

#### Summoner
- morado ritual
- capucha
- foco o staff corrupto

#### Brutal
- rojo oscuro
- metal pesado
- filo ancho

#### Robusto / Bastion
- acero frio
- escudo grande
- placas densas

#### Congelante
- azul hielo
- cristal frio
- detalles afilados

#### Vampirico
- rojo vino
- negro o acero oscuro
- reliquias o mascaras

## Reglas de ornamentacion

### Early game

- casi nada de filigrana
- pragmatismo
- apariencia de supervivencia

### Mid game

- remaches
- grabados leves
- detalles de gremio o oficio

### Late game

- simbolos
- marcos mas ricos
- gemas o focos centrales

## Reglas de consistencia

1. Un tier inferior no debe verse mas prestigioso que el siguiente.
2. Un asset magico no debe parecer tecnologia.
3. Los items de clase deben compartir familia visual.
4. Los bloques deben verse colocables, no solo bonitos en inventario.
5. Las afinidades deben leerse por color y silueta incluso antes del nombre.

## Aplicacion inmediata

Las siguientes familias deben seguir esta guia desde ya:

- `tin_ore`, `silver_ore`, `oricalco_ore`, `mithril_ore`
- `tin_block`, `bronze_block`, `silver_block`, `oricalco_block`, `mithril_block`
- escudos por tier
- varitas y staffs
- gear visual de elites y minibosses
