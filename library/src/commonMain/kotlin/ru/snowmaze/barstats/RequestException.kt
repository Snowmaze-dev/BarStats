package ru.snowmaze.barstats

import kotlinx.coroutines.delay

class RequestException(message: String, cause: Throwable) : RuntimeException(message, cause)

suspend fun <T : Any> retryRequest(times: Int = 3, errorDelay: Long = 1000L, block: suspend () -> T): Result<T> {
    var result: Result<T>? = null
    for (i in 0..<times) {
        result = runCatching { block() }
        if (result.isSuccess) break
        delay(errorDelay)
    }
    return result!!
}