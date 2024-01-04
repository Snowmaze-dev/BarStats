package ru.snowmaze.barstats

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import ru.snowmaze.barstats.models.external.CachedPlayerModel
import ru.snowmaze.barstats.models.external.MatchModel
import ru.snowmaze.barstats.models.external.SimpleMatchesResponse

interface BarAPIService {

    @GET("replays")
    suspend fun getMatches(
        @Query("players") players: String,
        @Query("preset") preset: String? = null,
        @Query("limit") limit: Int = 200000,
        @Query("hasBots") hasBots: Boolean = false,
        @Query("endedNormally") endedNormally: Boolean = true
    ): SimpleMatchesResponse

    @GET("replays/{id}")
    suspend fun getMatch(@Path("id") id: String): MatchModel

    @GET("cached-users")
    suspend fun getPlayers(): List<CachedPlayerModel>
}