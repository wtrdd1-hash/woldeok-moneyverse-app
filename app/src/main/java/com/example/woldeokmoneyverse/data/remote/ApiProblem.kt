package com.example.woldeokmoneyverse.data.remote

import com.google.gson.JsonParser
import retrofit2.Response

data class ApiProblem(val status: Int, val code: String?, val detail: String?)

fun apiProblem(response: Response<*>): ApiProblem {
    val raw = runCatching { response.errorBody()?.string().orEmpty() }.getOrDefault("")
    val objectValue = runCatching { JsonParser.parseString(raw).asJsonObject }.getOrNull()
    fun value(name: String): String? = objectValue?.get(name)?.takeUnless { it.isJsonNull }
        ?.let { runCatching { it.asString }.getOrNull() }
    return ApiProblem(response.code(), value("code"), value("detail") ?: value("message"))
}

fun koreanApiProblem(problem: ApiProblem, action: String): String = when (problem.code) {
    "work_reward_quota_reached" -> "오늘 또는 이번 주 근무 보상 한도에 도달했습니다. 근무 현황의 관리자 정책 한도를 확인해 주세요."
    "work_task_daily_limit_reached" -> "오늘 이 근무 과제의 수행 횟수 한도에 도달했습니다."
    "casino_daily_stake_limit_reached" -> "오늘 카지노 배팅 한도에 도달했습니다. 다음 서버 일일 초기화 후 다시 이용할 수 있습니다."
    "casino_daily_loss_limit_reached" -> "오늘 카지노 손실 한도에 도달했습니다. 다음 서버 일일 초기화 후 다시 이용할 수 있습니다."
    "casino_self_stake_limit_reached" -> "직접 설정한 카지노 일일 배팅 한도에 도달했습니다."
    "casino_self_loss_limit_reached" -> "직접 설정한 카지노 일일 손실 한도에 도달했습니다."
    "casino_self_excluded" -> "설정한 카지노 이용 제한 시간이 아직 끝나지 않았습니다."
    else -> when (problem.status) {
        400 -> "$action 실패: 요청 입력값을 확인해 주세요."
        401 -> "$action 실패: 인증이 만료되었습니다. 다시 로그인해 주세요."
        403 -> "$action 실패: 권한 또는 이용 상태를 확인해 주세요."
        409 -> problem.detail?.takeIf { it.isNotBlank() } ?: "${action}을 현재 상태에서 처리할 수 없습니다."
        422 -> "$action 실패: 데이터 규격을 확인해 주세요."
        429 -> "$action 실패: 요청이 너무 잦습니다. 잠시 후 다시 시도해 주세요."
        else -> "$action 실패 (${problem.status}): 서버 처리 중 오류가 발생했습니다."
    }
}
