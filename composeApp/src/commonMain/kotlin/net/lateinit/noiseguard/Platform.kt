package net.lateinit.noiseguard

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform