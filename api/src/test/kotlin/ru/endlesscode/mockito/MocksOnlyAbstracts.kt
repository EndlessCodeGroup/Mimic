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

package ru.endlesscode.mockito

import org.mockito.Mockito.CALLS_REAL_METHODS
import org.mockito.Mockito.RETURNS_DEFAULTS
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import java.lang.reflect.Modifier.isAbstract

class MocksOnlyAbstracts : Answer<Any> {
    override fun answer(invocation: InvocationOnMock): Any? {
        val methodModifiers = invocation.method.modifiers
        val answer =
            if (isAbstract(methodModifiers)) RETURNS_DEFAULTS else CALLS_REAL_METHODS

        return answer.answer(invocation)
    }
}
