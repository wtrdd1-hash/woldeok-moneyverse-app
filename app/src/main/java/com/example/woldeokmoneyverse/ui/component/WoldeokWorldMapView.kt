package com.example.woldeokmoneyverse.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.woldeokmoneyverse.util.formatWld

data class MapNode(
    val id: String,
    val name: String,
    val category: String, // "DISTRICT" or "STRONGHOLD"
    val normX: Float, // 0.0f ~ 1.0f
    val normY: Float,
    val ownerClub: String = "중립",
    val dailyTaxOrRent: Long = 50000L,
    val isUnderAttack: Boolean = false,
    val color: Color
)

@Composable
fun WoldeokWorldMapView(
    modifier: Modifier = Modifier,
    onNodeSelected: (MapNode) -> Unit = {}
) {
    val nodes = remember {
        listOf(
            // 구역
            MapNode("d1", "강남 테헤란로", "DISTRICT", 0.70f, 0.65f, "테크노 크루", 450000L, false, Color(0xFF38BDF8)),
            MapNode("d2", "여의도 금융가", "DISTRICT", 0.30f, 0.40f, "골드러시 길드", 720000L, false, Color(0xFF10B981)),
            MapNode("d3", "성수 유니콘밸리", "DISTRICT", 0.75f, 0.35f, "무주공산", 260000L, false, Color(0xFFF59E0B)),
            MapNode("d4", "판교 테크노밸리", "DISTRICT", 0.50f, 0.85f, "테크노 크루", 340000L, false, Color(0xFFA855F7)),

            // 요새 거점
            MapNode("s1", "월덕 중앙은행", "STRONGHOLD", 0.50f, 0.30f, "골드러시 길드", 120000L, true, Color(0xFFEF4444)),
            MapNode("s2", "테헤란 요새", "STRONGHOLD", 0.65f, 0.55f, "테크노 크루", 85000L, true, Color(0xFFEF4444)),
            MapNode("s3", "여의도 거래요새", "STRONGHOLD", 0.35f, 0.50f, "중립 거점", 60000L, false, Color(0xFFFBBF24))
        )
    }

    var selectedNode by remember { mutableStateOf<MapNode?>(null) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0B0F19))
            .border(1.5.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        val w = size.width
                        val h = size.height
                        // 가장 가까운 노드 탐색 (반경 35dp 이내)
                        val hit = nodes.firstOrNull { node ->
                            val nx = node.normX * w
                            val ny = node.normY * h
                            val dist = kotlin.math.hypot(tapOffset.x - nx, tapOffset.y - ny)
                            dist <= 40f
                        }
                        if (hit != null) {
                            selectedNode = hit
                            onNodeSelected(hit)
                        } else {
                            selectedNode = null
                        }
                    }
                }
        ) {
            val w = size.width
            val h = size.height

            // 1. 사이버펑크 격자 그리드 가이드라인
            val gridStep = 40.dp.toPx()
            var gx = 0f
            while (gx < w) {
                drawLine(Color(0xFF1E293B).copy(alpha = 0.35f), Offset(gx, 0f), Offset(gx, h), 1f)
                gx += gridStep
            }
            var gy = 0f
            while (gy < h) {
                drawLine(Color(0xFF1E293B).copy(alpha = 0.35f), Offset(0f, gy), Offset(w, gy), 1f)
                gy += gridStep
            }

            // 2. 거점 간 에너지 연결선 (네온 회로망)
            val connections = listOf(
                Pair(nodes[0], nodes[1]),
                Pair(nodes[1], nodes[4]),
                Pair(nodes[4], nodes[2]),
                Pair(nodes[0], nodes[3]),
                Pair(nodes[4], nodes[5]),
                Pair(nodes[5], nodes[6])
            )

            connections.forEach { (n1, n2) ->
                val p1 = Offset(n1.normX * w, n1.normY * h)
                val p2 = Offset(n2.normX * w, n2.normY * h)
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(n1.color.copy(alpha = 0.3f), n2.color.copy(alpha = 0.3f)),
                        start = p1,
                        end = p2
                    ),
                    start = p1,
                    end = p2,
                    strokeWidth = 2.dp.toPx()
                )
            }

            // 3. 노드 렌더링
            nodes.forEach { node ->
                val center = Offset(node.normX * w, node.normY * h)
                val isSelected = selectedNode?.id == node.id

                // 외곽 파동 / 글로우 링
                if (node.isUnderAttack) {
                    drawCircle(
                        color = Color(0xFFEF4444).copy(alpha = 0.3f),
                        radius = 22.dp.toPx(),
                        center = center
                    )
                }
                if (isSelected) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.4f),
                        radius = 24.dp.toPx(),
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                // 노드 메인 원
                val baseRadius = if (node.category == "STRONGHOLD") 16.dp.toPx() else 12.dp.toPx()
                drawCircle(
                    color = node.color,
                    radius = baseRadius,
                    center = center
                )
                drawCircle(
                    color = Color.White,
                    radius = baseRadius * 0.4f,
                    center = center
                )
            }
        }

        // 선택된 노드 인포 툴팁 카드 (상단 또는 하단에 표출)
        selectedNode?.let { node ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.95f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(12.dp)
                    .border(1.dp, node.color, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(node.color.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (node.category == "STRONGHOLD") "요새 거점" else "금융 구역",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = node.color
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = node.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "점령 길드: ${node.ownerClub} | 일일 세수: ${formatWld(node.dailyTaxOrRent)} WLD",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    if (node.isUnderAttack) {
                        Text(
                            text = "⚔️ 교전 중",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 전역 어디서든 호출 가능한 월덕 대륙 인터랙티브 맵 다이얼로그
 */
@Composable
fun WoldeokWorldMapDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Map, contentDescription = null, tint = Color(0xFF38BDF8))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("월덕 대륙 영지 인터랙티브 맵", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "닫기")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "원하는 거점이나 구역 노드를 터치하여 점령 길드 및 세수 현황을 확인하세요.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                WoldeokWorldMapView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("닫기")
            }
        }
    )
}
