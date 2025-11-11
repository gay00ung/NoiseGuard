package net.lateinit.noiseguard.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Primary700,
    onPrimary = White,
    primaryContainer = Primary100,
    onPrimaryContainer = Primary900,
    
    secondary = AccentTeal,
    onSecondary = White,
    secondaryContainer = Gray100,
    onSecondaryContainer = Gray900,
    
    tertiary = AccentPurple,
    onTertiary = White,
    tertiaryContainer = Primary100,
    onTertiaryContainer = Primary900,
    
    background = White,
    onBackground = Gray900,
    
    surface = White,
    onSurface = Gray900,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray700,
    
    outline = Gray300,
    outlineVariant = Gray100,
    
    error = Danger,
    onError = White,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    
    scrim = Gray900
)

private val DarkColorScheme = darkColorScheme(
    primary = Primary300,
    onPrimary = DarkBgPrimary,
    primaryContainer = Primary700,
    onPrimaryContainer = Primary100,
    
    secondary = AccentTeal,
    onSecondary = DarkBgPrimary,
    secondaryContainer = DarkBgSecondary,
    onSecondaryContainer = DarkTextPrimary,
    
    tertiary = AccentPurple,
    onTertiary = DarkBgPrimary,
    tertiaryContainer = DarkBgSecondary,
    onTertiaryContainer = DarkTextPrimary,
    
    background = DarkBgPrimary,
    onBackground = DarkTextPrimary,
    
    surface = DarkBgPrimary,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkBgSecondary,
    onSurfaceVariant = DarkTextSecondary,
    
    outline = DarkOutline,
    outlineVariant = DarkBgSecondary,
    
    error = DarkError,
    onError = DarkBgPrimary,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    
    scrim = Black
)

@Composable
fun NoiseGuardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = getTypography(),
        content = content
    )
}