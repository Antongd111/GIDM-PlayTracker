package com.example.playtracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.playtracker.data.model.Game
import androidx.compose.ui.unit.dp

@Composable
fun GameSearchResults(games: List<Game>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(games) { game ->
            GameListItem(game)
        }
    }
}