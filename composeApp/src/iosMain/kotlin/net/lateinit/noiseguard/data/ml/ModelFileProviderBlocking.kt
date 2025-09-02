package net.lateinit.noiseguard.data.ml

import kotlinx.coroutines.runBlocking

/**
 * Swift에서 쉽게 호출할 수 있도록 제공하는 동기(Blocking) 래퍼 함수들.
 */
fun ensureModelFileBlocking(fileName: String): String =
    runBlocking { ModelFileProvider.ensureModelFile(fileName) }

fun readTextResourceBlocking(fileName: String): String =
    runBlocking { ModelFileProvider.readTextResource(fileName) }

