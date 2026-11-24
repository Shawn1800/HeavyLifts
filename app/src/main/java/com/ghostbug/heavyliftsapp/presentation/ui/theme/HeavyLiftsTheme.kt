package com.ghostbug.heavyliftsapp.presentation.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.ghostbug.heavyLifts.R

object HeavyLiftsColors {
    val White = Color(0xFFFFFFFF)
    val Navy = Color(0xFF0F1E33)
    val NavyStrong = Color(0xFF002B52)
    val TextSecondary = Color(0xFF41546E)
    val TextMuted = Color(0xFF4F6380)
    val Outline = Color(0xFF7C93B0)

    val Bg = White
    val BgElevated = Color(0xFFE2EDFA)
    val BgOverlay = Color(0xFFEEF4FC)
    val BgChip = Color(0xFFD3E3F7)
    val NavBackground = BgOverlay

    val Fg1 = Navy
    val Fg2 = TextSecondary
    val Fg3 = TextMuted
    val Fg4 = Outline

    val Accent = Color(0xFF5AB0FF)
    val AccentStrong = Color(0xFF1F6FEB)
    val AccentSoft = Color(0xFFC5E0FF)
    val AccentMuted = Color(0xFFA8D2FF)
    val Success = Color(0xFF2E7D32)
    val Info = AccentStrong
    val Danger = Color(0xFFB3261E)
    val DangerSoft = Color(0xFFFFD9DE)

    val Green500 = Success
    val Red500 = Danger
    val BorderSubtle = BgChip
    val BorderDefault = Outline
    val BorderStrong = TextSecondary
    val Scrim = Color(0x660F1E33)
}

private val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val bricolageGrotesque = GoogleFont("Bricolage Grotesque")
private val dmSans = GoogleFont("DM Sans")

object HeavyLiftsType {
    val Display = FontFamily(
        Font(googleFont = bricolageGrotesque, fontProvider = fontProvider, weight = FontWeight.Medium),
        Font(googleFont = bricolageGrotesque, fontProvider = fontProvider, weight = FontWeight.SemiBold),
        Font(googleFont = bricolageGrotesque, fontProvider = fontProvider, weight = FontWeight.Bold),
        Font(googleFont = bricolageGrotesque, fontProvider = fontProvider, weight = FontWeight.ExtraBold),
    )

    val Body = FontFamily(
        Font(googleFont = dmSans, fontProvider = fontProvider, weight = FontWeight.Normal),
        Font(googleFont = dmSans, fontProvider = fontProvider, weight = FontWeight.Medium),
        Font(googleFont = dmSans, fontProvider = fontProvider, weight = FontWeight.SemiBold),
        Font(googleFont = dmSans, fontProvider = fontProvider, weight = FontWeight.Bold),
    )
}
