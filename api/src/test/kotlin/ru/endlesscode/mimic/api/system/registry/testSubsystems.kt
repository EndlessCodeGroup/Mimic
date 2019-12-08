package ru.endlesscode.mimic.api.system.registry

import ru.endlesscode.mimic.api.system.ClassSystem
import ru.endlesscode.mimic.api.system.LevelSystem
import ru.endlesscode.mimic.api.system.MimicSystem
import kotlin.test.fail


/** Correct implementation of LevelSystem */
@Subsystem(priority = SubsystemPriority.LOWEST)
abstract class CorrectLevelSystem : LevelSystem {
    companion object {
        @JvmField
        val FACTORY = LevelSystem.Factory<CorrectLevelSystem>("TAG") { fail() }
    }
}

/** Basic implementation of class system. */
@Subsystem(classes = ["ru.endlesscode.mimic.api.system.ClassSystem"])
abstract class CorrectClassSystem : ClassSystem {
    companion object {
        @JvmField
        val FACTORY = ClassSystem.Factory<CorrectClassSystem>("TAG") { fail() }
    }
}

/** Wrong implementation of class system, that requires not existing class. */
@Subsystem(
    priority = SubsystemPriority.HIGH,
    classes = ["ru.endlesscode.mimic.api.WrongClass"]
)
abstract class NotNeededClassSystem : ClassSystem

/** Wring player system, that hasn't factory inner class. */
abstract class MissingFactorySystem private constructor() : MimicSystem {
    @Suppress("unused")
    abstract class NotFactoryInnerClass
}
