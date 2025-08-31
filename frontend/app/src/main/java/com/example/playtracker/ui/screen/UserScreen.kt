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
import com.example.playtracker.domain.model.Friend
import com.example.playtracker.domain.model.UserGame
import com.example.playtracker.ui.viewmodel.*

@Composable
fun UserScreen(
    navController: NavController,
    userId: Int
) {
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

                    // ===== Header =====
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
                            model = u.avatarUrl?.takeIf { it.isNotBlank() } ?: R.drawable.default_avatar
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

                    // ===== Acciones amistad =====
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
                                    val b = bearer ?: return@Button snack("Necesitas iniciar sesión")
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

                    // ===== Cuerpo scroll =====
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 15.dp, bottom = 16.dp, start = 10.dp, end = 10.dp)
                            .verticalScroll(scroll)
                            .weight(1f)
                    ) {
                        // --- Favorito ---
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
                                val fav = ui.favorite!!
                                Card(
                                    onClick = { navController.navigate("gameDetail/${fav.gameRawgId}") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(modifier = Modifier.padding(12.dp)) {
                                        val img = fav.imageUrl
                                        Image(
                                            painter = rememberAsyncImagePainter(
                                                model = img?.takeIf { it.isNotBlank() } ?: R.drawable.default_avatar
                                            ),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(100.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(fav.gameTitle ?: "Juego", style = MaterialTheme.typography.titleMedium, maxLines = 1)
                                            Spacer(Modifier.height(4.dp))
                                            StarRowFrom100(score100 = fav.score, size = 16.dp)
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
                                items(ui.completed, key = { it.id }) { ug: UserGame ->
                                    Card(
                                        onClick = { navController.navigate("gameDetail/${ug.gameRawgId}") },
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
                                            val img = ug.imageUrl
                                            Image(
                                                painter = rememberAsyncImagePainter(
                                                    model = img?.takeIf { it.isNotBlank() } ?: R.drawable.default_avatar
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
                                                ug.gameTitle ?: "Juego",
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.fillMaxWidth(),
                                                maxLines = 1,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Row(
                                                horizontalArrangement = Arrangement.Center,
                                                modifier = Modifier.fillMaxWidth()
                                            ) { StarRowFrom100(score100 = ug.score, size = 14.dp) }
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
                                items(ui.reviews, key = { it.id }) { ug ->
                                    Card(
                                        onClick = { navController.navigate("gameDetail/${ug.gameRawgId}") },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.width(260.dp)
                                    ) {
                                        Row(modifier = Modifier.padding(12.dp)) {
                                            val painter = rememberAsyncImagePainter(model = ug.imageUrl ?: R.drawable.default_avatar)
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
                                                Text(ug.gameTitle ?: "Juego", style = MaterialTheme.typography.titleSmall, maxLines = 1)
                                                Spacer(Modifier.height(4.dp))
                                                StarRowFrom100(score100 = ug.score, size = 14.dp)
                                                Spacer(Modifier.height(6.dp))
                                                Text(ug.notes.orEmpty(), style = MaterialTheme.typography.bodySmall, maxLines = 3)
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
                                            model = friend.avatarUrl?.takeIf { it.isNotBlank() } ?: R.drawable.default_avatar
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
                            val b = bearer ?: return@EditProfileDialog Toast
                                .makeText(context, "Necesitas iniciar sesión", Toast.LENGTH_SHORT)
                                .show()
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

/* ====== Dialogo de edición ====== */
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
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
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

/* ====== Helper estrellas (0..100 -> 0..5) ====== */
@Composable
private fun StarRowFrom100(
    score100: Int?,
    maxStars: Int = 5,
    size: Dp = 16.dp
) {
    val s = (score100 ?: 0).coerceIn(0, 100)
    val stars = ((s + 10) / 20)

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
