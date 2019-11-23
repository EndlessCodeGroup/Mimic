package co.aikar.commands

internal typealias AbstractCommandManager =
    CommandManager<*, *, *, out MessageFormatter<*>, out CommandExecutionContext<*, *>, out ConditionContext<*>>

internal abstract class Command : BaseCommand() {

    override fun onRegister(manager: AbstractCommandManager) {
        super.onRegister(manager)
        afterRegister(manager)
    }

    protected open fun afterRegister(manager: AbstractCommandManager) {
        // nothing by default
    }
}
