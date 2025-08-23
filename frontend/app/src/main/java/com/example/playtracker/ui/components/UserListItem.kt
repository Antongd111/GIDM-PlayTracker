package com.example.playtracker.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.playtracker.R
import com.example.playtracker.data.model.User
import com.example.playtracker.ui.theme.AzulElectrico
import com.example.playtracker.ui.theme.TextoClaro
import com.example.playtracker.ui.viewmodel.FriendState

@Composable
fun UserListItem(
    user: User,
    isWorking: Boolean = false,
    state: FriendState = FriendState.NONE,
    onMainButtonClick: () -> Unit = {},
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.default_avatar),
                contentDescription = "Avatar",
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = user.status ?: "Sin estado",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Button(
            onClick = onMainButtonClick,
            enabled = !isWorking,
            modifier = Modifier
                .height(44.dp)
                .widthIn(min = 44.dp),
            contentPadding = PaddingValues(horizontal = 12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AzulElectrico,
                contentColor = TextoClaro,
                disabledContainerColor = AzulElectrico.copy(alpha = 0.6f),
                disabledContentColor = TextoClaro
            )
        ) {
            if (isWorking) {
                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
            } else {
                when (state) {
                    FriendState.NONE -> {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Añadir")
                        Spacer(Modifier.width(6.dp))
                        Text("Añadir")
                    }
                    FriendState.PENDING_SENT -> {
                        Icon(Icons.Default.Close, contentDescription = "Cancelar solicitud")
                        Spacer(Modifier.width(6.dp))
                        Text("Cancelar")
                    }
                    FriendState.FRIENDS -> {
                        Icon(Icons.Default.PersonRemove, contentDescription = "Dejar de ser amigos")
                        Spacer(Modifier.width(6.dp))
                        Text("Eliminar")
                    }
                }
            }
        }
    }
}
