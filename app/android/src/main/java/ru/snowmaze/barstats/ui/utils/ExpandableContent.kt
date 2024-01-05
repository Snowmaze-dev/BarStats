package ru.snowmaze.barstats.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun ExpandableContent(
    header: @Composable (isExpanded: Boolean, onExpand: () -> Unit) -> Unit,
    content: @Composable () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    header(isExpanded) {
        isExpanded = !isExpanded
    }
    if (isExpanded) content()
}