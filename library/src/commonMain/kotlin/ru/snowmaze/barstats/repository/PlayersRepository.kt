package ru.snowmaze.barstats.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.Path
import ru.snowmaze.barstats.BarAPIService
import ru.snowmaze.barstats.FileDataWriter
import ru.snowmaze.barstats.errors.PlayerNotFoundException
import ru.snowmaze.barstats.getSystemFileSystem
import ru.snowmaze.barstats.models.external.CachedPlayerModel
import ru.snowmaze.barstats.parse
import ru.snowmaze.barstats.write

class PlayersRepository(
    private val apiService: BarAPIService,
    private val fileDataWriter: FileDataWriter,
    dataDir: Path,
    private val showInfo: ((String) -> Unit)? = null
) {

    private val playersNamesMutex = Mutex()
    private val mutex = Mutex()
    private val playersByName = mutableMapOf<String, CachedPlayerModel>()
    private val playersById = mutableMapOf<Long, CachedPlayerModel>()
    private val playersFile = dataDir.resolve("players.json")
    private val playerNicknamesById = mutableMapOf<Long, MutableStateFlow<Set<String>>>()
    private val fileSystem = getSystemFileSystem()

    suspend fun addPlayerNickname(userId: Long, name: String) {
        playersNamesMutex.withLock {
            val stateFlow = playerNicknamesById.getOrPut(userId) { MutableStateFlow(setOf(name)) }
            if (stateFlow.value.contains(name)) return@withLock
            stateFlow.value += name
        }
    }

    fun getPlayerNicknames(userId: Long) = playerNicknamesById[userId]

    suspend fun getPlayer(id: Long): CachedPlayerModel {
        if (playersById[id] == null) downloadPlayers()
        return playersById.getValue(id)
    }

    suspend fun getPlayer(name: String): CachedPlayerModel {
        val lowerCaseName = name.lowercase()
        if (playersByName[lowerCaseName] == null) downloadPlayers()
        return playersByName[lowerCaseName]
            ?: throw PlayerNotFoundException("Player $name not found.")
    }

    private suspend fun downloadPlayers() {
        if (playersByName.isNotEmpty()) {
            fileSystem.delete(playersFile)
        }
        mutex.withLock { getPlayers(playersByName.isNotEmpty()) }
    }

    private suspend fun getPlayers(forceDownload: Boolean = false): List<CachedPlayerModel> {
        val dataList = if (!forceDownload && fileSystem.exists(playersFile)) {
            try {
                fileDataWriter.parse<List<CachedPlayerModel>>(playersFile)
            } catch (e: Exception) {
                fileSystem.delete(playersFile)
                getPlayers()
            }
        } else {
            showInfo?.invoke("Downloading players list")
            val dataList = apiService.getPlayers()
            fileDataWriter.write(dataList, playersFile)
            dataList
        }
        playersByName.putAll(dataList.associateBy { it.username.lowercase() })
        playersById.putAll(dataList.associateBy { it.id })
        return dataList
    }
}