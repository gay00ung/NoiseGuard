package net.lateinit.noiseguard.resources

import org.jetbrains.compose.resources.ExperimentalResourceApi
import noiseguard.composeapp.generated.resources.Res

/**
 * Compose Multiplatform의 리소스 시스템을 사용하여 모델 파일을 읽는 유틸리티 객체
 * assets/models/ 디렉토리에 있는 파일을 바이트 배열이나 문자열로 읽어옴
 */
object ModelResources {
    @OptIn(ExperimentalResourceApi::class)
    suspend fun readBytes(fileName: String): ByteArray =
        Res.readBytes("models/$fileName")

    @OptIn(ExperimentalResourceApi::class)
    suspend fun readText(fileName: String): String =
        Res.readBytes("models/$fileName").decodeToString()
}
