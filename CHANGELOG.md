## [Unreleased]

### API Changes

- Added vararg variants of ClassSystem methods

### Fixed

- Check plugin exists before registering built-in integrations
- Isolate `MimicItemsRegistry` from exceptions thrown from other items registries implementations.

### Housekeeping

- Update Kotlin to 1.6.10
- Update Gradle to 7.3.3

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

[unreleased]: https://github.com/EndlessCodeGroup/Mimic/compare/v0.6.1...develop
[v0.6.1]: https://github.com/EndlessCodeGroup/Mimic/compare/v0.6...v0.6.1
[v0.6]: https://github.com/EndlessCodeGroup/Mimic/compare/v0.5...v0.6
