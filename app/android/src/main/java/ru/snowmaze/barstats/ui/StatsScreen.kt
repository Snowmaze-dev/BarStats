@file:OptIn(ExperimentalFoundationApi::class)

package ru.snowmaze.barstats.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.snowmaze.barstats.MainState
import ru.snowmaze.barstats.MainViewModel
import ru.snowmaze.barstats.SelectedPlayerStat
import ru.snowmaze.barstats.models.MapStat
import ru.snowmaze.barstats.models.WithPlayerStat
import ru.snowmaze.barstats.mokoKoinViewModel
import ru.snowmaze.barstats.ui.utils.rememberExpandableState
import java.util.concurrent.TimeUnit

@Composable
fun StatsScreen(paddingValues: PaddingValues = PaddingValues(), peekHeight: Dp) {
    val mainViewModel = mokoKoinViewModel<MainViewModel>()
    val selectedPlayerStat by mainViewModel.selectedPlayerStat.collectAsState()
    val stateCollected by mainViewModel.state.collectAsState()
    val state = stateCollected
    when {
        selectedPlayerStat != null -> MatchesScreen(selectedPlayerStat!!, peekHeight)
        else -> when (state) {

            is MainState.Ready, is MainState.PartOfDataReady -> ReadyStatsScreen(state, peekHeight)

            is MainState.Loading -> StubContainer(paddingValues) {
                LoadingPlayerStats("Loading ${state.playerName} stats.")
            }

            is MainState.Empty -> StubContainer(paddingValues) {
                StateText("Click load stats button.")
            }

            is MainState.Error -> StubContainer(paddingValues) {
                StateText("Some error occurred: ${state.message}.")
            }
        }
    }
}

@Composable
private fun StubContainer(
    paddingValues: PaddingValues,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(paddingValues)
            .animateContentSize(),
        content = content
    )
    Spacer(modifier = Modifier.fillMaxHeight())
}

@Composable
fun StateText(text: String) {
    Text(
        text = text,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, start = 12.dp, end = 12.dp),
        color = MaterialTheme.colorScheme.onSurface
    )
}

