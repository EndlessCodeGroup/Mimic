// Copyright (c) 2017 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

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
