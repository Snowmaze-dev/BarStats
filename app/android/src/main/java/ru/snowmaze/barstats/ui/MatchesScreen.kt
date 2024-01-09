package ru.snowmaze.barstats.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ru.snowmaze.barstats.SelectedPlayerStat
import ru.snowmaze.barstats.models.external.AbstractMatchModel
import ru.snowmaze.barstats.parseDefaultDateToMillis
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.ceil

val matchTimeDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")

@Composable
fun MatchesScreen(stat: SelectedPlayerStat, peekHeight: Dp) {
    val horizontalPadding = PaddingValues(start = 12.dp, end = 12.dp)
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(top = 12.dp, bottom = peekHeight)
    ) {
        item {
            val withLabel = if (stat.isEnemy) "against" else "with"
            Text(
                text = "Matches list $withLabel player ${stat.withPlayerStat.withPlayerName}",
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 4.dp)
                    .padding(horizontalPadding),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        items(stat.withPlayerStat.playerStats.matchesTogether!!) {
            MatchItem(
                item = it,
                playerNicknames = stat.playerNickNames,
                modifier = Modifier.padding(horizontalPadding)
            )
        }
    }
}

@Composable
fun LazyItemScope.MatchItem(
    modifier: Modifier = Modifier,
    playerNicknames: Collection<String>,
    item: AbstractMatchModel,
) {
    val shape = RoundedCornerShape(10.dp)
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .padding(8.dp)
            .clip(shape)
            .clickable {
                val browserIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://bar-rts.com/replays/${item.id}")
                )
                context.startActivity(browserIntent)
            }
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                shape = shape
            )
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .then(modifier)
    ) {
        AsyncImage(
            modifier = Modifier
                .width(30.dp)
                .align(Alignment.CenterVertically),
            model = ImageRequest.Builder(LocalContext.current)
                .data("https://api.bar-rts.com/maps/${item.mapFilename}/texture-thumb.jpg")
                .crossfade(true)
                .build(),
            contentDescription = item.mapFilename,
            contentScale = ContentScale.Fit,
        )
        Column(
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Text(text = item.mapName, fontWeight = FontWeight.SemiBold)

            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = "Match start time: ${
                    matchTimeDateFormat.format(Date(parseDefaultDateToMillis(item.startTime)))
                }"
            )
            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = "Match duration: ${ceil(item.durationMs / 1000f / 60f).toInt()} minutes"
            )
            val isWin = item.playerTeam(playerNicknames)!!.winningTeam
            Text(
                modifier = Modifier.padding(top = 4.dp),
                color = if (isWin) Color(0xFF6CB63A) else Color.Red,
                text = if (isWin) "Win" else "Loss"
            )
        }
    }
}