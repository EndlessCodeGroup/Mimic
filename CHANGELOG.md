## [Unreleased]

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
// Get both equpped and not equipped items
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

// Get items registry imlpementation
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

[unreleased]: https://github.com/EndlessCodeGroup/Mimic/compare/v0.8.0...develop
[v0.8.0]: https://github.com/EndlessCodeGroup/Mimic/compare/v0.7.1...v0.8.0
[v0.7.1]: https://github.com/EndlessCodeGroup/Mimic/compare/v0.7...v0.7.1
[v0.7]: https://github.com/EndlessCodeGroup/Mimic/compare/v0.6.3...v0.7
[v0.6.3]: https://github.com/EndlessCodeGroup/Mimic/compare/v0.6.2...v0.6.3
[v0.6.2]: https://github.com/EndlessCodeGroup/Mimic/compare/v0.6.1...v0.6.2
[v0.6.1]: https://github.com/EndlessCodeGroup/Mimic/compare/v0.6...v0.6.1
[v0.6]: https://github.com/EndlessCodeGroup/Mimic/compare/v0.5...v0.6
