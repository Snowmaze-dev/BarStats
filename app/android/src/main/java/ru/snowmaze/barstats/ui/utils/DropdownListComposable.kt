package ru.snowmaze.barstats.ui.utils

import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDownList(
    modifier: Modifier = Modifier,
    list: List<String>,
    label: @Composable (() -> Unit)? = null,
    onDismiss: () -> Unit = {},
    selectedString: (String) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedValue by remember { mutableStateOf(list.first()) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        modifier = modifier,
        onExpandedChange = {
            expanded = !expanded
        }
    ) {
        TextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = label,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            list.forEach { text ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = text,
                            modifier = Modifier
                                .wrapContentSize()
                                .align(Alignment.Start)
                        )
                    },
                    modifier = Modifier.wrapContentSize(),
                    onClick = {
                        expanded = false
                        selectedValue = text
                        onDismiss()
                        selectedString(text)
                    }
                )
            }
        }
    }
}