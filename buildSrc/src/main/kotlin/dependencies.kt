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
    const val bstats = "org.bstats:bstats-bukkit:3.0.2"
    const val annotations = "org.jetbrains:annotations:24.1.0"
    const val serialization_hocon = "org.jetbrains.kotlinx:kotlinx-serialization-hocon:1.6.1"
    const val mockk = "io.mockk:mockk:1.13.8"
    const val adventure = "net.kyori:adventure-platform-bukkit:4.3.1"

    private const val commandapi_version = "9.2.0"
    const val commandapi = "dev.jorel:commandapi-bukkit-shade:$commandapi_version"
    const val commandapi_kotlin = "dev.jorel:commandapi-bukkit-kotlin:$commandapi_version"
}

// Testing
object junit {
    const val bom = "org.junit:junit-bom:5.10.1"
    const val jupiter = "org.junit.jupiter:junit-jupiter"
    const val jupiter_params = "org.junit.jupiter:junit-jupiter-params"
}

object kotest {
    private const val version = "5.8.0"
    const val assertions = "io.kotest:kotest-assertions-core:$version"
}
