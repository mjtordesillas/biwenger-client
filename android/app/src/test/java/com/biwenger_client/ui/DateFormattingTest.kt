package com.biwenger_client.ui

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.Calendar

private fun at(hour: Int, dayOffset: Int = 0): Long {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, dayOffset)
    calendar.set(Calendar.HOUR_OF_DAY, hour)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

class DateFormattingTest {

    @Test
    fun `shows hours when the target is later today, under 8h away`() {
        val now = at(hour = 10)
        val until = at(hour = 13) / 1000 // 3h later, same day

        assertThat(formatRelativeTime(until = until, now = now)).isEqualTo("in 3 hours")
    }

    @Test
    fun `shows hours when the target is later today, even 8h or more away`() {
        val now = at(hour = 8)
        val until = at(hour = 23) / 1000 // 15h later, still today

        assertThat(formatRelativeTime(until = until, now = now)).isEqualTo("in 15 hours")
    }

    @Test
    fun `shows hours, not tomorrow, when under 8h away crosses midnight`() {
        val now = at(hour = 23)
        val until = at(hour = 1, dayOffset = 1) / 1000 // 2h later, next calendar day

        assertThat(formatRelativeTime(until = until, now = now)).isEqualTo("in 2 hours")
    }

    @Test
    fun `shows tomorrow when the target is tomorrow, 8h or more away`() {
        val now = at(hour = 8)
        val until = at(hour = 14, dayOffset = 1) / 1000 // 30h later, next calendar day

        assertThat(formatRelativeTime(until = until, now = now)).isEqualTo("tomorrow")
    }

    @Test
    fun `shows days when the target is beyond tomorrow`() {
        val now = at(hour = 8)
        val until = at(hour = 8, dayOffset = 3) / 1000

        assertThat(formatRelativeTime(until = until, now = now)).isEqualTo("in 3 days")
    }
}
