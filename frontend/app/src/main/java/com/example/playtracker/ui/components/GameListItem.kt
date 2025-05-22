package com.example.playtracker.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.playtracker.data.model.Game

@Composable
fun GameListItem(game: Game) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(game.imageUrl),
            contentDescription = game.title,
            modifier = Modifier.size(70.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(game.title, style = MaterialTheme.typography.bodyLarge)
            Text(game.year.toString(), style = MaterialTheme.typography.bodySmall)
        }
    }
}