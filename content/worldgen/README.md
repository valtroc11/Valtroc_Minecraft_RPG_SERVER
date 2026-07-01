# Servidro Worldgen POC

Prueba de concepto para validar biomas custom antes de mezclar la generacion con Terralith/Tectonic.

Incluye:

- dimension `servidro:poc_helado`
- dimension registrada `servidro:cordilleras_heladas`
- bioma `servidro:cordilleras_heladas`
- bioma preparado `servidro:profundidades_heladas`
- terreno con noise vanilla overworld y bioma fijo
- remapeo runtime en overworld normal mediante `ServidroWorld`
- pasada runtime de menas heladas con afloraciones de estano/plata y vetas profundas de hierro/estano

Flujo recomendado:

1. Ejecutar `.\install-worldgen-poc.ps1`.
2. Arrancar el servidor.
3. Entrar como operador.
4. Ejecutar `/execute in servidro:poc_helado run tp <jugador> 0 160 0`.
5. Verificar con F3 que el bioma sea `servidro:cordilleras_heladas`.

Tambien se puede probar el registro limpio con:

```mcfunction
/execute in servidro:cordilleras_heladas run minecraft:locate biome servidro:cordilleras_heladas
```

Por defecto el instalador usa el `level-name` activo en `server.properties`. Tambien se puede instalar en un mundo especifico con `.\install-worldgen-poc.ps1 -WorldName preview_terralith_tectonic`.

La dimension POC sigue usando `cordilleras_heladas` como bioma fijo. Ademas, `ServidroWorld` puede remapear chunks de overworld de montana/frio al bioma `servidro:cordilleras_heladas`, con `servidro:profundidades_heladas` bajo tierra. Ese remapeo no altera bloques del terreno; solo cambia el bioma.
