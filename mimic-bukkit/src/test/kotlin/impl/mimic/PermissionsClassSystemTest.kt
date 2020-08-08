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

package ru.endlesscode.mimic.impl.mimic

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.bukkit.permissions.PermissionAttachmentInfo
import ru.endlesscode.mimic.BukkitTestBase
import ru.endlesscode.mimic.classes.ClassSystem
import java.util.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PermissionsClassSystemTest : BukkitTestBase() {

    // SUT
    private lateinit var classSystem: ClassSystem

    @BeforeTest
    override fun setUp() {
        super.setUp()
        classSystem = PermissionsClassSystem.Provider().get(player)
    }

    @Test
    fun `when check existing has class - should return true`() {
        // Given
        addClasses("first", "second", "third")

        // When
        val hasClass = classSystem.hasClass("second")

        // Then
        assertTrue(hasClass)
    }

    @Test
    fun `when check has existing class in mixed case - should return true`() {
        // Given
        addClasses("first", "second", "third")

        // When
        val hasClass = classSystem.hasClass("Second")

        // Then
        assertTrue(hasClass)
    }

    @Test
    fun `when check has not existing class - should return false`() {
        // Given
        addClasses("first", "second")

        // When
        val hasClass = classSystem.hasClass("FirstClass")

        // Then
        assertFalse(hasClass)
    }

    @Test
    fun `when get classes - should return right list of classes`() {
        // Given
        val perms = listOf(
            createClassPermissionMock("first", true),
            createClassPermissionMock("second", false),
            createClassPermissionMock("third", true),
            createPermissionMock("mimic.other.permission", true)
        )
        whenever(player.effectivePermissions) doReturn HashSet(perms)

        // When
        val expected = listOf("first", "third")
        val actual = classSystem.classes

        // Then
        assertTrue(actual.containsAll(expected) && expected.containsAll(actual))
    }

    private fun addClasses(vararg classes: String) {
        for (theClass in classes) {
            whenever(player.hasPermission(classPermission(theClass))) doReturn true
        }
    }

    private fun createClassPermissionMock(theClass: String, value: Boolean): PermissionAttachmentInfo {
        return createPermissionMock(classPermission(theClass), value)
    }

    private fun classPermission(theClass: String) = PermissionsClassSystem.PERMISSION_PREFIX + theClass

    private fun createPermissionMock(permission: String, value: Boolean): PermissionAttachmentInfo {
        return mock {
            on { it.permission } doReturn permission
            on { it.value } doReturn value
        }
    }
}
