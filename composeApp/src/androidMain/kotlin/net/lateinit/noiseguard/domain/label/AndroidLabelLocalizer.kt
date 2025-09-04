package net.lateinit.noiseguard.domain.label

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

class AndroidLabelLocalizer(private val context: Context) : LabelLocalizer {
    @Volatile
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
            val filename = "yamnet_class_map_ko.csv"
            try {
                context.assets.open(filename).use { input ->
                    BufferedReader(InputStreamReader(input)).use { br ->
                        parseCsv(br)
                    }
                }
                Log.d("LabelLocalizer", "Loaded KO labels from assets/$filename (${indexToKo.size} entries)")
            } catch (e: Exception) {
                Log.w("LabelLocalizer", "Failed to load $filename; falling back to English labels", e)
            }
            loaded = true
        }
    }

    private fun parseCsv(br: BufferedReader) {
        indexToKo.clear()
        val header = br.readLine() ?: return
        val cols = header.split(',').map { it.trim().lowercase() }
        val idxPos = cols.indexOfFirst { it == "index" }
        val enPos = cols.indexOfFirst { it == "en" || it.contains("english") }
        val koPos = cols.indexOfFirst { it == "ko" || it.contains("korean") }
        var line: String?
        while (true) {
            line = br.readLine() ?: break
            if (line.isBlank()) continue
            val parts = smartSplitCsv(line)
            fun parseIndexFromFirstNumeric(): Int? {
                val firstNum = parts.firstOrNull()?.trim()?.toIntOrNull()
                return firstNum
            }
            val index = when {
                idxPos >= 0 && idxPos < parts.size -> parts[idxPos].trim().toIntOrNull()
                else -> parseIndexFromFirstNumeric()
            } ?: continue
            val ko = when {
                koPos >= 0 && koPos < parts.size -> unquote(parts[koPos].trim())
                parts.size >= 2 -> unquote(parts.last().trim())
                else -> null
            } ?: continue
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
