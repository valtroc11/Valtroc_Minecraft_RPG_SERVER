# Oraxen en Servidro

Usaremos Oraxen como capa de:

- modelos y texturas
- items custom
- recipes custom
- bloques decorativos y props medievales

La logica de progresion sigue en `ServidroRpg`.

## Reparto de responsabilidades

### Oraxen

- apariencia del item
- id del item
- recipe
- bloques, muebles, glyphs, etc.

### ServidroRpg

- requisitos por clase y profesion
- nivel minimo para usar, equipar o craftear
- stats RPG
- drops escalados
- rarezas
- economia y coronas

## Estructura de trabajo

- `items/`: definiciones de items Oraxen
- `recipes/`: recipes custom
- `pack/textures/`: texturas png
- `pack/models/`: modelos json personalizados si hacen falta

## Flujo recomendado

1. Crear item base en Oraxen
2. Definir recipe en Oraxen
3. Dejar el item con un id estable
4. Conectar ese id a la progresion RPG
5. Probar en servidor

## Nota

Oraxen debe estar instalado en el servidor para que estas carpetas se sincronicen
contra `server/plugins/Oraxen/`.
