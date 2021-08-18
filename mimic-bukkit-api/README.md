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
    if (!MimicApiLevel.checkApiLevel(MimicApiLevel.VERSION_0_6)) {
        getLogger().severe("Required at least Mimic 0.6!");
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
ServicesManager sm = getServer().getServicesManager();
// Services will never be null because there always exists default
// implementation for each service
levelSystemProvider = sm.load(BukkitLevelSystem.Provider.class);
classSystemProvider = sm.load(BukkitClassSystem.Provider.class);
itemsRegistry = sm.load(BukkitItemsRegistry.class);
```

Now you can use APIs:
```java
// System got from the provider holds a weak reference to a player object
// and should not live a long time.
BukkitLevelSystem levelSystem = levelSystemProvider.get(player);
int playerLevel = levelSystem.getLevel();
levelSystem.giveExp(42);

BukkitClassSystem classSystem = classSystemProvider.get(player);
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
    
        // Declare field for needed APIs
        private static BukkitLevelSystem.Provider levelSystemProvider = null;
        private static BukkitClassSystem.Provider classSystemProvider = null;
        private static BukkitItemsRegistry itemsRegistry = null;
    
        public static BukkitLevelSystem getLevelSystem(Player player) {
            return levelSystemProvider.get(player);
        }
    
        public static BukkitClassSystem getClassSystem(Player player) {
            return classSystemProvider.get(player);
        }
    
        public static BukkitItemsRegistry getItemsRegistry() {
            return itemsRegistry;
        }
    
        @Override
        public void onEnable() {
            if (!checkMimicEnabled()) {
                getLogger().severe("Download latest version here: https://www.spigotmc.org/resources/82515/");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
    
            setupMimic();
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
    
        private void setupMimic() {
            ServicesManager sm = getServer().getServicesManager();
            // Services will never be null because there always exists default
            // implementation for each service
            levelSystemProvider = sm.load(BukkitLevelSystem.Provider.class);
            classSystemProvider = sm.load(BukkitClassSystem.Provider.class);
            itemsRegistry = sm.load(BukkitItemsRegistry.class);
        }
    
        // Method to demonstrate usage
        private void useApis(Player player) {
            // System got from provider holds weak reference to player object and should not live a long time.
            BukkitLevelSystem levelSystem = levelSystemProvider.get(player);
            int playerLevel = levelSystem.getLevel();
            levelSystem.giveExp(42);
    
            BukkitClassSystem classSystem = classSystemProvider.get(player);
            String playerPrimaryClass = classSystem.getPrimaryClass();
            boolean isMage = classSystem.hasAnyOfClasses(Arrays.asList("Mage", "Druid", "Necromancer"));
    
            // ItemsRegistry is not related to player so it can be used without provider
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

    // Level and Class systems are bounded to players so you need to create provider.
    // It will provide instance of your implementation initialized with player when need.
    static class Provider extends BukkitClassSystem.Provider {

        public Provider() {
            super(ID); // Specify your implementation ID here
        }

        @NotNull
        @Override
        public BukkitClassSystem getSystem(@NotNull Player player) {
            return new MyClassSystem(player);
        }
    }
}
```

When you've implemented API, register it in method **`onLoad`** of your plugin:
```java
@Override
public void onLoad() {
    ServicesManager sm = getServer().getServicesManager();
    
    // Register you Provider in service manager with priority you want.
    // Services with higher priority will be used first.
    sm.register(BukkitClassSystem.Provider.class, new MyClassSystem.Provider(), this, ServicePriority.High);
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
