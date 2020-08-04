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

package ru.endlesscode.mimic.level

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import ru.endlesscode.mimic.mockito.MOCKS_ONLY_ABSTRACTS

const val EXP_IN_LEVEL = 10.0

fun mockExpLevelConverter(): ExpLevelConverter {
    return mock(defaultAnswer = MOCKS_ONLY_ABSTRACTS) {
        on { getExpToReachLevel(any()) } doAnswer { invocation ->
            val level = invocation.getArgument<Int>(0)
            level * EXP_IN_LEVEL
        }
    }
}
