# mimic-bukkit

Bukkit plugin containing Mimic APIs implementations.

## Usage

### Permissions

`mimic.admin` - Permission to use mimic commands

### Commands

| Command                | Description                          |
|------------------------|--------------------------------------|
| `/mimic help <search>` | Print help                           |
| `/mimic level`         | Manage player's level and experience |
| `/mimic class`         | Check player's classes               |
| `/mimic items`         | Deal with items                      |

## Supported Implementations 

You can find code of all implementations [here](src/main/kotlin/impl).

#### [Level Systems][BukkitLevelSystem.Provider]

- [Minecraft][minecraft-exp] _(Default)_
- [SkillAPI]
- [BattleLevels]

#### [Class Systems][BukkitClassSystem.Provider]

- Permissions-based _(Default)_ - give permission `mimic.class.[class_name]` to assign class to player
- [SkillAPI]

#### [Items Registries][BukkitItemsRegistry]

[MimicItemsRegistry] - Items registry combining all others items registries.
It uses service ID as namespace for items IDs.
For example: `acacia_boat -> minecraft:acacia_boat`.
If you use item ID without namespace it will search over all registries.

| Registry                            | Namespace    |
|-------------------------------------|--------------|
| [Minecraft][MinecraftItemsRegistry] | `minecraft:` |

[minecraft-exp]: https://minecraft.gamepedia.com/Experience
[skillapi]: https://www.spigotmc.org/resources/4824/
[battlelevels]: https://www.spigotmc.org/resources/2218/

[BukkitLevelSystem.Provider]: ../mimic-bukkit-api/src/main/kotlin/BukkitLevelSystem.kt
[BukkitClassSystem.Provider]: ../mimic-bukkit-api/src/main/kotlin/BukkitClassSystem.kt
[BukkitItemsRegistry]: ../mimic-bukkit-api/src/main/kotlin/BukkitItemsRegistry.kt
[MimicItemsRegistry]: src/main/kotlin/impl/mimic/MimicItemsRegistry.kt
[MinecraftItemsRegistry]: src/main/kotlin/impl/vanilla/MinecraftItemsRegistry.kt
