package net.lateinit.noiseguard.data.ml

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import net.lateinit.noiseguard.resources.ModelResources
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSData
import platform.Foundation.NSBundle
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.create
import platform.Foundation.writeToFile

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData =
    this.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = this.size.toULong())
    }

object ModelFileProvider {
    /**
     * Compose 공용 리소스(models/<fileName>)에서 바이트를 읽어 iOS 캐시 디렉터리에 복사하고 절대 경로를 반환합니다.
     */
    @OptIn(ExperimentalForeignApi::class)
    suspend fun ensureModelFile(fileName: String): String {
        val cachesPaths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true)
        val cachesDir = (cachesPaths.firstOrNull() as? String) ?: NSTemporaryDirectory()
        val targetPath = "$cachesDir/$fileName"

        val fm = NSFileManager.defaultManager
        if (fm.fileExistsAtPath(targetPath)) return targetPath

        // 1) Try Compose resources first
        val composed = runCatching {
            val bytes = ModelResources.readBytes(fileName)
            val data = bytes.toNSData()
            data.writeToFile(targetPath, atomically = true)
            true
        }.getOrElse { false }
        if (composed) return targetPath

        // 2) Fallback: copy from iOS bundle resource if present
        findInBundle(fileName)?.let { bundlePath ->
            fm.copyItemAtPath(bundlePath, targetPath, null)
            return targetPath
        }

        error("Model resource not found: $fileName")
    }

    @OptIn(ExperimentalForeignApi::class)
    suspend fun readTextResource(fileName: String): String {
        val compose = runCatching { ModelResources.readText(fileName) }.getOrNull()
        if (compose != null) return compose
        val path = findInBundle(fileName) ?: return ""
        val data = NSData.dataWithContentsOfFile(path) ?: return ""
        val bytes = ByteArray(data.length.toInt())
        bytes.usePinned { pinned ->
            platform.posix.memcpy(pinned.addressOf(0), data.bytes, data.length)
        }
        return bytes.decodeToString()
    }

    private fun findInBundle(fileName: String): String? {
        val dot = fileName.lastIndexOf('.')
        val name = if (dot > 0) fileName.substring(0, dot) else fileName
        val ext = if (dot > 0) fileName.substring(dot + 1) else null
        // Try subdirectory "models" in bundle resources
        NSBundle.mainBundle.pathForResource(name, ext, "models")?.let { return it }
        // Try root of bundle
        return NSBundle.mainBundle.pathForResource(name, ext)
    }
}
