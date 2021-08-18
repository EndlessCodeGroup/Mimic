# mimic-bukkit

Bukkit plugin containing Mimic APIs implementations.

## Usage

### For Developers

If you are plugins developer, read [mimic-bukkit-api].
There you'll find a guide on how to use MimicAPI and how to create your own implementation of Mimic APIs.

### For Server Owners

If some plugin requires Mimic, just put it into `plugins/` folder.
No configuration needed.

#### Permissions

`mimic.admin` - Permission to use mimic commands

#### Commands

 Command                | Description                          
------------------------|--------------------------------------
 `/mimic help <search>` | Print help                           
 `/mimic info`          | Show mimic status                    
 `/mimic experience`    | Manage player's level and experience 
 `/mimic class`         | Check player's classes               
 `/mimic items`         | Deal with items                      

## Supported Implementations 

You can find code of all implementations [here](src/main/kotlin/impl).

### [Level Systems][BukkitLevelSystem.Provider]

- **[Minecraft][minecraft-exp]** _(Default)_
- **[SkillAPI]**
- **[BattleLevels]**
- **[MMOCore]**
- **[Heroes]**
- **[QuantumRPG]**

### [Class Systems][BukkitClassSystem.Provider]

- **Permissions-based** _(Default)_ - give permission `mimic.class.[class_name]` to assign class to player
- **[SkillAPI]**
- **[MMOCore]**
- **[Heroes]**
- **[QuantumRPG]**

### [Items Registries][BukkitItemsRegistry]

#### [MimicItemsRegistry] 

Items registry combining all others items registries.
It uses service ID as namespace for items IDs.\
For example: `acacia_boat -> minecraft:acacia_boat`.

> If you use item ID without a namespace, it will search over all registries.

#### [Minecraft][MinecraftItemsRegistry]

**ID Format:** `minecraft:[id]`\
**Payload:** [ItemMetaPayload]

#### [CustomItems]

**ID Format:** `customitems:[id]`\
**Payload:** *Not supported*

#### [MMOItems]

**ID Format:** `mmoitems:[id]`\
**Payload:** *Not supported*

#### [QuantumRPG]

**ID Format:** `quantumrpg:[type]/[id]`\
**Payload:** *Not supported*

[minecraft-exp]: https://minecraft.gamepedia.com/Experience
[skillapi]: https://www.spigotmc.org/resources/4824/
[battlelevels]: https://www.spigotmc.org/resources/2218/
[customitems]: https://www.spigotmc.org/resources/63848/
[mmocore]: https://www.spigotmc.org/resources/70575/
[mmoitems]: https://www.spigotmc.org/resources/39267/
[heroes]: https://www.spigotmc.org/resources/24734/
[quantumrpg]: https://www.spigotmc.org/resources/40007/

[mimic-bukkit-api]: ../mimic-bukkit-api

[BukkitLevelSystem.Provider]: ../mimic-bukkit-api/src/main/kotlin/level/BukkitLevelSystem.kt
[BukkitClassSystem.Provider]: ../mimic-bukkit-api/src/main/kotlin/classes/BukkitClassSystem.kt
[BukkitItemsRegistry]: ../mimic-bukkit-api/src/main/kotlin/items/BukkitItemsRegistry.kt
[MimicItemsRegistry]: src/main/kotlin/impl/mimic/MimicItemsRegistry.kt
[MinecraftItemsRegistry]: src/main/kotlin/impl/vanilla/MinecraftItemsRegistry.kt
[ItemMetaPayload]: src/main/kotlin/impl/vanilla/ItemMetaPayload.kt
