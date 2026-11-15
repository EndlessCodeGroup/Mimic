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
- [BukkitItemsRegistry]
- [BukkitBlockRegistry] *(experimental)*

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
BukkitPlayerInventory inventory = mimic.getPlayerInventory(player);
BukkitItemsRegistry itemsRegistry = mimic.getItemsRegistry();
BukkitBlockRegistry blockRegistry = mimic.getBlockRegistry();
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

// ItemsRegistry is not related to player so it can be used without provider
ItemStack stick = itemsRegistry.getItem("minecraft:stick");
boolean isPalmLogInHand = itemsRegistry.isSameItem(player.getInventory().getItemInMainHand(), "craftengine:default/palm_log");
boolean isPalmLogExists = itemsRegistry.isItemExists("craftengine:default/palm_log");

// BlockRegistry sets and breaks blocks without firing events, check protection yourself if needed
blockRegistry.setBlock(location.getBlock(), "minecraft:stone_stairs", "[facing=north]");
boolean isPalmLog = blockRegistry.isSameBlock(block, "craftengine:default/palm_log");
// Break a block with a pickaxe, collecting drops into a chest inventory.
// Items that don't fit are returned from the consumer and dropped on the ground.
blockRegistry.breakBlock(block, pickaxe, drops -> chestInventory.addItem(drops.toArray(new ItemStack[0])).values());
```

### IDs

Mimic identifies items, blocks, level tracks and classes by [resource locations](https://minecraft.wiki/w/Resource_location) `[namespace]:[path]`,
where the namespace is the ID of the plugin's implementation: `minecraft:acacia_boat`, `craftengine:default/palm_log`, `auraskills:mining`.
The path may contain lowercase letters, digits, `.`, `_`, `-` and `/`.

- **Items and blocks:** ID without namespace, or with the `mimic` namespace, is searched over all registries in order of priority.
- **Level tracks:** the namespace is required, see [Track IDs](#track-ids).
- **Classes and class slots:** the namespace is required, see [Class IDs](#class-ids).

> Item IDs with uppercase letters (`minecraft:DIAMOND_SWORD`) or with more than one `:` (`ia:itemsadder:ruby_sword`) are deprecated.
> They still work but log a warning.

<details>
    <summary>Full example</summary>
    
    ```java
    import org.bukkit.entity.Player;
    import org.bukkit.inventory.ItemStack;
    import org.bukkit.plugin.java.JavaPlugin;
    import ru.endlesscode.mimic.Mimic;
    import ru.endlesscode.mimic.MimicApiLevel;
    import ru.endlesscode.mimic.classes.Classes;
    import ru.endlesscode.mimic.items.BukkitItemsRegistry;
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
    
        public static BukkitItemsRegistry getItemsRegistry() {
            return mimic.getItemsRegistry();
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
    
            // ItemsRegistry is not related to player so it can be used without provider
            BukkitItemsRegistry itemsRegistry = getItemsRegistry();
            ItemStack stick = itemsRegistry.getItem("minecraft:stick");
            boolean isPalmLogInHand = itemsRegistry.isSameItem(player.getInventory().getItemInMainHand(), "craftengine:default/palm_log");
            boolean isPalmLogExists = itemsRegistry.isItemExists("craftengine:default/palm_log");
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
| `permissions`                           | `class`                                 | none                  |

Plugins that use the deprecated [BukkitClassSystem] are available as the single slot `<id>:class` holding the primary class.
Class names are converted to IDs: `Arcane Mage` becomes `arcane_mage`.
Built-in `permissions` integration works this way too.

The old API has limits:
- It can't list classes, so its classes are missing from `getAllClasses()`. Don't reject such class IDs in your config validation.
- It has no subclasses, class levels and class changes, and only the primary class is available.

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

Item and block registries receive and return IDs without the registry namespace, Mimic adds it.
Mapping the plugin's own IDs to valid Mimic IDs is up to the registry, for example `default:palm_log` → `default/palm_log`.
Invalid IDs returned by a registry are ignored with a warning.

<details>
    <summary>Block registry example</summary>

    ```java
    import org.bukkit.block.Block;
    import org.bukkit.inventory.ItemStack;
    import org.jetbrains.annotations.NotNull;
    import org.jetbrains.annotations.Nullable;
    import ru.endlesscode.mimic.blocks.BukkitBlockRegistry;
    import ru.endlesscode.mimic.blocks.DropsConsumer;

    import java.util.Collection;

    public class MyBlockRegistry implements BukkitBlockRegistry {

        @NotNull
        @Override
        public String getId() {
            return "myplugin";
        }

        @NotNull
        @Override
        public Collection<String> getKnownIds() {
            return MyBlocks.getIds();
        }

        @Override
        public boolean isBlockExists(@NotNull String blockId) {
            return MyBlocks.getIds().contains(blockId);
        }

        @Nullable
        @Override
        public String getBlockId(@NotNull Block block) {
            return MyBlocks.getIdOf(block);
        }

        @Nullable
        @Override
        public String getBlockIdFromItem(@NotNull ItemStack blockItem) {
            return MyBlocks.getIdOfPlacedBy(blockItem);
        }

        @Nullable
        @Override
        public ItemStack getBlockItem(@NotNull String blockId, int amount) {
            return MyBlocks.createItem(blockId, amount);
        }

        @Override
        public boolean setBlock(@NotNull Block block, @NotNull String blockId, @Nullable Object payload) {
            // Every registry should support block state properties like "[facing=north]" as a String payload
            if (!block.isEmpty() || !isBlockExists(blockId)) return false;
            MyBlocks.place(block, blockId, payload instanceof String ? (String) payload : null);
            return true;
        }

        @Override
        public boolean breakBlock(@NotNull Block block, @Nullable ItemStack tool, @NotNull DropsConsumer<ItemStack> dropsConsumer) {
            if (getBlockId(block) == null) return false;
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
    mimic.registerBlockRegistry(new MyBlockRegistry(), MimicApiLevel.CURRENT, this);
    mimic.registerLevelProvider(new MyLevelProvider(), MimicApiLevel.CURRENT, this);
}
```

Each ID belongs to a single implementation.
If your plugin registers an implementation with the same ID as a Mimic built-in integration, yours replaces the built-in one.
So you can ship the integration with your plugin and keep it up to date.
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
[BukkitClassSystem.Provider]: src/main/kotlin/classes/BukkitClassSystem.kt
[BukkitClassSystem]: src/main/kotlin/classes/BukkitClassSystem.kt
[BukkitPlayerInventory.Provider]: src/main/kotlin/inventory/BukkitPlayerInventory.kt
[BukkitItemsRegistry]: src/main/kotlin/items/BukkitItemsRegistry.kt
[BukkitBlockRegistry]: src/main/kotlin/blocks/BukkitBlockRegistry.kt

[impl]: ../mimic-bukkit/src/main/kotlin/impl/
[mimic-bukkit]: ../mimic-bukkit/
