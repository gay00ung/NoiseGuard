package net.lateinit.noiseguard

import androidx.compose.runtime.Composable
import net.lateinit.noiseguard.core.di.appModules
import net.lateinit.noiseguard.core.di.iosPlatformModule
import net.lateinit.noiseguard.presentation.theme.NoiseGuardTheme
import net.lateinit.noiseguard.presentation.ui.navigation.NoiseGuardNavigation
import org.koin.compose.KoinApplication

/**
 * iOS 플랫폼용 NoiseGuardApp
 */
@Composable
actual fun NoiseGuardApp() {
    KoinApplication(
        application = {
            modules(appModules() + iosPlatformModule)
        }
    ) {
        NoiseGuardTheme {
            NoiseGuardNavigation()
        }
    }
}