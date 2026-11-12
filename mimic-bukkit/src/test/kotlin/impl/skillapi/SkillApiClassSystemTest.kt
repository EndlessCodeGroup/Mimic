// Copyright (c) 2018 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

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
