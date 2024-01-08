package ru.snowmaze.barstats

import java.text.SimpleDateFormat

private val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

actual fun parseDefaultDateToMillis(date: String) = format.parse(date).time