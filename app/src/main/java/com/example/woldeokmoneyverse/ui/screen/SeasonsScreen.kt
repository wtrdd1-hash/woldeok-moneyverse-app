package com.example.woldeokmoneyverse.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.woldeokmoneyverse.data.model.UiState
import com.example.woldeokmoneyverse.ui.component.MoneyverseCard
import com.example.woldeokmoneyverse.ui.component.SkeletonLoader
import com.example.woldeokmoneyverse.ui.viewmodel.PlayViewModel

@Composable
fun SeasonsScreen(
    playViewModel: PlayViewModel
) {
    val seasonsState by playViewModel.seasonsState.collectAsState()
    val leaderboardState by playViewModel.leaderboardState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Text(
                text = "🏆 시즌 이벤트 & 대부호 리더보드 (`SEA-001`)",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- Seasons Active Card ---
        when (val sState = seasonsState) {
            is UiState.Success -> {
                items(sState.data) { season ->
                    MoneyverseCard(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                        Text(season.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(season.description, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { season.currentProgress.toFloat() / season.totalMilestone.toFloat() },
                            modifier = Modifier.fillMaxWidth().height(8.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("시즌 종료일: ${season.endsAt}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            is UiState.Loading -> item { SkeletonLoader() }
            else -> {}
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text("🥇 실시간 대부호 순위 TOP 100", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
        }

        when (val lState = leaderboardState) {
            is UiState.Success -> {
                items(lState.data) { entry ->
                    MoneyverseCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${entry.rank}위",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (entry.rank == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(entry.displayName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                    Text(entry.title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text(
                                text = entry.score,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
            is UiState.Loading -> item { SkeletonLoader() }
            else -> {}
        }
    }
}
