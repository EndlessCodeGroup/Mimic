# mimic-bukkit-api

Mimic API adopted for Bukkit.

## Usage

All Mimic APIs should implement [MimicService].

Available services:
- [BukkitLevelSystem.Provider]
- [BukkitClassSystem.Provider]
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
    if (!MimicApiLevel.checkApiLevel(MimicApiLevel.VERSION_0_9)) {
        getLogger().severe("Required at least Mimic 0.9!");
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
        getLogger().severe("Download latest version here: https://www.spigotmc.org/resources/82515/");
        getServer().getPluginManager().disablePlugin(this);
        return;
    }
}
```

You can get needed APIs from Mimic in `onEnable` of your plugin or later:
```java
Mimic mimic = Mimic.getInstance();

BukkitLevelSystem levelSystem = mimic.getLevelSystem(player);
BukkitClassSystem classSystem = mimic.getClassSystem(player);
BukkitPlayerInventory inventory = mimic.getPlayerInventory(player);
BukkitItemsRegistry itemsRegistry = mimic.getItemsRegistry();
BukkitBlockRegistry blockRegistry = mimic.getBlockRegistry();
```

Now you can use APIs:
```java
// LevelSystem and ClassSystem holds a weak reference to a player object
// and should not live a long time.
int playerLevel = levelSystem.getLevel();
levelSystem.giveExp(42);

String playerPrimaryClass = classSystem.getPrimaryClass();
boolean isMage = classSystem.hasAnyOfClasses(Arrays.asList("Mage", "Druid", "Necromancer"));

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

### Item and block IDs

Mimic identifies items and blocks by [resource locations](https://minecraft.wiki/w/Resource_location) `[namespace]:[path]`,
where the namespace is the ID of the registry: `minecraft:acacia_boat`, `craftengine:default/palm_log`.
The path may contain lowercase letters, digits, `.`, `_`, `-` and `/`.
ID without namespace, or with the `mimic` namespace, is searched over all registries in order of priority.

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
    import ru.endlesscode.mimic.classes.BukkitClassSystem;
    import ru.endlesscode.mimic.items.BukkitItemsRegistry;
    import ru.endlesscode.mimic.level.BukkitLevelSystem;
    
    import java.util.Arrays;
    
    public class MyPlugin extends JavaPlugin {
    
        private static Mimic mimic = null;
    
        public static BukkitLevelSystem getLevelSystem(Player player) {
            return mimic.getLevelSystem(player);
        }
    
        public static BukkitClassSystem getClassSystem(Player player) {
            return mimic.getClassSystem(player);
        }
    
        public static BukkitItemsRegistry getItemsRegistry() {
            return mimic.getItemsRegistry();
        }
    
        @Override
        public void onEnable() {
            if (!checkMimicEnabled()) {
                getLogger().severe("Download latest version here: https://www.spigotmc.org/resources/82515/");
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
            if (!MimicApiLevel.checkApiLevel(MimicApiLevel.VERSION_0_9)) {
                getLogger().severe("Required at least Mimic 0.9!");
                return false;
            }

            return true;
        }
    
        // Method to demonstrate usage
        private void useApis(Player player) {
            // LevelSystem and ClassSystem holds weak reference to player object and should not live a long time.
            BukkitLevelSystem levelSystem = getLevelSystem(player);
            int playerLevel = levelSystem.getLevel();
            levelSystem.giveExp(42);
    
            BukkitClassSystem classSystem = getClassSystem(player);
            String playerPrimaryClass = classSystem.getPrimaryClass();
            boolean isMage = classSystem.hasAnyOfClasses(Arrays.asList("Mage", "Druid", "Necromancer"));
    
            // ItemsRegistry is not related to player so it can be used without provider
            BukkitItemsRegistry itemsRegistry = getItemsRegistry();
            ItemStack stick = itemsRegistry.getItem("minecraft:stick");
            boolean isPalmLogInHand = itemsRegistry.isSameItem(player.getInventory().getItemInMainHand(), "craftengine:default/palm_log");
            boolean isPalmLogExists = itemsRegistry.isItemExists("craftengine:default/palm_log");
        }
    }
    ```
</details>

### How to implement API in my plugin?

Create the class implementing one of Mimic APIs.
For implementations examples, look [impl] package in [mimic-bukkit].

```java
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.mimic.classes.BukkitClassSystem;

import java.util.*;

// Declare class implementing needed API. In this case we need to implement classes system.
public class MyClassSystem extends BukkitClassSystem {

    // ID used to identify your implementation, usually it matches the name of the plugin
    public static final String ID = "myplugin";

    private Map<UUID, List<String>> playersClassesMap = new HashMap<>();

    public MyClassSystem(@NotNull Player player) {
        super(player);
    }

    @NotNull
    @Override
    public List<String> getClasses() {
        return playersClassesMap.getOrDefault(getPlayer().getUniqueId(), Collections.emptyList());
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

When you've implemented API, register it in method **`onLoad`** of your plugin:

```java
@Override
public void onLoad() {
    Mimic mimic = Mimic.getInstance();
    
    // Register your implementations and specify minimal required Mimic version for it.
    mimic.registerClassSystem(MyClassSystem::new, MimicApiLevel.CURRENT, this);
    mimic.registerBlockRegistry(new MyBlockRegistry(), MimicApiLevel.CURRENT, this);
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
[BukkitClassSystem.Provider]: src/main/kotlin/classes/BukkitClassSystem.kt
[BukkitPlayerInventory.Provider]: src/main/kotlin/inventory/BukkitPlayerInventory.kt
[BukkitItemsRegistry]: src/main/kotlin/items/BukkitItemsRegistry.kt
[BukkitBlockRegistry]: src/main/kotlin/blocks/BukkitBlockRegistry.kt

[impl]: ../mimic-bukkit/src/main/kotlin/impl/
[mimic-bukkit]: ../mimic-bukkit/
