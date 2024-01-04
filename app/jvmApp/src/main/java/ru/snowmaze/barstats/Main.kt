package ru.snowmaze.barstats

import de.jensklingenberg.ktorfit.ktorfit
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.json.Json
import okio.Path.Companion.toOkioPath
import ru.snowmaze.barstats.repository.GetMatchesRepository
import ru.snowmaze.barstats.repository.PlayersRepository
import ru.snowmaze.barstats.usecases.GetPlayerStatsUseCase
import ru.snowmaze.barstats.usecases.GetStatisticsUseCase
import ru.snowmaze.barstats.usecases.PlayersTopUseCase
import java.io.File

fun main(args: Array<String>) = runBlocking(Dispatchers.IO) {
    val jsonSerializer = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
    val json = StringFormatFileDataWriter(jsonSerializer)
    val hocon = HoconFileDataWriter(Hocon {
        encodeDefaults = true
    })

    val dataDir = File("data").toOkioPath()
    val appParamsFile = File("app-params.conf")
    if (!appParamsFile.exists()) {
        appParamsFile.createNewFile()
        javaClass.classLoader.getResourceAsStream("app-params.conf")?.use { inputStream ->
            appParamsFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        println("Params file created ${appParamsFile.name}")
        return@runBlocking
    }
    val params = hocon.parse<Map<String, String>>(appParamsFile.toOkioPath())
    val type = params["type"] ?: throw IllegalArgumentException("'type' field not specified.")
    val preset = params["preset"] ?: "team"

    val ktorfit = ktorfit {
        baseUrl(params["bar_api_url"] ?: "https://api.bar-rts.com/")
        httpClient(HttpClient {
            install(ContentNegotiation) {
                json(jsonSerializer)
            }
        })
    }

    val service = ktorfit.create<BarAPIService>()
    val getMatchesRepository = GetMatchesRepository(service, json, dataDir)
    val playersRepository = PlayersRepository(service, json, dataDir)
    val getPlayerStatsUseCase =
        GetPlayerStatsUseCase(getMatchesRepository, playersRepository, jsonSerializer)
    val fromDate = params["from_date"]
    val sectionsPrinter = SectionsPrinter()
    var section: Section? = null

    when (type) {
        "get_player_stats" -> {
            val getStatisticsUseCase = GetStatisticsUseCase(getPlayerStatsUseCase)
            val playerName = params["player"]
                ?: throw IllegalArgumentException("'player' field not specified.")
            println("Getting player $playerName stats for analyze")
            section = getStatisticsUseCase.getStatistics(
                playerName = playerName,
                preset = preset,
                shouldPrintStatsWithOtherPlayers = params["should_gather_additional_data"].toBoolean(),
                map = params["map"],
                fromTimeSeconds = fromDate?.let {
                    runCatching {
                        defaultParseDateToSeconds(fromDate)
                    }.getOrElse {
                        println("Cant parse date $fromDate")
                        null
                    }
                },
                countOfTeammatesAndEnemiesToShow = params["count_of_teammates_and_enemies_show"]?.toIntOrNull()
                    ?: 50,
                minGamesForStats = params["min_games_for_stats"]?.toIntOrNull() ?: 10,
                limit = params["count_of_last_games"]?.toIntOrNull()
            ).mapToSection(params["splitter_style"] ?: " | ")
        }

        "show_cached_top_of_players" -> {
            val playersTopUseCase = PlayersTopUseCase(
                getMatchesRepository = getMatchesRepository,
                playersRepository = playersRepository,
                getPlayerStatsUseCase = getPlayerStatsUseCase,
                dataDir = dataDir,
            )
            playersTopUseCase.printTopByPlayerMatches(preset = preset, map = params["map"])
        }
    }

    sectionsPrinter.printSections(section ?: return@runBlocking)
}