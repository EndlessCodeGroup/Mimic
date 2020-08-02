package ru.endlesscode.mimic

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
