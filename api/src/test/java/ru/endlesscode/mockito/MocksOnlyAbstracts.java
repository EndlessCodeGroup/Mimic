/*
 * This file is part of MimicAPI.
 * Copyright (C) 2017 Osip Fatkullin
 * Copyright (C) 2017 EndlessCode Group and Contributors
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

package ru.endlesscode.mockito;

import org.mockito.invocation.*;
import org.mockito.stubbing.*;

import static java.lang.reflect.Modifier.isAbstract;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.RETURNS_DEFAULTS;

public class MocksOnlyAbstracts implements Answer<Object> {
    @Override
    public Object answer(InvocationOnMock invocation) throws Throwable {
        int methodModifiers = invocation.getMethod().getModifiers();
        Answer<Object> answer = isAbstract(methodModifiers) ? RETURNS_DEFAULTS : CALLS_REAL_METHODS;

        return answer.answer(invocation);
    }
}
