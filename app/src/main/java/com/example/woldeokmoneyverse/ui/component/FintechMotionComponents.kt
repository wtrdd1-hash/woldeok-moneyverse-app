package com.example.woldeokmoneyverse.ui.component

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.woldeokmoneyverse.ui.theme.CardBackgroundDark
import com.example.woldeokmoneyverse.ui.theme.MoneyverseGreen
import com.example.woldeokmoneyverse.ui.theme.MoneyverseRed
import java.text.NumberFormat
import java.util.Locale

// ---------------------------------------------------------
// 1. 시스템 햅틱 피드백 컨트롤러 (Haptic Feedback Controller)
// ---------------------------------------------------------
class FintechHapticController(private val haptic: HapticFeedback) {
    fun click() {
        try {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        } catch (_: Exception) {}
    }

    fun success() {
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (_: Exception) {}
    }

    fun heavy() {
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (_: Exception) {}
    }
}

@Composable
fun rememberFintechHaptic(): FintechHapticController {
    val haptic = LocalHapticFeedback.current
    return remember(haptic) { FintechHapticController(haptic) }
}

// ---------------------------------------------------------
// 2. 부드러운 숫자 롤링 애니메이션 카운터 (AnimatedFintechCounter)
// ---------------------------------------------------------
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedFintechCounter(
    value: Long,
    prefix: String = "",
    suffix: String = " WLD",
    style: TextStyle = MaterialTheme.typography.titleLarge,
    color: Color = Color.White,
    modifier: Modifier = Modifier
) {
    val formatted = remember(value) {
        NumberFormat.getNumberInstance(Locale.KOREA).format(value)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (prefix.isNotEmpty()) {
            Text(
                text = prefix,
                style = style.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                color = color
            )
        }

        AnimatedContent(
            targetState = formatted,
            transitionSpec = {
                (slideInVertically { height -> height } + fadeIn()) togetherWith
                        (slideOutVertically { height -> -height } + fadeOut())
            },
            label = "FintechCounter"
        ) { targetText ->
            Text(
                text = targetText,
                style = style.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (suffix.isNotEmpty()) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = suffix,
                style = style.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold),
                color = color.copy(alpha = 0.85f),
                maxLines = 1
            )
        }
    }
}

// ---------------------------------------------------------
// 3. 실시간 글래스모피즘 마키 티커 바 (GlobalFintechTickerRibbon)
// ---------------------------------------------------------
data class TickerItem(
    val label: String,
    val value: String,
    val changeRate: String? = null,
    val isPositive: Boolean? = null
)

val sampleTickerItems = listOf(
    TickerItem("WLD 통화량", "248.5억 WLD"),
    TickerItem("국채 90일 APR", "8.2%", "+0.3%", true),
    TickerItem("네오전자", "74,200 WLD", "+1.8%", true),
    TickerItem("하이테크솔", "38,500 WLD", "-0.9%", false),
    TickerItem("월덕바이오", "121,000 WLD", "+4.2%", true),
    TickerItem("기준금리", "3.25%", "동결", null)
)

@Composable
fun GlobalFintechTickerRibbon(
    items: List<TickerItem> = sampleTickerItems,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(34.dp),
        color = CardBackgroundDark.copy(alpha = 0.92f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27272A))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(scrollState)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // LIVE 뱃지
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF10B981).copy(alpha = 0.15f))
                    .border(0.5.dp, Color(0xFF10B981), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "LIVE TICKER",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    ),
                    color = Color(0xFF10B981)
                )
            }

            items.forEach { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color(0xFFA1A1AA)
                    )
                    Text(
                        text = item.value,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        ),
                        color = Color.White
                    )

                    if (item.changeRate != null) {
                        val changeColor = when (item.isPositive) {
                            true -> MoneyverseGreen
                            false -> MoneyverseRed
                            null -> Color(0xFFA1A1AA)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (item.isPositive == true) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = changeColor,
                                    modifier = Modifier.size(11.dp)
                                )
                            } else if (item.isPositive == false) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = changeColor,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                            Text(
                                text = item.changeRate,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = changeColor
                            )
                        }
                    }
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = Color(0xFF3F3F46)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------
// 4. Toss/Revolut 감성 핀테크 모달 바텀시트 컨테이너
// ---------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FintechBottomSheetContainer(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF18181B), // Zinc-900
        contentColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF52525B))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtitle != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFA1A1AA),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "닫기",
                        tint = Color(0xFFA1A1AA)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 바텀시트 본문
            content()
        }
    }
}
