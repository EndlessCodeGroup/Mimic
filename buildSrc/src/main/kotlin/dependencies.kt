@file:Suppress("ClassName")

object rpgplugins {
    const val skillapi = "com.sucy:SkillAPI:3.102"
    const val battlelevels = "me.robin.battlelevels:battlelevels-api:6.9.1"
    const val mmoCore = "net.Indyuce:MMOCore:1.9"
    const val mmoItems = "net.Indyuce:MMOItems:6.7"
    const val mythicLib = "io.lumine:MythicLib-dist:1.3" 
    const val heroes = ":Heroes:1.9.9"
}

object misc {
    const val bstats = "org.bstats:bstats-bukkit:3.0.0"
    const val annotations = "org.jetbrains:annotations:23.0.0"
    const val serialization_hocon = "org.jetbrains.kotlinx:kotlinx-serialization-hocon:1.3.2"
    const val mockk = "io.mockk:mockk:1.12.3"
    const val adventure = "net.kyori:adventure-platform-bukkit:4.3.1"

    private const val commandapi_version = "9.2.0"
    const val commandapi = "dev.jorel:commandapi-bukkit-shade:$commandapi_version"
    const val commandapi_kotlin = "dev.jorel:commandapi-bukkit-kotlin:$commandapi_version"
}

// Testing
object junit {
    private const val jupiter_version = "5.7.2"
    const val jupiter = "org.junit.jupiter:junit-jupiter:$jupiter_version"
    const val jupiter_params = "org.junit.jupiter:junit-jupiter-params:$jupiter_version"
}

object kotest {
    private const val version = "5.2.3"
    const val assertions = "io.kotest:kotest-assertions-core:$version"
}
