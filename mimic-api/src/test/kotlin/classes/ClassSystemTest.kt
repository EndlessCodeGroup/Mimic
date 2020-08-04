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

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import ru.endlesscode.mimic.mockito.MOCKS_ONLY_ABSTRACTS
import kotlin.test.*

class ClassSystemTest {

    // SUT
    private lateinit var classSystem: ClassSystem

    @BeforeTest
    fun setUp() {
        classSystem = mock(defaultAnswer = MOCKS_ONLY_ABSTRACTS)
    }

    @Test
    fun `when hasClass - and there are class - should return true`() {
        // Given
        classSystem.prepareClasses("SomeClass", "AnotherClass")

        // When
        val hasClass = classSystem.hasClass()

        // Then
        assertTrue(hasClass)
    }

    @Test
    fun `when hasClass - and there no classes - should return false`() {
        // Given
        classSystem.prepareClasses()

        // When
        val hasClass = classSystem.hasClass()

        // Then
        assertFalse(hasClass)
    }

    @Test
    fun `when hasRequiredClass - and there are right class - should return true`() {
        // Given
        classSystem.prepareClasses("First", "Second", "Third")

        // When
        val hasRequiredClass = classSystem.hasRequiredClass("First")

        // Then
        assertTrue(hasRequiredClass)
    }

    @Test
    fun `when hasRequiredClass - and there no right class - should return false`() {
        // Given
        classSystem.prepareClasses("First", "Second", "Third")

        // When
        val hasRequiredClass = classSystem.hasRequiredClass("UnknownClass")

        // Then
        assertFalse(hasRequiredClass)
    }

    @Test
    fun `when hasOneOfRequiredClasses - and there are right class - should return true`() {
        // Given
        val requiredClasses = listOf("First", "Second", "Third")
        classSystem.prepareClasses("First", "AnotherOne")

        // When
        val hasOneOfRequired = classSystem.hasOneOfRequiredClasses(requiredClasses)

        // Then
        assertTrue(hasOneOfRequired)
    }

    @Test
    fun `when hasOneOfRequiredClasses - and there no right classes - should return false`() {
        // Given
        val requiredClasses = listOf("First", "Second", "Third")
        classSystem.prepareClasses("AnotherOne")

        // When
        val hasOneOfRequired = classSystem.hasOneOfRequiredClasses(requiredClasses)

        // Then
        assertFalse(hasOneOfRequired)
    }

    @Test
    fun `when hasAllRequiredClasses - and there are all classes - should return true`() {
        // Given
        val requiredClasses = listOf("First", "Second")
        classSystem.prepareClasses("First", "Second", "Third")

        // When
        val hasAllClasses = classSystem.hasAllRequiredClasses(requiredClasses)

        // Then
        assertTrue(hasAllClasses)
    }

    @Test
    fun `when hasAllRequiredClasses - and there are not all classes - should return false`() {
        // Given
        val requiredClasses = listOf("First", "Second")
        classSystem.prepareClasses("First", "Third")

        // When
        val hasAllClasses = classSystem.hasAllRequiredClasses(requiredClasses)

        // Then
        assertFalse(hasAllClasses)
    }

    @Test
    fun `when get primaryClass - and there no classes - should return null`() {
        // Given
        classSystem.prepareClasses()

        // When
        val primaryClass = classSystem.primaryClass

        // Then
        assertNull(primaryClass)
    }

    @Test
    fun `when get primaryClass - and there are classes - should return first class`() {
        // Given
        val expectedClass = "PrimaryClass"
        classSystem.prepareClasses(expectedClass, "SecondaryClass")

        // When
        val primaryClass = classSystem.primaryClass

        // Then
        assertEquals(expectedClass, primaryClass)
    }

    private fun ClassSystem.prepareClasses(vararg classes: String) {
        whenever(this.classes).thenReturn(classes.toList())
    }
}
