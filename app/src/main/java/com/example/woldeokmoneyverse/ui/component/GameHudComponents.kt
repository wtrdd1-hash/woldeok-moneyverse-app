package com.example.woldeokmoneyverse.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * 사이버펑크 네온 RPG 상단 전역 HUD 헤더 바
 */
@Composable
fun GameStatusHudHeader(
    level: Int = 14,
    currentExp: Long = 68400,
    maxExp: Long = 100000,
    tier: String = "PLATINUM",
    ap: Int = 85,
    maxAp: Int = 100,
    onHudClick: () -> Unit
) {
    val expProgress = (currentExp.toFloat() / maxExp.toFloat()).coerceIn(0f, 1f)
    val apProgress = (ap.toFloat() / maxAp.toFloat()).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E1B4B),
                        Color(0xFF0F172A)
                    )
                )
            )
            .border(
                1.dp,
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF38BDF8).copy(alpha = 0.5f),
                        Color(0xFFA855F7).copy(alpha = 0.5f)
                    )
                ),
                RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            )
            .clickable { onHudClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. 프로필 아바타 & 레벨 뱃지
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF38BDF8).copy(alpha = 0.2f))
                            .border(1.5.dp, Color(0xFF38BDF8), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFF59E0B))
                            .padding(horizontal = 3.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "Lv.$level",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // 2. EXP 경험치 바
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "EXP",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${(expProgress * 100).toInt()}%",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    LinearProgressIndicator(
                        progress = { expProgress },
                        modifier = Modifier
                            .width(100.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF38BDF8),
                        trackColor = Color(0xFF334155)
                    )
                }
            }

            // 3. 금융 티어 뱃지 & 행동력 (AP)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 티어 뱃지
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFA855F7).copy(alpha = 0.25f))
                        .border(1.dp, Color(0xFFA855F7), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.MilitaryTech,
                            contentDescription = null,
                            tint = Color(0xFFA855F7),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = tier,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE9D5FF)
                        )
                    }
                }

                // 행동력 AP
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "$ap/$maxAp AP",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFBBF24)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    LinearProgressIndicator(
                        progress = { apProgress },
                        modifier = Modifier
                            .width(60.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFFFBBF24),
                        trackColor = Color(0xFF334155)
                    )
                }
            }
        }
    }
}

/**
 * 4대 금융 스탯 다각형 레이더 차트 (Canvas 2D)
 */
@Composable
fun FinancialStatsRadarChart(
    wealth: Float = 0.85f,    // 재력 (NetWorth)
    credit: Float = 0.92f,    // 신용 (Credit)
    strategy: Float = 0.78f,  // 지략 (Quant)
    influence: Float = 0.65f  // 영향력 (Territory)
) {
    val stats = listOf(
        "재력\n${(wealth * 100).toInt()}" to wealth,
        "신용\n${(credit * 100).toInt()}" to credit,
        "지략\n${(strategy * 100).toInt()}" to strategy,
        "영향력\n${(influence * 100).toInt()}" to influence
    )

    Box(
        modifier = Modifier
            .size(180.dp)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.width / 2.4f

            // 1. 방사형 그리드 원/다각형 (25%, 50%, 75%, 100%)
            val levels = listOf(0.25f, 0.5f, 0.75f, 1.0f)
            levels.forEach { levelFrac ->
                val gridPath = Path()
                for (i in 0 until 4) {
                    val angle = (Math.PI / 2.0 * i - Math.PI / 2.0).toFloat()
                    val r = radius * levelFrac
                    val x = center.x + r * cos(angle)
                    val y = center.y + r * sin(angle)
                    if (i == 0) gridPath.moveTo(x, y) else gridPath.lineTo(x, y)
                }
                gridPath.close()
                drawPath(gridPath, Color(0xFF334155), style = Stroke(width = 1.dp.toPx()))
            }

            // 2. 축 가이드선
            for (i in 0 until 4) {
                val angle = (Math.PI / 2.0 * i - Math.PI / 2.0).toFloat()
                val x = center.x + radius * cos(angle)
                val y = center.y + radius * sin(angle)
                drawLine(Color(0xFF475569), center, Offset(x, y), strokeWidth = 1.dp.toPx())
            }

            // 3. 실제 스탯 영역 다각형 폴리곤
            val statPath = Path()
            stats.forEachIndexed { index, pair ->
                val angle = (Math.PI / 2.0 * index - Math.PI / 2.0).toFloat()
                val r = radius * pair.second.coerceIn(0.1f, 1.0f)
                val x = center.x + r * cos(angle)
                val y = center.y + r * sin(angle)
                if (index == 0) statPath.moveTo(x, y) else statPath.lineTo(x, y)
            }
            statPath.close()

            // 채우기 그라데이션
            drawPath(
                path = statPath,
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF38BDF8).copy(alpha = 0.5f), Color(0xFFA855F7).copy(alpha = 0.2f)),
                    center = center,
                    radius = radius
                )
            )

            // 외곽선 네온 스트로크
            drawPath(
                path = statPath,
                color = Color(0xFF38BDF8),
                style = Stroke(width = 2.5.dp.toPx())
            )

            // 각 꼭지점 네온 포인트
            stats.forEachIndexed { index, pair ->
                val angle = (Math.PI / 2.0 * index - Math.PI / 2.0).toFloat()
                val r = radius * pair.second.coerceIn(0.1f, 1.0f)
                val x = center.x + r * cos(angle)
                val y = center.y + r * sin(angle)
                drawCircle(Color(0xFFF43F5E), radius = 3.5.dp.toPx(), center = Offset(x, y))
            }
        }
    }
}

