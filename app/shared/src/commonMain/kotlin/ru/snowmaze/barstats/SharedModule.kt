package ru.snowmaze.barstats

import de.jensklingenberg.ktorfit.ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.StringFormat
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import ru.snowmaze.barstats.repository.GetMatchesRepository
import ru.snowmaze.barstats.repository.PlayersRepository
import ru.snowmaze.barstats.usecases.GetPlayerStatsUseCase
import ru.snowmaze.barstats.usecases.GetStatisticsUseCase

val sharedModule = module {
    single<Json> {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }
    }
    single<StringFormat> {
        get<Json>()
    }
    single<FileDataWriter> {
        StringFormatFileDataWriter(get())
    }
    single {
        val ktorfit = ktorfit {
            baseUrl("https://api.bar-rts.com/")
            httpClient(HttpClient {
                install(ContentNegotiation) {
                    json(get())
                }
            })
        }

        ktorfit.create<BarAPIService>()
    }
    single {
        GetMatchesRepository(get(), get(), get<DataPath>().path)
    }
    single {
        PlayersRepository(get(), get(), get<DataPath>().path)
    }
    single {
        GetPlayerStatsUseCase(get(), get(), get())
    }
    single {
        GetStatisticsUseCase(get())
    }
}