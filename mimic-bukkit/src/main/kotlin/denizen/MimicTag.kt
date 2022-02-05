package ru.endlesscode.mimic.denizen

import com.denizenscript.denizencore.objects.Fetchable
import com.denizenscript.denizencore.objects.ObjectTag
import com.denizenscript.denizencore.tags.TagContext


public class MimicTag(
    private var prefix: String,
) : ObjectTag {

    override fun getPrefix(): String {
        return prefix;
    }

    override fun setPrefix(p0: String?): ObjectTag {
        prefix = p0!!;
        return this;
    }

    override fun isUnique(): Boolean {
        return true;
    }

    override fun getObjectType(): String {
        return "Mimic"
    }

    override fun identify(): String {
        return "mimic@" +
    }

    override fun identifySimple(): String {
        TODO("Not yet implemented")
    }



    companion object {
        @Fetchable("faction")
        @JvmStatic
        fun valueOf(string: String?, context: TagContext?): MimicTag? {
            var string = string ?: return null

            ////////
            // Match faction name
            string = string.replace("mimic@", "")
            val faction: Faction = FactionColl.get().getByName(string)
            return if (faction != null) {
                FactionTag(faction)
            } else null
        }
    }
}