package com.example.playtracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.playtracker.domain.model.GamePreview

@Composable
fun GamePreviewCard(
    game: GamePreview,
    onClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(140.dp)
            .height(210.dp)
            .clickable { onClick(game.id) }
    ) {
        AsyncImage(
            model = game.imageUrl,
            contentDescription = game.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )
        Column(Modifier.padding(8.dp)) {
            Text(
                text = game.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = game.releaseDate,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
