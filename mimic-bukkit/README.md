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
| `/mimic resources` | Manage player's mana and similar     |
| `/mimic inventory` | Player's inventory info              |
| `/mimic items`     | Give, find and compare items         |
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

### [Resource Providers][ResourceProvider]

- **Minecraft** _(Default)_ - health `minecraft:health`, food level `minecraft:food` and remaining air `minecraft:air`, no primary resource
- **[MMOCore]** - `mmocore:mana` (primary), `mmocore:stamina` and `mmocore:stellium`
- **[Heroes]** - `heroes:mana` (primary) and `heroes:stamina`
- **[AuraSkills]** - `auraskills:mana`
- **[Fabled]** - `fabled:mana`

### [Player Inventory Providers][BukkitPlayerInventory.Provider]

- **[Minecraft][minecraft-inv]** _(Default)_
- **[RPGInventory]**

### [Item Providers][ItemProvider]

Items of all providers are available via [Items], with the provider ID as a namespace of item IDs.
The namespace is required.
An item belongs to the provider with the highest priority that recognizes it, vanilla items belong to `minecraft`.

> The deprecated [ItemsRegistry][MimicItemsRegistry] API also accepts item ID without a namespace or with the `mimic` namespace
> and searches it over all providers. Plugins using this API accept MMOItems IDs without item type too, if only one type has the item.

Providers marked with _(deprecated API)_ are provided by the plugin itself via the deprecated [ItemsRegistry][BukkitItemsRegistry] API.

#### [Minecraft][MinecraftItemProvider]

**ID Format:** `minecraft:[id]`\
**Payload:** [Item components][item-components] in the format of `/give` command, for example `[damage=10,unbreakable={}]`.\
[ItemMetaPayload] in HOCON format is deprecated: it still works, but logs a warning.

#### [CraftEngine]

**ID Format:** `craftengine:[namespace]/[id]`\
**Payload:** *Not supported*

#### [CustomItems]

**ID Format:** `customitems:[id]`\
**Payload:** *Not supported*

#### [ExecutableItems]

**ID Format:** `executableitems:[id]`, ExecutableItems ID is lowercased and characters not allowed in IDs are replaced with `_`\
**Payload:** *Not supported*\
An item given to a player is built by ExecutableItems with this player as its owner.

#### [MMOItems]

**ID Format:** `mmoitems:[type]/[id]`, for example `mmoitems:sword/excalibur`\
**Payload:** *Not supported*\
An item given to a player is built by MMOItems for this player.

#### [ProRPGItems] _(deprecated API)_

**ID Format:** `prorpgitems:[type]/[id]`\
**Payload:** *Not supported*

#### [RPGInventory] _(deprecated API)_

**ID Format:** `rpginventory:[id]`\
**Payload:** *Not supported*

#### [ItemsAdder] _(deprecated API)_

**ID Format:** `ia:[id]`\
**Payload:** *Not supported*

### [Block Providers][BlockProvider]

Blocks of all providers are available via [Blocks], with the provider ID as a namespace of block IDs.
The namespace is required.
A block belongs to the provider with the highest priority that recognizes it, vanilla blocks belong to `minecraft`.

#### [Minecraft][MinecraftBlockProvider]

**ID Format:** `minecraft:[id]`\
**State:** Block state properties, for example `[facing=north]`

#### [CraftEngine]

**ID Format:** `craftengine:[namespace]/[id]`\
**State:** Block state properties, for example `[facing=north]`

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
[executableitems]: https://www.spigotmc.org/resources/77578/
[auraskills]: https://modrinth.com/plugin/auraskills
[mcmmo]: https://www.spigotmc.org/resources/64348/
[jobs reborn]: https://www.spigotmc.org/resources/4216/
[fabled]: https://www.spigotmc.org/resources/91913/
[valhallammo]: https://modrinth.com/plugin/valhallammo

[mimic-bukkit-api]: ../mimic-bukkit-api

[BukkitLevelSystem.Provider]: ../mimic-bukkit-api/src/main/kotlin/level/BukkitLevelSystem.kt
[BukkitClassSystem.Provider]: ../mimic-bukkit-api/src/main/kotlin/classes/BukkitClassSystem.kt
[ResourceProvider]: ../mimic-bukkit-api/src/main/kotlin/resources/ResourceProvider.kt
[BukkitPlayerInventory.Provider]: ../mimic-bukkit-api/src/main/kotlin/inventory/BukkitPlayerInventory.kt
[BukkitItemsRegistry]: ../mimic-bukkit-api/src/main/kotlin/items/BukkitItemsRegistry.kt
[BlockProvider]: ../mimic-bukkit-api/src/main/kotlin/blocks/BlockProvider.kt
[Blocks]: ../mimic-bukkit-api/src/main/kotlin/blocks/Blocks.kt
[MinecraftBlockProvider]: src/main/kotlin/impl/vanilla/MinecraftBlockProvider.kt
[MimicItemsRegistry]: src/main/kotlin/impl/mimic/MimicItemsRegistry.kt
[ItemProvider]: ../mimic-bukkit-api/src/main/kotlin/items/ItemProvider.kt
[Items]: ../mimic-bukkit-api/src/main/kotlin/items/Items.kt
[MinecraftItemProvider]: src/main/kotlin/impl/vanilla/MinecraftItemProvider.kt
[ItemMetaPayload]: src/main/kotlin/impl/vanilla/ItemMetaPayload.kt
