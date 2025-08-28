package net.lateinit.noiseguard

import androidx.compose.runtime.Composable
import net.lateinit.noiseguard.core.di.appModules
import org.koin.compose.KoinApplication
import net.lateinit.noiseguard.presentation.theme.NoiseGuardTheme
import net.lateinit.noiseguard.presentation.ui.navigation.NoiseGuardNavigation

@Composable
fun NoiseGuardApp() {
    KoinApplication(
        application = {
            modules(appModules())
        }
    ) {
        NoiseGuardTheme {
            NoiseGuardNavigation()
        }
    }
}