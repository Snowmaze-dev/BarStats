package ru.snowmaze.barstats.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun rememberExpandableState(isExpanded: Boolean = false) = remember {
    mutableStateOf(isExpanded)
}