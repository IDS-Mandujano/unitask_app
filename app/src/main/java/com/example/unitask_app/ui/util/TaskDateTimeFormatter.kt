package com.example.unitask_app.ui.util

import android.content.Context
import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val parsePatterns = listOf(
    "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
    "yyyy-MM-dd'T'HH:mm:ss.SSSX",
    "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
    "yyyy-MM-dd'T'HH:mm:ssXXX",
    "yyyy-MM-dd'T'HH:mm:ssX",
    "yyyy-MM-dd'T'HH:mm:ssZ",
    "yyyy-MM-dd'T'HH:mm:ss'Z'",
    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
    "yyyy-MM-dd'T'HH:mmXXX",
    "yyyy-MM-dd'T'HH:mmX",
    "yyyy-MM-dd'T'HH:mmZ",
    "yyyy-MM-dd'T'HH:mm'Z'",
    "yyyy-MM-dd HH:mm:ss",
    "yyyy-MM-dd HH:mm"
)

fun formatDueDateForDisplay(raw: String, context: Context): String {
    if (raw.isBlank()) return "Sin fecha"
    val parsedDate = parseDate(raw) ?: return raw
    val outputPattern = if (DateFormat.is24HourFormat(context)) "dd/MM/yyyy HH:mm" else "dd/MM/yyyy hh:mm a"
    return SimpleDateFormat(outputPattern, Locale.getDefault()).format(parsedDate)
}

private fun parseDate(raw: String): Date? {
    parsePatterns.forEach { pattern ->
        val formatter = SimpleDateFormat(pattern, Locale.US)
        if (pattern.contains("'Z'")) {
            formatter.timeZone = TimeZone.getTimeZone("UTC")
        }
        formatter.isLenient = false
        val parsed = runCatching { formatter.parse(raw) }.getOrNull()
        if (parsed != null) return parsed
    }
    return null
}
