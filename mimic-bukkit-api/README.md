# mimic-bukkit-api

Mimic API adopted for Bukkit.

## Usage

Mimic uses [ServiceManager] to store [MimicServices][MimicService].

Available services:
- [BukkitLevelSystem.Provider]
- [BukkitClassSystem.Provider]
- [BukkitItemsRegistry]

To use any of services you should get it from `ServiceManager`:
```java
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.endlesscode.mimic.bukkit.BukkitClassSystem;
import ru.endlesscode.mimic.bukkit.BukkitItemsRegistry;
import ru.endlesscode.mimic.bukkit.BukkitLevelSystem;

public class MyPlugin extends JavaPlugin {

    private static BukkitLevelSystem.Provider levelSystemProvider = null;
    private static BukkitClassSystem.Provider classSystemProvider = null;
    private static BukkitItemsRegistry itemsRegistry = null;

    @Override
    public void onEnable() {
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
        // Services will never be null because there always exists default vanilla
        // implementation for each service
        levelSystemProvider = sm.load(BukkitLevelSystem.Provider.class);
        classSystemProvider = sm.load(BukkitClassSystem.Provider.class);
        itemsRegistry = sm.load(BukkitItemsRegistry.class);
    }

    public static BukkitLevelSystem getLevelSystem(Player player) {
        return levelSystemProvider.get(player);
    }

    public static BukkitClassSystem getClassSystem(Player player) {
        return classSystemProvider.get(player);
    }
    
    public static BukkitItemsRegistry getItemsRegistry() {
        return itemsRegistry;
    }
}
```

[ServicesManager]: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/plugin/ServicesManager.html

[MimicService]: ../mimic-api/src/main/kotlin/MimicService.kt
[BukkitLevelSystem.Provider]: src/main/kotlin/BukkitLevelSystem.kt
[BukkitClassSystem.Provider]: src/main/kotlin/BukkitClassSystem.kt
[BukkitItemsRegistry]: src/main/kotlin/BukkitItemsRegistry.kt
