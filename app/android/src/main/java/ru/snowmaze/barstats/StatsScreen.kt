package ru.snowmaze.barstats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.snowmaze.barstats.models.MapStat
import ru.snowmaze.barstats.usecases.GetStatisticsResult
import ru.snowmaze.barstats.usecases.WithPlayerStat

@Composable
fun StatsScreen(paddingValues: PaddingValues = PaddingValues()) {
    val mainViewModel = mokoKoinViewModel<MainViewModel>()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, start = 12.dp, end = 12.dp)
            .padding(paddingValues)
    ) {
        val stateCollected by mainViewModel.state.collectAsState()
        when (val state = stateCollected) {
            is MainState.Empty -> Text(
                text = "Click load stats button.",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurface
            )

            is MainState.Loading -> Text(
                text = "Loading ${state.playerName} stats.",
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                color = MaterialTheme.colorScheme.onSurface
            )

            is MainState.Ready -> ReadyStatsScreen(getStatisticsResult = state.getStatisticsResult)
        }
        Spacer(modifier = Modifier.fillMaxHeight())
    }
}

@Composable
fun ReadyStatsScreen(getStatisticsResult: GetStatisticsResult) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 128.dp),
    ) {
        item(span = { GridItemSpan(3) }) {
            Text(
                text = "Stats for player ${getStatisticsResult.playerName}",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        val stats = getStatisticsResult.playerStats
        item {
            StatItem(
                title = "Total games analyzed",
                value = "${stats.totalMatchesCount}"
            )
        }
        item {
            StatItem(
                title = "Won games",
                value = "${stats.wonMatchesCount}"
            )
        }
        item {
            StatItem(
                title = "Winrate",
                value = "${stats.winrate}%"
            )
        }
        item {
            StatItem(
                title = "Lost games",
                value = "${stats.lostMatchesCount}"
            )
        }
        if (stats.averageTeammateSkill != null) {
            item {
                StatItem(
                    title = "Average teammate skill",
                    value = "${stats.averageTeammateSkill}"
                )
            }
        }
        itemsWithTitle("Maps stats", stats.mapsStats) { index, item ->
            MapItem(item = item)
        }
        itemsWithTitle("Best teammates", getStatisticsResult.bestTeammates) { index, item ->
            PlayerItem(item = item, isEnemies = false)
        }
        itemsWithTitle("Lobster teammates", getStatisticsResult.lobsterTeammates) { index, item ->
            PlayerItem(item = item, isEnemies = false)
        }
        itemsWithTitle("Best against", getStatisticsResult.bestAgainst) { index, item ->
            PlayerItem(item = item, isEnemies = true)
        }
        itemsWithTitle("Best opponents", getStatisticsResult.bestOpponents) { index, item ->
            PlayerItem(item = item, isEnemies = true)
        }
    }
}

@Composable
fun PlayerItem(modifier: Modifier = Modifier, item: WithPlayerStat, isEnemies: Boolean) {
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier = Modifier
            .padding(8.dp)
            .clip(shape)
            .clickable {}
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                shape = shape
            )
            .padding(12.dp)
            .then(modifier)
    ) {
        Text(text = item.playerData.playerName, fontWeight = FontWeight.SemiBold)
        Box(
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth(0.5f)) {
                val totalGamesWithText = if (isEnemies) "Games against" else "Games together"
                Text(text = totalGamesWithText + System.lineSeparator() + item.playerStats.totalGamesTogether)
                val withText = "Winrate " + if (isEnemies) "against player" else "with teammate"
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = withText + " " + item.playerStats.winrate + "%"
                )
            }

            Column(
                Modifier
                    .padding(start = 8.dp)
                    .align(Alignment.CenterEnd)
                    .fillMaxWidth(0.5f)
            ) {
                Text(text = "Player overall winrate " + item.playerData.winrate + "%")
                val winsWith = "Wins " + if (isEnemies) "against player" else "with teammate"
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = winsWith + System.lineSeparator() + if (isEnemies) item.playerStats.lostGames
                    else item.playerStats.wonGames
                )
            }
        }
    }
}

@Composable
fun MapItem(modifier: Modifier = Modifier, item: MapStat) {
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier = Modifier
            .padding(8.dp)
            .clip(shape)
            .clickable {}
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                shape = shape
            )
            .padding(12.dp)
            .then(modifier)
    ) {
        Text(text = item.mapName, fontWeight = FontWeight.Bold)
        Box(
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth(0.5f)) {
                Text(text = "Total matches count" + System.lineSeparator() + item.totalMatchesCount.toString())
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = "Winrate" + System.lineSeparator() + item.winrate + "%"
                )
            }

            Column(
                Modifier
                    .padding(start = 8.dp)
                    .align(Alignment.CenterEnd)
                    .fillMaxWidth(0.5f)
            ) {
                Text(text = "Wins on this map${System.lineSeparator()}" + item.wins)
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = "Loses${System.lineSeparator()}" + item.wins
                )
            }
        }
    }
}

@Composable
fun StatItem(modifier: Modifier = Modifier, title: String, value: String) {
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier = Modifier
            .padding(8.dp)
            .clip(shape)
            .clickable {}
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                shape = shape
            )
            .padding(8.dp)
            .wrapContentSize()
            .then(modifier)
    ) {
        Text(
            text = title,
            modifier = Modifier.fillMaxWidth(),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

inline fun <T> LazyGridScope.itemsWithTitle(
    title: String,
    items: List<T>?,
    crossinline itemContent: @Composable LazyGridItemScope.(index: Int, item: T) -> Unit
) {
    if (items.isNullOrEmpty()) return
    item(span = { GridItemSpan(3) }) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
    itemsIndexed(items = items, span = { _, _ ->
        GridItemSpan(3)
    }, itemContent = itemContent)
}