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

object Versions {
    const val skillApi = "3.102"
    const val battleLevels = "6.9.1"

    const val acf = "0.5.0-SNAPSHOT"

    const val junit = "4.12"
    const val mockitoKotlin = "2.2.0"
    const val mockito = "2.23.0"
}

object Dependencies {
    // RPG Plugins
    const val skillApi = "com.sucy:SkillAPI:${versions.skillApi}"
    const val battleLevelsApi = "me.robin.battlelevels:battlelevels-api:${versions.battleLevels}"

    // Libs
    const val acf = "co.aikar:acf-paper:${versions.acf}"

    // Testing
    const val junit = "junit:junit:${versions.junit}"
    const val mockito = "com.nhaarman.mockitokotlin2:mockito-kotlin:${versions.mockitoKotlin}"
    const val mockitoInline = "org.mockito:mockito-inline:${versions.mockito}"
}

// Aliases
val versions = Versions
val deps = Dependencies
