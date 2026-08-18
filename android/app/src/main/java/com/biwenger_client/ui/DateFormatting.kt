package com.biwenger_client.ui

import java.util.Calendar
import kotlin.math.ceil

// Relative, per how soon `until` is:
// - <8h away, or today: "in N hours"
// - tomorrow: "tomorrow"
// - otherwise: "in N days"
// Shared by Market (listing expiry) and Squad (transfer-lock countdown) —
// same "how soon from now" shape, each screen wraps it in its own label
// ("Expires ..." vs "Sellable ...").
fun formatRelativeTime(until: Long, now: Long = System.currentTimeMillis()): String {
    val untilMillis = until * 1000
    val diffHours = (untilMillis - now) / (1000.0 * 60 * 60)
    val dayDiff = calendarDayDiff(untilMillis = untilMillis, nowMillis = now)

    return when {
        dayDiff <= 0 || diffHours < 8 -> "in ${ceil(diffHours).toLong().coerceAtLeast(1)} hours"
        dayDiff == 1L -> "tomorrow"
        else -> "in $dayDiff days"
    }
}

private fun calendarDayDiff(untilMillis: Long, nowMillis: Long): Long {
    fun startOfDay(millis: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    val millisPerDay = 1000L * 60 * 60 * 24
    return (startOfDay(untilMillis) - startOfDay(nowMillis)) / millisPerDay
}
