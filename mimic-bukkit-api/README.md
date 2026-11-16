# mimic-bukkit-api

Mimic API adopted for Bukkit.

## Usage

All Mimic APIs should implement [MimicService].

Available services:
- [LevelProvider]
- [BukkitLevelSystem.Provider] *(deprecated, use [LevelProvider])*
- [ClassProvider]
- [BukkitClassSystem.Provider] *(deprecated, use [ClassProvider])*
- [BukkitPlayerInventory.Provider] *(experimental)*
- [ItemProvider]
- [BukkitItemsRegistry] *(deprecated, use [ItemProvider])*
- [BlockProvider]

> Experimental APIs may change in future versions.
> In Kotlin, they require opt-in with `@OptIn(ExperimentalMimicApi::class)`.

### How to use Mimic APIs?

Firstly you should make sure Mimic is enabled:
```java
private boolean checkMimicEnabled() {
    if (!getServer().getPluginManager().isPluginEnabled("Mimic")) {
        getLogger().severe("Mimic is required for the plugin!");
        return false;
    }

    // You can also check if Mimic version is right
    if (!MimicApiLevel.checkApiLevel(MimicApiLevel.VERSION_0_10)) {
        getLogger().severe("Required at least Mimic 0.10!");
        return false;
    }

    return true;
}
```

If you want to make Mimic hard dependency for your plugin, you can add it to the `depend` list in `plugin.yml`.
Another way is to add it to `softdepend` and show a clear error message to the server owner what he should to do:
```java
@Override
public void onEnable() {
    if (!checkMimicEnabled()) {
        getLogger().severe("Download latest version: https://github.com/EndlessCodeGroup/Mimic/releases/latest");
        getServer().getPluginManager().disablePlugin(this);
        return;
    }
}
```

You can get needed APIs from Mimic in `onEnable` of your plugin or later:
```java
Mimic mimic = Mimic.getInstance();

Levels levels = mimic.getLevels();
Classes classes = mimic.getClasses();
Resources resources = mimic.getResources();
BukkitPlayerInventory inventory = mimic.getPlayerInventory(player);
Items items = mimic.getItems();
Blocks blocks = mimic.getBlocks();
```

Now you can use APIs:
```java
// See the "Levels" section below for details
Key playerLevel = Key.key("minecraft:level");
boolean isExperienced = levels.hasLevel(player, playerLevel, 30);
levels.giveExp(player, playerLevel, 42);

// See the "Classes" section below for details
boolean isWarrior = classes.hasClass(player, Key.key("fabled:warrior"));
CharacterClass playerClass = classes.getPrimaryClass(player);

// See the "Resources" section below for details
boolean isPaid = resources.take(player, Key.key("mmocore:mana"), 20.0);

// See the "Items" section below for details
ItemStack sword = items.createItem(Key.key("mmoitems:sword/excalibur"), 1, player);
boolean isSwordInHand = items.isItem(player.getInventory().getItemInMainHand(), Key.key("mmoitems:sword/excalibur"));

// See the "Blocks" section below for details
boolean isPalmLog = blocks.isBlock(block, Key.key("craftengine:default/palm_log"));
blocks.setBlock(location.getBlock(), Key.key("minecraft:stone_stairs"), "[facing=north]");
```

### IDs

