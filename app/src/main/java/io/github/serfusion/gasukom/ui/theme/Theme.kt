package io.github.serfusion.gasukom.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Warna tambahan untuk status setoran (dipakai di luar skema Material). */
object WarnaStatus {
    val Aman = Color(0xFF1E8E3E)
    val Peringatan = Color(0xFFB26A00)
    val Bahaya = Color(0xFFC62828)
}

private val SkemaTerang = lightColorScheme(
    primary = Color(0xFF1B6B45),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFA6F2C3),
    onPrimaryContainer = Color(0xFF002111),
    secondary = Color(0xFF4E6354),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD0E8D5),
    onSecondaryContainer = Color(0xFF0B1F13),
    tertiary = Color(0xFF7A5900),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDEA3),
    onTertiaryContainer = Color(0xFF261900),
    background = Color(0xFFF6FBF4),
    onBackground = Color(0xFF181D19),
    surface = Color(0xFFF6FBF4),
    onSurface = Color(0xFF181D19),
    surfaceVariant = Color(0xFFDCE5DB),
    onSurfaceVariant = Color(0xFF414942),
)

private val SkemaGelap = darkColorScheme(
    primary = Color(0xFF8BD6A8),
    onPrimary = Color(0xFF003920),
    primaryContainer = Color(0xFF005231),
    onPrimaryContainer = Color(0xFFA6F2C3),
    secondary = Color(0xFFB4CCB9),
    onSecondary = Color(0xFF203527),
    secondaryContainer = Color(0xFF364B3D),
    onSecondaryContainer = Color(0xFFD0E8D5),
    tertiary = Color(0xFFF3BF48),
    onTertiary = Color(0xFF402D00),
    tertiaryContainer = Color(0xFF5C4300),
    onTertiaryContainer = Color(0xFFFFDEA3),
    background = Color(0xFF101511),
    onBackground = Color(0xFFDFE4DD),
    surface = Color(0xFF101511),
    onSurface = Color(0xFFDFE4DD),
    surfaceVariant = Color(0xFF414942),
    onSurfaceVariant = Color(0xFFC0C9BF),
)

@Composable
fun GasukomTheme(
    gelap: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (gelap) SkemaGelap else SkemaTerang,
        content = content,
    )
}
