# Clases, armas y atributos

## Estado actual del plugin

Hoy el plugin ya distingue clases base y usa estas armas o focos:

- `Guerrero`: espada, hacha y escudo
- `Explorador`: arco
- `Mago`: `STICK` como varita temporal
- `Clerigo`: `BLAZE_ROD` como staff temporal

Eso sirve para prototipo, pero es muy estrecho. La idea correcta es pasar de
"un item exacto" a **familias de armas** por clase.

## Familias de armas recomendadas

### Guerrero

Rol:

- frontal
- aguante
- dano sostenido

Familias:

- espada
- hacha
- escudo
- lanza futura
- espadon futuro

### Explorador

Rol:

- dano sostenido
- movilidad
- critico

Familias:

- arco
- ballesta
- espada corta temporal
- daga futura
- lanza corta futura

### Mago

Rol:

- mana
- burst
- control

Familias:

- varita
- staff
- libro futuro
- orbe futuro

### Clerigo

Rol:

- soporte
- aguante medio
- dano estable sin "dano magico" separado

Familias:

- staff
- escudo
- maza
- reliquia futura

## Filosofia de atributos

No queremos 20 stats opacas. Lo sano es:

- `Fuerza`
- `Inteligencia`
- `Agilidad`

Y que estas alimenten stats planas visibles:

- Vida
- Dano
- Velocidad de ataque
- Velocidad de movimiento
- Critico
- Penetracion
- Armadura
- Mana

Por ahora **no** separamos `dano magico`. Toda clase escala `dano` general y
luego sus habilidades usan esa base.

## Atributo principal por clase

| Clase | Primario | Secundario | Terciario |
| --- | --- | --- | --- |
| Guerrero | Fuerza | Agilidad | Inteligencia |
| Explorador | Agilidad | Fuerza | Inteligencia |
| Mago | Inteligencia | Agilidad | Fuerza |
| Clerigo | Inteligencia | Fuerza | Agilidad |

## Conversion por punto

### Fuerza

- `+1.6` vida
- `+0.18` dano
- `+0.03` penetracion

### Inteligencia

- `+0.16` dano
- `+4` mana

### Agilidad

- `+0.10` dano
- `+0.012` velocidad de ataque
- `+0.0008` velocidad de movimiento
- `+0.05` critico

## Ganancia por nivel

La gracia es que desde nivel 1 ya se sientan distintas, pero el impacto fuerte
aparezca en mid y late.

### Guerrero

- `+3.0` fuerza por nivel
- `+1.0` agilidad por nivel
- `+0.5` inteligencia por nivel

### Explorador

- `+3.0` agilidad por nivel
- `+1.2` fuerza por nivel
- `+0.8` inteligencia por nivel

### Mago

- `+3.0` inteligencia por nivel
- `+1.0` agilidad por nivel
- `+0.5` fuerza por nivel

### Clerigo

- `+3.0` inteligencia por nivel
- `+1.0` fuerza por nivel
- `+0.8` agilidad por nivel

## Bonos de arranque

Para que desde el inicio se sientan especiales:

### Guerrero

- `+8` vida
- `+1.0` dano
- `+1.0` armadura

### Explorador

- `+0.6` dano
- `+0.12` velocidad de ataque
- `+3%` critico
- `+0.01` velocidad

### Mago

- `+0.8` dano
- `+30` mana

### Clerigo

- `+4` vida
- `+0.6` dano
- `+24` mana
- `+0.5` armadura

## Soft caps sugeridos

Para que late no explote:

- critico: `35%`
- penetracion: `30%`
- velocidad: `0.18`
- velocidad de ataque extra: `1.6`

Eso no significa bloquear del todo, sino empezar a dar retorno decreciente.

## Recomendacion de implementacion

### Fase 1

- mostrar atributos primarios en `/estadisticas`
- mostrar stats planas derivadas
- aplicar ganancia por nivel automaticamente
- mantener armas permitidas por clase

### Fase 2

- equipamiento con requisitos por familia de arma
- mana real
- critico y penetracion en formulas de dano

### Fase 3

- Oraxen para familias reales:
  - dagas
  - varitas
  - staffs
  - reliquias
  - mazas

## Nota importante

Ahora mismo el plugin **ya** aumenta:

- vida
- dano
- velocidad

por nivel de clase, pero de forma generica. El siguiente paso sano no es seguir
subiendo solo esas tres, sino reemplazar esa escalada generica por este sistema
de atributos primarios + stats planas derivadas.
