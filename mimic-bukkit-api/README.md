# mimic-bukkit-api

Mimic API adopted for Bukkit.

## Usage

Mimic uses [ServicesManager] to store [MimicServices][MimicService].

Available services:
- [BukkitLevelSystem.Provider]
- [BukkitClassSystem.Provider]
- [BukkitItemsRegistry]

### How to use Mimic APIs?

To use any of APIs you should get it from `ServiceManager`:
```java
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import ru.endlesscode.mimic.classes.BukkitClassSystem;
import ru.endlesscode.mimic.items.BukkitItemsRegistry;
import ru.endlesscode.mimic.level.BukkitLevelSystem;

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
        // Check that Mimic exists before API usage.
        // You can also add `Mimic` to `depend` list in plugin.yml
        if (!checkMimic()) {
            getLogger().severe("Mimic is required for the plugin!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        setupMimic();
    }

    private boolean checkMimic() {
        return getServer().getPluginManager().isPluginEnabled("Mimic");
    }

    private void setupMimic() {
        ServicesManager sm = getServer().getServicesManager();
        // Services will never be null because there always exists default
        // implementation for each service
        levelSystemProvider = sm.load(BukkitLevelSystem.Provider.class);
        classSystemProvider = sm.load(BukkitClassSystem.Provider.class);
        itemsRegistry = sm.load(BukkitItemsRegistry.class);
    }
}
```

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
