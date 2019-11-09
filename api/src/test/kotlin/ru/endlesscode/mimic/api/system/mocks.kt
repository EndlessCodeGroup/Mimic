package ru.endlesscode.mimic.api.system

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import ru.endlesscode.mockito.MOCKS_ONLY_ABSTRACTS

const val EXP_IN_LEVEL = 10.0

fun mockExpLevelConverter(): ExpLevelConverter {
    return mock(defaultAnswer = MOCKS_ONLY_ABSTRACTS) {
        on(it.getExpToReachLevel(any())) doAnswer { invocation ->
            val level = invocation.getArgument<Int>(0)
            level * EXP_IN_LEVEL
        }
    }
}
