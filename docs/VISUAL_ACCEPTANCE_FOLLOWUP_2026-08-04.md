# Визуальная приёмка: обязательные доработки

## Статус

Этот документ фиксирует результат проверки принятой визуальной вертикали.
Thaumometer, сканирование, жезлы, HUD Vis, Goggles of Revealing, Aura Node,
Node Jar, Infusion, Runic Matrix, Alembic, Infuser, Eldritch Guardian, Obelisk
и Wisp считаются принятыми и не должны переписываться без новой конкретной
ошибки.

## Выполненные изменения

### Рецепты скипетров

Рецепт зарегистрирован как динамический
`arcane_sceptre_assembly`, а не как набор отдельных статических JSON-рецептов.
Он проверяет точную классическую схему `" TF"/" RT"/"T  "`: три одинаковых
cap, Salis Mundus в правом верхнем слоте и обычный rod в центре. Рецепт требует
`sceptre` и исследования выбранных cap/rod, рассчитывает `(cap × rod × 1.5)`
vis каждого примала с усечением и создаёт NBT-скипетр. Отдельный Forge GameTest
`dynamicSceptreRecipeUsesThreeCapsSalisAndTruncatedCost` проверяет загрузку
runtime-рецепта, схему, стоимость и сохранение выбранных компонентов.

### Research colors

Цветовое различие узлов Thaumonomicon восстановлено не подбором RGB, а точными
областями оригинального `gui_research.png`: `PRIMARY`, `ROUND`, `SECONDARY`,
`HIDDEN` и отдельный `SPECIAL` overlay. Доступное незавершённое исследование
использует исходный серый sinus pulse TC4 с пользовательским периодом
`800 ms` вместо оригинальных `600 ms`, завершённое —
белый tint, заблокированное — исходное затемнение.

Research Notes и Discovery теперь тонируют только overlay-маску цветом первого
аспекта из `legacy.research_aspects` — именно этот аспект оригинальный
`ResearchManager.createNote` записывал как `ResearchNoteData.color`. Тот же
resolver используется свитком на Research Table; при неизвестном/старом ID
сохраняется классический fallback `0x999999`.

### Arcane Workbench

`ArcaneWorkbenchBlockEntity` сохраняет `Crafting` и `Wand` через slot-indexed
container NBT, загружает их в те же контейнеры и после загрузки включает
обычную menu/block-entity synchronization. GameTest
`arcaneWorkbenchPreservesEverySlotAcrossNbtReload` проверяет крайние и
центральный слоты, количества, общую сумму без дублирования и NBT жезла.

### Furious Zombie возле dark aura node

Оригинал имеет два независимых пути. Eerie biome естественно спавнит обычного
Brainy Zombie с weight `32` и Giant Brainy Zombie с weight `8`; общий
overworld-путь обычного Brainy Zombie имеет weight `10`. Кроме того, сам dark
node раз в `50` тиков распространяет Eerie biome и в hard-node режиме делает
прямую попытку Giant Brainy Zombie.

Прямая попытка требует случайный gate `50%`, игрока в радиусе `24`, пригодную
позицию в spread `±5`, collision и обычные monster spawn rules. Локальная AABB
имеет радиус `10×6×10`. В точности сохранено оригинальное условие `count <= 3`:
попытка при трёх существующих зомби разрешена, поэтому успешный спавн может
довести локальное количество до четырёх. Опция `spawnAngryZombies` управляет
естественными biome spawn lists; прямой путь dark node, как и в `TileNode`,
зависит только от hard-node режима.

Сам факт отсутствия зомби в обычной игре не доказывает отсутствие механики:
освещение, поверхность, collision, общий mob cap, сложность, область Eerie и
случайный gate способны отклонить попытку. Естественный долгий survival-проход
остаётся отдельной runtime-проверкой.

## Валидация

- `MoundGuardianSpawnRulesTest` подтверждает интервал, случайный gate, радиус
  игрока и точную границу `<= 3`.
- `FuriousZombieBehaviorTest` подтверждает поведение Furious Zombie, но не
  полный natural spawn cycle возле узла.
- `ResearchColorResolverTest` подтверждает выбор первого аспекта и fallback.
- `WandAssemblyGameTests` и `VisualAcceptanceFollowupGameTests` содержат
  сфокусированные runtime-проверки скипетра и NBT round-trip верстака.
- Клиентская проверка рамок, цветов предметов/свитка и восстановленной сетки,
  а также survival-статистика спавна обязательны отдельно от компиляции.

### Фактический прогон 2026-08-04

- `compileJava`, `processResources`, `jar`, `reobfJar`, `sourcesJar` и
  `assemble` завершились; собран JAR `1.5.68`, embedded version проверена.
- Сфокусированный JVM-прогон: `7/7` (`ResearchColorResolverTest`,
  `MoundGuardianSpawnRulesTest`, `ArcaneWandAssemblyCostTest`,
  `ThaumonomiconResearchTintTest`).
- Полный JVM-прогон: `666/672`; шесть падений находятся в параллельных
  вертикалях pedestal renderer, essentia visual, Vis Exhaust icon, structure
  disassembly, harvest axes и wand asset layout.
- Forge GameTest server не дошёл до выполнения тестов: common registration
  загрузил client-only `HumanoidModel` из `ModItems.cultistArmor` на
  `DEDICATED_SERVER`. Поэтому runtime-тесты скипетра и Arcane Workbench,
  настоящий stop/start мира и server spawn cycle не заявляются пройденными.

Статус до исправления dedicated-server blocker и ручного запуска клиента:
`COMPILED_NOT_VISUALLY_VERIFIED`.
