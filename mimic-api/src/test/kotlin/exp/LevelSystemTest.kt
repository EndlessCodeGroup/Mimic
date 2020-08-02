/*
 * This file is part of MimicAPI.
 * Copyright (C) 2018 Osip Fatkullin
 * Copyright (C) 2018 EndlessCode Group and contributors
 *
 * MimicAPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MimicAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with MimicAPI.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.endlesscode.mimic.exp

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import ru.endlesscode.mimic.LevelSystem
import ru.endlesscode.mimic.mockExpLevelConverter
import ru.endlesscode.mimic.mockito.MOCKS_ONLY_ABSTRACTS
import ru.endlesscode.mimic.mockito.doubleEq
import kotlin.test.*

class LevelSystemTest {

    // SUT
    private lateinit var ls: LevelSystem

    @BeforeTest
    fun setUp() {
        val converter = mockExpLevelConverter()
        ls = mock(defaultAnswer = MOCKS_ONLY_ABSTRACTS) {
            on(it.converter) doReturn converter
        }
    }

    @Test
    fun `when increase level - should set right level`() {
        // Given
        ls.prepare(level = 1)

        // When
        ls.giveLevel(4)

        // Then
        verify(ls).level = 5
    }

    @Test
    fun `when decrease level - should set right level`() {
        // Given
        ls.prepare(level = 6)

        // When
        ls.takeLevel(4)

        // Then
        verify(ls).level = 2
    }

    @Test
    fun `when decrease level by more than current level - should set zero level`() {
        // Given
        ls.prepare(level = 6)

        // When
        ls.takeLevel(10)

        // Then
        verify(ls).level = 0
    }

    @Test
    fun `when check did reach level lower than current - should return true`() {
        // Given
        ls.prepare(level = 10)

        // When
        val didReachLevel = ls.didReachLevel(5)

        // Then
        assertTrue(didReachLevel)
    }

    @Test
    fun `when check did reach negative level - should return true`() {
        // Given
        ls.prepare(level = 10)

        // When
        val didReachLevel = ls.didReachLevel(-1)

        // Then
        assertTrue(didReachLevel)
    }

    @Test
    fun `when check did reach higher level - should return false`() {
        // Given
        ls.prepare(level = 10)

        // When
        val didReachLevel = ls.didReachLevel(11)

        // Then
        assertFalse(didReachLevel)
    }

    @Test
    fun `when check did reach same level as current - should return true`() {
        // Given
        ls.prepare(level = 10)

        // When
        val didReachLevel = ls.didReachLevel(10)

        // Then
        assertTrue(didReachLevel)
    }

    @Test
    fun `when get fractional exp - and exp is zero - should return zero`() {
        // Given
        ls.prepare(exp = 0.0)

        // When
        val fractionalExp = ls.fractionalExp

        // Then
        assertEquals(0.0, fractionalExp)
    }

    @Test
    fun `when get fractional exp - and exp is negative - should return zero`() {
        // Given
        ls.prepare(exp = -1.0)

        // When
        val fractionalExp = ls.fractionalExp

        // Then
        assertEquals(0.0, fractionalExp)
    }

    @Test
    fun `when get fractional exp - should return right value`() {
        // Given
        ls.prepare(exp = 5.12)

        // When
        val fractionalExp = ls.fractionalExp

        // Then
        assertEquals(0.512, fractionalExp)
    }

    @Test
    fun `when get fractional exp - and exp is higher than possible at current level - should return value close to 1`() {
        // Given
        ls.prepare(exp = 11.0)

        // When
        val fractionalExp = ls.fractionalExp

        // Then
        assertEquals(0.9, fractionalExp)
    }

    @Test
    fun `when set negative fractional exp - should set ext to zero`() {
        // When
        ls.fractionalExp = -1.0

        // Then
        verify(ls).exp = doubleEq(0.0)
    }

    @Test
    fun `when set some fractional exp - should set right exp`() {
        // When
        ls.fractionalExp = 0.512

        // Then
        verify(ls).exp = doubleEq(5.12)
    }

    @Test
    fun `when set full fractional exp - should set next level and reset exp`() {
        // When
        ls.fractionalExp = 1.0

        // Then
        verify(ls).level = 1
        verify(ls).exp = doubleEq(0.0)
    }

    @Test
    fun `when set more than full fractional exp - should set next level and reset exp`() {
        // When
        ls.fractionalExp = 2.0

        // Then
        verify(ls).level = 1
        verify(ls).exp = doubleEq(0.0)
    }

    @Test
    fun `when get total exp - and has zero exp and level - should return zero`() {
        // Given
        ls.prepare(level = 0, exp = 0.0)

        // When
        val totalExp = ls.totalExp

        // Then
        assertEquals(0.0, totalExp)
    }

    @Test
    fun `when get total exp - and has exp and level - should return right value`() {
        // Given
        ls.prepare(level = 2, exp = 12.0)

        // When
        val totalExp = ls.totalExp

        // Then
        assertEquals(42.0, totalExp)
    }

    @Test
    fun `when set negative total exp - should set zero level and exp`() {
        // When
        ls.totalExp = -10.0

        // Then
        verify(ls).level = 0
        verify(ls).exp = doubleEq(0.0)
    }

    @Test
    fun `when set zero total exp - should set zero level and exp`() {
        // When
        ls.totalExp = 0.0

        // Then
        verify(ls).level = 0
        verify(ls).exp = doubleEq(0.0)
    }

    @Test
    fun `when set some total exp - should set right level and exp`() {
        // When
        ls.totalExp = 42.0

        // Then
        verify(ls).level = 2
        verify(ls).exp = doubleEq(12.0)
    }

    @Test
    fun `when check has exact total experience - should return true`() {
        // Given
        ls.prepare(level = 1, exp = 5.0)

        // When
        val hasTotalExp = ls.hasExpTotal(15.0)

        // Then
        assertTrue(hasTotalExp)
    }

    @Test
    fun `when check has less than total experience - should return true`() {
        // Given
        ls.prepare(level = 1, exp = 5.0)

        // When
        val hasTotalExp = ls.hasExpTotal(5.0)

        // Then
        assertTrue(hasTotalExp)
    }

    @Test
    fun `when check has more than total experience - should return false`() {
        // Given
        ls.prepare(level = 1, exp = 5.0)

        // When
        val hasTotalExp = ls.hasExpTotal(18.0)

        // Then
        assertFalse(hasTotalExp)
    }

    @Test
    fun `when give not more exp than needed for next level - should keep level and increase exp`() {
        // Given
        ls.prepare(level = 10, exp = 5.0)

        // When
        ls.giveExp(3.0)

        // Then
        verify(ls).level = 10
        verify(ls).exp = doubleEq(8.0)
    }

    @Test
    fun `when give exp equal to needed for next level - should increase level and set exp to zero`() {
        // Given
        ls.prepare(level = 1, exp = 5.0)

        // When
        ls.giveExp(15.0)

        // Then
        verify(ls).level = 2
        verify(ls).exp = doubleEq(0.0)
    }

    @Test
    fun `when give exp more than needed for next level - should increase level and exp`() {
        // Given
        ls.prepare(level = 10, exp = 5.0)

        // When
        ls.giveExp(113.0)

        // Then
        verify(ls).level = 11
        verify(ls).exp = doubleEq(8.0)
    }

    @Test
    fun `when take not more exp than have - should keep level and decrease exp`() {
        // Given
        ls.prepare(level = 10, exp = 5.0)

        // When
        ls.takeExp(5.0)

        // Then
        verify(ls).level = 10
        verify(ls).exp = doubleEq(0.0)
    }

    @Test
    fun `when take more exp than have - should decrease level and set remaining exp`() {
        // Given
        ls.prepare(level = 10, exp = 5.0)

        // When
        ls.takeExp(6.0)

        // Then
        verify(ls).level = 9
        verify(ls).exp = doubleEq(99.0)
    }

    @Test
    fun `when take more exp than have total - should set zero level and exp`() {
        // Given
        ls.prepare(level = 1, exp = 5.0)

        // When
        ls.takeExp(150.0)

        // Then
        verify(ls).level = 0
        verify(ls).exp = doubleEq(0.0)
    }

    private fun LevelSystem.prepare(
        level: Int? = null,
        exp: Double? = null
    ) {
         level?.let { whenever(this.level) doReturn it }
         exp?.let { whenever(this.exp) doReturn it }
    }
}
