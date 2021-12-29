/*
 * This file is part of BukkitMimic.
 * Copyright (C) 2021 Osip Fatkullin
 * Copyright (C) 2021 EndlessCode Group and contributors
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

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.bukkit.permissions.PermissionAttachmentInfo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import ru.endlesscode.mimic.BukkitTestBase
import ru.endlesscode.mimic.classes.ClassSystem
import kotlin.test.Test

class PermissionsClassSystemTest : BukkitTestBase() {

    // SUT
    private val classSystem: ClassSystem = PermissionsClassSystem.Provider().getSystem(player)

    @ParameterizedTest
    @CsvSource(
        "second,    true",
        "Second,    true",
        "another,   false",
    )
    fun `test hasClass`(theClass: String, hasClass: Boolean) {
        addClasses("first", "second", "third")
        classSystem.hasClass(theClass) shouldBe hasClass
    }

    @Test
    fun `when get classes - should return right list of classes`() {
        val perms = listOf(
            createClassPermissionMock("first", value = true),
            createClassPermissionMock("second", value = false),
            createClassPermissionMock("third", value = true),
            createPermissionMock("mimic.other.permission", value = true)
        )
        every { player.effectivePermissions } returns HashSet(perms)

        classSystem.classes.shouldContainExactlyInAnyOrder("first", "third")
    }

    private fun addClasses(vararg classes: String) {
        every { player.hasPermission(any<String>()) } returns false
        for (theClass in classes) {
            every { player.hasPermission(classPermission(theClass)) } returns true
        }
    }

    private fun createClassPermissionMock(theClass: String, value: Boolean): PermissionAttachmentInfo {
        return createPermissionMock(classPermission(theClass), value)
    }

    private fun classPermission(theClass: String) = PermissionsClassSystem.PERMISSION_PREFIX + theClass

    private fun createPermissionMock(permission: String, value: Boolean): PermissionAttachmentInfo {
        return mockk {
            every { this@mockk.permission } returns permission
            every { this@mockk.value } returns value
        }
    }
}
