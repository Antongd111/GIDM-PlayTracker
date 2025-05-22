package com.example.playtracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GameCategorySection() {
    val categories = listOf("Acción", "Aventura", "Estrategia", "RPG")

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(categories) { category ->
            Button(onClick = { /* TODO */ }) {
                Text(category)
            }
        }
    }
}