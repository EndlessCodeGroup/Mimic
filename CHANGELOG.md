## [Unreleased]

### Items API

New Items API replaces `ItemsRegistry` ([#95](https://github.com/EndlessCodeGroup/Mimic/issues/95)).
See [Items](https://github.com/EndlessCodeGroup/Mimic/blob/develop/mimic-bukkit-api/README.md#items) for details and examples.

- `Mimic.getItems()` creates and identifies items, `Mimic.registerItemProvider()` makes a plugin's items available.
  Requires Mimic API level `MimicApiLevel.VERSION_0_11`. The API is stable.
- Item IDs are `Key`s with a required namespace: `minecraft:diamond`, `craftengine:default/palm_log`.
  There is no search over all providers in the new API.
- An item belongs to the provider with the highest priority recognizing it,
  so a custom item no longer matches the ID of its vanilla base ([#86](https://github.com/EndlessCodeGroup/Mimic/issues/86)).
- Items can be created for a player, MMOItems, CraftEngine, MythicMobs and ExecutableItems use it.
- A provider may override `ItemProvider.hasItem` to check an item faster, or if it can't list its items.
- The payload is a `String` in the format of the item's provider. An item is not created if its payload is invalid.
  A blank payload is the same as no payload.
- `ItemsRegistry` API is deprecated but keeps working, every items registry is available via Items API.
  `Mimic.getItemsRegistry()` shows items of all item providers and still accepts IDs without a namespace.
- Items registries' own `isSameItem` is used again ([#85](https://github.com/EndlessCodeGroup/Mimic/issues/85)).
- **Changed:** MMOItems IDs include the item type: `mmoitems:sword/excalibur` instead of `mmoitems:excalibur` ([#87](https://github.com/EndlessCodeGroup/Mimic/issues/87)).
  `ItemsRegistry` API still accepts IDs without the type if only one type has the item, but logs a warning.
- **Changed:** `ItemsRegistry.getItem` returns `null` if an item provider can't use the payload. Previously, the payload was ignored.
  A payload which isn't a `String`, like an `ItemMetaPayload` instance, is passed only to items registries.
- **Changed:** A built-in item provider is replaced only by an `ItemProvider` with the same ID, not by an `ItemsRegistry`.
- **Breaking:** `CraftEngineItemsRegistry` is removed, CraftEngine items are still available via Items API and `Mimic.getItemsRegistry()`.
- **Deprecated:** The HOCON payload (`ItemMetaPayload`) of `minecraft` items still works, but logs a warning.
  Use item components instead, for example `[damage=10,unbreakable={}]`.
- **Changed:** Empty stacks have no ID, and `minecraft:air` is not an item.
- Config option `disabled-items-registries` is renamed to `disabled-item-providers`, the old value is moved automatically.
- `/mimic items` requires item IDs with a namespace and gives items created for the target player.

### Blocks API

New Blocks API replaces the experimental `BlockRegistry` ([#96](https://github.com/EndlessCodeGroup/Mimic/issues/96)).
See [Blocks](https://github.com/EndlessCodeGroup/Mimic/blob/develop/mimic-bukkit-api/README.md#blocks) for details and examples.

- `Mimic.getBlocks()` identifies, sets and breaks blocks, `Mimic.registerBlockProvider()` makes a plugin's blocks available.
  Requires Mimic API level `MimicApiLevel.VERSION_0_11`. The API is stable.
- **Breaking:** `DropsConsumer` is moved to `mimic-bukkit-api` and is no longer generic: it consumes `ItemStack`s.
  The package is the same, it is no longer experimental.
- Block IDs are `Key`s with a required namespace: `minecraft:stone`, `craftengine:default/palm_log`.
  IDs without a namespace and the `mimic:` namespace are no longer searched over all providers.
- A provider may override `BlockProvider.hasBlock` to check a block faster, or if it can't list its blocks.
- **Breaking:** `BlockRegistry`, `BukkitBlockRegistry`, `Mimic.getBlockRegistry()`, `Mimic.registerBlockRegistry()`
  and `Mimic.getAllBlockRegistries()` are removed without replacement bridges.
- **Breaking:** A block is not set if its state is invalid. Previously, the default state was used.
  A blank state is the same as no state.
- Config option `disabled-block-registries` is renamed to `disabled-block-providers`, the old value is moved automatically.
- `/mimic blocks` requires block IDs with a namespace, `payload` argument of `/mimic blocks set` is renamed to `state`.

### Integrations

- New item and block providers: [ItemsAdder](https://www.spigotmc.org/resources/73355/) ([#49](https://github.com/EndlessCodeGroup/Mimic/issues/49)),
  [Oraxen](https://github.com/oraxen/oraxen) ([#51](https://github.com/EndlessCodeGroup/Mimic/issues/51)),
  [Nexo](https://nexomc.com/) ([#50](https://github.com/EndlessCodeGroup/Mimic/issues/50)).
- **Changed:** The `ia` items registry provided by ItemsAdder itself is not available via Items API, Mimic warns about it.
  Use `itemsadder:ruby_sword` instead of `ia:itemsadder:ruby_sword`.
- New item providers: [MythicMobs](https://modrinth.com/plugin/mythicmobs), including items of MythicCrucible ([#38](https://github.com/EndlessCodeGroup/Mimic/issues/38)),
  [ExecutableItems](https://www.spigotmc.org/resources/77578/) ([#111](https://github.com/EndlessCodeGroup/Mimic/issues/111)),
  [EcoItems](https://github.com/Auxilor/EcoItems) ([#53](https://github.com/EndlessCodeGroup/Mimic/issues/53)).

### Plugin changes

- Added support for Minecraft 26.2 and 26.3.

## [v0.10.0] (2026-11-15)

### Levels API

New Levels API replaces `LevelSystem` ([#80](https://github.com/EndlessCodeGroup/Mimic/issues/80)): a player can have many levels from several plugins at once.
See [Levels](https://github.com/EndlessCodeGroup/Mimic/blob/develop/mimic-bukkit-api/README.md#levels) for details and examples.

- `Mimic.getLevels()` reads and changes levels, `Mimic.registerLevelProvider()` makes a plugin's levels available.
  Requires Mimic API level `MimicApiLevel.VERSION_0_10`.
- `LevelSystem` API is deprecated but keeps working, every level system is available via Levels API as `<id>:level`.
- Config option `level-system` is renamed to `level-provider`, the old value is moved automatically.
- Command `/mimic xp` is replaced with `/mimic levels`, which can also set, give and take levels.

### Classes API

New Classes API replaces `ClassSystem` ([#81](https://github.com/EndlessCodeGroup/Mimic/issues/81)): classes in slots like class and race, subclasses and class levels.
See [Classes](https://github.com/EndlessCodeGroup/Mimic/blob/develop/mimic-bukkit-api/README.md#classes) for details and examples.

- `Mimic.getClasses()` reads and changes classes, `Mimic.registerClassProvider()` makes a plugin's classes available.
  Requires Mimic API level `MimicApiLevel.VERSION_0_10`.
- `ClassSystem` API is deprecated but keeps working, every class system is available via Classes API as `<id>:class`.
  The built-in `permissions` is the exception: it can't list its classes and a player may have several of them, while a slot holds one.
  `PermissionsClassSystem` is deprecated, it stays the default of `ClassSystem` API.
- Config option `class-system` is renamed to `class-provider`, the old value is moved automatically.
- Command `/mimic class` is replaced with `/mimic classes`, which can also set classes.

### Resources API

New Resources API for mana, stamina and similar resources ([#29](https://github.com/EndlessCodeGroup/Mimic/issues/29)).
See [Resources](https://github.com/EndlessCodeGroup/Mimic/blob/develop/mimic-bukkit-api/README.md#resources) for details and examples.

- `Mimic.getResources()` reads, takes and gives resources, `Mimic.registerResourceProvider()` makes a plugin's resources available.
  Requires Mimic API level `MimicApiLevel.VERSION_0_10`.
- `take` takes the whole amount or nothing and never sends messages to the player.
- New config option `resource-provider` selects the plugin providing the player's mana.
- New command `/mimic resources` shows resources and can take, give and set them.
- Built-in resources: vanilla health, food and air, MMOCore, Heroes, AuraSkills and Fabled.

### Integrations

- Added [AuraSkills](https://modrinth.com/plugin/auraskills) levels provider ([#22](https://github.com/EndlessCodeGroup/Mimic/issues/22)).
- Added [mcMMO](https://www.spigotmc.org/resources/64348/) levels provider ([#55](https://github.com/EndlessCodeGroup/Mimic/issues/55)).
- Added [Jobs Reborn](https://www.spigotmc.org/resources/4216/) levels provider ([#57](https://github.com/EndlessCodeGroup/Mimic/issues/57)).
- Added [Fabled](https://www.spigotmc.org/resources/91913/) levels and classes providers ([#48](https://github.com/EndlessCodeGroup/Mimic/issues/48)): class groups are slots and level tracks.
- Added [ValhallaMMO](https://modrinth.com/plugin/valhallammo) levels provider ([#99](https://github.com/EndlessCodeGroup/Mimic/issues/99)).
- MMOCore and Heroes support Levels and Classes API natively: class and profession levels, class slots and subclasses.
  This fixes class IDs in MMOCore ([#78](https://github.com/EndlessCodeGroup/Mimic/issues/78)) and reporting class history as classes in Heroes ([#79](https://github.com/EndlessCodeGroup/Mimic/issues/79)).
- Fixed MMOCore experience to reach a level in the deprecated `LevelSystem` API ([#76](https://github.com/EndlessCodeGroup/Mimic/issues/76)).
- Removed integrations with abandoned plugins: SkillAPI ([#58](https://github.com/EndlessCodeGroup/Mimic/issues/58)), QuantumRPG ([#59](https://github.com/EndlessCodeGroup/Mimic/issues/59)) and BattleLevels ([#77](https://github.com/EndlessCodeGroup/Mimic/issues/77)).
  If one of them is selected in the config, Mimic warns and uses other available plugins.

### Plugin changes

- `/mimic levels`, `/mimic classes` and `/mimic inventory` have a `[change]` button opening `/mimic config`.

### Housekeeping

- Mimic is now published on Hangar in addition to Modrinth.
- License changed to MIT. Previously, the API modules were licensed under LGPL-3.0 and the plugin under GPL-3.0.
- `Mimic.getItemsRegistry()` and `Mimic.getBlockRegistry()` became Kotlin properties `itemsRegistry` and `blockRegistry`.
  Nothing changes for Java, Kotlin code should use the properties.

## [v0.9.0] (2026-11-12)

### Paper-first

Keeping the plugin compatible both with Paper and Spigot consumes a lot of time.
Now, Mimic is Paper-first as it is the most popular platform.
This means that compatibility with Spigot is not guaranteed.

To reduce the maintenance burden, support for versions older than 1.20.6 has been dropped.
Java 21 is required.

### BlockRegistry API

This version introduces new *experimental* BlockRegistry API.
It can be used to get IDs of placed blocks and block items, get items placing blocks, set blocks, and break blocks passing drops to a custom consumer.
Block IDs from all registries are available via `Mimic.getBlockRegistry()`, namespaced with the registry ID.
Block IDs are [resource locations](https://minecraft.wiki/w/Resource_location): `minecraft:stone`, `craftengine:default/palm_log`.
ID without namespace or with the reserved `mimic` namespace is searched over all registries in order of priority.

```java
// Register a block registry
Mimic.getInstance().registerBlockRegistry(new MyBlockRegistry(), MimicApiLevel.CURRENT, plugin, ServicePriority.Normal);

// Set a block
BukkitBlockRegistry blockRegistry = Mimic.getInstance().getBlockRegistry();
blockRegistry.setBlock(location.getBlock(), "minecraft:stone_stairs", "[facing=north]");

// Break a block with a pickaxe, collecting drops into a chest inventory
blockRegistry.breakBlock(block, pickaxe, drops -> chestInventory.addItem(drops.toArray(new ItemStack[0])).values());
```

Added commands to debug block registries:

- `/mimic blocks info` - show information about block registries
- `/mimic blocks give [target] [block] (amount)` - give the item placing the given block
- `/mimic blocks set [location] [block] (payload)` - set block at the given location, replacing the existing one
- `/mimic blocks break (location)` - break block at the given location, or the targeted block, with the item in hand
- `/mimic blocks compare (block)` - compare targeted block with the given block, or with the block placed by item in hand
- `/mimic blocks find [block]` - check that block with the given ID exists

### Improved commands

Now it is possible to change Mimic config using commands in two ways:

1. Using interactive config `/mimic config`.
   Every option in the output is interactive, so you can change it just by mouse click.
2. Change options by commands:
   - `/mimic config level-system [value]` - set the specified level system as preferred for Mimic
   - `/mimic config class-system [value]` - set the specified class system as preferred for Mimic
   - `/mimic config inventory-provider [value]` - set the specified inventory provider as preferred for Mimic
   - `/mimic config disabled-items-registries add [item-registry]` - disable the specified item registry for Mimic
   - `/mimic config disabled-items-registries remove [item-registry]` - enable the specified item registry for Mimic
   - `/mimic config disabled-block-registries add [block-registry]` - disable the specified block registry for Mimic
   - `/mimic config disabled-block-registries remove [block-registry]` - enable the specified block registry for Mimic
   - `/mimic config reset [option]` - reset the specified option to its default value

Commands are now easier to use in general:
- `/mimic info` replaced with `/mimic`
- `/mimic class has` replaced with `/mimic class check` with simpler syntax
- `/mimic xp info [player]` and `/mimic class check [player]` send the result to the command sender instead of the target player.
- `/mimic items give` has the same arguments order as vanilla `/give`: `/mimic items give [target] [item] (amount) (payload)`.
- `/mimic xp set|give|take` replaced with a single value: `/mimic xp [value] (player)`
  Value is `[+|-]<amount>[L|P|T]`: `+` gives, `-` takes, no sign sets; `L` is levels, `P` is points (default), `T` is total exp.
  For example, `/mimic xp +5L` gives five levels and `/mimic xp 100` sets 100 exp points.
- `/mimic xp has` removed, use `/mimic xp info` instead
- `/mimic items id` removed, `/mimic items` shows ID of the item in hand.
- Aliases `/mimic experience` and `/mimic inv` removed, use `/mimic xp` and `/mimic inventory` instead.
- Subcommand `info` is optional: `/mimic items`, `/mimic blocks`, `/mimic xp`, `/mimic inventory` and `/mimic class` show info.
  Info output lists available subcommands, click to use them, hover to see usage.
- Clearer command output: colored answers, errors for failed actions, click-to-copy IDs.
- Item and block IDs are completed step by step: namespace first (`craftengine:`), then folder (`craftengine:default/`), then ID.

### Item and block IDs format

Item and block IDs now share one format (#66): a [resource location](https://minecraft.wiki/w/Resource_location) with the registry ID as namespace,
like `minecraft:acacia_boat` or `craftengine:default/palm_log`.

- Invalid IDs are rejected without querying registries.
  Invalid IDs returned by registries are ignored with a warning, for example IDs with uppercase letters, including the registry ID.
- Items: the `mimic` namespace searches over all registries, like IDs without namespace.
- Registries map plugin IDs to Mimic IDs themselves: `itemsadder:ruby_sword` → `itemsadder/ruby_sword`.
- **Deprecated:** item IDs with uppercase letters (`minecraft:DIAMOND_SWORD`) are lowercased,
  and IDs with more than one `:` (`ia:itemsadder:ruby_sword`) are passed as is. Both log a warning.

### API changes

- Added tag `@since 0.8.0` to all APIs added in 0.8.0
- Make `BukkitPlayerInventory.collectEquippedItems` protected. It was public by an accident.
- Fixed `BukkitPlayerInventory.collectStoredItems` excluding the first stack equal to the item in the main hand instead of the held slot itself.
- Default implementation of `ClassSystem.hasAnyClass()` changed.
  Now it checks if `getClasses()` returns a non-empty list instead of checking `getPrimaryClass() != null`.

### Plugin changes

- Added CraftEngine integration: items registry and block registry (#52).
  CraftEngine IDs `namespace:path` become `craftengine:namespace/path`, for example `craftengine:default/palm_log`.
- `minecraft` items registry accepts [item components](https://minecraft.wiki/w/Data_component_format) as a payload in the format of the `/give` command:
  `/mimic items give Steve minecraft:diamond_sword 1 [damage=10,unbreakable={}]`.
  The HOCON payload (`ItemMetaPayload`) is still supported as an alternative, with MiniMessage formatting for name and lore.
- MMOCore integration requires MMOCore 1.13 or newer
- Class system `permissions` takes into account only explicitly set `mimic.class.*` permissions.
  Previously, operators had all classes.
- Implementations are stored in Mimic instead of Bukkit's `ServicesManager` (#70).
  Each ID now belongs to a single implementation:
  - An implementation registered by another plugin replaces the Mimic built-in integration with the same ID.
  - If two plugins register the same ID, the one with higher priority is used.
    On equal priorities, the first registered is used.
  - If a plugin registers the same ID again, the new implementation replaces the previous one.
  - IDs of default implementations (`mimic`, `minecraft`, `permissions`) are reserved.
  - Default implementations are always used last, regardless of priority.
    So a custom block registry registered with `Lowest` priority still recognizes its blocks before the vanilla registry.

  Implementations with different IDs and equal priority are ordered by registration: the first registered is used by default.
  Previously, the implementation with the lowest priority was selected by ID, and the order of equal priorities was undefined.
- `mimic` is not listed in `Mimic.getAllItemsRegistries()` and `Mimic.getAllBlockRegistries()` anymore,
  and is not an option of `disabled-items-registries` and `disabled-block-registries` in config.
- **Breaking change!** Implementations can't be loaded from `ServicesManager` anymore, use `Mimic.getInstance()` instead.
  Implementations registered directly in `ServicesManager` are still moved to Mimic.
- **Breaking change!** `MimicItemsRegistry` constructor is internal now, use `Mimic.getItemsRegistry()` instead.

### Housekeeping

- Update required Java 1.8 → 21
- Update Kotlin 1.6.20 → 2.4.21, preserving API and Language level 2.2
- Replace ACF with Paper's Brigadier command API
- Update Gradle 7.4.2 → 9.8.2
- Update dependencies
- Migrate to version catalogs
- Publish to Maven Central via Central Portal

## [v0.8.0] (2022-04-18)

### PlayerInventory API

This version introduces new *experimental* PlayerInventory API.
For now, it can be used only to get player's inventory content and equipped items.
It can be useful for integration with plugins providing custom player inventory.

```java
// Get player's inventory
Mimic mimic = Mimic.getInstance();
BukkitPlayerInventory inventory = mimic.getPlayerInventory(player);

// Get items equipped on the player
List<ItemStack> equippedItems = inventory.getEquippedItems();
// Get not equipped items stored in the inventory
List<ItemStack> storedItems = inventory.getStoredItems();
// Get both equipped and not equipped items
List<ItemStack> allItems = inventory.getItems();
```

> This API is **experimental**, so it can be changed further.
> I'll do my best to save backward compatibility but in some cases it is not possible.
> It also means I'm open for ideas how to improve this API to make it more useful.
> Please, [write me in Discord](https://discord.gg/HUtb4v8NFy) or [file an issue](https://github.com/EndlessCodeGroup/Mimic/issues) if you have any ideas.

### API changes

- Make implementations ID validation less strict.
  Since now, it may contain more special symbols: `._-`
- Make `ServicesManager` extensions inaccessible from Java

### Plugin changes

- Added command `/mimic inventory info` to get info about current player inventory provider

### Fixed

- Catch not only Exceptions from `ItemsRegistry` implementations but also Errors.
  `ItemsRegistry` will not crash other plugins.

### Housekeeping

- Versioning strategy changed.
  Now version always includes `PATCH` version according to SemVer. 
- Update Kotlin to 1.6.20
- Update bStats to 3.0.0
- Update ACF to 0.5.1-SNAPSHOT
- Update Gradle to 7.4.2
- Update Dokka to 1.6.20
- Update binary-compatibility-validator to 0.9.0
- Update gradle-download-task to 5.0.5

## [v0.7.1] (2022-02-19)

### Fixed

- Compatibility with Minecraft 1.13 - 1.16.5
- FileNotFoundException on config initialization

## [v0.7] (2022-02-06)

### New implementations registration API

Registration via `ServiceManager` is deprecated because it was error-prone and not intuitive.
Since now, you should use class `Mimic` to register or get APIs implementations.

```java
Mimic mimic = Mimic.getInstance();

// Register ItemsRegistry implementation
mimic.registerItemsRegistry(new MyItemsRegistry(), plugin);

// Get items registry implementation
BukkitItemsRegistry registry = mimic.getItemsRegistry();
```

New mechanism allows maintaining better backward compatibility for APIs implementations. 
It also allows users to select preferred APIs implementation via config.

### API Changes

- Added default implementations for `MimicService` methods:
  - `isEnabled()` returns `true` by default
  - `getId()` returns lowercase plugin name by default
- **Breaking change!** ID should contain only lowercase Latin letters and digits (a-z, 0-9).
- **Breaking change!** `BukkitClassSystem.Provider` and `BukkitLevelSystem.Provider` converted to interface instead of abstract classes.
  If you want to use ID different from plugin name, you should override `getId()` method.
- `BukkitClassSystem.Provider` and `BukkitLevelSystem.Provider` are functional interfaces since now.

### Plugin changes

- Added config file allowing to specify preferred APIs implementations.
- Fixed errors in case when commands used from console.

## [v0.6.3] (2022-01-07)

### Fixed

- Update MMOItems and MMOCore to the latest version

## [v0.6.2] (2021-12-29)

### API Changes

- Added vararg variants of ClassSystem methods

### Fixed

- Check plugin exists before registering built-in integrations
- Isolate `MimicItemsRegistry` from exceptions thrown from other items registries implementations.

### Housekeeping

- Update Kotlin to 1.6.10
- Update Gradle to 7.3.3
- Migrate tests to MockK and Kotest assertions

## [v0.6.1] (2021-08-23)

### Fixed

- `MimicApiLevel.checkApiLevel` now works correctly

## [v0.6] (2021-08-19)

### API

- Add `MimicApiLevel` class to check current running Mimic API version:
  ```kotlin
  // Specify here the version required for APIs you use.
  if (!MimicApiLevel.checkApiLevel(MimicApiLevel.VERSION_0_6)) {
      println("At least Mimic 0.6 is required. Please download it from {link here}")
  }
  ```
- Add optional payload to `ItemsRegistry.getItem`. It may be used to customize item.

### Bukkit Plugin

- More detailed output of command `/mimic items info`:
  ```
  Items Service: mimic
  Known IDs amount: 1161
    rpginventory: 4
    quantumrpg: 55
    mmoitems: 126
    minecraft: 976
  ```
- Improve integration with Heroes class system (#14)
- Add statistics about used items registries
- Add payload support to `MinecraftItemsRegistry`.

### Housekeeping

- Add API binary compatibility validation
- Publish docs to GitHub Pages
- Update Kotlin to 1.5.21
- Update bukkit-gradle to 0.10.0
- Update Gradle to 7.2

[unreleased]: https://github.com/EndlessCodeGroup/Mimic/compare/v0.10.0...develop
[v0.10.0]: https://github.com/EndlessCodeGroup/Mimic/compare/v0.9.0...v0.10.0
[v0.9.0]: https://github.com/EndlessCodeGroup/Mimic/compare/v0.8.0...v0.9.0
[v0.8.0]: https://github.com/EndlessCodeGroup/Mimic/compare/v0.7.1...v0.8.0
[v0.7.1]: https://github.com/EndlessCodeGroup/Mimic/compare/v0.7...v0.7.1
[v0.7]: https://github.com/EndlessCodeGroup/Mimic/compare/v0.6.3...v0.7
[v0.6.3]: https://github.com/EndlessCodeGroup/Mimic/compare/v0.6.2...v0.6.3
[v0.6.2]: https://github.com/EndlessCodeGroup/Mimic/compare/v0.6.1...v0.6.2
[v0.6.1]: https://github.com/EndlessCodeGroup/Mimic/compare/v0.6...v0.6.1
[v0.6]: https://github.com/EndlessCodeGroup/Mimic/compare/v0.5...v0.6
