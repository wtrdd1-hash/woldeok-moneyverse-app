package com.example.woldeokmoneyverse.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.woldeokmoneyverse.ui.component.MoneyverseCard

@Composable
fun AdminScreen(adminRoles: List<String>) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "관리자",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        MoneyverseCard {
            Text("서버 관리자 권한 확인 완료", fontWeight = FontWeight.Bold)
            Text("권한: ${adminRoles.joinToString(", ")}")
            Text(
                "이 화면은 로그인 후 /auth/viewer와 /admin/me 검증을 모두 통과한 계정에만 표시됩니다. 고위험 관리 작업은 서버의 관리자 세션과 2차 인증 정책을 계속 따릅니다.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
