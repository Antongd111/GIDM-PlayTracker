package com.example.playtracker.ui.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.playtracker.R
import com.example.playtracker.data.local.datastore.UserPreferences
import com.example.playtracker.data.remote.service.RetrofitInstance
import com.example.playtracker.data.repository.impl.UserGameRepositoryImpl
import com.example.playtracker.data.repository.impl.UserRepositoryImpl
import com.example.playtracker.data.repository.impl.FriendsRepositoryImpl
import com.example.playtracker.domain.model.Game
import com.example.playtracker.domain.model.Friend
import com.example.playtracker.ui.viewmodel.*

@Composable
fun UserScreen(
    navController: NavController,
    userId: Int
) {
    // VM sin factory (crea deps internas). Key por usuario para instancia estable.
    val viewModel: UserViewModel = viewModel(key = "UserVM_$userId")

    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    val token by prefs.tokenFlow.collectAsState(initial = null)
    val bearer = token?.let { "Bearer $it" }

    val scroll = rememberScrollState()
    val ui by viewModel.ui.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId, token) {
        viewModel.load(userId, token)
    }

    fun snack(msg: String) = Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

    when {
        ui.loading && ui.user == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        ui.error != null && ui.user == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(ui.error ?: "Error")
        }
        ui.user != null -> {
            val u = ui.user!!

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {

                    val avatarSize = 100.dp

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .heightIn(min = avatarSize + 20.dp)
                            .padding(horizontal = 10.dp)
                            .padding(top = 20.dp, bottom = 10.dp)
                    ) {
                        val avatarPainter = rememberAsyncImagePainter(
                            model = if (!u.avatarUrl.isNullOrBlank()) u.avatarUrl else R.drawable.default_avatar
                        )
                        Image(
                            painter = avatarPainter,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(avatarSize)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(Modifier.width(20.dp))

                        Column(Modifier.weight(1f)) {
                            Text(u.name, style = MaterialTheme.typography.titleLarge)
                            Spacer(Modifier.height(6.dp))
                            u.status?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                        }

                        if (ui.isOwn) {
                            IconButton(onClick = { showEditDialog = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar perfil")
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(bottom = 10.dp, end = 10.dp, start = 10.dp)
                    ) {
                        Spacer(modifier = Modifier.weight(1f))

                        if (!ui.isOwn) {
                            val (label, enabled) = when (ui.friendState) {
                                FriendState.NONE -> "Seguir" to !ui.workingFriend
                                FriendState.PENDING_SENT -> "Pendiente..." to !ui.workingFriend
                                FriendState.FRIENDS -> "Amigos ✓" to !ui.workingFriend
                            }
                            Button(
                                onClick = {
                                    val b = bearer
                                    if (b == null) {
                                        snack("Necesitas iniciar sesión")
                                        return@Button
                                    }
                                    viewModel.toggleFriendAction(b)
                                },
                                enabled = enabled
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = label,
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(label)
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 15.dp, bottom = 16.dp, start = 10.dp, end = 10.dp)
                            .verticalScroll(scroll)
                            .weight(1f)
                    ) {
                        // --- Juego favorito ---
                        Text("Juego favorito", style = MaterialTheme.typography.titleMedium)

                        when {
                            ui.favorite == null -> {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(modifier = Modifier.padding(12.dp)) {
                                        Image(
                                            painter = rememberAsyncImagePainter(R.drawable.default_avatar),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(90.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text("— sin favorito —", style = MaterialTheme.typography.bodyLarge)
                                            Text("Elige un juego y márcalo con la ⭐", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                            else -> {
                                val game = ui.favorite!!
                                Card(
                                    onClick = { navController.navigate("gameDetail/${game.id}") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(modifier = Modifier.padding(12.dp)) {
                                        val imageUrl = game.imageUrl
                                        Image(
                                            painter = rememberAsyncImagePainter(
                                                model = if (!imageUrl.isNullOrBlank()) imageUrl else R.drawable.default_avatar
                                            ),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(100.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(game.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                                            Spacer(Modifier.height(4.dp))
                                            val favScore100 = ui.userGames.firstOrNull { it.gameRawgId == game.id }?.score
                                            StarRowFrom100(score100 = favScore100, size = 16.dp)
                                            Spacer(Modifier.height(6.dp))
                                            Text(
                                                "Ver detalles",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // --- Completados ---
                        val completedCount = ui.completed.size
                        Text("Juegos completados ($completedCount)", style = MaterialTheme.typography.titleMedium)

                        if (ui.completed.isEmpty()) {
                            Text(
                                "Aún no hay juegos completados.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                items(ui.completed, key = { it.id }) { game: Game ->
                                    Card(
                                        onClick = { navController.navigate("gameDetail/${game.id}") },
                                        modifier = Modifier
                                            .width(160.dp)
                                            .wrapContentHeight(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(8.dp)
                                        ) {
                                            val imageUrl = game.imageUrl
                                            Image(
                                                painter = rememberAsyncImagePainter(
                                                    model = if (!imageUrl.isNullOrBlank()) imageUrl else R.drawable.default_avatar
                                                ),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .height(120.dp)
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                            Spacer(Modifier.height(8.dp))
                                            Text(
                                                game.title,
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.fillMaxWidth(),
                                                maxLines = 1,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            val score100 = ui.userGames.firstOrNull { it.gameRawgId == game.id }?.score
                                            Row(
                                                horizontalArrangement = Arrangement.Center,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                StarRowFrom100(score100 = score100, size = 14.dp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // --- Reseñas ---
                        Text("Reseñas (${ui.reviews.size})", style = MaterialTheme.typography.titleMedium)

                        if (ui.reviews.isEmpty()) {
                            Text(
                                "Aún no hay reseñas.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                items(ui.reviews, key = { it.rawgId.toString() + (it.addedAt ?: "") }) { r ->
                                    Card(
                                        onClick = { navController.navigate("gameDetail/${r.rawgId}") },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.width(260.dp)
                                    ) {
                                        Row(modifier = Modifier.padding(12.dp)) {
                                            val painter = rememberAsyncImagePainter(model = r.imageUrl ?: R.drawable.default_avatar)
                                            Image(
                                                painter = painter,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                            Spacer(Modifier.width(12.dp))
                                            Column(Modifier.weight(1f)) {
                                                Text(r.title, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                                                Spacer(Modifier.height(4.dp))
                                                StarRowFrom100(score100 = r.score, size = 14.dp)
                                                Spacer(Modifier.height(6.dp))
                                                Text(
                                                    r.text,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    maxLines = 3
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // --- Amigos ---
                        Text("Amigos", style = MaterialTheme.typography.titleMedium)

                        if (ui.friends.isEmpty()) {
                            Text(
                                "Aún no tiene amigos.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(top = 12.dp)
                            ) {
                                items(ui.friends, key = { it.id }) { friend: Friend ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .width(88.dp)
                                            .clickable { navController.navigate("user/${friend.id}") }
                                    ) {
                                        val avatar = rememberAsyncImagePainter(
                                            model = if (!friend.avatarUrl.isNullOrBlank())
                                                friend.avatarUrl else R.drawable.default_avatar
                                        )
                                        Image(
                                            painter = avatar,
                                            contentDescription = "Avatar de ${friend.name}",
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(CircleShape)
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            friend.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 1,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(30.dp))
                    }
                }

                if (ui.isOwn) {
                    EditProfileDialog(
                        isOpen = showEditDialog,
                        initialName = u.name,
                        initialStatus = u.status,
                        onDismiss = { showEditDialog = false },
                        onSave = { newName, newStatus ->
                            val b = bearer
                            if (b == null) {
                                Toast.makeText(context, "Necesitas iniciar sesión", Toast.LENGTH_SHORT).show()
                                return@EditProfileDialog
                            }
                            viewModel.updateProfile(newName, newStatus, b)
                            showEditDialog = false
                            Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}


/* ====== Mantén este composable como lo pegaste ====== */
@Composable
private fun EditProfileDialog(
    isOpen: Boolean,
    initialName: String,
    initialStatus: String?,
    onDismiss: () -> Unit,
    onSave: (name: String, status: String?) -> Unit
) {
    if (!isOpen) return

    var name by remember(initialName) { mutableStateOf(initialName) }
    var status by remember(initialStatus) { mutableStateOf(initialStatus.orEmpty()) }
    var nameError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val n = name.trim()
                if (n.isEmpty()) {
                    nameError = "El nombre no puede estar vacío"
                    return@TextButton
                }
                onSave(n, status.trim().ifBlank { null })
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        title = { Text("Editar perfil") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (nameError != null) nameError = null
                    },
                    label = { Text("Nombre de usuario") },
                    singleLine = true,
                    isError = nameError != null,
                    supportingText = { if (nameError != null) Text(nameError!!) }
                )
                OutlinedTextField(
                    value = status,
                    onValueChange = { status = it },
                    label = { Text("Estado (opcional)") },
                    singleLine = true
                )
            }
        },
        shape = MaterialTheme.shapes.extraLarge
    )
}

/* ====== Helper de estrellas para puntuación 0..100 -> 0..5 ====== */
@Composable
private fun StarRowFrom100(
    score100: Int?,           // 0..100
    maxStars: Int = 5,
    size: Dp = 16.dp
) {
    val s = (score100 ?: 0).coerceIn(0, 100)
    // 0..100 -> 0..5 (redondeo al entero más cercano)
    val stars = ((s + 10) / 20)  // 0..5

    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(maxStars) { i ->
            Icon(
                imageVector = if (i < stars) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = if (i < stars) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(size)
            )
        }
    }
}
