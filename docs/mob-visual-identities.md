# Identidad visual de Elites y Bosses

Este documento define como haremos que los mobs especiales "se lean" visualmente
en combate sin depender desde el primer dia de modelos 3D complejos.

## Limite tecnico importante

Con `MythicMobs + Oraxen` podemos equipar mobs con items custom y darles una
identidad clara por:

- casco / capucha / mascara
- pechera / hombreras
- arma principal
- escudo / foco / reliquia
- color y silueta del equipo
- particulas, nombre, sonido y efectos

Eso nos permite que un `elite summoner` se vea como invocador y un `elite brutal`
se vea como una bestia de choque.

Pero una "skin completa" del cuerpo del zombie/esqueleto/creeper no se puede
cambiar por variante individual solo con textures vanilla del resource pack:
si cambiamos la textura base del zombie, cambiamos todos los zombies.

Para una transformacion completa por entidad, la fase correcta es:

- `MythicMobs` para IA, skills y spawns
- `Oraxen` para gear, drops y assets
- `ModelEngine` para modelos/skins unicas por mob

## Fase 1: Identidad inmediata con Oraxen

### Rare

Objetivo: que se sientan "tocados" por una afinidad, pero sin sobresaturar.

- 1 pieza visual distintiva
- arma acorde al rol
- color/acento leve

Ejemplos:

- `brutal`: arma mas pesada, herraje rojo
- `robusto`: casco ancho o peto reforzado
- `veloz`: capa ligera, filo fino, acento verde
- `congelante`: metal azulado
- `vampirico`: acero oscuro y rojo

### Elite

Objetivo: que se identifiquen rapido desde media distancia.

- set visual parcial o completo
- arma propia del rol
- silueta clara
- un motivo por afinidad

### MiniBoss / Boss

Objetivo: que parezcan lideres o monstruos de evento.

- set completo
- arma/escudo/staff unico
- corona, capa, cuernos, reliquia o estandarte
- telegraphs y sonido mas fuertes

## Roles visuales por comportamiento

### Summoner / Invocador

Lectura visual:

- capucha o mascara ritual
- staff o foco corrupto
- hombreras mas delgadas
- amuletos, cuerdas, huesos o placas runicas
- paleta: morado, hueso, verde enfermizo o rojo oscuro

Equipo sugerido:

- `summoner_hood`
- `summoner_mask`
- `summoner_staff`
- `summoner_talisman`

### Brutal

Lectura visual:

- silueta ancha
- peto pesado
- guanteletes, pinchos, placas gruesas
- hacha, mandoble o maza
- paleta: rojo hierro, negro, bronce oscuro

Equipo sugerido:

- `brutal_helm`
- `brutal_chest`
- `brutal_axe`

### Robusto

Lectura visual:

- armadura cerrada
- gran escudo
- placas gruesas y sobrias
- paleta: hierro, plata, gris piedra

Equipo sugerido:

- `bastion_helm`
- `bastion_shield`
- `bastion_plate`

### Veloz

Lectura visual:

- poca placa
- hombros ligeros
- dagas, sable o arco corto
- paleta: verde, cuero oscuro, acero delgado

Equipo sugerido:

- `swift_hood`
- `swift_blade`
- `swift_bow`

### Congelante

Lectura visual:

- cristales azul hielo
- filos finos
- metal claro
- staff o espada escarchada

Equipo sugerido:

- `frost_crown`
- `frost_blade`
- `frost_focus`

### Vampirico

Lectura visual:

- cuernos pequeños o mascara de colmillos
- acero negro con rojo
- reliquia o hoja ritual

Equipo sugerido:

- `blood_mask`
- `blood_blade`
- `blood_relic`

## Familias base por clase de mob

La afinidad no debe borrar la familia del mob. Debe montarse sobre ella.

### Zombie

- placas pesadas
- cuero podrido
- cadenas, remaches, casco roto

### Skeleton

- capucha, restos de uniforme, carcaj, hombreras ligeras

### Creeper

- los cambios deben ser muy sobrios
- collar ritual, placas de pecho o runas
- evitar hacerlo ilegible por el hitbox/animacion

### Piglin / Undead bruto

- metal en calor, bronce, hierro rojo, colmillos y placas toscas

## Recomendacion de implementacion

### Paso 1

Crear un paquete de gear Oraxen para elites:

- `summoner_hood`
- `summoner_staff`
- `brutal_helm`
- `brutal_axe`
- `bastion_shield`
- `swift_hood`
- `frost_focus`
- `blood_mask`

### Paso 2

Asignar gear por afinidad/rol en `ServidroRpg` o `MythicMobs`.

Regla sugerida:

- rare: 1 item visual
- elite: 2-4 items visuales
- miniboss: set completo y arma unica

### Paso 3

Cuando el pipeline este estable, pasar bosses clave a `ModelEngine`.

Los primeros candidatos:

- invocador de aldea
- king
- captain
- stormbeast

## Resultado buscado

Que al ver un mob el jugador pueda pensar de inmediato:

- "ese invoca"
- "ese tanquea"
- "ese pega durisimo"
- "ese congela"

sin tener que leer todo el nombre antes de reaccionar.
