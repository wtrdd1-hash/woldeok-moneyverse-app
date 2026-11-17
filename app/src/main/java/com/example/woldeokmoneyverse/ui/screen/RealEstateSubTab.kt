package com.example.woldeokmoneyverse.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.woldeokmoneyverse.data.model.RealEstateDistrictDto
import com.example.woldeokmoneyverse.data.model.RealEstateLandDto
import com.example.woldeokmoneyverse.data.model.UiState
import com.example.woldeokmoneyverse.ui.component.*
import com.example.woldeokmoneyverse.ui.viewmodel.EconomyViewModel
import com.example.woldeokmoneyverse.util.formatWld

@Composable
fun RealEstateSubTab(economyViewModel: EconomyViewModel) {
    val districtsState by economyViewModel.districtsState.collectAsState()
    val landsState by economyViewModel.landsState.collectAsState()
    val selectedDistrictId by economyViewModel.selectedDistrictId.collectAsState()
    val haptic = rememberFintechHaptic()

    var selectedLandForAuction by remember { mutableStateOf<RealEstateLandDto?>(null) }
    var selectedLandForPurchase by remember { mutableStateOf<RealEstateLandDto?>(null) }
    var bidInputAmount by remember { mutableStateOf("10000000") }
    var showWorldMap by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        economyViewModel.loadRealEstate(selectedDistrictId)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // 1. Overview & Rent Collection Bento Card
        item {
            BentoCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("🏙️ 가상 부동산 & 메타버스 랜드", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text("주요 특별상권 빌딩 소유 및 일일 임대료 원장 정산", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        BentoMetricTile(
                            title = "보유 필지",
                            value = "2 개소",
                            modifier = Modifier.weight(1f)
                        )
                        BentoMetricTile(
                            title = "정산 가능 임대료",
                            value = "1,170,000 WLD",
                            subValue = "일일 자동 누적",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            haptic.success()
                            economyViewModel.claimAllRealEstateRent()
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("💰 보유 부동산 일일 임대료 일괄 징수", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = Color.White))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            haptic.click()
                            showWorldMap = true
                        },
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("🗺️ 월덕 대륙 영지 인터랙티브 맵 보기", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }

        // 2. District Filter Selector Pills
        item {
            when (val dState = districtsState) {
                is UiState.Success -> {
                    val districts = dState.data
                    Text("📍 특별 구역 선택", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(vertical = 6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedDistrictId == null,
                            onClick = {
                                haptic.click()
                                economyViewModel.selectDistrict(null)
                            },
                            label = { Text("전체 상권") }
                        )
                        districts.take(3).forEach { d ->
                            FilterChip(
                                selected = selectedDistrictId == d.id,
                                onClick = {
                                    haptic.click()
                                    economyViewModel.selectDistrict(d.id)
                                },
                                label = { Text(d.name) }
                            )
                        }
                    }
                }
                else -> {}
            }
        }

        // 3. Court Real Estate Auctions Section
        item {
            Text("🏛️ 법원 가상 부동산 경매 (실시간 입찰)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(vertical = 6.dp))
        }

        when (val lState = landsState) {
            is UiState.Loading -> {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp))
                    }
                }
            }
            is UiState.Error -> {
                item {
                    Text("부동산 정보를 불러오지 못했습니다: ${lState.message}", color = MaterialTheme.colorScheme.error)
                }
            }
            is UiState.Success -> {
                val lands = lState.data
                val auctions = lands.filter { it.isAuctionActive }
                val regularLands = lands.filter { !it.isAuctionActive }

                if (auctions.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("현재 진행 중인 법원 경매 물건이 없습니다.", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                } else {
                    items(auctions) { land ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(color = Color(0xFFEF4444), shape = RoundedCornerShape(6.dp)) {
                                            Text("법원 경매 진행 중", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(land.districtName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Text("⏱️ 잔여 ${land.auctionEndMinutesRemaining}분", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFFEF4444)))
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(land.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Text(land.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("현재 최고 입찰가", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${formatWld(land.currentAuctionBid.toString())} WLD", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFFEF4444), fontFamily = FontFamily.Monospace))
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("예상 일일 임대료", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${formatWld(land.dailyRent.toString())} WLD/일", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF10B981), fontFamily = FontFamily.Monospace))
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        haptic.click()
                                        bidInputAmount = (land.currentAuctionBid + 1000000L).toString()
                                        selectedLandForAuction = land
                                    },
                                    modifier = Modifier.fillMaxWidth().height(40.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("🔨 경매 입찰표 제출", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }
                }

                // 4. Regular Real Estate Lands Section
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("🏢 분양 및 매매 가능 부동산 & 필지", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(vertical = 6.dp))
                }

                items(regularLands) { land ->
                    val isMine = land.ownerId == "usr_self"
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isMine) Color(0xFF0F172A) else MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isMine) Color(0xFF3B82F6).copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(land.districtName, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6)))
                                if (isMine) {
                                    Surface(color = Color(0xFF1E3A8A), shape = RoundedCornerShape(6.dp)) {
                                        Text("🏢 본인 소유", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF93C5FD), fontWeight = FontWeight.Bold))
                                    }
                                } else {
                                    Text("매수 가능", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(land.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(land.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("공시 매매가", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${formatWld(land.purchasePrice.toString())} WLD", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("일일 임대료 수익", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("+${formatWld(land.dailyRent.toString())} WLD/일", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF10B981), fontFamily = FontFamily.Monospace))
                                }
                            }

                            if (!isMine) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        haptic.click()
                                        selectedLandForPurchase = land
                                    },
                                    modifier = Modifier.fillMaxWidth().height(40.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("등기 매수 (${formatWld(land.purchasePrice.toString())} WLD)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }
                }
            }
            else -> {
                item {
                    Text("부동산 정보를 준비 중입니다.", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }

    // Modal Bottom Sheet: Court Auction Bid
    FintechBottomSheetContainer(
        visible = selectedLandForAuction != null,
        onDismiss = { selectedLandForAuction = null },
        title = "🔨 법원 가상 경매 입찰"
    ) {
        val land = selectedLandForAuction
        if (land != null) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(land.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Text(land.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(10.dp))

                Text("현재 최고 입찰가: ${formatWld(land.currentAuctionBid.toString())} WLD", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFFEF4444), fontFamily = FontFamily.Monospace))
                Text("입찰 최소 단위: 100만 WLD 이상", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = bidInputAmount,
                    onValueChange = { bidInputAmount = it },
                    label = { Text("입찰 금액 (WLD)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val parsed = bidInputAmount.replace(",", "").trim().toLongOrNull() ?: 0L
                        if (parsed > land.currentAuctionBid) {
                            haptic.success()
                            economyViewModel.bidRealEstateAuction(land.id, parsed)
                            selectedLandForAuction = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("입찰표 전자 서명 및 제출", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }

    // Modal Bottom Sheet: Direct Land Purchase
    FintechBottomSheetContainer(
        visible = selectedLandForPurchase != null,
        onDismiss = { selectedLandForPurchase = null },
        title = "🏢 부동산 필지 등기 매수"
    ) {
        val land = selectedLandForPurchase
        if (land != null) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(land.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Text(land.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(10.dp))

                Text("매입 대금: ${formatWld(land.purchasePrice.toString())} WLD", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace))
                Text("소유 시 일일 기대 임대료: +${formatWld(land.dailyRent.toString())} WLD / 일", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF10B981), fontFamily = FontFamily.Monospace))

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        haptic.success()
                        economyViewModel.purchaseRealEstateLand(land.id, land.purchasePrice)
                        selectedLandForPurchase = null
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("매매 계약 체결 및 소유권 등기", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }

    if (showWorldMap) {
        WoldeokWorldMapDialog(onDismiss = { showWorldMap = false })
    }
}
