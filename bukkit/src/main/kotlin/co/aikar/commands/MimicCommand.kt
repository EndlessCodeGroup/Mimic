package co.aikar.commands

internal typealias AbstractCommandManager =
    CommandManager<*, *, *, out MessageFormatter<*>, out CommandExecutionContext<*, *>, out ConditionContext<*>>

// This class stored in this package because onRegister is package-private method
internal abstract class MimicCommand : BaseCommand() {

    final override fun onRegister(manager: AbstractCommandManager) {
        super.onRegister(manager)
        afterRegister(manager)
    }

    protected open fun afterRegister(manager: AbstractCommandManager) {
        // nothing by default
    }
}
