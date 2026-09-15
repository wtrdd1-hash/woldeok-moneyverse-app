package com.example.woldeokmoneyverse

import com.example.woldeokmoneyverse.data.remote.ApiProblem
import com.example.woldeokmoneyverse.data.remote.koreanApiProblem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiProblemTest {
    @Test
    fun workQuotaConflictIsExplainedWithoutRaw409() {
        val text = koreanApiProblem(ApiProblem(409, "work_reward_quota_reached", "work reward quota reached"), "근무 완료")
        assertTrue(text.contains("근무 보상 한도"))
        assertFalse(text.contains("409"))
    }

    @Test
    fun casinoLossConflictIsExplainedWithoutRaw409() {
        val text = koreanApiProblem(ApiProblem(409, "casino_daily_loss_limit_reached", "daily loss limit reached"), "카지노")
        assertTrue(text.contains("카지노 손실 한도"))
        assertFalse(text.contains("409"))
    }
}
