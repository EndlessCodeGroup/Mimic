/*
 * This file is part of BukkitMimic.
 * Copyright (C) 2020 Osip Fatkullin
 * Copyright (C) 2020 EndlessCode Group and contributors
 *
 * BukkitMimic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BukkitMimic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BukkitMimic.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.endlesscode.mimic.impl.skillapi

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import ru.endlesscode.mimic.classes.ClassSystem
import kotlin.test.Test

class SkillApiClassSystemTest : SkillApiTestBase() {

    private val classSystem: ClassSystem = SkillApiClassSystem(player, skillApi)

    @Test
    fun `when get classes - should return right classes`() {
        val expectedClasses = setOf("Mage", "Cleric")
        prepareClasses(expectedClasses)

        classSystem.classes.shouldContainExactly(expectedClasses)
    }

    @Test
    fun `when there no classes - should return empty list of classes`() {
        prepareClasses(/* empty classes list */)

        assertSoftly {
            classSystem.classes.shouldBeEmpty()
            classSystem.primaryClass.shouldBeNull()
        }
    }

    @Test
    fun `when get primary class - should return right class`() {
        prepareClasses("Primary", "Secondary", "Third")
        classSystem.primaryClass shouldBe "Primary"
    }
}
