# Servidro MX - servidor local de prototipo

Entorno local de Paper para probar el survival RPG medieval.

## Capa visual del proyecto

Servidro MX usa dos capas distintas:

- **Pack base del servidor (Excalibur desde Modrinth CDN)** para que el mundo se vea medieval
- **Pack del servidor (Oraxen)** para items, materiales y visuales RPG custom, montado encima de Excalibur

La guia recomendada para jugadores esta en:

- [docs/visual-setup.md](</C:/Users/Valtroc/Documents/Proyecto Servidro MX/docs/visual-setup.md>)

La capa medieval base que enviamos desde `server.properties` es:

- [Excalibur en Modrinth](https://modrinth.com/resourcepack/excal)

Y la capa custom del servidor se puede reenviar con:

```text
/oraxen pack send
```

## Requisitos

- Java 21
- PowerShell 5.1 o superior

## Instalacion

1. Ejecuta `.\setup-paper.ps1`.
2. Lee el EULA de Minecraft enlazado por el script.
3. Ejecuta `.\accept-eula.ps1` solamente si aceptas el EULA.
4. Inicia el servidor con `.\start-server.ps1`.
5. En Minecraft Java, entra a `localhost:25565`.

## Inicio con ngrok

Si quieres replicar el flujo del proyecto viejo y abrir el servidor con un tunel
TCP de ngrok:

1. Instala ngrok.
2. Configura tu token una vez:

```powershell
ngrok config add-authtoken TU_TOKEN
```

3. Enciende Minecraft mas ngrok en segundo plano:

```powershell
.\start-server-ngrok.ps1
```

4. Consulta el estado y la direccion publica:

```powershell
.\server-status.ps1
```

5. Apaga ambos procesos:

```powershell
.\stop-server-ngrok.ps1
```

El script intentara encontrar `ngrok.exe` en `PATH` o en rutas comunes de
Windows. Si no lo encuentra, te dira exactamente que falta.

### Variante Linux

Si el servidor real corre en Linux y ya tiene `ngrok`, puedes usar:

```bash
chmod +x start-server-ngrok.sh stop-server-ngrok.sh server-status.sh
./start-server-ngrok.sh
./server-status.sh
./stop-server-ngrok.sh
```

Variables utiles:

```bash
SERVER_PORT=25565
JAVA_BIN=java
NGROK_BIN=ngrok
```

La version Linux ahora:

- espera a que Paper llegue a `Done (...)!`
- levanta `ngrok` despues del arranque real del server
- guarda la IP publica en `server/ngrok-public.txt`
- muestra el ultimo `Done (...)!` en `./server-status.sh`

### Variante Linux con playit.gg

Este ya es el camino recomendado para el servidor 24/7:

```bash
chmod +x start-server-playit.sh stop-server-playit.sh server-status-playit.sh
./start-server-playit.sh
./server-status-playit.sh
./stop-server-playit.sh
```

Supone que:

- `playit` esta instalado en Linux
- el agente ya fue reclamado
- existe un tunel `Minecraft Java` apuntando a `127.0.0.1:25565`

El servicio `playit` corre con `systemd`, asi que estos scripts:

- arrancan o detienen Paper
- arrancan o detienen `playit`
- muestran si el puerto `25565` esta escuchando
- intentan leer el dominio publico desde los logs de `playit`

Para sincronizar cambios desde GitHub en la maquina Linux:

```bash
./update-linux-runtime.sh
```

Ese script hace:

- `git pull --ff-only`
- copia `custom-plugins/servidro-rpg/build/ServidroRpg.jar` a `server/plugins/ServidroRpg.jar`
- reaplica permisos de ejecucion a los scripts Linux

## Flujo Git recomendado

Queremos separar dos tipos de cosas:

- **Versionable en Git**:
  - codigo
  - scripts
  - datapacks y configuracion
  - estructuras personalizadas exportadas
  - snapshots de metricas
- **No versionable en Git**:
  - `server/world/`
  - logs del servidor
  - caches
  - runtime 24/7

El mundo vivo sigue fuera del repo. Si haces estructuras o mediciones que
quieres conservar, exportalas a:

- `content/structures/`
- `content/metrics/`

### Publicar cambios desde Linux

```bash
./publish-linux-changes.sh "mensaje del cambio"
```

Hace:

- `git pull --rebase --autostash origin main`
- commit de los cambios versionables
- `git push origin main`

### Sincronizar la maquina local desde GitHub

```powershell
.\sync-local-from-origin.ps1
```

### Publicar cambios desde local

```powershell
.\publish-local-changes.ps1 "mensaje del cambio"
```

### Orden recomendado de trabajo

Cuando el cambio nace o madura en la Linux:

1. Exportar estructuras o metricas al repo
2. `./publish-linux-changes.sh "..."`
3. En local: `.\sync-local-from-origin.ps1`
4. Seguir trabajando localmente
5. Publicar local cuando toque

Asi evitamos que Linux y local se pisen entre si.

## Plugins base

Con el servidor detenido, ejecuta:

```powershell
.\install-base-plugins.ps1
```

El script instala versiones compatibles con Paper `1.21.11` de:

- LuckPerms
- GriefPrevention
- PlaceholderAPI
- Chunky

CoreProtect se descarga manualmente desde su
[canal oficial](https://hangar.papermc.io/CORE/CoreProtect), ya que su descarga
actual se publica mediante un enlace externo.

El script de inicio usa el almacen de certificados de Windows. Esto permite que
Java funcione correctamente en equipos donde un antivirus o proxy local
inspecciona conexiones HTTPS.

## Economia y mercado

Con el servidor detenido, ejecuta:

```powershell
.\install-economy-plugins.ps1
```

El script instala:

- VaultUnlocked
- EzEconomy
- EzAuction

Configuracion provisional del prototipo:

- Moneda: coronas
- Almacenamiento local: SQLite
- Claims: 900 bloques gratuitos y ampliaciones a 5 coronas por bloque
- Auction House: mercado normal habilitado; subastas en vivo deshabilitadas

## Operacion del servidor

Con el servidor detenido, ejecuta:

```powershell
.\install-operations-plugins.ps1
```

El script instala:

- EssentialsX
- CoreProtect Community Edition

## Herramientas de mapa

Con el servidor detenido, ejecuta:

```powershell
.\install-map-tools.ps1
```

El script instala:

- WorldEdit 7.3.19
- WorldGuard 7.0.16

Estas versiones se fijan porque son compatibles con Java 21. Las versiones mas
recientes de estas herramientas para Minecraft 1.21.11 requieren Java 25.

## Plugin RPG propio

Con el servidor detenido, compila e instala:

```powershell
.\build-servidro-rpg.ps1
```

El primer prototipo incluye:

- Clases base rigidas durante la temporada
- Especializaciones desbloqueadas al nivel 10
- Cambio de especializacion por 250 coronas y reinicio de progreso
- Estado derribado durante 60 segundos con postura horizontal y cuenta regresiva visible
- Reanimacion asistida con clic derecho y 5 segundos de canalizacion
- Al caer se elimina la amenaza acumulada y los enemigos dejan de seleccionar al jugador

## Contenido y amenaza

Con el servidor detenido, instala MythicMobs:

```powershell
.\install-content-plugins.ps1
```

El plugin RPG propio incluye una tabla de amenaza inicial para enemigos de
Servidro: dano, curacion y provocacion del Guardian.

Habilidades de tanque disponibles en el prototipo:

- Guerrero, Guardian y Berserker: clic derecho con escudo provoca enemigos en un radio de 8 bloques.
- Paladin: clic derecho con escudo desafia al enemigo seleccionado.
- `/provocar`: comando equivalente para diagnostico y pruebas.
- Los enemigos provocados quedan resaltados en rojo durante 4 segundos.

## Oraxen

Queremos usar Oraxen para generar items, crafteos y resource pack del proyecto.

Base preparada en:

- [content/oraxen/items](</C:/Users/Valtroc/Documents/Proyecto Servidro MX/content/oraxen/items>)
- [content/oraxen/recipes](</C:/Users/Valtroc/Documents/Proyecto Servidro MX/content/oraxen/recipes>)
- [content/oraxen/pack](</C:/Users/Valtroc/Documents/Proyecto Servidro MX/content/oraxen/pack>)

Sincronizacion al servidor:

```powershell
.\sync-oraxen-content.ps1
```

En Linux:

```bash
chmod +x sync-oraxen-content.sh
./sync-oraxen-content.sh
```

Estos scripts ahora:

- crean `server/plugins/Oraxen/` si todavia no existe
- copian `items`, `recipes` y `pack`
- avisan si `Oraxen.jar` aun no esta instalado

Eso nos permite dejar el runtime preparado antes de tener el plugin.

La idea del proyecto sera:

- Oraxen genera items, modelos, recipes y pack
- ServidroRpg controla stats, rarezas, requisitos y progresion

Primeros ejemplos preparados:

- `apprentice_sword`
- `guardian_tower_shield`
- `berserker_war_axe`
- `iron_hunter_bow`
- `mage_focus`
- `cleric_relic`

Habilidades del Clerigo disponibles en el prototipo:

- Clic derecho con vara de blaze: Curacion menor, 3 corazones y cooldown de 8 segundos.
- Reanimacion asistida en 3 segundos en lugar de 5.
- Paladin y Druida tambien reciben la reanimacion acelerada.

Controles temporales por especializacion:

| Especializacion | Objeto y accion | Habilidad |
| --- | --- | --- |
| Guardian | Escudo, clic derecho | Provocacion en area |
| Berserker | Hacha de hierro, clic derecho | Torbellino |
| Cazador | Brujula, clic derecho | Marca del cazador |
| Picaro | Pluma, clic derecho | Desvanecerse |
| Piromante | Carga de fuego, clic derecho | Bola de fuego |
| Arcanista | Fragmento de amatista, clic derecho | Vinculo protector |
| Paladin | Escudo, clic derecho | Desafio individual |
| Druida | Semillas de trigo, clic derecho | Floracion |

Torbellino muestra un remolino breve de rafagas y barridos alrededor del
Berserker. El dano sigue aplicandose inmediatamente al activar la habilidad.

Cada habilidad activa incluye feedback visual y auditivo. Golpe Pesado dibuja
una trayectoria frontal de barridos y termina con un impacto metalico grave.

### Kit del Guerrero

| Ranura | Control vanilla | Guerrero y Guardian | Berserker |
| --- | --- | --- | --- |
| 1 | `F` mirando a un enemigo | Dash de hasta 7 bloques, cooldown 8 s | Igual |
| 2 | `Shift` + clic izquierdo con espada o hacha | Golpe Abrumador, cooldown 15 s | Torbellino, cooldown 15 s |
| 3 | Saltar y presionar `Shift` | Salto Desolador, radio 5 y cooldown 6 s | Igual |
| 4 | Pendiente | Definitiva por definir | Definitiva por definir |

Golpe Abrumador ralentiza durante 3.5 segundos. El Guerrero inflige `1.7x`
el dano del arma; el Guardian inflige `1.5x` y recupera vida equivalente al
`20%` del dano total. Torbellino inflige `1.7x` el dano de la mano principal
mas `0.7x` el dano del arma secundaria y recupera `0.7x` ese dano secundario.

Salto Desolador ralentiza enemigos cercanos durante 3.5 segundos. Si lo usa un
Guardian, tambien provoca a los enemigos alcanzados.

Las habilidades ofensivas pueden afectar jugadores, criaturas hostiles,
neutrales y pacificas. Los objetivos ralentizados muestran particulas azules
temporales y el dano efectivo aparece como un numero flotante rojo.

Durante el estado derribado se fuerza una postura horizontal hasta que el
jugador sea reanimado o termine la cuenta regresiva.

Las entidades vivas cercanas muestran una barra flotante de vida con segmentos
verdes, amarillos o rojos. El radio inicial es de 12 bloques. El dano de golpes
normales y habilidades aparece como texto rojo; los estados como ralentizacion
tambien muestran un aviso flotante.

Los jugadores derribados permanecen a oscuras, tumbados y resaltados en blanco
para que sus aliados puedan localizarlos y reanimarlos.

Las apariencias y controles definitivos se reemplazaran al integrar el resource
pack medieval.

Activas compartidas por clase base:

| Clase | Control temporal | Habilidad |
| --- | --- | --- |
| Guerrero | Agacharse y clic izquierdo con espada de hierro | Golpe contundente |
| Explorador | Agacharse y clic izquierdo con arco | Rodar |
| Mago | Agacharse y clic izquierdo con palo | Barrera |
| Clerigo | Agacharse y clic izquierdo con vara de blaze | Bendicion |

Herramientas administrativas para la alfa:

```text
/servidro kit
/servidro arena
/servidro resetclass <jugador>
/servidro tumbar <jugador>
/servidro levantar <jugador>
```

## TAB del servidor

La lista de jugadores muestra clase y nivel:

```text
[Guerrero] Valtroc 16
```

Los jugadores se agrupan por clase. El nivel ya forma parte persistente del
perfil RPG y se usara mas adelante para limitar el aprendizaje de habilidades.

## Profesiones

Las profesiones son independientes de la clase de combate. Cada jugador tiene
las cinco profesiones desde el inicio y decide cuales desarrollar con su tiempo:

- Minero
- Herrero
- Alquimista
- Agricultor
- Explorador

```text
/profesion lista
/profesion estado
/profesion minero
```

Durante las pruebas, un administrador puede ajustar el nivel desde consola:

```text
servidro profesionxp <jugador> <profesion> <cantidad>
```

## Desbloqueos por nivel

La matriz inicial es configurable en `plugins/ServidroRpg/config.yml`.

| Acción | Requisito provisional |
| --- | --- |
| Usar o equipar hierro | Clase nivel 5 |
| Usar o equipar oro | Clase nivel 8 |
| Usar o equipar diamante | Clase nivel 15 |
| Craftear equipo de hierro | Herrero nivel 5 |
| Craftear equipo de oro | Herrero nivel 10 |
| Craftear equipo de diamante | Herrero nivel 15 |
| Colocar bloques minerales de hierro | Minero nivel 5 |
| Colocar bloques minerales de oro | Minero nivel 10 |
| Colocar bloques minerales de diamante | Minero nivel 15 |
| Craftear consumibles avanzados | Alquimista nivel 8 |
| Craftear cultivos avanzados | Agricultor nivel 8 |
| Colocar utilidades de expedicion | Explorador nivel 8 |

Dentro del juego:

```text
/desbloqueos
```

## XP de profesiones

Cada oficio sube mediante acciones del mundo:

| Profesion | Fuente inicial de XP |
| --- | --- |
| Minero | Romper piedra natural, piedra negra y minerales; los minerales raros otorgan mas XP |
| Agricultor | Cosechar cultivos maduros |
| Alquimista | Completar una elaboracion en un soporte de pociones |
| Herrero | Craftear armas, herramientas, escudos y armaduras |
| Explorador | Pendiente: cofres personales, puntos de interes y expediciones |

La curva provisional requiere `100 x nivel actual` XP para alcanzar el siguiente
nivel. Cada accion muestra progreso en la barra de accion. Al subir de nivel se
muestra un titulo; cuando el nuevo nivel habilita contenido aparece tambien un
mensaje `Desbloqueado: ...`.

### Ruta inicial de Herreria

| Nivel | Etapa | Crafteos recomendados | XP base por pieza |
| ---: | --- | --- | ---: |
| 1-4 | Aprendiz | Herramientas de madera o piedra y equipo de entrenamiento | 8 |
| 5-9 | Oficial | Herramientas, armas, escudos y armaduras de hierro | 22 |
| 10-14 | Artesano | Equipo de oro para comercio y builds especializados | 28 |
| 15+ | Maestro | Equipo de diamante y preparacion para netherita | 42-65 |

Las pecheras y grebas otorgan `6 XP` adicionales por su mayor costo. Picos,
palas y azadas cuentan como piezas de herreria. Los bloques minables colocados
por jugadores no entregan XP al romperse nuevamente durante la sesion.

## Comercio entre jugadores

El comercio con aldeanos y comerciantes errantes esta deshabilitado. La
economia se concentra en intercambios entre jugadores y en `/auction`.

## Guia de entrada

Al conectarse, cada jugador recibe una guia breve con los comandos principales.
Tambien puede consultarla en cualquier momento:

```text
/guia
/informacion
/info
```

La guia enlaza clases, especializaciones, profesiones, desbloqueos y Auction
House. Mas adelante puede sustituirse visualmente por hologramas en el spawn.

## Economia de progresion

El prototipo entrega coronas mediante misiones diarias. Todavia no recompensa
directamente cada subida de nivel ni incluye misiones semanales. Antes de
ampliar la emision automatica de moneda se definiran limites y mas sumideros
economicos.

## Caps de temporada

| Progresion | Nivel maximo |
| --- | ---: |
| Clase del personaje | 100 |
| Especializacion | 20 |
| Cada profesion | 100 |

Los tiers superiores aun no estan cableados como items custom definitivos. La
hoja de ruta actual de tiers, progresion y spawns esta documentada en
`docs/tier-progression.md`.

## Misiones diarias

Cada jugador recibe tres encargos diarios rotativos: uno de Mineria, uno de
Herreria y uno de Agricultura. Las plantillas se filtran por nivel de profesion,
por lo que nunca exigen materiales o cultivos fuera de su marco de acceso.

```text
/misiones
/diarias
```

Al completar un encargo, la recompensa en coronas se deposita automaticamente.
Las misiones semanales se agregaran cuando estén conectados los bosses
rejugables y los dungeons de grupo.

Para simular XP desde consola:

```text
servidro profesionxp <jugador> <profesion> <xp>
```

## Cofres personales

Cada jugador saquea su propia tirada de botin y tiene cooldown independiente.
El cofre fisico permanece vacio y no se puede romper accidentalmente.

| Rareza | Cooldown | XP de Explorador |
| --- | ---: | ---: |
| Comun | 24 h | 20 |
| Raro | 48 h | 45 |
| Corrompido | 72 h | 80 |

Como operador, mira un cofre a menos de 6 bloques:

```text
/servidro cofre comun
/servidro cofre raro
/servidro cofre corrompido
/servidro cofre eliminar
```

El pack inicial de MythicMobs incluye:

- Bandido Corrompido
- Capitan Corrompido
- Refuerzos invocados por el capitan

El servidor se instala dentro de `server\`. Esa carpeta contiene mundos, logs,
configuracion local y plugins, por lo que no se versiona.

## Version fijada

- Minecraft: `1.21.11`
- Paper: ultimo build estable disponible para esa version al ejecutar el setup
- Java: `21`

## Previsualizacion del mundo

Antes de construir el spawn se puede crear un mapa separado con Terralith y
Tectonic. El mundo previo `world` no se elimina ni se modifica.

```powershell
.\setup-terralith-tectonic-preview.ps1
.\start-server.ps1
```

El instalador consulta Modrinth y descarga las versiones datapack compatibles
con Minecraft `1.21.11`. El mapa generado se guarda en
`server\preview_terralith_tectonic`.

Para regenerar el mundo principal con ambos datapacks:

```powershell
.\setup-terralith-tectonic-world.ps1
.\start-server.ps1
```

El instalador mueve los mundos anteriores a un respaldo fechado dentro de
`server\world-backups` antes de generar el nuevo mapa.

El mundo principal incluye tambien contenido de exploracion:

| Datapack | Funcion |
| --- | --- |
| Dungeons and Taverns | Mazmorras, tabernas y estructuras explorables |
| Structory | Ruinas y puntos de interes ambientales |
| Structory Towers | Torres repartidas por los biomas |
| DnT Enchant Disabler | Desactiva encantamientos propios de DnT para respetar la progresion RPG |

## Enemigos escalados

Los mobs hostiles y los neutrales capaces de volverse agresivos reciben nivel
y rareza segun el jugador mas cercano. El formato visible es:

```text
[LVL 12 Normal] Zombie
[LVL 15 Raro] Polar Bear
[LVL 18 Elite] Enderman
[LVL 20 MiniBoss] Zombie
```

Reglas actuales:

- Los `Normal` nunca sueltan equipo con estadisticas.
- Los `Normal` pueden soltar equipo base sin bonos.
- Los `Raro`, `Elite` y `MiniBoss` pueden soltar equipo del tier del jugador
  con bonos simples como vida, armadura o dano.
- Los neutrales configurados como `POLAR_BEAR`, `WOLF`, `BEE`, `ENDERMAN`,
  `PIGLIN`, `ZOMBIFIED_PIGLIN`, `IRON_GOLEM`, `LLAMA` y `TRADER_LLAMA`
  tambien entran al sistema.

La configuracion se ajusta desde `plugins/ServidroRpg/config.yml` en el bloque
`mob-scaling`.
