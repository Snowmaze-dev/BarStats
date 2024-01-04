package ru.snowmaze.barstats.repository

import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.datetime.Clock
import okio.Path
import ru.snowmaze.barstats.*
import ru.snowmaze.barstats.models.external.MatchModel
import ru.snowmaze.barstats.models.external.SimpleMatchModel
import ru.snowmaze.barstats.models.external.SimpleMatchesResponse

class GetMatchesRepository(
    private val barAPIInterface: BarAPIService,
    private val fileDataWriter: FileDataWriter,
    dataDir: Path,
) {

    private val playersDir = dataDir.resolve("players_matches")
    private val matchesDir = dataDir.resolve("matches")
    private val networkSemaphore = Semaphore(10)
    private val fileSystem = getSystemFileSystem()
    private val daySeconds = 3600 * 24

    suspend fun getPlayerMatches(playerName: String, preset: String): List<SimpleMatchModel> {
        val playerFile = playersDir.resolve("$playerName-$preset.json")

        return if (fileSystem.exists(playerFile)) try {
            val data = fileDataWriter.parse<SimpleMatchesResponse>(playerFile)
            if (Clock.System.now().epochSeconds - data.responseTime > daySeconds) {
                fileSystem.delete(playerFile)
                getPlayerMatches(playerName, preset)
            } else data.data
        } catch (e: Exception) {
            throw FileOpenException("Unable to read player matches in file $playerFile", e)
        }
        else {
            val matches = retryRequest {
                networkSemaphore.withPermit {
                    barAPIInterface.getMatches(playerName, preset.takeUnless { it == "all" })
                }
            }.getOrElse { exception ->
                throw RequestException("Unable to get matches of $playerName player", exception)
            }
            fileSystem.createDirectories(playersDir)
            kotlin.runCatching { fileDataWriter.write(matches, playerFile) }.onFailure {
                it.printStackTrace()
            }
            matches.data
        }
    }

    suspend fun getExtendedMatch(matchId: String): MatchModel {
        val matchFile = matchesDir.resolve("$matchId.json")
        return if (fileSystem.exists(matchFile)) fileDataWriter.parse<MatchModel>(matchFile)
        else {
            val match = retryRequest {
                networkSemaphore.withPermit { barAPIInterface.getMatch(matchId) }
            }.getOrElse { exception ->
                throw RequestException("Unable download match", exception)
            }
            fileSystem.createDirectories(matchesDir)
            kotlin.runCatching { fileDataWriter.write(match, matchFile) }.onFailure {
                it.printStackTrace()
            }
            match
        }
    }
}