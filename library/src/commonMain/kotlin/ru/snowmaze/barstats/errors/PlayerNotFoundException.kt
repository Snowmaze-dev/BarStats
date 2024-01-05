package ru.snowmaze.barstats.errors

class PlayerNotFoundException(message: String?, cause: Throwable? = null) :
    RuntimeException(message, cause)