package ru.snowmaze.barstats.errors

class FileOpenException(message: String, cause: Throwable): RuntimeException(message, cause)