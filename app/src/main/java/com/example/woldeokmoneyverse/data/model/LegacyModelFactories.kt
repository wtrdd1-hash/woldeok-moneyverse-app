package com.example.woldeokmoneyverse.data.model

/**
 * Compatibility factories for runtime adapters that still use the older
 * season/leaderboard constructor shapes. Keep these narrow so current UI
 * models remain the canonical representation.
 */
@Suppress("FunctionName", "UNUSED_PARAMETER")
fun SeasonDto(
    id: String,
    name: String,
    description: String,
    endsAt: String,
    isActive: Boolean
): SeasonDto = SeasonDto(
    id = id,
    name = name,
    description = description,
    endsAt = endsAt,
    currentProgress = 0,
    totalMilestone = 0
)

@Suppress("FunctionName")
fun LeaderboardEntryDto(
    rank: Int,
    userId: String,
    displayName: String,
    score: Long
): LeaderboardEntryDto = LeaderboardEntryDto(
    rank = rank,
    userId = userId,
    displayName = displayName,
    score = score.toString(),
    title = ""
)
