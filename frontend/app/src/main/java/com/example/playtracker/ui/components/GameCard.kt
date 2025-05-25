package com.example.playtracker.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import coil.compose.rememberAsyncImagePainter
import com.example.playtracker.data.model.Game
import com.example.playtracker.ui.theme.FondoSecundario
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.clip

@Composable
fun GameCard(game: Game) {
    Card(
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier
            .width(150.dp)
            .height(220.dp),
        colors = CardDefaults.cardColors(
            containerColor = FondoSecundario,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp),
        ) {
            Image(
                painter = rememberAsyncImagePainter(game.imageUrl),
                contentDescription = game.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = game.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}