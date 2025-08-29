package net.lateinit.noiseguard

import androidx.compose.runtime.Composable
import net.lateinit.noiseguard.presentation.theme.NoiseGuardTheme
import net.lateinit.noiseguard.presentation.ui.navigation.NoiseGuardNavigation

/**
 * Android 플랫폼용 NoiseGuardApp
 * Koin은 Application 클래스에서 초기화됨
 */
@Composable
actual fun NoiseGuardApp() {
    NoiseGuardTheme {
        NoiseGuardNavigation()
    }
}