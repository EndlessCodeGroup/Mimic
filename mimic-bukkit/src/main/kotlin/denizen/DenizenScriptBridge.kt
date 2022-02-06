package ru.endlesscode.mimic.denizen

import com.denizenscript.denizen.objects.PlayerTag
import com.denizenscript.denizencore.objects.Mechanism
import com.denizenscript.denizencore.objects.ObjectTag
import com.denizenscript.denizencore.objects.core.ElementTag
import com.denizenscript.denizencore.objects.properties.Property
import com.denizenscript.denizencore.objects.properties.PropertyParser
import com.denizenscript.denizencore.tags.Attribute
import com.sucy.skill.SkillAPI
import org.bukkit.Bukkit
import ru.endlesscode.mimic.classes.BukkitClassSystem
import ru.endlesscode.mimic.classes.ClassSystem
import ru.endlesscode.mimic.level.BukkitLevelSystem
import ru.endlesscode.mimic.level.LevelSystem


public class DenizenScriptBridge {

    public companion object {

        public fun hook() {
            PropertyParser.registerProperty(MimicPlayerTagClassProperties::class.java, PlayerTag::class.java)
        }
    }

    public class MimicPlayerTagClassProperties private constructor(
            public var player: PlayerTag
            ) : Property {

        override fun getPropertyString(): String? {
            return null
        }

        override fun getPropertyId(): String {
            return "MimicPlayerClass"
        }

        override fun adjust(mechanism: Mechanism) {}

        private fun classSystem(): ClassSystem? {
            val provider = Bukkit.getServicesManager().getRegistration(BukkitClassSystem.Provider::class.java);
            return provider?.provider?.getSystem(player.playerEntity)
        }

        private fun levelSystem(): LevelSystem {
            val provider = Bukkit.getServicesManager().getRegistration(BukkitLevelSystem.Provider::class.java);
            return provider!!.provider.getSystem(player.playerEntity)
        }

        override fun getAttribute(a: Attribute): String? {
            if (a.startsWith("mimic")) {

               val attribute = a.fulfill(1)

                // <--[tag]
                // @attribute <PlayerTag.mimic.has_class[classname]>
                // @returns ElementTag(Boolean)
                // @description
                // Returns whether the player has specific class
                // -->
                if (attribute.startsWith("has_class")) {
                    if (!attribute.hasParam()) {
                        attribute.echoError("has_class requires a class name argument");
                        return null;
                    }
                    return ElementTag(classSystem()!!.hasClass(attribute.param)).getAttribute(attribute.fulfill(1))
                }

                // <--[tag]
                // @attribute <PlayerTag.mimic.primary_class>
                // @returns ElementTag(String)
                // @description
                // Returns primary class
                // -->
                if (attribute.startsWith("primary_class")) {
                    return ElementTag(classSystem()!!.primaryClass).getAttribute(attribute.fulfill(1));
                }

                // <--[tag]
                // @attribute <PlayerTag.mimic.class_level[class_name]>
                // @returns ElementTag(Int)
                // @description
                // Returns the player class level
                // -->
                if (attribute.startsWith("class_level")) {
                    //todo level system supports getting only primary class level
                    //if (!attribute.hasParam()) {
                    //    attribute.echoError("has_class requires a class name argument");
                    //    return null;
                    //}
                    return ElementTag(levelSystem().level).getAttribute(attribute.fulfill(1))
                }

                // <--[tag]
                // @attribute <PlayerTag.mimic.class_give_exp[class, int]>
                // @returns ElementTag(String)
                // @description
                // Returns level after exp were given
                // -->
                if (attribute.startsWith("class_give_exp")) {
                    return ElementTag(levelSystem().level).getAttribute(attribute.fulfill(1));
                }
            }

            return null
        }

        public companion object {
            public fun describes(`object`: ObjectTag?): Boolean {
                return `object` is PlayerTag
            }

            public fun getFrom(`object`: ObjectTag): MimicPlayerTagClassProperties? {
                return if (!describes(`object`)) {
                    null
                } else {
                    MimicPlayerTagClassProperties(`object` as PlayerTag)
                }
            }

            public val handledTags: Array<String> = arrayOf(
                    "mimic",
            )
            public val handledMechs: Array<String> = arrayOf<String>()
        }
    }
}