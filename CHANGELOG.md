## [Unreleased]

### Fixed

- Catch not only Exceptions from `ItemsRegistry` implementations but also Errors.
  `ItemsRegistry` will not crash other plugins.

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

[unreleased]: https://github.com/EndlessCodeGroup/Mimic/compare/v0.7.1...develop
[v0.7.1]: https://github.com/EndlessCodeGroup/Mimic/compare/v0.7...v0.7.1
[v0.7]: https://github.com/EndlessCodeGroup/Mimic/compare/v0.6.3...v0.7
[v0.6.3]: https://github.com/EndlessCodeGroup/Mimic/compare/v0.6.2...v0.6.3
[v0.6.2]: https://github.com/EndlessCodeGroup/Mimic/compare/v0.6.1...v0.6.2
[v0.6.1]: https://github.com/EndlessCodeGroup/Mimic/compare/v0.6...v0.6.1
[v0.6]: https://github.com/EndlessCodeGroup/Mimic/compare/v0.5...v0.6
