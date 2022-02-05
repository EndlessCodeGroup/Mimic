package ru.endlesscode.mimic.denizen

import org.bukkit.Bukkit
import org.bukkit.Registry
import ru.endlesscode.mimic.MimicPlugin
import ru.endlesscode.mimic.classes.BukkitClassSystem

public class DenizenScriptBridge {

    public companion object {

        @JvmStatic
        public fun ínit(mimicPlugin: MimicPlugin) {

        }

        @JvmStatic
        public fun hookClassSystem(mimicPlugin: MimicPlugin) {
            val registration = Bukkit.getServicesManager().getRegistration(BukkitClassSystem::class.java) ?: return
            val provider = registration.provider ?: return

            provider.
        }
    }
}