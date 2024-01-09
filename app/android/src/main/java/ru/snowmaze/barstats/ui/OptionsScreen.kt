package ru.snowmaze.barstats.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.snowmaze.barstats.MainViewModel
import ru.snowmaze.barstats.mokoKoinViewModel
import ru.snowmaze.barstats.ui.utils.DropDownList

@Composable
fun OptionsScreen() {
    val mainViewModel = mokoKoinViewModel<MainViewModel>()
    Column(
        modifier = Modifier
            .padding(all = 12.dp)
            .fillMaxWidth()
            .fillMaxHeight(0.8f)
    ) {
        val inputsColors = TextFieldDefaults.colors(
            focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row {
            Row(modifier = Modifier.fillMaxWidth()) {
                val text by mainViewModel.playerName.collectAsState()
                TextField(
                    value = text,
                    onValueChange = { mainViewModel.playerName.value = it },
                    label = { Text("Player name") },
                    supportingText = { Text(text = "Case-sensitive") },
                    colors = inputsColors,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                DropDownList(
                    modifier = Modifier.weight(1f),
                    label = { Text("Preset") },
                    list = listOf("all", "team", "duel", "ffa"),
                    selectedString = {
                        mainViewModel.preset.value = it
                    }
                )
            }
        }
        var mapText by remember { mutableStateOf("") }

        TextField(
            value = mapText,
            onValueChange = { mapText = it },
            label = { Text("Map") },
            supportingText = { Text("Optional") },
            colors = inputsColors,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )

        var limitText by remember { mutableStateOf("") }

        TextField(
            value = limitText,
            onValueChange = { limitText = it },
            label = { Text("Limit count of games") },
            supportingText = { Text("Optional") },
            colors = inputsColors,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        var minGamesForStats by remember { mutableStateOf("10") }

        TextField(
            value = minGamesForStats,
            onValueChange = { minGamesForStats = it },
            label = { Text("Minimum amount of games to show with player stats") },
            supportingText = { Text("Optional") },
            colors = inputsColors,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        var shouldCalculateOverallWinrates by remember {
            mutableStateOf(false)
        }

        Row(
            modifier = Modifier.padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(checked = shouldCalculateOverallWinrates, onCheckedChange = {
                shouldCalculateOverallWinrates = it
            })
            Text(
                text = "Should calculate overall winrates?${System.lineSeparator()}(Expensive operation)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            onClick = {
                mainViewModel.getPlayerStats(
                    map = mapText,
                    limitCount = limitText.toIntOrNull(),
                    minGamesForStats = minGamesForStats.toIntOrNull(),
                    shouldCalculateOverallWinrates = shouldCalculateOverallWinrates
                )
            }
        ) {
            Text(text = "Load stats")
        }
    }
}