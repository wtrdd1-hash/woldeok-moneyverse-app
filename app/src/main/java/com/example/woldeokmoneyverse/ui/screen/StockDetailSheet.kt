package com.example.woldeokmoneyverse.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.woldeokmoneyverse.data.model.StockDto
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.Locale

private fun stockMoney(value: String): String {
    val number = runCatching { BigDecimal(value.replace(",", "")) }.getOrNull() ?: return "$value WLD"
    return "${DecimalFormat("#,##0.##").format(number)} WLD"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailSheet(
    stock: StockDto,
    maxBuyQuantity: Int = 0,
    maxSellQuantity: Int = 0,
    onDismiss: () -> Unit,
    onOrder: (String, Int) -> Unit
) {
    var quantity by remember { mutableIntStateOf(1) }
    var orderType by remember { mutableStateOf("BUY") }
    val isGain = stock.priceChangePercent >= 0
    val changeText = String.format(
        Locale.getDefault(),
        "%s %.2f%% %s",
        if (isGain) "▲" else "▼",
        kotlin.math.abs(stock.priceChangePercent),
        if (isGain) "상승" else "하락"
    )

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("${stock.name} (${stock.symbol})", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Text("현재가 ${stockMoney(stock.currentPrice)}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text(
                text = "오늘 $changeText",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = if (isGain) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
            )
            Text(
                "※ 위 퍼센트는 오늘 시가 대비 등락입니다. 내 보유 수익률은 포트폴리오의 평균 매수가 대비 수익률을 확인하세요.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("📊 주가 가격 추이", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))

            val history = if (stock.historyPrices.isEmpty()) listOf(100.0, 105.0, 102.0, 110.0, 115.0) else stock.historyPrices
            val chartColor = if (isGain) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
            Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                val minPrice = history.minOrNull() ?: 1.0
                val maxPrice = history.maxOrNull() ?: 2.0
                val priceRange = if (maxPrice - minPrice == 0.0) 1.0 else maxPrice - minPrice
                val path = Path()
                history.forEachIndexed { index, price ->
                    val x = size.width * index / (history.size - 1)
                    val y = size.height - ((price - minPrice) / priceRange * size.height).toFloat()
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path = path, color = chartColor, style = Stroke(width = 4.dp.toPx()))
            }

            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = orderType == "BUY", onClick = { orderType = "BUY"; quantity = 1 }, label = { Text("매수 (BUY)") }, modifier = Modifier.weight(1f))
                FilterChip(selected = orderType == "SELL", onClick = { orderType = "SELL"; quantity = 1 }, label = { Text("매도 (SELL)") }, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))
            val availableMax = if (orderType == "BUY") maxBuyQuantity else maxSellQuantity
            val sliderMax = maxOf(100, availableMax, 1)
            Text("주문 수량: $quantity 주", style = MaterialTheme.typography.labelMedium)
            Slider(
                value = quantity.coerceAtMost(sliderMax).toFloat(),
                onValueChange = { quantity = it.toInt().coerceAtLeast(1) },
                valueRange = 1f..sliderMax.toFloat(),
                enabled = availableMax > 0
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { quantity = availableMax }, enabled = availableMax > 0) {
                    Text(if (orderType == "BUY") "전액 매수 (${maxBuyQuantity}주)" else "전량 매도 (${maxSellQuantity}주)")
                }
            }

            val unitPrice = stock.currentPrice.replace(",", "").toBigDecimalOrNull() ?: BigDecimal.ZERO
            val total = unitPrice.multiply(BigDecimal.valueOf(quantity.toLong()))
            Text(
                "예상 총 ${if (orderType == "BUY") "결제" else "매도"}금액: ${stockMoney(total.toPlainString())}",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onOrder(orderType, quantity); onDismiss() },
                enabled = availableMax > 0 && quantity in 1..availableMax,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (orderType == "BUY") "매수 주문 제출" else "매도 주문 제출")
            }
        }
    }
}
