@file:OptIn(ExperimentalFoundationApi::class)

package ru.snowmaze.barstats.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.snowmaze.barstats.MainState
import ru.snowmaze.barstats.MainViewModel
import ru.snowmaze.barstats.models.MapStat
import ru.snowmaze.barstats.mokoKoinViewModel
import ru.snowmaze.barstats.ui.utils.rememberExpandableState
import ru.snowmaze.barstats.usecases.GetStatisticsResult
import ru.snowmaze.barstats.usecases.WithPlayerStat

@Composable
fun StatsScreen(paddingValues: PaddingValues = PaddingValues()) {
    val mainViewModel = mokoKoinViewModel<MainViewModel>()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp)
            .padding(paddingValues)
    ) {
        val stateCollected by mainViewModel.state.collectAsState()
        when (val state = stateCollected) {
            is MainState.Ready -> ReadyStatsScreen(getStatisticsResult = state.getStatisticsResult)

            is MainState.Loading -> Column(Modifier.fillMaxWidth()) {
                StateText("Loading ${state.playerName} stats.")
                CircularProgressIndicator(
                    modifier = Modifier
                        .wrapContentHeight()
                        .heightIn(24.dp)
                        .padding(top = 8.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }

            is MainState.Empty -> StateText("Click load stats button.")

            is MainState.Error -> StateText("Some error occurred: ${state.message}.")
        }
        Spacer(modifier = Modifier.fillMaxHeight())
    }
}

@Composable
fun StateText(text: String) {
    Text(
        text = text,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun ReadyStatsScreen(getStatisticsResult: GetStatisticsResult) {
    val mapStatsExpandableState = rememberExpandableState()
    val bestTeammatesExpandableState = rememberExpandableState()
    val lobsterTeammatesExpandableState = rememberExpandableState()
    val bestAgainstTeammatesExpandableState = rememberExpandableState()
    val bestOpponentsTeammatesExpandableState = rememberExpandableState()
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(top = 12.dp)
    ) {
        item {
            Text(
                text = "Stats for player ${getStatisticsResult.playerName}",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        val stats = getStatisticsResult.playerStats
        item {
            Row {
                StatItem(
                    title = "Total games analyzed",
                    value = "${stats.totalMatchesCount}",
                    modifier = Modifier.weight(1f)
                )
                if (stats.averageTeammateSkill != null) {
                    StatItem(
                        title = "Average teammate skill",
                        value = "${stats.averageTeammateSkill}",
                        modifier = Modifier.weight(1f)
                    )
                }
                StatItem(
                    title = "Winrate",
                    value = "${stats.winrate}%",
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Row {
                StatItem(
                    title = "Won games",
                    value = "${stats.wonMatchesCount}",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    title = "Lost games",
                    value = "${stats.lostMatchesCount}",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        addExpandableItemsWithHeader(
            "Maps stats",
            stats.mapsStats,
            mapStatsExpandableState,
            "map"
        ) {
            MapItem(item = it)
        }

        addExpandableItemsWithHeader(
            "Best teammates",
            getStatisticsResult.bestTeammates,
            bestTeammatesExpandableState,
            "player"
        ) {
            PlayerItem(item = it, isEnemies = false)
        }
        addExpandableItemsWithHeader(
            "Lobster teammates",
            getStatisticsResult.lobsterTeammates,
            lobsterTeammatesExpandableState,
            "player"
        ) {
            PlayerItem(item = it, isEnemies = false)
        }

        addExpandableItemsWithHeader(
            "Best against",
            getStatisticsResult.bestAgainst,
            bestAgainstTeammatesExpandableState,
            "player"
        ) {
            PlayerItem(item = it, isEnemies = true)
        }

        addExpandableItemsWithHeader(
            "Best opponents",
            getStatisticsResult.bestOpponents,
            bestOpponentsTeammatesExpandableState,
            "player"
        ) {
            PlayerItem(item = it, isEnemies = true)
        }
    }
}

fun <T> LazyListScope.addExpandableItemsWithHeader(
    title: String,
    items: List<T>?,
    expandableState: MutableState<Boolean>,
    itemType: String,
    itemContent: @Composable LazyItemScope.(T) -> Unit
) {
    if (items?.isNotEmpty() == true) ItemsHeader(title, expandableState.value) {
        expandableState.value = it
    }
    if (expandableState.value) {
        items(items ?: emptyList(), contentType = { itemType }) {
            itemContent(it)
        }
    }
}

@Composable
fun LazyItemScope.PlayerItem(
    modifier: Modifier = Modifier,
    item: WithPlayerStat,
    isEnemies: Boolean
) {
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
            .fillMaxWidth()
            .padding(12.dp)
            .animateItemPlacement()
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
fun LazyItemScope.MapItem(modifier: Modifier = Modifier, item: MapStat) {
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
            .fillMaxWidth()
            .padding(12.dp)
            .animateItemPlacement()
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

inline fun LazyListScope.ItemsHeader(
    title: String,
    isExpanded: Boolean,
    crossinline onClickHeader: (isExpanded: Boolean) -> Unit
) {
    stickyHeader(title, contentType = "header") {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .let {
                    if (isExpanded) it.clip(
                        RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomStart = 10.dp,
                            bottomEnd = 10.dp
                        )
                    ) else it
                }
                .clickable { onClickHeader(!isExpanded) }
                .background(MaterialTheme.colorScheme.background)
                .padding(top = 8.dp, bottom = 4.dp)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.CenterStart),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp
                )
                Image(
                    if (isExpanded) Icons.Filled.KeyboardArrowDown
                    else Icons.Filled.KeyboardArrowUp,
                    contentDescription = null,
                    modifier = Modifier.align(
                        Alignment.CenterEnd
                    ),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                )
            }
            if (!isExpanded) {
                Divider(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }
        }
    }
}