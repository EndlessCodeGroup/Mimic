package ru.endlesscode.mimic.bukkit.command

import co.aikar.commands.CommandHelp
import co.aikar.commands.MimicCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.HelpCommand

@CommandAlias("%command")
@CommandPermission("%perm")
internal class MainCommand : MimicCommand() {
    @HelpCommand
    @Description("Show help")
    fun doHelp(help: CommandHelp) {
        help.showHelp()
    }
}
