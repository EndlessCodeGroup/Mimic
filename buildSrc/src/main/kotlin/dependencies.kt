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

object Dependencies {
    // RPG Plugins
    const val skillapi = "com.sucy:SkillAPI:3.102"
    const val battlelevels = "me.robin.battlelevels:battlelevels-api:6.9.1"

    // Libs
    const val bstats_bukkit = "org.bstats:bstats-bukkit:1.7"

    // Testing
    private const val jupiter_version = "5.6.2"
    const val jupiter = "org.junit.jupiter:junit-jupiter:$jupiter_version"
    const val jupiter_params = "org.junit.jupiter:junit-jupiter-params:$jupiter_version"
    const val mockito = "com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0"
    const val mockito_inline = "org.mockito:mockito-inline:2.23.0"
}

object acf {
    private const val version = "0.5.0-SNAPSHOT"
    const val paper = "co.aikar:acf-paper:$version"
    const val brigadier = "co.aikar:acf-brigadier:$version"
}

// Aliases
val deps = Dependencies
