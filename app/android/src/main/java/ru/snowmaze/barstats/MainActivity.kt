package ru.snowmaze.barstats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.snowmaze.barstats.ui.OptionsScreen
import ru.snowmaze.barstats.ui.StatsScreen
import ru.snowmaze.barstats.ui.theme.BarStatsTheme
import ru.snowmaze.barstats.ui.utils.BackHandler
import ru.snowmaze.barstats.ui.utils.keyboardAsState

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel = mokoKoinViewModel<MainViewModel>()
            val coroutineScope = rememberCoroutineScope()
            BarStatsTheme(dynamicColor = false) {
                val focusManager = LocalFocusManager.current
                val isKeyboardOpen by keyboardAsState()
                var enabled by remember { mutableStateOf(true) }
                val bottomSheetState = rememberStandardBottomSheetState(
                    initialValue = SheetValue.Expanded,
                    confirmValueChange = {
                        if (isKeyboardOpen) {
                            return@rememberStandardBottomSheetState false
                        }
                        enabled = it == SheetValue.Expanded
                        true
                    }
                )
                val partiallyExpand = suspend {
                    focusManager.clearFocus()
                    bottomSheetState.partialExpand()
                    enabled = false
                }
                val scaffoldState = rememberBottomSheetScaffoldState(
                    bottomSheetState = bottomSheetState
                )
                if (isKeyboardOpen && bottomSheetState.hasPartiallyExpandedState) {
                    LaunchedEffect("expand") {
                        enabled = true
                        bottomSheetState.expand()
                    }
                }
                val selectedPlayerStat by mainViewModel.selectedPlayerStat.collectAsState()
                BackHandler(enabled || selectedPlayerStat != null) {
                    if (mainViewModel.selectedPlayerStat.value != null) {
                        mainViewModel.selectedPlayerStat.value = null
                    } else {
                        coroutineScope.launch {
                            partiallyExpand()
                        }
                    }
                }
                LaunchedEffect(key1 = "state") {
                    mainViewModel.state.collect { state ->
                        if (state is MainState.Loading) {
                            partiallyExpand()
                        }
                    }
                }
                val peekHeight = 156.dp
                BottomSheetScaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                            title = {
                                Text(stringResource(id = R.string.app_name))
                            }
                        )
                    },
                    modifier = Modifier.fillMaxSize(),
                    sheetContent = { OptionsScreen() },
                    containerColor = MaterialTheme.colorScheme.background,
                    sheetContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    sheetPeekHeight = peekHeight
                ) {
                    StatsScreen(it, peekHeight)
                }
            }
        }
    }
}