Mimic identifies items, blocks, level tracks, classes and resources by [resource locations](https://minecraft.wiki/w/Resource_location) `[namespace]:[path]`,
where the namespace is the ID of the plugin's implementation: `minecraft:acacia_boat`, `craftengine:default/palm_log`, `auraskills:mining`.
The path may contain lowercase letters, digits, `.`, `_`, `-` and `/`.

- **Items:** the namespace is required, see [Item IDs](#item-ids).
- **Blocks:** the namespace is required, see [Block IDs](#block-ids).
- **Level tracks:** the namespace is required, see [Track IDs](#track-ids).
- **Classes and class slots:** the namespace is required, see [Class IDs](#class-ids).
- **Resources:** the namespace is required, see [Resource IDs](#resource-ids).

> The deprecated `ItemsRegistry` API still searches an item ID without namespace, or with the `mimic` namespace, over all plugins.
> It also accepts item IDs with uppercase letters (`minecraft:DIAMOND_SWORD`), with more than one `:` (`ia:itemsadder:ruby_sword`)
> and MMOItems IDs without item type (`mmoitems:excalibur`), but logs a warning.

<details>
    <summary>Full example</summary>
    
    ```java
    import org.bukkit.entity.Player;
    import org.bukkit.inventory.ItemStack;
    import org.bukkit.plugin.java.JavaPlugin;
    import ru.endlesscode.mimic.Mimic;
    import ru.endlesscode.mimic.MimicApiLevel;
    import ru.endlesscode.mimic.classes.Classes;
    import ru.endlesscode.mimic.items.Items;
    import ru.endlesscode.mimic.level.Levels;
    import net.kyori.adventure.key.Key;
    
    public class MyPlugin extends JavaPlugin {
    
        private static Mimic mimic = null;
    
        public static Levels getLevels() {
            return mimic.getLevels();
        }
    
        public static Classes getClasses() {
            return mimic.getClasses();
        }
    
        public static Items getItems() {
            return mimic.getItems();
        }
    
        @Override
        public void onEnable() {
            if (!checkMimicEnabled()) {
                getLogger().severe("Download latest version: https://github.com/EndlessCodeGroup/Mimic/releases/latest");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
    
            mimic = Mimic.getInstance();
        }

        private boolean checkMimicEnabled() {
            if (!getServer().getPluginManager().isPluginEnabled("Mimic")) {
                getLogger().severe("Mimic is required for the plugin!");
                return false;
            }

            // You can also check if Mimic version is right
            if (!MimicApiLevel.checkApiLevel(MimicApiLevel.VERSION_0_10)) {
                getLogger().severe("Required at least Mimic 0.10!");
                return false;
            }

            return true;
        }
    
        // Method to demonstrate usage
        private void useApis(Player player) {
            Key playerLevel = Key.key("minecraft:level");
            boolean isExperienced = getLevels().hasLevel(player, playerLevel, 30);
            getLevels().giveExp(player, playerLevel, 42);

            boolean isWarrior = getClasses().hasClass(player, Key.key("fabled:warrior"));
    
            Key excalibur = Key.key("mmoitems:sword/excalibur");
            ItemStack sword = getItems().createItem(excalibur, 1, player);
            boolean isSwordInHand = getItems().isItem(player.getInventory().getItemInMainHand(), excalibur);
        }
    }
    ```
</details>

### Levels

Levels API gives access to players' levels from all RPG plugins at once.
A plugin may have one level or many: vanilla Minecraft has a single level,
while skill plugins have a level per skill, and job plugins have a level per job.
Each of these levels is called a **track**.

Levels API requires Mimic 0.10, check it with `MimicApiLevel.checkApiLevel(MimicApiLevel.VERSION_0_10)`.

#### Track IDs

Each track has an ID made of the plugin ID and the track name: `minecraft:level`, `auraskills:mining`, `myplugin:profession/fishing`.
The plugin part is required, because different plugins may have tracks with the same name.
See [IDs](#ids) for allowed characters.

If you read track IDs from your config, validate them:

```java
Key trackId = Key.key(config.getString("track"));
boolean exists = levels.getAllTracks().stream().anyMatch(track -> track.key().equals(trackId));
```

#### Reading levels

If you just need "the player's level", use the primary track.
Server owners choose which plugin provides it with the `level-provider` option in Mimic config.

```java
Levels levels = Mimic.getInstance().getLevels();

LevelTrack track = levels.getPrimaryTrack(player);
if (track == null) return; // The selected plugin has no primary track

// Check the level quickly
boolean isExperienced = levels.hasLevel(player, track.key(), 30);

// Or read the level together with experience
LevelProgress progress = levels.getProgress(player, track.key());
if (progress != null) {
    int level = progress.getLevel();
    if (progress.hasMaxLevel()) {
        int maxLevel = progress.getMaxLevel();
    }
    // Some tracks have levels only, and the experience needed for the next level may be unknown
    if (progress.hasExp() && progress.hasExpToNextLevel()) {
        double exp = progress.getExp();                       // experience within the current level
        double expToNextLevel = progress.getExpToNextLevel(); // the whole step to the next level, 0.0 if it's free
    }
}
```

`getTracks(player)` lists the tracks the player has, and `getAllTracks()` lists all tracks of all plugins.

#### Changing levels

```java
Key mining = Key.key("auraskills:mining");

levels.giveExp(player, mining, 100); // 100 experience, without boosters and party sharing if the plugin allows
levels.takeExp(player, mining, 50);  // Lowers the level if needed
levels.setLevel(player, mining, 10); // Level 10 with 0 experience, kept within the track's min and max level
```

A change returns `true` if it was requested, and `false` if the plugin doesn't support it or the player's data isn't loaded yet.
`true` doesn't guarantee the change: another plugin may cancel it.

#### Rules

See [Levels] for the rules: which players are supported, what `null` and `false` mean, caching and threading.

#### Built-in tracks

| Plugin                                   | Tracks                                                                                |
|------------------------------------------|---------------------------------------------------------------------------------------|
| `minecraft`                              | `level`                                                                               |
| `mmocore`                                | Primary: `level`, the level of the current class. Professions: `profession/mining`, … |
| `heroes`                                 | Classes: `warrior`, … Primary: the player's primary class                             |
| `auraskills`                             | Skills: `mining`, `farming`, … Primary: `power`, the read-only sum of skill levels    |
| `mcmmo`                                  | Skills: `mining`, `swords`, … Primary: `power`, the read-only sum of skill levels     |
| `jobs`                                   | Joined jobs: `miner`, `woodcutter`, … Primary: `total`, the read-only sum of job levels |
| `fabled`                                 | Class groups: `class`, `race`, … Primary: the group of the main class                 |
| `valhallammo`                            | Skills: `mining`, `light_armor`, … Primary: `power`, the Power skill                  |

Plugins that still use the deprecated [BukkitLevelSystem] are available the same way, as the single track `<id>:level`.

### Classes

Classes API gives access to players' classes from all RPG plugins at once.
A **slot** holds at most one of the player's classes:
one plugin has a single class per player, another has a primary and a secondary class, or a class and a race.
Races are classes too, held in a race slot.
A player may have classes from several plugins at once, for example a class from one plugin and a race from another.

Classes API requires Mimic 0.10, check it with `MimicApiLevel.checkApiLevel(MimicApiLevel.VERSION_0_10)`.

#### Class IDs

Slots and classes have IDs made of the plugin ID and the slot or class name: `fabled:race`, `fabled:warrior`.
The plugin part is required, because different plugins may have classes with the same name.
See [IDs](#ids) for allowed characters.

If you read class IDs from your config, validate them:

```java
Key classId = Key.key(config.getString("class"));
boolean exists = classes.getAllClasses().stream().anyMatch(c -> c.key().equals(classId));
```

Classes of plugins using the deprecated [BukkitClassSystem] are missing from `getAllClasses()`, see [Built-in classes](#built-in-classes).

#### Reading classes

```java
Classes classes = Mimic.getInstance().getClasses();

// Check the class, a subclass counts too: a paladin is a warrior if the plugin says so
boolean isWarrior = classes.hasClass(player, Key.key("fabled:warrior"));

// If you just need "the player's class", use the primary class.
// Server owners choose which plugin provides it with the `class-provider` option in Mimic config.
CharacterClass playerClass = classes.getPrimaryClass(player);
if (playerClass == null) return; // The player hasn't picked a class yet

// A class may have a level, read it with Levels API
Key levelTrack = playerClass.levelTrack();
boolean isExperienced = levelTrack != null && levels.hasLevel(player, levelTrack, 30);

// All classes of the player, where key is a slot ID. Empty slots are omitted.
Map<Key, CharacterClass> playerClasses = classes.getClasses(player);
```

`getAllClasses()` and `getAllSlots()` list all classes and slots of all plugins.

#### Changing classes

```java
classes.setClass(player, Key.key("fabled:paladin"));                         // The plugin chooses the slot
classes.setClass(player, Key.key("fabled:elf"), Key.key("fabled:race"));      // In the given slot
```

It works like the plugin's admin command changing the class, with its usual side effects:
the plugin may keep or reset the class progress, give items, or run commands.
A change returns `true` if it was requested, and `false` if the plugin doesn't support it or the player's data isn't loaded yet.

#### Rules

See [Classes] for the rules: which players are supported, what `null` and `false` mean, caching and threading.

#### Built-in classes

| Plugin                                  | Slots                                   | Class level track     |
|-----------------------------------------|-----------------------------------------|-----------------------|
| `mmocore`                               | `class`                                 | `mmocore:level`       |
| `heroes`                                | Primary: `primary`. `secondary`, `race` | The same as the class |
| `fabled`                                | Class groups: `class`, `race`, …        | The same as the slot  |

Plugins that use the deprecated [BukkitClassSystem] are available as the single slot `<id>:class` holding the primary class.
Class names are converted to IDs: `Arcane Mage` becomes `arcane_mage`.
The built-in `permissions` class system is the exception: it is available only via the deprecated API,
because it can't list its classes and a player may have several of them.
So without a class plugin there are no slots and classes, and `getPrimaryClass()` returns `null`.

The old API has limits:
- It can't list classes, so its classes are missing from `getAllClasses()`. Don't reject such class IDs in your config validation.
- It has no subclasses, class levels and class changes, and only the primary class is available.

### Resources

Resources API gives access to players' mana, stamina and similar resources from all RPG plugins at once.
A **resource** is something a player spends and gets back, with a current amount and a maximum.

Resources API requires Mimic 0.10, check it with `MimicApiLevel.checkApiLevel(MimicApiLevel.VERSION_0_10)`.

#### Resource IDs

Resources have IDs made of the plugin ID and the resource name: `mmocore:mana`, `heroes:stamina`.
The plugin part is required, because different plugins may have resources with the same name.
Note that `Key.key("mana")` turns into `minecraft:mana`, so validate IDs read from your config:

```java
Key resourceId = Key.key(config.getString("resource")); // "mmocore:mana"
boolean exists = resources.getAllResources().stream().anyMatch(r -> r.key().equals(resourceId));
```

If your config just says "mana", use the primary resource. Server owners choose its plugin with `resource-provider` in Mimic config:

```java
Resource mana = resources.getPrimaryResource(player);
if (mana == null) return; // There is no mana on this server
```

Vanilla resources `minecraft:health`, `minecraft:food` and `minecraft:air` are never primary, so mana never means the food bar.

#### Reading resources

```java
Resources resources = Mimic.getInstance().getResources();
Key mana = Key.key("mmocore:mana");

ResourceState state = resources.getState(player, mana);
if (state != null) {
    double percent = state.getAmount() / state.getMax() * 100;
}

boolean canCast = resources.has(player, mana, 20.0);
```

#### Changing resources

```java
// Pay a cost. Takes the amount or nothing, and never sends messages to the player
if (!resources.take(player, mana, 20.0)) {
    player.sendMessage("Not enough mana!");
    return;
}

resources.give(player, mana, 5.0); // Not above the maximum
resources.set(player, mana, 0.0);  // Kept between 0 and the maximum
```

`take` returns `true` only if the resource was taken: `false` means the player doesn't have enough,
another plugin cancelled the change, or the plugin doesn't support it.
The plugin may take a different amount by its own rules, for example with a discount.
Plugins storing whole numbers round amounts down, so `take` may take a bit more and `give` a bit less.

`give` and `set` return `true` if the change was requested, another plugin may still cancel it.

#### Rules

See [Resources] for the rules: which players are supported, what `null` and `false` mean, caching and threading.
To make resources of your plugin available, implement [ResourceProvider]: only reading the state and setting the amount.

#### Built-in resources

| Plugin       | Resources                                 | Values        |
|--------------|-------------------------------------------|---------------|
| `minecraft`  | `health`, `food`, `air` (in ticks). No primary | `health` is decimal, others are whole numbers |
| `mmocore`    | Primary: `mana`. `stamina`, `stellium`    | Decimal       |
| `heroes`     | Primary: `mana`. `stamina`                | Whole numbers |
| `auraskills` | Primary: `mana`                           | Decimal       |
| `fabled`     | Primary: `mana`, shared by all classes    | Decimal       |

`minecraft:health` is changed directly, as a cost and not as damage: no damage or regain events are fired,
armor and absorption hearts are ignored, and taking all health kills the player.
### Items

[Items] gives access to vanilla items and custom items of all supported plugins:
create an item by its ID, get the ID of a stack, check that a stack is a specific item.

#### Item IDs

An item ID is a `Key` like `mmoitems:sword/excalibur`: the namespace is the ID of the plugin providing the item,
the path is the item within that plugin.
The namespace is required, there is no search over all plugins.
`Key.key("excalibur")` turns into `minecraft:excalibur`, so validate IDs read from configs:

```java
Key itemId = Key.key(config.getString("item"));
if (!items.hasItem(itemId)) {
    getLogger().warning("Unknown item " + itemId.asString());
}
```

`items.getAllItems()` lists all IDs, for example, for tab completion.
See ID formats of the supported plugins in [Supported Implementations](../mimic-bukkit/README.md#supported-implementations).

#### Identifying items

An item belongs to the plugin with the highest priority that recognizes it, and vanilla items belong to `minecraft`.
So a ruby built on a diamond is never `minecraft:diamond`.
Items are compared by ID only: amount, stats, durability and lore of the stack are ignored.

```java
Key itemId = items.getItemId(stack); // null if no plugin recognizes the item
boolean isExcalibur = items.isItem(stack, Key.key("mmoitems:sword/excalibur"));
// Any diamond, vanilla or a custom item built on it
boolean isDiamond = stack.getType() == Material.DIAMOND;
```

#### Creating items

```java
// A new stack with a single item, or null if the item doesn't exist
ItemStack stick = items.createItem(Key.key("minecraft:stick"));
ItemStack palmLogs = items.createItem(Key.key("craftengine:default/palm_log"), 16);

// Create the item for the player: some plugins scale items to the player's level
ItemStack excalibur = items.createItem(Key.key("mmoitems:sword/excalibur"), 1, player);

// The payload is in the format of the plugin providing the item, with an invalid payload the item is not created
ItemStack sword = items.createItem(Key.key("minecraft:diamond_sword"), 1, player, "[damage=10]");
```

In Kotlin, the parameters have default values: `items.createItem(itemId, player = player)`.

The amount is kept within `1` and the maximum stack size of the item.
Each call returns a new stack, so it is safe to change it.

#### Rules

- Call `createItem` on the main thread. Other methods may be called from any thread.
- Item IDs may be unknown until their plugins finish loading, and may change when a plugin reloads.
  Don't keep created stacks across reloads.
- A payload is passed only to the plugin named by the item ID. Plugins taking no payload ignore it.

#### Migration from ItemsRegistry

`ItemsRegistry` API is deprecated but keeps working: every registered items registry is available via [Items],
and `Mimic.getItemsRegistry()` shows items of all item providers.

| ItemsRegistry                        | Items                                               |
|--------------------------------------|-----------------------------------------------------|
| `getItem(id, payload, amount)`       | `createItem(id, amount, player, payload)`           |
| `getItemId(item)`                    | `getItemId(item)`                                   |
| `isSameItem(item, id)`               | `isItem(item, id)`                                  |
| `isItemExists(id)`                   | `hasItem(id)`                                       |
| `getKnownIds()`                      | `getAllItems()`                                     |

- IDs are `Key`s with a required namespace. To keep accepting IDs without a namespace in your configs,
  look them up in `getAllItems()` yourself.
- MMOItems IDs include the item type: `mmoitems:sword/excalibur` instead of `mmoitems:excalibur`.
- The payload is a `String`.

### Blocks

[Blocks] gives access to vanilla blocks and custom blocks of all supported plugins:
identify a placed block, set and break blocks, get an item placing a block.

#### Block IDs

A block ID is a `Key` like `craftengine:default/palm_log`: the namespace is the ID of the plugin providing the block,
the path is the block within that plugin.
The namespace is required, there is no search over all plugins.
`Key.key("palm_log")` turns into `minecraft:palm_log`, so validate IDs read from configs:

```java
Key blockId = Key.key(config.getString("block"));
if (!blocks.hasBlock(blockId)) {
    getLogger().warning("Unknown block " + blockId.asString());
}
```

Block IDs are separate from item IDs: a block and the item placing it may have different IDs.

#### Identifying blocks

A block belongs to the plugin with the highest priority that recognizes it, and vanilla blocks belong to `minecraft`.
So a custom block built on a note block is never `minecraft:note_block`.

```java
Key blockId = blocks.getBlockId(block); // null if no plugin recognizes the block
boolean isPalmLog = blocks.isBlock(block, Key.key("craftengine:default/palm_log"));
// ID of the block placed by the item, or null if the item doesn't place a block
Key placedBlockId = blocks.getBlockIdFromItem(player.getInventory().getItemInMainHand());
```

#### Changing blocks

```java
// A new stack of the item placing the block, or null if the block has no item
ItemStack palmLogs = blocks.createBlockItem(Key.key("craftengine:default/palm_log"), 16);

// Set a block in place of an empty one. The state is optional, with an invalid state the block is not set
blocks.setBlock(location.getBlock(), Key.key("minecraft:stone_stairs"), "[facing=north]");

// Break a block with a pickaxe, collecting drops into a chest inventory.
// Items that don't fit are returned from the consumer and dropped on the ground.
blocks.breakBlock(block, pickaxe, drops -> chestInventory.addItem(drops.toArray(new ItemStack[0])).values());
// Or drop everything on the ground
blocks.breakBlock(block, pickaxe);
```

#### Rules

- Use [Blocks] only on the main thread.
- No place or break events are fired and no protection checks are made, check protection yourself if needed.
- Block IDs may be unknown until their plugins finish loading, and may change when a plugin reloads.
- To replace a block, break it first: `setBlock` returns `false` for a non-empty block.

### How to implement API in my plugin?

Create the class implementing one of Mimic APIs.
For implementations examples, look [impl] package in [mimic-bukkit].

```java
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.mimic.classes.ClassProvider;

import java.util.Collection;
import java.util.List;
import java.util.Map;

// Declare class implementing needed API. In this case we need to implement classes.
public class MyClassProvider implements ClassProvider {

    private static final Slot CLASS_SLOT = new Slot("class", Component.text("Class"));

    // ID used to identify your implementation, usually it matches the name of the plugin
    @NotNull
    @Override
    public String getId() {
        return "myplugin";
    }

    @NotNull
    @Override
    public Collection<Slot> getAllSlots() {
        return List.of(CLASS_SLOT);
    }

    // Read on every request, so it can change on the plugin reload
    @NotNull
    @Override
    public Collection<ClassDefinition> getAllClasses() {
        return MyClasses.getAll().stream().map(MyClassProvider::toDefinition).toList();
    }

    @NotNull
    @Override
    public Map<String, ClassDefinition> getClasses(@NotNull Player player) {
        MyClass playerClass = MyClasses.getClass(player);
        if (playerClass == null) return Map.of(); // The player hasn't picked a class yet
        return Map.of(CLASS_SLOT.getId(), toDefinition(playerClass));
    }

    @Nullable
    @Override
    public Slot getPrimarySlot(@NotNull Player player) {
        return CLASS_SLOT;
    }

    private static ClassDefinition toDefinition(MyClass myClass) {
        return new ClassDefinition(myClass.getId(), Component.text(myClass.getDisplayName()), myClass.getParentIds());
    }
}
```

Item providers and block providers receive and return IDs without the namespace, Mimic adds it.
Mapping the plugin's own IDs to valid Mimic IDs is up to the implementation, for example `default:palm_log` → `default/palm_log`.
Invalid IDs returned by an implementation are ignored with a warning.
See [ItemProvider] and [BlockProvider] for the rules about IDs, ownership of items and blocks, and threading.

<details>
    <summary>Item provider example</summary>

    ```java
    import org.bukkit.entity.Player;
    import org.bukkit.inventory.ItemStack;
    import org.jetbrains.annotations.NotNull;
    import org.jetbrains.annotations.Nullable;
    import ru.endlesscode.mimic.items.ItemProvider;

    import java.util.Collection;

    public class MyItemProvider implements ItemProvider {

        @NotNull
        @Override
        public String getId() {
            return "myplugin";
        }

        // Read on every request, so it can change on the plugin reload
        @NotNull
        @Override
        public Collection<String> getAllItems() {
            return MyItems.getIds();
        }

        // Return null for items of other plugins and for vanilla items
        @Nullable
        @Override
        public String getItemId(@NotNull ItemStack item) {
            return MyItems.getId(item);
        }

        // Return a new stack on every call, or null if the item is unknown.
        // This provider takes no payload, so it is ignored
        @Nullable
        @Override
        public ItemStack createItem(@NotNull String itemId, int amount, @Nullable Player player, @Nullable String payload) {
            return MyItems.create(itemId, amount, player);
        }
    }
    ```
</details>

<details>
    <summary>Block provider example</summary>

    ```java
    import org.bukkit.block.Block;
    import org.bukkit.inventory.ItemStack;
    import org.jetbrains.annotations.NotNull;
    import org.jetbrains.annotations.Nullable;
    import ru.endlesscode.mimic.blocks.BlockProvider;
    import ru.endlesscode.mimic.blocks.DropsConsumer;

    import java.util.Collection;

    public class MyBlockProvider implements BlockProvider {

        @NotNull
        @Override
        public String getId() {
            return "myplugin";
        }

        @NotNull
        @Override
        public Collection<String> getAllBlocks() {
            return MyBlocks.getIds();
        }

        @Nullable
        @Override
        public String getBlockId(@NotNull Block block) {
            // Return null for blocks of other plugins and vanilla blocks
            return MyBlocks.getIdOf(block);
        }

        @Nullable
        @Override
        public String getBlockIdFromItem(@NotNull ItemStack item) {
            return MyBlocks.getIdOfPlacedBy(item);
        }

        @Nullable
        @Override
        public ItemStack createBlockItem(@NotNull String blockId, int amount) {
            // Should be a new stack on every call, or null if the ID is unknown
            return MyBlocks.createItem(blockId, amount);
        }

        @Override
        public boolean setBlock(@NotNull Block block, @NotNull String blockId, @Nullable String state) {
            // Mimic has already checked that the block is empty.
            // The state is in your own format, usually block state properties like "[facing=north]".
            // Return false without setting the block if the ID is unknown or the state is invalid
            if (!MyBlocks.getIds().contains(blockId)) return false;
            return MyBlocks.place(block, blockId, state);
        }

        @Override
        public boolean breakBlock(@NotNull Block block, @Nullable ItemStack tool, @NotNull DropsConsumer dropsConsumer) {
            // Mimic calls it only for blocks this provider returns an ID for
            Collection<ItemStack> drops = MyBlocks.remove(block, tool);
            // Drop on the ground only items returned by the consumer
            for (ItemStack item : dropsConsumer.consume(drops)) {
                block.getWorld().dropItemNaturally(block.getLocation(), item);
            }
            return true;
        }
    }
    ```
</details>

<details>
    <summary>Level provider example</summary>

    ```java
    import net.kyori.adventure.text.Component;
    import org.bukkit.entity.Player;
    import org.jetbrains.annotations.NotNull;
    import org.jetbrains.annotations.Nullable;
    import ru.endlesscode.mimic.level.LevelProgress;
    import ru.endlesscode.mimic.level.LevelProvider;

    import java.util.Collection;
    import java.util.List;

    public class MyLevelProvider implements LevelProvider {

        @NotNull
        @Override
        public String getId() {
            return "myplugin";
        }

        // Read on every request, so it can change on the plugin reload
        @NotNull
        @Override
        public Collection<Track> getAllTracks() {
            return MySkills.getAll().stream().map(MyLevelProvider::toTrack).toList();
        }

        @NotNull
        @Override
        public Collection<Track> getTracks(@NotNull Player player) {
            return MySkills.getUnlocked(player).stream().map(MyLevelProvider::toTrack).toList();
        }

        @Nullable
        @Override
        public Track getPrimaryTrack(@NotNull Player player) {
            return null; // No track represents the player's level
        }

        private static Track toTrack(MySkill skill) {
            return new Track(skill.getId(), Component.text(skill.getDisplayName()));
        }

        @Nullable
        @Override
        public LevelProgress getProgress(@NotNull Player player, @NotNull String trackId) {
            MySkillData data = MySkills.getData(player, trackId);
            if (data == null) return null;
            return LevelProgress.level(data.getLevel())
                    .minLevel(1)
                    .maxLevel(data.getMaxLevel())
                    .exp(data.getExp())
                    // The whole requirement for the step from the current level to the next one, not the remaining part
                    .expToNextLevel(data.getExpToNextLevel())
                    .build();
        }

        // Writes are optional, override only what your plugin supports
        @Override
        public boolean giveExp(@NotNull Player player, @NotNull String trackId, double amount) {
            MySkillData data = MySkills.getData(player, trackId);
            if (data == null) return false;
            data.addRawExp(amount);
            return true;
        }
    }
    ```
</details>

A level provider returns track IDs without the plugin part: `mining`, and Mimic turns it into `myplugin:mining`.
See [LevelProvider] for the rules about track IDs, unloaded data and threading.
A class provider does the same with slot and class IDs, see [ClassProvider].

When you've implemented API, register it in method **`onLoad`** of your plugin:

```java
@Override
public void onLoad() {
    Mimic mimic = Mimic.getInstance();
    
    // Register your implementations and specify minimal required Mimic version for it.
    mimic.registerClassProvider(new MyClassProvider(), MimicApiLevel.CURRENT, this);
    mimic.registerItemProvider(new MyItemProvider(), MimicApiLevel.CURRENT, this);
    mimic.registerBlockProvider(new MyBlockProvider(), MimicApiLevel.CURRENT, this);
    mimic.registerLevelProvider(new MyLevelProvider(), MimicApiLevel.CURRENT, this);
}
```

Each ID belongs to a single implementation.
If your plugin registers an implementation with the same ID as a Mimic built-in integration, yours replaces the built-in one.
So you can ship the integration with your plugin and keep it up to date.
An implementation of a deprecated API doesn't replace a built-in implementation of the API replacing it:
an `ItemsRegistry` with the ID `mmoitems` doesn't replace the built-in `mmoitems` item provider.
If two plugins register the same ID, the one with higher priority is used.
IDs of the default implementations (`mimic`, `minecraft`, `permissions`) are reserved.

> Don't use **`onEnable`** to register services.
> Plugins should be able to get services in **`onEnable`** and all services should be already registered.

[MimicService]: ../mimic-api/src/main/kotlin/MimicService.kt
[BukkitLevelSystem.Provider]: src/main/kotlin/level/BukkitLevelSystem.kt
[BukkitLevelSystem]: src/main/kotlin/level/BukkitLevelSystem.kt
[Levels]: https://endlesscodegroup.github.io/Mimic/mimic-bukkit-api/ru.endlesscode.mimic.level/-levels/index.html
[LevelProvider]: https://endlesscodegroup.github.io/Mimic/mimic-bukkit-api/ru.endlesscode.mimic.level/-level-provider/index.html
[Classes]: https://endlesscodegroup.github.io/Mimic/mimic-bukkit-api/ru.endlesscode.mimic.classes/-classes/index.html
[ClassProvider]: https://endlesscodegroup.github.io/Mimic/mimic-bukkit-api/ru.endlesscode.mimic.classes/-class-provider/index.html
[Resources]: https://endlesscodegroup.github.io/Mimic/mimic-bukkit-api/ru.endlesscode.mimic.resources/-resources/index.html
[ResourceProvider]: https://endlesscodegroup.github.io/Mimic/mimic-bukkit-api/ru.endlesscode.mimic.resources/-resource-provider/index.html
[BukkitClassSystem.Provider]: src/main/kotlin/classes/BukkitClassSystem.kt
[BukkitClassSystem]: src/main/kotlin/classes/BukkitClassSystem.kt
[BukkitPlayerInventory.Provider]: src/main/kotlin/inventory/BukkitPlayerInventory.kt
[BukkitItemsRegistry]: src/main/kotlin/items/BukkitItemsRegistry.kt
[Items]: https://endlesscodegroup.github.io/Mimic/mimic-bukkit-api/ru.endlesscode.mimic.items/-items/index.html
[ItemProvider]: https://endlesscodegroup.github.io/Mimic/mimic-bukkit-api/ru.endlesscode.mimic.items/-item-provider/index.html
[Blocks]: https://endlesscodegroup.github.io/Mimic/mimic-bukkit-api/ru.endlesscode.mimic.blocks/-blocks/index.html
[BlockProvider]: https://endlesscodegroup.github.io/Mimic/mimic-bukkit-api/ru.endlesscode.mimic.blocks/-block-provider/index.html

[impl]: ../mimic-bukkit/src/main/kotlin/impl/
[mimic-bukkit]: ../mimic-bukkit/
