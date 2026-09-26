package com.example.woldeokmoneyverse

import android.content.Intent
import android.os.Bundle
import android.webkit.CookieManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.woldeokmoneyverse.data.local.SessionManager
import com.example.woldeokmoneyverse.data.model.AuthResponse
import com.example.woldeokmoneyverse.data.model.PolicyVersions
import com.example.woldeokmoneyverse.data.remote.ApiClient
import com.example.woldeokmoneyverse.data.repository.AuthRepository
import com.example.woldeokmoneyverse.ui.screen.*
import com.example.woldeokmoneyverse.ui.theme.WoldeokThemeContainer
import com.example.woldeokmoneyverse.ui.viewmodel.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val _isLoggedInFlow = MutableStateFlow(false)
    val isLoggedInFlow = _isLoggedInFlow.asStateFlow()
    private val _isSessionCheckingFlow = MutableStateFlow(true)
    private val _oauthErrorFlow = MutableStateFlow<String?>(null)

    private val authRepo = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ApiClient.init(this)
        ApiClient.csrfToken = SessionManager.getCsrfToken(this)
        _isLoggedInFlow.value = false

        val hasOAuthCallback = isOAuthCallbackIntent(intent)
        handleOAuthIntent(intent)

        setContent {
            val context = LocalContext.current

            val settingsViewModel: SettingsViewModel = viewModel()
            val authViewModel: AuthViewModel = viewModel()
            val currentTheme by settingsViewModel.selectedTheme.collectAsState()
            val customColor by settingsViewModel.customPrimaryColor.collectAsState()

            val isLoggedIn by _isLoggedInFlow.collectAsState()
            val isSessionChecking by _isSessionCheckingFlow.collectAsState()
            val oauthError by _oauthErrorFlow.collectAsState()

            WoldeokThemeContainer(preset = currentTheme, customPrimaryColor = customColor) {
                var showConsentDialog by remember { mutableStateOf(false) }
                var pendingPolicy by remember { mutableStateOf<PolicyVersions?>(null) }
                var policyError by remember { mutableStateOf<String?>(null) }
                var isSavingConsent by remember { mutableStateOf(false) }
                var isSessionReady by remember { mutableStateOf(false) }
                var verifiedAdminRoles by remember { mutableStateOf<List<String>>(emptyList()) }
                val coroutineScope = rememberCoroutineScope()
                var selectedTab by remember { mutableIntStateOf(0) }

                LaunchedEffect(Unit) {
                    if (!hasOAuthCallback) authRepo.verifyViewerSession().fold(
                        onSuccess = { viewer ->
                            if (viewer.signedIn) {
                                _isLoggedInFlow.value = true
                                SessionManager.saveSession(
                                    context = context,
                                    sessionCookie = null,
                                    csrfToken = ApiClient.csrfToken,
                                    userId = viewer.user?.userId,
                                    displayName = viewer.user?.displayName,
                                    email = viewer.user?.email,
                                    title = viewer.user?.title,
                                    level = viewer.user?.level ?: 1
                                )
                            } else {
                                _isLoggedInFlow.value = false
                                SessionManager.clearSession(context)
                                ApiClient.clearSession()
                            }
                        },
                        onFailure = {
                            _isLoggedInFlow.value = false
                        }
                    ).also { _isSessionCheckingFlow.value = false }
                }

                LaunchedEffect(isLoggedIn) {
                    isSessionReady = false
                    verifiedAdminRoles = if (isLoggedIn) verifyAdminRoles() else emptyList()
                    if (isLoggedIn) {
                        authRepo.getCurrentPolicyVersions().fold(
                            onSuccess = { policy ->
                                if (!SessionManager.hasAcceptedPolicyVersions(
                                        context,
                                        policy.termsVersion,
                                        policy.privacyVersion
                                    )
                                ) {
                                    pendingPolicy = policy
                                    showConsentDialog = true
                                } else {
                                    authRepo.refreshAuthenticatedSession().fold(
                                        onSuccess = { isSessionReady = true },
                                        onFailure = {
                                            pendingPolicy = policy
                                            showConsentDialog = true
                                        }
                                    )
                                }
                            },
                            onFailure = { policyError = it.message ?: "약관 버전을 확인하지 못했습니다." }
                        )
                    }
                }

                if (isSessionChecking) {
                    StartupLoadingScreen()
                } else if (!isLoggedIn) {
                    AuthScreen(
                        authViewModel = authViewModel,
                        onLoginSuccess = { authRes ->
                            SessionManager.saveSession(
                                context = context,
                                sessionCookie = null,
                                csrfToken = authRes.csrfToken ?: ApiClient.csrfToken,
                                userId = authRes.userId,
                                displayName = authRes.displayName,
                                email = authRes.email
                            )
                            _isLoggedInFlow.value = true
                        }
                    )
                } else {
                    val homeViewModel: HomeViewModel = viewModel()
                    val economyViewModel: EconomyViewModel = viewModel()
                    val playViewModel: PlayViewModel = viewModel()
                    val communityViewModel: CommunityViewModel = viewModel()

                    LaunchedEffect(isSessionReady) {
                        if (isSessionReady) {
                            homeViewModel.loadDashboardData()
                        }
                    }

                    MainAppScaffold(
                        selectedTab = selectedTab,
                        onTabSelected = { tab ->
                            selectedTab = tab
                            when (tab) {
                                0 -> homeViewModel.loadDashboardData()
                                1 -> economyViewModel.loadAllEconomyData()
                                2 -> playViewModel.loadPlayData()
                                3 -> communityViewModel.loadCommunityData()
                            }
                        },
                        homeViewModel = homeViewModel,
                        economyViewModel = economyViewModel,
                        playViewModel = playViewModel,
                        communityViewModel = communityViewModel,
                        authViewModel = authViewModel,
                        settingsViewModel = settingsViewModel,
                        adminRoles = verifiedAdminRoles,
                        onLoggedOut = {
                            selectedTab = 0
                            verifiedAdminRoles = emptyList()
                            _isLoggedInFlow.value = false
                        }
                    )
                }

                if (showConsentDialog) {
                    AlertDialog(
                        onDismissRequest = {},
                        title = { Text("📜 이용약관 및 개인정보 처리방침 변경 동의") },
                        text = {
                            Column {
                                Text(
                                    text = "월덕 머니버스 서비스 이용약관 및 개인정보 처리방침이 개정되었습니다.",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "서버 정책이 변경되었습니다. 계속하려면 새 이용약관 및 개인정보처리방침에 동의해 주세요.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                enabled = !isSavingConsent,
                                onClick = {
                                    val policy = pendingPolicy ?: return@Button
                                    isSavingConsent = true
                                    coroutineScope.launch {
                                        authRepo.acceptCurrentPolicy(policy).fold(
                                            onSuccess = {
                                                SessionManager.saveAcceptedPolicyVersions(
                                                    context,
                                                    policy.termsVersion,
                                                    policy.privacyVersion
                                                )
                                                showConsentDialog = false
                                                pendingPolicy = null
                                                authRepo.refreshAuthenticatedSession().fold(
                                                    onSuccess = { isSessionReady = true },
                                                    onFailure = {
                                                        policyError = it.message ?: "인증 세션을 갱신하지 못했습니다."
                                                    }
                                                )
                                            },
                                            onFailure = { policyError = it.message ?: "동의 저장에 실패했습니다." }
                                        )
                                        isSavingConsent = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (isSavingConsent) "동의 저장 중..." else "동의하고 서비스 계속 이용하기", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }

                policyError?.let { error ->
                    AlertDialog(
                        onDismissRequest = { policyError = null },
                        title = { Text("정책 확인 필요") },
                        text = { Text(error) },
                        confirmButton = { TextButton(onClick = { policyError = null }) { Text("확인") } }
                    )
                }

                oauthError?.let { error ->
                    AlertDialog(
                        onDismissRequest = { _oauthErrorFlow.value = null },
                        title = { Text("OAuth 로그인 실패") },
                        text = { Text(error) },
                        confirmButton = {
                            TextButton(onClick = { _oauthErrorFlow.value = null }) { Text("확인") }
                        }
                    )
                }
            }
        }
    }

    private suspend fun verifyAdminRoles(): List<String> {
        val viewerResponse = runCatching {
            ApiClient.api.contractGet("app-api/v1/auth/viewer")
        }.getOrNull() ?: return emptyList()
        if (!viewerResponse.isSuccessful) return emptyList()
        val viewer = viewerResponse.body()?.takeIf { it.isJsonObject }?.asJsonObject ?: return emptyList()
        if (viewer.get("signedIn")?.takeUnless { it.isJsonNull }?.asBoolean != true) return emptyList()
        val claimed = viewer.getAsJsonArray("adminRoles")
            ?.mapNotNull { if (it.isJsonPrimitive) it.asString else null }
            ?.filter { it.isNotBlank() }
            ?.distinct()
            ?: emptyList()
        if (claimed.isEmpty()) return emptyList()

        val adminResponse = runCatching {
            ApiClient.api.contractGet("app-api/v1/admin/me")
        }.getOrNull() ?: return emptyList()
        if (!adminResponse.isSuccessful) return emptyList()
        val admin = adminResponse.body()?.takeIf { it.isJsonObject }?.asJsonObject ?: return emptyList()
        val verified = admin.getAsJsonArray("roles")
            ?.mapNotNull { if (it.isJsonPrimitive) it.asString else null }
            ?.filter { it.isNotBlank() }
            ?.distinct()
            ?: emptyList()
        return verified.filter { it in claimed }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleOAuthIntent(intent)
    }

    private fun isOAuthCallbackIntent(intent: Intent?): Boolean {
        val uri = intent?.data ?: return false
        return uri.scheme == "woldeok-moneyverse" && uri.host == "oauth" && uri.path == "/callback"
    }

    private fun handleOAuthIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme != "woldeok-moneyverse" || uri.host != "oauth" || uri.path != "/callback") return

        val code = uri.getQueryParameter("code")
        if (code.isNullOrBlank()) {
            _oauthErrorFlow.value = "OAuth 인증이 취소되었거나 앱 복귀 코드가 없습니다."
            _isSessionCheckingFlow.value = false
            return
        }
        _isSessionCheckingFlow.value = true
        lifecycleScope.launch {
            authRepo.exchangeMobileHandoff(code).fold(
                onSuccess = { authRes ->
                    SessionManager.saveSession(
                        context = this@MainActivity,
                        sessionCookie = null,
                        csrfToken = ApiClient.csrfToken,
                        userId = authRes.userId,
                        displayName = authRes.displayName,
                        email = authRes.email
                    )
                    _oauthErrorFlow.value = null
                    _isLoggedInFlow.value = true
                },
                onFailure = {
                    _oauthErrorFlow.value = it.message
                        ?: "OAuth 앱 세션을 만들지 못했습니다. 다시 시도해 주세요."
                }
            )
            _isSessionCheckingFlow.value = false
        }
    }
}

@Composable
private fun StartupLoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("로그인 상태를 확인하고 있습니다…")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    homeViewModel: HomeViewModel,
    economyViewModel: EconomyViewModel,
    playViewModel: PlayViewModel,
    communityViewModel: CommunityViewModel,
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    adminRoles: List<String>,
    onLoggedOut: () -> Unit
) {
    var serverClockLabel by remember { mutableStateOf("서버 시간 확인 중…") }
    LaunchedEffect(Unit) {
        runCatching { ApiClient.api.contractGet("app-api/v1/game-clock") }
            .onSuccess { response ->
                val clock = response.body()?.takeIf { it.isJsonObject }?.asJsonObject
                if (response.isSuccessful && clock != null) {
                    val day = clock.get("dayIndex")?.takeUnless { it.isJsonNull }?.asLong ?: clock.get("day_index")?.takeUnless { it.isJsonNull }?.asLong ?: 0L
                    val week = clock.get("weekIndex")?.takeUnless { it.isJsonNull }?.asLong ?: clock.get("week_index")?.takeUnless { it.isJsonNull }?.asLong ?: 0L
                    val dayOfWeek = clock.get("dayOfWeek")?.takeUnless { it.isJsonNull }?.asInt ?: clock.get("day_of_week")?.takeUnless { it.isJsonNull }?.asInt ?: 1
                    serverClockLabel = "서버 ${day + 1}일 · ${week + 1}주차 ${dayOfWeek}일차 · 현실 10분=1일"
                } else {
                    serverClockLabel = "서버 시간 정보 없음"
                }
            }
            .onFailure { serverClockLabel = "서버 시간 연결 확인 필요" }
    }

    val navItems = listOf(
        NavItem("홈", Icons.Filled.Home),
        NavItem("경제", Icons.Filled.ShoppingCart),
        NavItem("플레이", Icons.Filled.PlayArrow),
        NavItem("커뮤니티", Icons.AutoMirrored.Filled.Send),
        NavItem("MY", Icons.Filled.Person)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Woldeok Moneyverse",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = serverClockLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { onTabSelected(6) }) {
                        Text("전체 API")
                    }
                    if (adminRoles.isNotEmpty()) {
                        TextButton(onClick = { onTabSelected(5) }) {
                            Text("관리자")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { onTabSelected(index) },
                        icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> HomeScreen(homeViewModel = homeViewModel, onNavigateToTab = onTabSelected)
                1 -> EconomyScreen(economyViewModel = economyViewModel)
                2 -> PlayScreen(playViewModel = playViewModel)
                3 -> CommunityScreen(communityViewModel = communityViewModel)
                4 -> MyScreen(
                    authViewModel = authViewModel,
                    settingsViewModel = settingsViewModel,
                    onLoggedOut = onLoggedOut,
                    onNavigateToAllFeatures = { onTabSelected(6) }
                )
                5 -> if (adminRoles.isNotEmpty()) {
                    AdminScreen(adminRoles)
                } else {
                    HomeScreen(homeViewModel = homeViewModel, onNavigateToTab = onTabSelected)
                }
                6 -> AllFeaturesScreen()
            }
        }
    }
}

data class NavItem(val label: String, val icon: ImageVector)