/**
 * 캐릭터 상세 스테이터스 & 장비 인벤토리 모달
 */
@Composable
fun CharacterStatsDialog(
    level: Int = 14,
    tier: String = "PLATINUM",
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF38BDF8))
                Spacer(modifier = Modifier.width(8.dp))
                Text("캐릭터 스테이터스 & 금융 스탯", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 레이더 차트
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    FinancialStatsRadarChart()
                }

                // 4대 스탯 상세 요약
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("재력 (NetWorth)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("85 pt (상위 5%)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF10B981))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("신용 (Credit)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("92 pt (1등급)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF38BDF8))
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("지략 (Quant)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("78 pt (봇 3대 가동)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFA855F7))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("영향력 (Territory)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("65 pt (거점 1곳 점령)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFF59E0B))
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 장착 칭호 및 장비 슬롯
                Text("장착 중인 칭호 & 코스메틱 장비", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF334155))
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🏆 천만장자", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24))
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF334155))
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🤖 퀀트 마스터", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF334155))
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🏰 요새 점령자", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA855F7))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("닫기")
            }
        }
    )
}

/**
 * 레벨업 & 퀘스트 보상 수령 시 황금 코인 파티클 폭죽 (Canvas 가속도 물리 애니메이션)
 */
@Composable
fun GoldParticleExplosion(
    trigger: Boolean,
    onAnimationEnd: () -> Unit = {}
) {
    if (!trigger) return

    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1400, easing = LinearOutSlowInEasing)
        )
        onAnimationEnd()
    }

    val particleCount = 40
    val particles = remember {
        List(particleCount) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 320f + 120f
            val size = Random.nextFloat() * 6f + 4f
            val color = if (Random.nextBoolean()) Color(0xFFF59E0B) else Color(0xFFFBBF24)
            ParticleData(angle, speed, size, color)
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val p = animProgress.value

        particles.forEach { part ->
            val dist = part.speed * p
            val gravity = 300f * p * p
            val x = center.x + dist * cos(part.angle)
            val y = center.y + dist * sin(part.angle) + gravity
            val alpha = (1f - p).coerceIn(0f, 1f)

            drawCircle(
                color = part.color.copy(alpha = alpha),
                radius = part.size.dp.toPx() * (1f - p * 0.4f),
                center = Offset(x, y)
            )
        }
    }
}

private data class ParticleData(
    val angle: Float,
    val speed: Float,
    val size: Float,
    val color: Color
)

/**
 * 공성전 및 타격 시 플로팅 대미지 텍스트 애니메이션
 */
@Composable
fun FloatingDamageText(
    damageText: String,
    isVisible: Boolean
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(150)) + slideInVertically(
            initialOffsetY = { 40 },
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ),
        exit = fadeOut(animationSpec = tween(400)) + slideOutVertically(
            targetOffsetY = { -60 },
            animationSpec = tween(400)
        )
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFDC2626).copy(alpha = 0.9f))
                .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = damageText,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
