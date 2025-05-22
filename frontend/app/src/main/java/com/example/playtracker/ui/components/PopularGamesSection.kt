package com.example.playtracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.playtracker.data.model.Game
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items

@Composable
fun PopularGamesSection(games: List<Game>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(games) { game ->
            GameCard(game)
        }
    }
}