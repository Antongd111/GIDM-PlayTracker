package com.example.playtracker.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.playtracker.data.model.Game
import com.example.playtracker.ui.theme.AzulElectrico
import com.example.playtracker.ui.theme.TextoClaro

@Composable
fun GameListItem(
    game: Game,
    navController: NavController
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .background(color = MaterialTheme.colorScheme.surface)
            .clickable { navController.navigate("gameDetail/${game.id}") }
    ) {
        // Imagen
        Image(
            painter = rememberAsyncImagePainter(game.imageUrl),
            contentDescription = game.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(4.dp))
        )

        Spacer(Modifier.width(12.dp))

        // Título y año
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = game.title,
                style = MaterialTheme.typography.bodyLarge,
                color = TextoClaro,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (game.year == 0) "Por determinar" else game.year.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = TextoClaro
            )
        }

        // Botón lateral (independiente del click del item)
        Button(
            onClick = { /* De momento no hace nada */ },
            modifier = Modifier
                .height(90.dp)
                .width(30.dp),
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AzulElectrico,
                contentColor = TextoClaro
            )
        ) {
            Text("+", fontSize = 24.sp, textAlign = TextAlign.Center)
        }
    }
}
