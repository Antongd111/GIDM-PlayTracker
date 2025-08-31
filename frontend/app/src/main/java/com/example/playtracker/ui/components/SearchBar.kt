package com.example.playtracker.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions

@Composable
fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text("Búsqueda...") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() })
    )
}