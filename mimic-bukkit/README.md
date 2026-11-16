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

| Plugin                             | ID format                      | Payload          | Notes                                            |
|------------------------------------|--------------------------------|------------------|--------------------------------------------------|
| [Minecraft][MinecraftItemProvider] | `minecraft:[id]`               | Item components¹ |                                                  |
| [CraftEngine]                      | `craftengine:[namespace]/[id]` | -                |                                                  |
| [CustomItems]                      | `customitems:[id]`             | -                |                                                  |
| [ExecutableItems]                  | `executableitems:[id]`         | -                | Converted ID², owned by the player               |
| [ItemsAdder]                       | `itemsadder:[namespace]/[id]`  | -                | `itemsadder:[id]` for the `itemsadder` namespace |
| [MMOItems]                         | `mmoitems:[type]/[id]`         | -                | Built by MMOItems for the player                 |
| [MythicMobs]                       | `mythicmobs:[id]`              | -                | Converted ID², includes MythicCrucible items     |
| [Nexo]                             | `nexo:[id]`                    | -                | Converted ID²                                    |
| [Oraxen]                           | `oraxen:[id]`                  | -                | Converted ID²                                    |
| [ProRPGItems]                      | `prorpgitems:[type]/[id]`      | -                | Deprecated API³                                  |
| [RPGInventory]                     | `rpginventory:[id]`            | -                | Deprecated API³                                  |

¹ [Item components][item-components] in the format of `/give` command, for example `[damage=10,unbreakable={}]`.\
² The plugin's ID is lowercased and characters not allowed in IDs are replaced with `_`: `Ruby Sword` -> `ruby_sword`.
If several IDs become the same, only one of the items is available.\
³ Provided via the deprecated [ItemsRegistry][BukkitItemsRegistry] API.

### [Block Providers][BlockProvider]

Blocks of all providers are available via [Blocks], with the provider ID as a namespace of block IDs.
The namespace is required.
A block belongs to the provider with the highest priority that recognizes it, vanilla blocks belong to `minecraft`.

| Plugin                              | ID format                      | State        | Notes                                            |
|-------------------------------------|--------------------------------|--------------|--------------------------------------------------|
| [Minecraft][MinecraftBlockProvider] | `minecraft:[id]`               | Block state¹ |                                                  |
| [CraftEngine]                       | `craftengine:[namespace]/[id]` | Block state¹ |                                                  |
| [ItemsAdder]                        | `itemsadder:[namespace]/[id]`  | -            | `itemsadder:[id]` for the `itemsadder` namespace |
| [Nexo]                              | `nexo:[id]`                    | -            | Item-based², note, string and chorus blocks³     |
| [Oraxen]                            | `oraxen:[id]`                  | -            | Item-based²                                      |

¹ Block state properties, for example `[facing=north]`.\
² The block ID is the converted ID of the item placing the block, as for items.
Variants of a directional block and their items have the ID of their parent block.
Content of a storage block is dropped on the ground when the block is broken. Furniture is not a block.\
³ Every level of a stackable block has the ID of the block.

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
[oraxen]: https://github.com/oraxen/oraxen
[nexo]: https://nexomc.com/
[mythicmobs]: https://modrinth.com/plugin/mythicmobs
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
