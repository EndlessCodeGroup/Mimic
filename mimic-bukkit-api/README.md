# mimic-bukkit-api

Mimic API adopted for Bukkit.

## Usage

Mimic uses [ServicesManager] to store [MimicServices][MimicService].

Available services:
- [BukkitLevelSystem.Provider]
- [BukkitClassSystem.Provider]
- [BukkitItemsRegistry]

### How to use Mimic APIs?

Firstly you should make sure Mimic is enabled:
```java
private boolean checkMimicEnabled() {
    if (!getServer().getPluginManager().isPluginEnabled("Mimic")) {
        getLogger().severe("Mimic is required for the plugin!");
        return false;
    }

    // You can also check if Mimic version is right
    if (!MimicApiLevel.checkApiLevel(MimicApiLevel.VERSION_0_7)) {
        getLogger().severe("Required at least Mimic 0.7!");
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

Mimic uses `ServicesManager` to hold API implementations and each API implements interface [MimicService].
You can load needed APIs in `onEnable` of your plugin:
```java
Mimic mimic = Mimic.getInstance();

BukkitLevelSystem levelSystem = mimic.getLevelSystem(player);
BukkitClassSystem classSystem = mimic.getClassSystem(player);
BukkitItemsRegistry itemsRegistry = mimic.getItemsRegistry();
```

Now you can use APIs:
```java
// LevelSystem and ClassSystem holds a weak reference to a player object
// and should not live a long time.
int playerLevel = levelSystem.getLevel();
levelSystem.giveExp(42);

String playerPrimaryClass = classSystem.getPrimaryClass();
boolean isMage = classSystem.hasOneOfClasses(Arrays.asList("Mage", "Druid", "Necromancer"));

// ItemsRegistry is not related to player so it can be used without provider
ItemStack stick = itemsRegistry.getItem("minecraft:stick");
boolean isStickMagic = itemsRegistry.isSameItem(stick, "customitems:magic_wand");
boolean isMagicStickExists = itemsRegistry.isItemExists("customitems:magic_wand");
```

<details>
    <summary>Full example</summary>
    
    ```java
    import org.bukkit.entity.Player;
    import org.bukkit.inventory.ItemStack;
    import org.bukkit.plugin.ServicesManager;
    import org.bukkit.plugin.java.JavaPlugin;
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
            if (!MimicApiLevel.checkApiLevel(MimicApiLevel.VERSION_0_6)) {
                getLogger().severe("Required at least Mimic 0.6!");
                return false;
            }

            return true;
        }
    
        // Method to demonstrate usage
        private void useApis(Player player) {
            // LevelSystem and ClassSystem holds holds weak reference to player object and should not live a long time.
            BukkitLevelSystem levelSystem = getLevelSystem(player);
            int playerLevel = levelSystem.getLevel();
            levelSystem.giveExp(42);
    
            BukkitClassSystem classSystem = getClassSystem(player);
            String playerPrimaryClass = classSystem.getPrimaryClass();
            boolean isMage = classSystem.hasAnyOfClasses(Arrays.asList("Mage", "Druid", "Necromancer"));
    
            // ItemsRegistry is not related to player so it can be used without provider
            BukkitItemsRegistry itemsRegistry = getItemsRegistry();
            ItemStack stick = itemsRegistry.getItem("minecraft:stick");
            boolean isStickMagical = itemsRegistry.isSameItem(stick, "customitems:magic_wand");
            boolean isMagicStickExists = itemsRegistry.isItemExists("customitems:magic_wand");
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
        return playersClassesMap.get(getPlayer().getUniqueId());
    }
}
```

When you've implemented API, register it in method **`onLoad`** of your plugin:

```java
@Override
public void onLoad() {
    Mimic mimic = Mimic.getInstance();
    
    // Register your implementations and specify minimal required Mimic version for it.
    mimic.registerClassSystem(MyClassSystem::new, MimicApiLevel.CURRENT, this);
}
```

> Don't use **`onEnable`** to register services.
> Plugins should be able to get services in **`onEnable`** and all services should be already registered.

[ServicesManager]: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/plugin/ServicesManager.html

[MimicService]: ../mimic-api/src/main/kotlin/MimicService.kt
[BukkitLevelSystem.Provider]: src/main/kotlin/level/BukkitLevelSystem.kt
[BukkitClassSystem.Provider]: src/main/kotlin/classes/BukkitClassSystem.kt
[BukkitItemsRegistry]: src/main/kotlin/items/BukkitItemsRegistry.kt

[impl]: ../mimic-bukkit/src/main/kotlin/impl/
[mimic-bukkit]: ../mimic-bukkit/
