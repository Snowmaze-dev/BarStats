package ru.snowmaze.barstats

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlin.coroutines.CoroutineContext

suspend fun <T, R> Collection<T>.parallelMap(
    parallelism: Int,
    mapContext: CoroutineContext = Dispatchers.IO + SupervisorJob(),
    transform: suspend (T) -> R
) = parallelMap(Semaphore(parallelism), mapContext, transform)

suspend fun <T, R> Collection<T>.parallelMap(
    semaphore: Semaphore,
    mapContext: CoroutineContext = Dispatchers.IO + SupervisorJob(),
    transform: suspend (T) -> R
) = map {
    coroutineScope {
        async(mapContext) {
            semaphore.withPermit {
                transform(it)
            }
        }
    }
}.awaitAll()