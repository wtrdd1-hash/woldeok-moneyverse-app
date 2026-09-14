package com.example.woldeokmoneyverse.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.woldeokmoneyverse.data.model.StockDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailSheet(
    stock: StockDto,
    onDismiss: () -> Unit,
    onOrder: (String, Int) -> Unit
) {
    var quantity by remember { mutableIntStateOf(1) }
    var orderType by remember { mutableStateOf("BUY") } // "BUY" or "SELL"

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("${stock.name} (${stock.symbol})", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Text("현재가: ${stock.currentPrice} WLD (${if (stock.priceChangePercent >= 0) "+" else ""}${stock.priceChangePercent}%)",
                style = MaterialTheme.typography.titleMedium,
                color = if (stock.priceChangePercent >= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("📊 주가 가격 추이 차트", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))

            // Stock Chart Graphic
            val history = if (stock.historyPrices.isEmpty()) listOf(100.0, 105.0, 102.0, 110.0, 115.0) else stock.historyPrices
            val chartColor = if (stock.priceChangePercent >= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
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
                FilterChip(
                    selected = orderType == "BUY",
                    onClick = { orderType = "BUY" },
                    label = { Text("매수 (BUY)") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = orderType == "SELL",
                    onClick = { orderType = "SELL" },
                    label = { Text("매도 (SELL)") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("주문 수량: $quantity 주", style = MaterialTheme.typography.labelMedium)
            Slider(
                value = quantity.toFloat(),
                onValueChange = { quantity = it.toInt().coerceAtLeast(1) },
                valueRange = 1f..100f
            )

            val unitPrice = stock.currentPrice.toLongOrNull() ?: 285000L
            Text("예상 총 결제금액: ${unitPrice * quantity} WLD", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onOrder(orderType, quantity)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (orderType == "BUY") "매수 주문 제출" else "매도 주문 제출")
            }
        }
    }
}
