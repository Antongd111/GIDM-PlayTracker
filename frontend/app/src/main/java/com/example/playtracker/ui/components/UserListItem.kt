package com.example.playtracker.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable // ⬅️ AÑADE ESTE IMPORT
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.playtracker.R
import com.example.playtracker.domain.model.User
import com.example.playtracker.ui.theme.AzulElectrico
import com.example.playtracker.ui.theme.TextoClaro
import com.example.playtracker.ui.viewmodel.FriendState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import coil.compose.rememberAsyncImagePainter

@Composable
fun UserListItem(
    user: User,
    isWorking: Boolean = false,
    state: FriendState = FriendState.NONE,
    onMainButtonClick: () -> Unit = {},
    onClick: (() -> Unit)? = null,
) {
    val cardHeight = 88.dp
    val avatarSize = cardHeight
    val actionWidth = 56.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight)
    ) {
        // Card desplazada a la derecha: empieza en el CENTRO del avatar
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .matchParentSize()
                .padding(start = avatarSize / 2) // 👈 la card comienza en el centro de la foto
        ) {
            Row(Modifier.fillMaxSize()) {
                // Contenido (texto) clicable
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = avatarSize / 2 + 12.dp, end = 12.dp)
                        .let { base -> if (onClick != null) base.clickable { onClick() } else base }
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = user.status ?: "Sin estado",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                // Franja de acción a la derecha
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(actionWidth)
                        .background(AzulElectrico)
                        .clickable(enabled = !isWorking) { onMainButtonClick() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isWorking) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp),
                            color = Color.Black
                        )
                    } else {
                        val icon = when (state) {
                            FriendState.NONE -> Icons.Default.PersonAdd
                            FriendState.PENDING_SENT -> Icons.Default.Close
                            FriendState.FRIENDS -> Icons.Default.PersonRemove
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Imagen del usuario: ocupa TODO el alto y queda por ENCIMA de la card
        val avatarPainter = rememberAsyncImagePainter(
            model = user.avatarUrl?.takeIf { it.isNotBlank() } ?: R.drawable.default_avatar
        )
        Image(
            painter = avatarPainter,
            contentDescription = "Avatar de ${user.name}",
            modifier = Modifier
                .size(avatarSize) // 👈 mismo alto que la card
                .align(Alignment.CenterStart)
                .clip(CircleShape)
                .let { base -> if (onClick != null) base.clickable { onClick() } else base },
            contentScale = ContentScale.Crop
        )
    }
}
