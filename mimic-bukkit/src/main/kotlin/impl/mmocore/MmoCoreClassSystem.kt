package ru.endlesscode.mimic.impl.mmocore

import net.Indyuce.mmocore.api.player.profess.PlayerClass
import org.bukkit.entity.Player
import ru.endlesscode.mimic.classes.BukkitClassSystem

/**
 * Implementation of ClassSystem using MMOCore.
 *
 * Supports one class per player.
 */
public class MmoCoreClassSystem private constructor(
    player: Player,
    private val mmoCore: MmoCoreWrapper,
) : BukkitClassSystem(player) {

    public companion object {
        public const val ID: String = "mmocore"
    }

    override val classes: List<String>
        get() = listOf(playerClass.name)

    private val playerClass: PlayerClass
        get() = mmoCore.getPlayerClass(player)

    internal class Provider : BukkitClassSystem.Provider(ID) {

        private val mmoCore = MmoCoreWrapper()

        override val isEnabled: Boolean
            get() = mmoCore.isEnabled

        override fun getSystem(player: Player): BukkitClassSystem = MmoCoreClassSystem(player, mmoCore)
    }
}