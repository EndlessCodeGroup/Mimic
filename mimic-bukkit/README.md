# mimic-bukkit

Bukkit plugin containing Mimic APIs implementations.

## Usage

### For Developers

If you are plugins developer, read [mimic-bukkit-api].
There you'll find a guide on how to use MimicAPI and how to create your own implementation of Mimic APIs.

### For Server Owners

If some plugin requires Mimic, just put it into `plugins/` folder.
Mimic requires Paper 1.20.6+ and Java 21.

You can configure in `config.yml` what APIs implementations should be used, or use `/mimic config` command.

#### Permissions

`mimic.admin` - Permission to use mimic commands

#### Commands

| Command            | Description                          |
|--------------------|--------------------------------------|
| `/mimic`           | Show mimic status                    |
| `/mimic config`    | Show and change Mimic config         |
| `/mimic levels`    | Manage player's levels               |
| `/mimic classes`   | Manage player's classes              |
| `/mimic inventory` | Player's inventory info              |
| `/mimic items`     | Deal with items                      |
| `/mimic blocks`    | Deal with blocks                     |

Commands without arguments show info and list available subcommands: click a subcommand to use it, hover to see usage.

## Supported Implementations 

You can find code of all built-in implementations [here](src/main/kotlin/impl).

### [Level Systems][BukkitLevelSystem.Provider]

- **[Minecraft][minecraft-exp]** _(Default)_
- **[AuraSkills]** _(Levels API only)_ - every skill is a level track, the power level is the primary track
- **[mcMMO]** _(Levels API only)_ - every skill is a level track, the power level is the primary track
- **[Jobs Reborn]** _(Levels API only)_ - every job is a level track, the total level is the primary track
- **[ValhallaMMO]** _(Levels API only)_ - every skill is a level track, the Power skill is the primary track
- **[ProSkillAPI]**
- **[MMOCore]**
- **[Heroes]**
- **[Fabled]** _(Levels API only)_ - every class group is a level track
- **[ProRPGItems]**

### [Class Systems][BukkitClassSystem.Provider]

- **Permissions-based** _(Default, ClassSystem API only)_ - give permission `mimic.class.[class_name]` to assign class to player
- **[ProSkillAPI]**
- **[MMOCore]**
- **[Heroes]**
- **[Fabled]** _(Classes API only)_ - every class group is a slot
- **[ProRPGItems]**

### [Player Inventory Providers][BukkitPlayerInventory.Provider]

- **[Minecraft][minecraft-inv]** _(Default)_
- **[RPGInventory]**

### [Items Registries][BukkitItemsRegistry]

#### [MimicItemsRegistry] 

Items registry combining all others items registries.
It uses registry ID as a namespace for items IDs.\
For example: `acacia_boat -> minecraft:acacia_boat`.

> If you use item ID without a namespace or with the `mimic` namespace, it will search over all registries.

#### [Minecraft][MinecraftItemsRegistry]

**ID Format:** `minecraft:[id]`\
**Payload:** [Item components][item-components] in the format of `/give` command, for example `[damage=10,unbreakable={}]`.\
Alternatively, [ItemMetaPayload] in HOCON format with MiniMessage support for name and lore, for example `{name: "<gold>Sword", unbreakable: true}`.

#### [CraftEngine]

**ID Format:** `craftengine:[namespace]/[id]`\
**Payload:** *Not supported*

#### [CustomItems]

**ID Format:** `customitems:[id]`\
**Payload:** *Not supported*

#### [MMOItems]

**ID Format:** `mmoitems:[id]`\
**Payload:** *Not supported*

#### [ProRPGItems]

**ID Format:** `prorpgitems:[type]/[id]`\
**Payload:** *Not supported*

#### [RPGInventory]

**ID Format:** `rpginventory:[id]`\
**Payload:** *Not supported*

#### [ItemsAdder]

**ID Format:** `ia:[id]`\
**Payload:** *Not supported*

### [Block Registries][BukkitBlockRegistry] *(experimental)*

#### [MimicBlockRegistry]

Block registry combining all others block registries.
It uses registry ID as a namespace for block IDs, like items registry does.

#### [Minecraft][MinecraftBlockRegistry]

**ID Format:** `minecraft:[id]`\
**Payload:** Block state properties, for example `[facing=north]`

#### [CraftEngine]

**ID Format:** `craftengine:[namespace]/[id]`\
**Payload:** Block state properties, for example `[facing=north]`

[minecraft-exp]: https://minecraft.fandom.com/wiki/Experience
[minecraft-inv]: https://minecraft.fandom.com/wiki/Inventory
[item-components]: https://minecraft.wiki/w/Data_component_format
[proskillapi]: https://www.spigotmc.org/resources/91913/
[customitems]: https://www.spigotmc.org/resources/63848/
[mmocore]: https://www.spigotmc.org/resources/70575/
[mmoitems]: https://www.spigotmc.org/resources/39267/
[heroes]: https://www.spigotmc.org/resources/24734/
[prorpgitems]: https://www.spigotmc.org/resources/91386/
[rpginventory]: https://www.spigotmc.org/resources/12498/
[itemsadder]: https://www.spigotmc.org/resources/73355/
[craftengine]: https://modrinth.com/plugin/craftengine
[auraskills]: https://modrinth.com/plugin/auraskills
[mcmmo]: https://www.spigotmc.org/resources/64348/
[jobs reborn]: https://www.spigotmc.org/resources/4216/
[fabled]: https://www.spigotmc.org/resources/91913/
[valhallammo]: https://modrinth.com/plugin/valhallammo

[mimic-bukkit-api]: ../mimic-bukkit-api

[BukkitLevelSystem.Provider]: ../mimic-bukkit-api/src/main/kotlin/level/BukkitLevelSystem.kt
[BukkitClassSystem.Provider]: ../mimic-bukkit-api/src/main/kotlin/classes/BukkitClassSystem.kt
[BukkitPlayerInventory.Provider]: ../mimic-bukkit-api/src/main/kotlin/inventory/BukkitPlayerInventory.kt
[BukkitItemsRegistry]: ../mimic-bukkit-api/src/main/kotlin/items/BukkitItemsRegistry.kt
[BukkitBlockRegistry]: ../mimic-bukkit-api/src/main/kotlin/blocks/BukkitBlockRegistry.kt
[MimicBlockRegistry]: src/main/kotlin/impl/mimic/MimicBlockRegistry.kt
[MinecraftBlockRegistry]: src/main/kotlin/impl/vanilla/MinecraftBlockRegistry.kt
[MimicItemsRegistry]: src/main/kotlin/impl/mimic/MimicItemsRegistry.kt
[MinecraftItemsRegistry]: src/main/kotlin/impl/vanilla/MinecraftItemsRegistry.kt
[ItemMetaPayload]: src/main/kotlin/impl/vanilla/ItemMetaPayload.kt
