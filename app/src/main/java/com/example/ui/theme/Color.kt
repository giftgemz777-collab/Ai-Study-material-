package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Light Theme Colors (Clean, Modern, Student-Friendly)
val IndigoPrimary = Color(0xFF4F46E5)
val IndigoOnPrimary = Color(0xFFFFFFFF)
val IndigoPrimaryContainer = Color(0xFFEEF2FF)
val IndigoOnPrimaryContainer = Color(0xFF1E1B4B)

val SlateSecondary = Color(0xFF0284C7)
val SlateOnSecondary = Color(0xFFFFFFFF)
val SlateSecondaryContainer = Color(0xFFE0F2FE)
val SlateOnSecondaryContainer = Color(0xFF075985)

val AmberTertiary = Color(0xFFD97706)
val AmberOnTertiary = Color(0xFFFFFFFF)
val AmberTertiaryContainer = Color(0xFFFEF3C7)
val AmberOnTertiaryContainer = Color(0xFF78350F)

val LightBackground = Color(0xFFF8FAFC)
val LightOnBackground = Color(0xFF0F172A)
val LightSurface = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF0F172A)
val LightSurfaceVariant = Color(0xFFF1F5F9)
val LightOnSurfaceVariant = Color(0xFF475569)
val LightOutline = Color(0xFFE2E8F0)
val LightOutlineVariant = Color(0xFFCBD5E1)

// Dark Theme Colors (Deep Navy / Midnight with crisp contrast)
val DarkIndigoPrimary = Color(0xFF818CF8)
val DarkIndigoOnPrimary = Color(0xFF0F172A)
val DarkIndigoPrimaryContainer = Color(0xFF312E81)
val DarkIndigoOnPrimaryContainer = Color(0xFFEEF2FF)

val DarkSlateSecondary = Color(0xFF38BDF8)
val DarkSlateOnSecondary = Color(0xFF082F49)
val DarkSlateSecondaryContainer = Color(0xFF0369A1)
val DarkSlateOnSecondaryContainer = Color(0xFFE0F2FE)

val DarkAmberTertiary = Color(0xFFFBBF24)
val DarkAmberOnTertiary = Color(0xFF78350F)
val DarkAmberTertiaryContainer = Color(0xFF78350F)
val DarkAmberOnTertiaryContainer = Color(0xFFFEF3C7)

val DarkBackground = Color(0xFF0B0F19)
val DarkOnBackground = Color(0xFFF8FAFC)
val DarkSurface = Color(0xFF111827)
val DarkOnSurface = Color(0xFFF8FAFC)
val DarkSurfaceVariant = Color(0xFF1F2937)
val DarkOnSurfaceVariant = Color(0xFF94A3B8)
val DarkOutline = Color(0xFF374151)
val DarkOutlineVariant = Color(0xFF4B5563)

// Semantic Accents
val SuccessGreen = Color(0xFF10B981)
val SuccessGreenContainer = Color(0xFFD1FAE5)
val DarkSuccessGreen = Color(0xFF34D399)
val DarkSuccessGreenContainer = Color(0xFF064E3B)

val WarningOrange = Color(0xFFF59E0B)
val WarningOrangeContainer = Color(0xFFFEF3C7)

val ErrorRed = Color(0xFFEF4444)
val ErrorRedContainer = Color(0xFFFEE2E2)
val DarkErrorRed = Color(0xFFF87171)
val DarkErrorRedContainer = Color(0xFF7F1D1D)

val PurpleAccent = Color(0xFF8B5CF6)
val PurpleAccentContainer = Color(0xFFEDE9FE)
val CyanAccent = Color(0xFF06B6D4)
val PinkAccent = Color(0xFFEC4899)
val EmeraldAccent = Color(0xFF059669)

// Beautiful Gradient Helpers for Modern Visual Craft
val PrimaryGradient = Brush.linearGradient(
    colors = listOf(IndigoPrimary, PurpleAccent)
)

val DarkPrimaryGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF6366F1), Color(0xFFA855F7))
)

val HeroCardGradientLight = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF4F46E5).copy(alpha = 0.08f),
        Color(0xFF8B5CF6).copy(alpha = 0.03f)
    )
)

val HeroCardGradientDark = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF312E81).copy(alpha = 0.4f),
        Color(0xFF1E1B4B).copy(alpha = 0.2f)
    )
)

val AmbientCardGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF6366F1).copy(alpha = 0.12f),
        Color(0xFF0EA5E9).copy(alpha = 0.06f)
    )
)

