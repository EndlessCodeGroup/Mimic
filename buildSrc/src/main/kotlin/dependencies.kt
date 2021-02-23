@file:Suppress("ClassName")
/*
* This file is part of RPGInventory.
* Copyright (C) 2019 EndlessCode Group and contributors
*
* RPGInventory is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* RPGInventory is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with RPGInventory.  If not, see <http://www.gnu.org/licenses/>.
*/

object rpgplugins {
    const val skillapi = "com.sucy:SkillAPI:3.102"
    const val battlelevels = "me.robin.battlelevels:battlelevels-api:6.9.1"
    const val mmoCore = "net.Indyuce:MMOCore:1.7.0"
    const val mmoLib = "net.Indyuce:MMOLib:1.7.3"
    const val mmoItems = "net.Indyuce:MMOItems:6.5.5"
    const val mythicLib = "io.lumine:MythicLib:1.0.10"
    const val heroes = "com.herocraftonline.heroes:Heroes:1.9.6-SNAPSHOT:stripped"
}

object acf {
    const val paper = "co.aikar:acf-paper:0.5.0-SNAPSHOT"
}

object misc {
    const val bstats = "org.bstats:bstats-bukkit:1.8"
}

// Testing
object junit {
    private const val jupiter_version = "5.7.0"
    const val jupiter = "org.junit.jupiter:junit-jupiter:$jupiter_version"
    const val jupiter_params = "org.junit.jupiter:junit-jupiter-params:$jupiter_version"
}

object mockito {
    const val kotlin = "com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0"
    const val inline = "org.mockito:mockito-inline:2.23.0"
}
