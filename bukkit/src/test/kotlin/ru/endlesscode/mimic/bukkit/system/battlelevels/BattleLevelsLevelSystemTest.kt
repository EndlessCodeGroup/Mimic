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

    @Test
    fun `when set total exp lower than current - should remove score`() {
        // Given
        whenever(battleLevelsApi.getScore(any())) doReturn 100.0

        // When
        levelSystem.totalExp = 60.0

        // Then
        verify(battleLevelsApi).removeScore(any(), eq(40.0))
    }

    @Test
    fun `when set total exp higher than current - should add score`() {
        // Given
        whenever(battleLevelsApi.getScore(any())) doReturn 100.0

        // When
        levelSystem.totalExp = 160.0

        // Then
        verify(battleLevelsApi).addScore(any(), eq(60.0))
    }

    @Test
    fun `when set total exp equal to current - should not change score`() {
        // Given
        whenever(battleLevelsApi.getScore(any())) doReturn 100.0

        // When
        levelSystem.totalExp = 100.0

        // Then
        verify(battleLevelsApi, never()).removeScore(any(), any())
        verify(battleLevelsApi, never()).addScore(any(), any())
    }
}
