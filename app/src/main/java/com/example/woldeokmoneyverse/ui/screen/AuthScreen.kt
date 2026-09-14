package com.example.woldeokmoneyverse.ui.screen

import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.woldeokmoneyverse.R
import com.example.woldeokmoneyverse.data.local.SessionManager
import com.example.woldeokmoneyverse.data.model.AuthResponse
import com.example.woldeokmoneyverse.data.model.AuthProviderDto
import com.example.woldeokmoneyverse.data.model.UiState
import com.example.woldeokmoneyverse.ui.component.MoneyverseButton
import com.example.woldeokmoneyverse.ui.component.MoneyverseCard
import com.example.woldeokmoneyverse.ui.component.MoneyverseSecondaryButton
import com.example.woldeokmoneyverse.ui.component.MoneyverseSubTabRow
import com.example.woldeokmoneyverse.ui.viewmodel.AuthViewModel

enum class AuthType {
    LOCAL_LOGIN, REGISTER, OAUTH_GOOGLE, OAUTH_DISCORD
}

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: (AuthResponse) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Login, 1: Register
    val authTabs = listOf("🔑 로그인", "📝 회원가입")

    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var displayNameInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var agreeTerms by remember { mutableStateOf(SessionManager.isTermsAgreed(context)) }
    var agreePrivacy by remember { mutableStateOf(SessionManager.isTermsAgreed(context)) }
    var agreeAge by remember { mutableStateOf(SessionManager.isTermsAgreed(context)) }

    var validationError by remember { mutableStateOf<String?>(null) }
    var webViewModalTitle by remember { mutableStateOf<String?>(null) }
    var webViewModalUrl by remember { mutableStateOf<String?>(null) }

    var isOAuthLoading by remember { mutableStateOf(false) }
    var verificationCodeInput by remember { mutableStateOf("") }

    val authState by authViewModel.authState.collectAsState()
    val authProvidersState by authViewModel.authProvidersState.collectAsState()
    val isVerificationPending by authViewModel.isVerificationPending.collectAsState()
    val registeredEmail by authViewModel.registeredEmail.collectAsState()

    val isActionLoading = authState is UiState.Loading || isOAuthLoading

    LaunchedEffect(Unit) {
        authViewModel.loadAuthProviders()
    }

    LaunchedEffect(authState) {
        if (authState is UiState.Success) {
            val res = (authState as UiState.Success).data
            if (!res.verificationRequired) {
                onLoginSuccess(res)
            }
        }
    }

    fun launchCustomTabOAuth(provider: String) {
        if (isOAuthLoading) return
        isOAuthLoading = true
        authViewModel.startOAuthLogin(
            provider = provider,
            onUrlReady = { authorizationUrl ->
                isOAuthLoading = false
                try {
                    val customTabsIntent = CustomTabsIntent.Builder()
                        .setShowTitle(true)
                        .build()
                    customTabsIntent.launchUrl(context, Uri.parse(authorizationUrl))
                } catch (_: Exception) {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(authorizationUrl))
                    context.startActivity(browserIntent)
                }
            },
            onFailure = { error ->
                isOAuthLoading = false
                validationError = error
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🌙 ${stringResource(R.string.app_name)}",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(R.string.auth_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Native M3 Login / Register Mode Switcher SubTab Row
        MoneyverseSubTabRow(
            tabs = authTabs,
            selectedTabIndex = selectedTab,
            onTabSelected = {
                selectedTab = it
                validationError = null
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        MoneyverseCard {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = {
                        emailInput = it
                        validationError = null
                    },
                    label = { Text("이메일 주소") },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        validationError = null
                    },
                    label = { Text("비밀번호") },
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                if (selectedTab == 1) {
                    OutlinedTextField(
                        value = displayNameInput,
                        onValueChange = {
                            displayNameInput = it
                            validationError = null
                        },
                        label = { Text("닉네임 (표시명)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                if (selectedTab == 1) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("📜 법적 약관 필수 동의", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { agreeTerms = !agreeTerms }) {
                        Checkbox(checked = agreeTerms, onCheckedChange = { agreeTerms = it })
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("서비스 이용약관 동의 (필수)", style = MaterialTheme.typography.bodySmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { agreePrivacy = !agreePrivacy }) {
                        Checkbox(checked = agreePrivacy, onCheckedChange = { agreePrivacy = it })
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("개인정보 처리방침 동의 (필수)", style = MaterialTheme.typography.bodySmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { agreeAge = !agreeAge }) {
                        Checkbox(checked = agreeAge, onCheckedChange = { agreeAge = it })
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("만 14세 이상 이용자 확인 (필수)", style = MaterialTheme.typography.bodySmall)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { webViewModalTitle = "공식 이용약관 전문"; webViewModalUrl = "https://easy-scraping.com/terms" }, modifier = Modifier.weight(1f)) {
                            Text("이용약관 전문 보기", style = MaterialTheme.typography.labelSmall)
                        }
                        TextButton(onClick = { webViewModalTitle = "공식 개인정보 처리방침 전문"; webViewModalUrl = "https://easy-scraping.com/privacy" }, modifier = Modifier.weight(1f)) {
                            Text("개인정보방침 전문 보기", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                validationError?.let { errText ->
                    Text(
                        text = errText,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                MoneyverseButton(
                    text = if (isActionLoading) "요청 처리 중..." else if (selectedTab == 0) "실제 라이브 로그인" else "실제 라이브 회원가입",
                    onClick = {
                        if (selectedTab == 1 && (!agreeTerms || !agreePrivacy || !agreeAge)) {
                            validationError = "필수 약관 및 14세 이상 확인에 동의해 주세요."
                            return@MoneyverseButton
                        }
                        if (emailInput.isBlank() || passwordInput.isBlank()) {
                            validationError = "이메일 주소와 비밀번호를 입력해 주세요."
                            return@MoneyverseButton
                        }
                        if (selectedTab == 1 && displayNameInput.trim().codePointCount(0, displayNameInput.trim().length) < 2) {
                            validationError = "닉네임(표시명)은 2자 이상 입력해 주세요."
                            return@MoneyverseButton
                        }

                        validationError = null

                        if (selectedTab == 0) {
                            authViewModel.login(emailInput, passwordInput)
                        } else {
                            authViewModel.register(emailInput, passwordInput, displayNameInput)
                        }
                    },
                    enabled = !isActionLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                )

                // Native BFF OAuth Launch via Custom Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MoneyverseSecondaryButton(
                        text = "Google OAuth",
                        onClick = {
                            launchCustomTabOAuth("google")
                        },
                        enabled = !isActionLoading && authProvidersState.isProviderEnabled("google"),
                        modifier = Modifier.weight(1f)
                    )
                    MoneyverseSecondaryButton(
                        text = "Discord OAuth",
                        onClick = {
                            launchCustomTabOAuth("discord")
                        },
                        enabled = !isActionLoading && authProvidersState.isProviderEnabled("discord"),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (authState is UiState.Error) {
            val err = (authState as UiState.Error).message
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = err,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Live Website Legal Terms / Privacy Policy In-App Viewer Modal
    webViewModalUrl?.let { url ->
        AlertDialog(
            onDismissRequest = {
                webViewModalUrl = null
                webViewModalTitle = null
            },
            title = { Text(webViewModalTitle ?: "법적 약관 안내") },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                @Suppress("SetJavaScriptEnabled")
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                webViewClient = WebViewClient()
                                loadUrl(url)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    webViewModalUrl = null
                    webViewModalTitle = null
                }) {
                    Text("확인 및 닫기")
                }
            }
        )
    }

    // Email Verification Code Input Modal Dialog
    if (isVerificationPending) {
        AlertDialog(
            onDismissRequest = { authViewModel.dismissVerification() },
            title = { Text("📧 이메일 인증 토큰 입력", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "회원가입 요청이 성공적으로 접수되었습니다.\n[${registeredEmail}] 주소로 발송된 이메일 인증 토큰을 입력해 주세요.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = verificationCodeInput,
                        onValueChange = { verificationCodeInput = it },
                        label = { Text("인증 토큰 (Verification Token)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (verificationCodeInput.isNotBlank()) {
                            val supplied = verificationCodeInput.trim()
                            val token = Uri.parse(supplied).getQueryParameter("token") ?: supplied
                            authViewModel.verifyEmail(token)
                        }
                    },
                    enabled = verificationCodeInput.isNotBlank() && !isActionLoading
                ) {
                    Text(if (isActionLoading) "인증 중..." else "인증 완료 및 로그인")
                }
            },
            dismissButton = {
                TextButton(onClick = { authViewModel.dismissVerification() }) {
                    Text("취소")
                }
            }
        )
    }
}

private fun UiState<List<AuthProviderDto>>.isProviderEnabled(provider: String): Boolean = when (this) {
    is UiState.Success -> data.any { it.provider.equals(provider, ignoreCase = true) && it.enabled }
    // Provider discovery is advisory.  Do not make OAuth unreachable when its
    // optional catalogue endpoint is delayed or unavailable: the provider's
    // dedicated mobile authorize endpoint remains the authority.
    else -> true
}
