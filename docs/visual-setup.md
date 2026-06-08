# Configuracion visual recomendada

Servidro MX usa dos capas visuales distintas:

1. **Pack del servidor (Oraxen)**  
   Este pack contiene los items, materiales, escudos, varitas, staffs y
   visuales RPG propios del servidor.

2. **Pack recomendado del jugador (Excalibur)**  
   Este pack mejora la apariencia del mundo vanilla con una estetica medieval.

## Objetivo

La idea es simple:

- **Oraxen** da identidad visual al RPG del servidor.
- **Excalibur** hace que aldeas, biomas, bloques y equipo vanilla se sientan
  medievales.

## Recomendacion para jugadores

### Paso 1: instalar Excalibur

Descarga Excalibur desde:

- [Excalibur en Modrinth](https://modrinth.com/resourcepack/excal)

Instalalo como cualquier resource pack normal de Minecraft Java.

### Paso 2: entrar al servidor y aceptar el pack del servidor

Al entrar a Servidro MX, Oraxen deberia enviar automaticamente el pack del
servidor.

Si no aparece el prompt, pide a un admin o ejecuta:

```text
/oraxen pack send
```

### Paso 3: usar ambas capas

La experiencia visual que buscamos es:

- **Excalibur** como base del mundo
- **Pack del servidor** para contenido RPG custom

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

- el pack del servidor si cargo
- pero no tienes Excalibur instalado o activo

### Veo Excalibur pero no los items RPG

Eso suele significar que el pack del servidor no cargo correctamente. Reintenta:

```text
/oraxen pack send
```

## Nota de licencias

Excalibur se trata como **pack recomendado para jugadores**, no como pack
redistribuido dentro del servidor. El contenido propio del servidor vive en el
pack generado por Oraxen.
