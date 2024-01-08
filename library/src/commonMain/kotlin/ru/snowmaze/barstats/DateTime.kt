package ru.snowmaze.barstats

import kotlinx.datetime.*

expect fun parseDefaultDateToMillis(date: String): Long

fun defaultParseDateToSeconds(date: String): Long {
    val parts = date.split(".")
    return (if (parts.size == 3) {
        val day = parts[0].toInt()
        val month = parts[1].toInt()
        val year = parts[2].toInt()
        LocalDateTime(
            LocalDate(year, month, day),
            LocalTime(0, 0, 0, 0)
        ).toInstant(TimeZone.UTC).epochSeconds
    } else (parseDefaultDateToMillis(date) / 1000))
}

fun defaultMultiplatformParseDateToSeconds(date: String): Long {
    return LocalDateTime.parse(date.dropLast(1)).toInstant(TimeZone.UTC).epochSeconds
}