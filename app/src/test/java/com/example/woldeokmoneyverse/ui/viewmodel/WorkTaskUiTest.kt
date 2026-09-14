package com.example.woldeokmoneyverse.ui.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkTaskUiTest {
    private fun task(dailyLimit: Int, takenToday: Int) = WorkTaskUi(
        id = "task-1",
        name = "Task",
        description = "",
        jobType = "developer",
        reward = "100",
        experience = "10",
        minimumDurationSeconds = 0,
        recommended = false,
        dailyLimit = dailyLimit,
        takenToday = takenToday
    )

    @Test
    fun reportsRemainingQuotaUntilLimitIsReached() {
        val task = task(dailyLimit = 3, takenToday = 2)
        assertEquals(1, task.remainingToday)
        assertFalse(task.dailyQuotaReached)
    }

    @Test
    fun marksQuotaReachedAtConfiguredLimit() {
        val task = task(dailyLimit = 3, takenToday = 3)
        assertEquals(0, task.remainingToday)
        assertTrue(task.dailyQuotaReached)
    }

    @Test
    fun zeroLimitRemainsCompatibleWithUnlimitedLegacyPayloads() {
        val task = task(dailyLimit = 0, takenToday = 99)
        assertEquals(Int.MAX_VALUE, task.remainingToday)
        assertFalse(task.dailyQuotaReached)
    }
}
