package com.example.woldeokmoneyverse.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class ThemePreset(val title: String) {
    MIDNIGHT("🌙 미드나잇 AI 네온 (다크)"),
    DAYLIGHT("☀️ 피낸셜 클린 (라이트)"),
    CYBERPUNK("🏙️ 사이버펑크 AI (네온)"),
    GOLD_WEALTH("👑 골든 럭셔리 (골드/블랙)"),
    EMERALD_FINTECH("🌿 에메랄드 핀테크 (딥그린)"),
    DEEP_VIOLET("🔮 딥 바이올렛 AI (자주/보라)")
}

private val MidnightColorScheme = darkColorScheme(
    primary = Color(0xFF6366F1),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF312E81),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFF10B981),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF064E3B),
    background = Color(0xFF0B0F19),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF131C2E),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFFCBD5E1),
    error = Color(0xFFEF4444)
)

private val DaylightColorScheme = lightColorScheme(
    primary = Color(0xFF4F46E5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEF2FF),
    onPrimaryContainer = Color(0xFF312E81),
    secondary = Color(0xFF059669),
    onSecondary = Color.White,
    secondaryContainer = Color(0xD1D1FAE5),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    error = Color(0xFFDC2626)
)

private val CyberpunkColorScheme = darkColorScheme(
    primary = Color(0xFFEC4899),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF831843),
    onPrimaryContainer = Color(0xFFFCE7F3),
    secondary = Color(0xFF06B6D4),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF164E63),
    background = Color(0xFF030712),
    onBackground = Color(0xFFF9FAFB),
    surface = Color(0xFF111827),
    onSurface = Color(0xFFF3F4F6),
    surfaceVariant = Color(0xFF1F2937),
    onSurfaceVariant = Color(0xFF9CA3AF),
    error = Color(0xFFF43F5E)
)

private val GoldWealthColorScheme = darkColorScheme(
    primary = Color(0xFFF59E0B),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF78350F),
    onPrimaryContainer = Color(0xFFFEF3C7),
    secondary = Color(0xFF10B981),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF064E3B),
    background = Color(0xFF18181B),
    onBackground = Color(0xFFFAFAFA),
    surface = Color(0xFF27272A),
    onSurface = Color(0xFFF4F4F5),
    surfaceVariant = Color(0xFF3F3F46),
    onSurfaceVariant = Color(0xFFD4D4D8),
    error = Color(0xFFEF4444)
)

private val EmeraldFintechColorScheme = darkColorScheme(
    primary = Color(0xFF10B981),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF064E3B),
    onPrimaryContainer = Color(0xD1D1FAE5),
    secondary = Color(0xFF3B82F6),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1E3A8A),
    background = Color(0xFF022C22),
    onBackground = Color(0xFFECFDF5),
    surface = Color(0xFF065F46),
    onSurface = Color(0xFFF0FDF4),
    surfaceVariant = Color(0xFF047857),
    onSurfaceVariant = Color(0xFFA7F3D0),
    error = Color(0xFFF87171)
)

private val DeepVioletColorScheme = darkColorScheme(
    primary = Color(0xFFA855F7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF581C87),
    onPrimaryContainer = Color(0xFFF3E8FF),
    secondary = Color(0xFFEC4899),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF831843),
    background = Color(0xFF0F0716),
    onBackground = Color(0xFFFAF5FF),
    surface = Color(0xFF1E1035),
    onSurface = Color(0xFFF5F3FF),
    surfaceVariant = Color(0xFF2E1A47),
    onSurfaceVariant = Color(0xFFDDD6FE),
    error = Color(0xFFFB7185)
)

@Composable
fun WoldeokThemeContainer(
    preset: ThemePreset = ThemePreset.MIDNIGHT,
    customPrimaryColor: Color? = null,
    content: @Composable () -> Unit
) {
    val baseScheme = when (preset) {
        ThemePreset.MIDNIGHT -> MidnightColorScheme
        ThemePreset.DAYLIGHT -> DaylightColorScheme
        ThemePreset.CYBERPUNK -> CyberpunkColorScheme
        ThemePreset.GOLD_WEALTH -> GoldWealthColorScheme
        ThemePreset.EMERALD_FINTECH -> EmeraldFintechColorScheme
        ThemePreset.DEEP_VIOLET -> DeepVioletColorScheme
    }

    val finalScheme = if (customPrimaryColor != null) {
        baseScheme.copy(primary = customPrimaryColor)
    } else {
        baseScheme
    }

    MaterialTheme(
        colorScheme = finalScheme,
        content = content
    )
}
