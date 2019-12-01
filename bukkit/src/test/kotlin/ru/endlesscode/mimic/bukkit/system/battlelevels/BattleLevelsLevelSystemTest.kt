package ru.endlesscode.mimic.bukkit.system.battlelevels

import com.nhaarman.mockitokotlin2.*
import org.junit.Test
import ru.endlesscode.mimic.bukkit.BukkitTestBase
import kotlin.test.BeforeTest

class BattleLevelsLevelSystemTest : BukkitTestBase() {

    private lateinit var battleLevelsApi: BattleLevelsApiWrapper

    // SUT
    private lateinit var levelSystem: BattleLevelsLevelSystem

    @BeforeTest
    override fun setUp() {
        super.setUp()
        battleLevelsApi = mock()
        levelSystem = BattleLevelsLevelSystem(player, battleLevelsApi)
    }

    @Test
    fun `when set level lower than current - should remove level`() {
        // Given
        whenever(battleLevelsApi.getLevel(any())) doReturn 10

        // When
        levelSystem.level = 6

        // Then
        verify(battleLevelsApi).removeLevel(any(), eq(4))
    }

    @Test
    fun `when set level higher than current - should add level`() {
        // Given
        whenever(battleLevelsApi.getLevel(any())) doReturn 10

        // When
        levelSystem.level = 16

        // Then
        verify(battleLevelsApi).addLevel(any(), eq(6))
    }

    @Test
    fun `when set level equal to current - should not change level`() {
        // Given
        whenever(battleLevelsApi.getLevel(any())) doReturn 10

        // When
        levelSystem.level = 10

        // Then
        verify(battleLevelsApi, never()).removeLevel(any(), any())
        verify(battleLevelsApi, never()).addLevel(any(), any())
    }
}
