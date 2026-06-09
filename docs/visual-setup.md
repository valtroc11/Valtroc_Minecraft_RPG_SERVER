# Configuracion visual recomendada

Servidro MX usa dos capas visuales distintas:

1. **Pack base del servidor (Excalibur)**  
   Paper lo envia desde su URL oficial en Modrinth. Se usa para medievalizar
   el mundo vanilla.

2. **Pack del servidor (Oraxen)**  
   Este pack contiene los items, materiales, escudos, varitas, staffs y
   visuales RPG propios del servidor.

## Objetivo

La idea es simple:

- **Excalibur** da la base medieval del mundo.
- **Oraxen** se monta encima para dar identidad visual al RPG del servidor.
- **Excalibur** hace que aldeas, biomas, bloques y equipo vanilla se sientan
  medievales.

## Recomendacion para jugadores

### Paso 1: entrar y aceptar Excalibur

Al entrar al servidor, Minecraft deberia pedirte el pack medieval base.

La fuente oficial del pack es:

- [Excalibur en Modrinth](https://modrinth.com/resourcepack/excal)

### Paso 2: dejar que Oraxen cargue la capa RPG

Despues de entrar, Oraxen deberia enviar automaticamente el pack RPG del
servidor como capa adicional.

Si no aparece el prompt, pide a un admin o ejecuta:

```text
/oraxen pack send
```

### Paso 3: jugar con ambas capas

La experiencia visual que buscamos es:

- **Excalibur** como base del mundo
- **Oraxen** para contenido RPG custom

## Que cubre cada capa

### Excalibur

- bloques vanilla
- aldeas
- armas y armaduras vanilla
- entorno medieval general

### Oraxen del servidor

- materiales custom
- menas custom
- escudos por tier
- varitas y staffs
- visuales de elites y bosses
- recipes e items propios del RPG

## Solucion de problemas

### No veo el pack medieval base

Reintenta entrando al servidor. Si el juego te deja pasar sin el prompt:

- revisa que no hayas rechazado el pack base
- borra el historial del server en la lista de multijugador y vuelve a entrar

### No veo los items custom

Prueba:

```text
/oraxen pack send
```

Si sigue igual:

- reconecta al servidor
- revisa que no hayas rechazado el pack del servidor

### Veo el mundo normal pero los items RPG si cambian

Eso significa que:

- la capa de Oraxen si cargo
- pero Excalibur no fue aceptado o no se descargo bien

### Veo Excalibur pero no los items RPG

Eso suele significar que el pack del servidor no cargo correctamente. Reintenta:

```text
/oraxen pack send
```

## Nota de licencias

Excalibur se descarga desde su URL oficial en Modrinth. El contenido propio del
servidor vive en el pack generado por Oraxen y se monta como capa adicional.
