package net.lateinit.noiseguard.data.ml

import android.content.Context
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 모델 파일을 앱의 캐시 디렉토리에 복사하고 경로를 제공하는 유틸리티 객체
 * 앱의 assets 폴더에 있는 모델 파일을 캐시 디렉토리로 복사하여
 * MediaPipe나 TensorFlow Lite와 같은 라이브러리에서 접근할 수 있도록 함
 *
 */
object ModelFileProvider {
    suspend fun ensureModelFile(context: Context, fileName: String): String {
        val target = File(context.cacheDir, fileName)
        if (!target.exists()) {
            val bytes = withContext(Dispatchers.IO) {
                context.assets.open(fileName).use { it.readBytes() }
            }
            withContext(Dispatchers.IO) {
                target.outputStream().use { it.write(bytes) }
            }
        }
        return target.absolutePath
    }

    suspend fun readTextResource(context: Context, fileName: String): String {
        val bytes = withContext(Dispatchers.IO) {
            context.assets.open(fileName).use { it.readBytes() }
        }
        return bytes.decodeToString()
    }
}
