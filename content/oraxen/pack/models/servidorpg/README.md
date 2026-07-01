Esta carpeta deberia quedarse vacia en el flujo actual, salvo excepciones reales de display.

Regla del proyecto:

- no abrir modelos JSON propios para armas o escudos mientras Excalibur o una
  silueta vanilla ya resuelvan la lectura
- preferir retexturizar antes que tocar geometria

Solo usar modelos JSON aqui si una necesidad real de gameplay o display no se
puede resolver con la base existente.

Excepcion actual:

- `*_staff.json`: mantienen la misma textura 2D del proyecto, pero con
  transformaciones de mano mas largas para que los staffs se lean como bastones
  y no como varitas cortas.
- `*_wand.json`, `*_cleric_relic.json`, `*_mage_focus.json`: aplican
  transformaciones suaves de display para que las familias magicas se lean con
  una silueta propia sin cambiar su textura base.
- `*_spellbook.json`: usan una textura de grimorio abierto y una postura de
  display pensada para que se lean mejor en la mano izquierda del mago.
