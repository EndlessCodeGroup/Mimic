/*
 * This file is part of Mimic.
 * Copyright (C) 2020 Osip Fatkullin
 * Copyright (C) 2020 EndlessCode Group and contributors
 *
 * Mimic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Mimic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Mimic.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.endlesscode.mimic.classes

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ClassSystemTest {

    class DummyClassSystem : ClassSystem {
        override var classes: List<String> = emptyList()

        fun prepareClasses(vararg classes: String) {
            this.classes = classes.toList()
        }
    }

    // SUT
    private val classSystem: DummyClassSystem = DummyClassSystem()

    @Test
    fun `when hasAnyClass - and there are class - should return true`() {
        classSystem.prepareClasses("SomeClass", "AnotherClass")
        classSystem.hasAnyClass().shouldBeTrue()
    }

    @Test
    fun `when hasAnyClass - and there no classes - should return false`() {
        classSystem.prepareClasses(/* empty classes list */)
        classSystem.hasAnyClass().shouldBeFalse()
    }

    @Test
    fun `when hasClass - and there are right class - should return true`() {
        classSystem.prepareClasses("First", "Second", "Third")
        classSystem.hasClass("First").shouldBeTrue()
    }

    @Test
    fun `when hasClass - and there no right class - should return false`() {
        classSystem.prepareClasses("First", "Second", "Third")
        classSystem.hasClass("UnknownClass").shouldBeFalse()
    }

    @Test
    fun `when hasOneOfClasses - and there are right class - should return true`() {
        classSystem.prepareClasses("First", "AnotherOne")
        classSystem.hasAnyOfClasses("First", "Second", "Third").shouldBeTrue()
    }

    @Test
    fun `when hasOneOfClasses - and there no right classes - should return false`() {
        classSystem.prepareClasses("AnotherOne")
        classSystem.hasAnyOfClasses("First", "Second", "Third").shouldBeFalse()
    }

    @Test
    fun `when hasAllClasses - and there are all classes - should return true`() {
        classSystem.prepareClasses("First", "Second", "Third")
        classSystem.hasAllClasses("First", "Second").shouldBeTrue()
    }

    @Test
    fun `when hasAllClasses - and there are not all classes - should return false`() {
        classSystem.prepareClasses("First", "Third")
        classSystem.hasAllClasses("First", "Second").shouldBeFalse()
    }

    @Test
    fun `when get primaryClass - and there no classes - should return null`() {
        classSystem.prepareClasses(/* empty classes list */)
        classSystem.primaryClass.shouldBeNull()
    }

    @Test
    fun `when get primaryClass - and there are classes - should return first class`() {
        classSystem.prepareClasses("PrimaryClass", "SecondaryClass")
        classSystem.primaryClass shouldBe "PrimaryClass"
    }
}
