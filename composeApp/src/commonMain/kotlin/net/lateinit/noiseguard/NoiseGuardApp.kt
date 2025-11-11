package net.lateinit.noiseguard

import androidx.compose.runtime.Composable

/**
 * 멀티플랫폼 앱 진입점
 * 각 플랫폼별로 구현 필요
 */
@Composable
expect fun NoiseGuardApp()