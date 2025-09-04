package net.lateinit.noiseguard.domain.label

import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.dataWithContentsOfFile
import kotlin.native.concurrent.AtomicInt

class IOSLabelLocalizer : LabelLocalizer {
    private var loaded = false
    private val indexToKo = mutableMapOf<Int, String>()
    private val nameToKo = mutableMapOf<String, String>()

    override fun localize(index: Int?, englishName: String): String {
        ensureLoaded()
        if (index != null) indexToKo[index]?.let { return it }
        nameToKo[englishName]?.let { return it }
        return englishName
    }

    private fun ensureLoaded() {
        if (loaded) return
        synchronized(this) {
            if (loaded) return
            val path = NSBundle.mainBundle.pathForResource("yamnet_class_map_ko", ofType = "csv")
            if (path != null) {
                val data: NSData? = NSData.dataWithContentsOfFile(path)
                val nsStr: NSString? = data?.let { NSString.create(it, NSUTF8StringEncoding) }
                val text = nsStr?.toString() ?: ""
                if (text.isNotBlank()) parseCsv(text)
            }
            loaded = true
        }
    }

    private fun parseCsv(text: String) {
        indexToKo.clear()
        nameToKo.clear()
        val lines = text.split('\n')
        if (lines.isEmpty()) return
        val cols = lines.first().split(',').map { it.trim().lowercase() }
        val idxPos = cols.indexOfFirst { it == "index" }
        val enPos = cols.indexOfFirst { it == "en" || it.contains("english") }
        val koPos = cols.indexOfFirst { it == "ko" || it.contains("korean") }
        lines.drop(1).forEach { line ->
            if (line.isBlank()) return@forEach
            val parts = smartSplitCsv(line)
            fun parseIndexFromFirstNumeric(): Int? = parts.firstOrNull()?.trim()?.toIntOrNull()
            val index = when {
                idxPos >= 0 && idxPos < parts.size -> parts[idxPos].trim().toIntOrNull()
                else -> parseIndexFromFirstNumeric()
            } ?: return@forEach
            val ko = when {
                koPos >= 0 && koPos < parts.size -> unquote(parts[koPos].trim())
                parts.size >= 2 -> unquote(parts.last().trim())
                else -> null
            } ?: return@forEach
            indexToKo[index] = ko
            val en = when {
                enPos >= 0 && enPos < parts.size -> unquote(parts[enPos].trim())
                else -> null
            }
            if (!en.isNullOrBlank()) nameToKo[en] = ko
        }
    }

    private fun unquote(s: String): String =
        if (s.length >= 2 && ((s.startsWith('"') && s.endsWith('"')) || (s.startsWith('\'') && s.endsWith('\'')))) s.substring(1, s.length - 1) else s

    private fun smartSplitCsv(line: String): List<String> {
        val out = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        line.forEach { ch ->
            when (ch) {
                '"' -> {
                    inQuotes = !inQuotes
                    sb.append(ch)
                }
                ',' -> if (inQuotes) sb.append(ch) else { out.add(sb.toString()); sb.setLength(0) }
                else -> sb.append(ch)
            }
        }
        out.add(sb.toString())
        return out
    }
}