// TODO добавить ключи
@Composable
fun ReadyStatsScreen(state: MainState, peekHeight: Dp) {
    val mapStatsExpandableState = rememberExpandableState()
    val bestTeammatesExpandableState = rememberExpandableState()
    val lobsterTeammatesExpandableState = rememberExpandableState()
    val bestAgainstTeammatesExpandableState = rememberExpandableState()
    val bestOpponentsTeammatesExpandableState = rememberExpandableState()
    val mainViewModel = mokoKoinViewModel<MainViewModel>()
    val horizontalPadding = PaddingValues(start = 12.dp, end = 12.dp)
    val playerData = when (state) {
        is MainState.PartOfDataReady -> state.data
        is MainState.Ready -> state.getStatisticsResult.playerData
        else -> throw IllegalStateException()
    }
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(top = 12.dp, bottom = peekHeight)
    ) {
        item {
            Text(
                text = "Stats for player ${playerData.playerName}",
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 4.dp)
                    .padding(horizontalPadding),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        val stats = playerData
        item {
            Row(modifier = Modifier.padding(horizontalPadding)) {
                StatItem(
                    title = "Total games analyzed",
                    value = "${stats.totalMatchesCount}",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatItem(
                    title = "Winrate",
                    value = getWinrateString(stats.winrate),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatItem(
                    title = "Hours spent in analyzed games",
                    value = TimeUnit.MILLISECONDS.toHours(stats.overallPlayerPlaytimeMs).toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Row(modifier = Modifier.padding(horizontalPadding)) {
                StatItem(
                    title = "Won games",
                    value = "${stats.wonMatchesCount}",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatItem(
                    title = "Lost games",
                    value = "${stats.lostMatchesCount}",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (stats.averageTeammateSkill != null) {
                    StatItem(
                        title = "Average teammate skill",
                        value = "${stats.averageTeammateSkill}",
                        modifier = Modifier.weight(1f)
                    )
                }
                if (stats.averageEnemySkill != null) {
                    StatItem(
                        title = "Average ${stats.preset} enemy skill",
                        value = "${stats.averageEnemySkill}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        val setSelected: (WithPlayerStat, Boolean) -> Unit = { withPlayerStat, isEnemy ->
            mainViewModel.selectedPlayerStat.value = SelectedPlayerStat(
                playerNickNames = playerData.playerNames,
                withPlayerStat = withPlayerStat,
                isEnemy = isEnemy
            )
        }

        val allyPlayerItemCreator: @Composable LazyItemScope.(WithPlayerStat) -> Unit = {
            PlayerItem(
                modifier = Modifier.padding(horizontalPadding),
                item = it,
                isEnemies = false
            ) { withPlayerStat -> setSelected(withPlayerStat, false) }
        }

        val enemyPlayerItemCreator: @Composable LazyItemScope.(WithPlayerStat) -> Unit = {
            PlayerItem(
                modifier = Modifier.padding(horizontalPadding),
                item = it,
                isEnemies = true
            ) { withPlayerStat -> setSelected(withPlayerStat, true) }
        }

        addExpandableItemsWithHeader(
            "Maps stats",
            stats.mapsStats,
            mapStatsExpandableState,
            "map",
            horizontalPadding
        ) {
            MapItem(item = it)
        }

        val getStatisticsResult = if (state is MainState.Ready) state.getStatisticsResult
        else {
            item { LoadingPlayerStats("Loading ${playerData.playerName} teammates and opponents stats.") }
            return@LazyColumn
        }

        addExpandableItemsWithHeader(
            "Best teammates",
            getStatisticsResult.bestTeammates,
            bestTeammatesExpandableState,
            "player",
            horizontalPadding,
            allyPlayerItemCreator
        )

        addExpandableItemsWithHeader(
            "Lobster teammates",
            getStatisticsResult.lobsterTeammates,
            lobsterTeammatesExpandableState,
            "player",
            horizontalPadding,
            allyPlayerItemCreator
        )

        addExpandableItemsWithHeader(
            "Best against",
            getStatisticsResult.bestAgainst,
            bestAgainstTeammatesExpandableState,
            "player",
            horizontalPadding,
            enemyPlayerItemCreator
        )

        addExpandableItemsWithHeader(
            "Best opponents",
            getStatisticsResult.bestOpponents,
            bestOpponentsTeammatesExpandableState,
            "player",
            horizontalPadding,
            enemyPlayerItemCreator
        )
    }
}

@Composable
fun LoadingPlayerStats(text: String) {
    Column(Modifier.fillMaxWidth()) {
        StateText(text)
        CircularProgressIndicator(
            modifier = Modifier
                .wrapContentHeight()
                .heightIn(24.dp)
                .padding(top = 8.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}

fun <T> LazyListScope.addExpandableItemsWithHeader(
    title: String,
    items: List<T>?,
    expandableState: MutableState<Boolean>,
    itemType: String,
    paddingValues: PaddingValues,
    itemContent: @Composable LazyItemScope.(T) -> Unit
) {
    if (items?.isNotEmpty() == true) addItemsHeader(title, expandableState.value, paddingValues) {
        expandableState.value = it
    }
    if (expandableState.value) {
        items(items ?: emptyList(), contentType = { itemType }) {
            itemContent(it)
        }
    }
}

// TODO сделать список матчей против или с игроком при клике
@Composable
fun LazyItemScope.PlayerItem(
    modifier: Modifier = Modifier,
    item: WithPlayerStat,
    isEnemies: Boolean,
    onClick: (WithPlayerStat) -> Unit
) {
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier = Modifier
            .padding(8.dp)
            .clip(shape)
            .clickable {
                if (item.playerStats.matchesTogether != null) onClick(item)
            }
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                shape = shape
            )
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .animateItemPlacement()
            .then(modifier)
    ) {
        Text(text = item.withPlayerName, fontWeight = FontWeight.SemiBold)
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
                    text = withText + System.lineSeparator() + getWinrateString(item.playerStats.winrate)
                )
            }

            Column(
                Modifier
                    .padding(start = 8.dp)
                    .align(Alignment.TopEnd)
                    .fillMaxWidth(0.5f)
            ) {
                val withWinrate = item.playerData?.winrate
                if (withWinrate != null) {
                    Text(text = "Player overall winrate " + getWinrateString(withWinrate))
                }
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
                    text = "Winrate" + System.lineSeparator() + getWinrateString(item.winrate)
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
                    text = "Loses${System.lineSeparator()}" + item.loses
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
            .padding(top = 8.dp, bottom = 8.dp)
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

inline fun LazyListScope.addItemsHeader(
    title: String,
    isExpanded: Boolean,
    paddingValues: PaddingValues,
    crossinline onClickHeader: (isExpanded: Boolean) -> Unit
) {
    stickyHeader(title, contentType = "header") {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClickHeader(!isExpanded) }
                .background(MaterialTheme.colorScheme.background)
                .padding(top = 8.dp, bottom = 4.dp)
                .padding(paddingValues)
        ) {
            Box(Modifier.fillMaxWidth()) {
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

fun getWinrateString(winrate: Float): String {
    return "%.2f".format(winrate) + "%"
}