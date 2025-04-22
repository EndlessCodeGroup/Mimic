package ru.endlesscode.mimic.command

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.CommandSender
import org.bukkit.command.defaults.BukkitCommand
import ru.endlesscode.mimic.internal.append
import ru.endlesscode.mimic.internal.appendLine
import ru.endlesscode.mimic.internal.text

internal class FallbackCommand(
    private val pluginFullName: String
) : BukkitCommand(
    COMMAND_NAME,
    COMMAND_INFO_DESCRIPTION,
    "/$COMMAND_NAME",
    emptyList(),
) {

    override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>?): Boolean {
        val message = text {
            appendLine(pluginFullName, NamedTextColor.GREEN)
            color(NamedTextColor.GRAY)
            append("Install ")
            append(commandApiLink())
            append(" to unlock all Mimic commands.")
        }
        sender.sendMessage(message)
        return true
    }

    private fun commandApiLink() = text {
        color(NamedTextColor.YELLOW)
        append("CommandAPI", TextDecoration.UNDERLINED)
        hoverEvent(HoverEvent.showText(Component.text("Open CommandAPI website")))
        clickEvent(ClickEvent.openUrl("https://docs.commandapi.dev/user-setup/install"))
    }
}
