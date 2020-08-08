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
    fun `when hasAnyClass - and there are class - should return true`() {
        // Given
        classSystem.prepareClasses("SomeClass", "AnotherClass")

        // When
        val hasClass = classSystem.hasAnyClass()

        // Then
        assertTrue(hasClass)
    }

    @Test
    fun `when hasAnyClass - and there no classes - should return false`() {
        // Given
        classSystem.prepareClasses()

        // When
        val hasAnyClass = classSystem.hasAnyClass()

        // Then
        assertFalse(hasAnyClass)
    }

    @Test
    fun `when hasClass - and there are right class - should return true`() {
        // Given
        classSystem.prepareClasses("First", "Second", "Third")

        // When
        val hasClass = classSystem.hasClass("First")

        // Then
        assertTrue(hasClass)
    }

    @Test
    fun `when hasClass - and there no right class - should return false`() {
        // Given
        classSystem.prepareClasses("First", "Second", "Third")

        // When
        val hasClass = classSystem.hasClass("UnknownClass")

        // Then
        assertFalse(hasClass)
    }

    @Test
    fun `when hasOneOfClasses - and there are right class - should return true`() {
        // Given
        val requiredClasses = listOf("First", "Second", "Third")
        classSystem.prepareClasses("First", "AnotherOne")

        // When
        val hasOneOfClasses = classSystem.hasAnyOfClasses(requiredClasses)

        // Then
        assertTrue(hasOneOfClasses)
    }

    @Test
    fun `when hasOneOfClasses - and there no right classes - should return false`() {
        // Given
        val requiredClasses = listOf("First", "Second", "Third")
        classSystem.prepareClasses("AnotherOne")

        // When
        val hasOneOfClasses= classSystem.hasAnyOfClasses(requiredClasses)

        // Then
        assertFalse(hasOneOfClasses)
    }

    @Test
    fun `when hasAllClasses - and there are all classes - should return true`() {
        // Given
        val requiredClasses = listOf("First", "Second")
        classSystem.prepareClasses("First", "Second", "Third")

        // When
        val hasAllClasses = classSystem.hasAllClasses(requiredClasses)

        // Then
        assertTrue(hasAllClasses)
    }

    @Test
    fun `when hasAllClasses - and there are not all classes - should return false`() {
        // Given
        val requiredClasses = listOf("First", "Second")
        classSystem.prepareClasses("First", "Third")

        // When
        val hasAllClasses = classSystem.hasAllClasses(requiredClasses)

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
