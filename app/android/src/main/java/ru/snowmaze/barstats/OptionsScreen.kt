package ru.snowmaze.barstats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import ru.snowmaze.barstats.utils.DropDownList

@Composable
fun OptionsScreen() {
    val mainViewModel = mokoKoinViewModel<MainViewModel>()
    Column(
        modifier = Modifier
            .padding(all = 12.dp)
            .fillMaxWidth()
            .fillMaxHeight(0.8f)
    ) {
        Row {
            Row(modifier = Modifier.fillMaxWidth()) {
                val text by mainViewModel.playerName.collectAsState()
                TextField(
                    value = text,
                    onValueChange = { mainViewModel.playerName.value = it },
                    label = { Text("Player name") },
                    modifier = Modifier
                        .weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                DropDownList(
                    modifier = Modifier.weight(1f),
                    label = { Text("Preset") },
                    list = listOf("all", "team", "duel", "ffa", "tourney", "coop"),
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            onClick = { mainViewModel.getPlayerStats(mapText) }
        ) {
            Text(text = "Load stats")
        }
    }
}