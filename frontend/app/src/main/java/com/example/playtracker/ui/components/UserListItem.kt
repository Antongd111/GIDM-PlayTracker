package com.example.playtracker.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.playtracker.R
import com.example.playtracker.data.model.User
import com.example.playtracker.ui.theme.AzulElectrico
import com.example.playtracker.ui.theme.TextoClaro

@Composable
fun UserListItem(user: User, onAddClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Avatar redondo (usa una imagen local temporal si no tienes URL cargada aún)
            Image(
                painter = painterResource(id = R.drawable.default_avatar), // 👈 pon tu recurso local aquí
                contentDescription = "Avatar",
                modifier = Modifier
                    .height(80.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
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

        // Botón al lado derecho
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
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = "Añadir amigo",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}