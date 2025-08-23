package com.example.playtracker.ui.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.playtracker.R
import com.example.playtracker.data.UserPreferences
import com.example.playtracker.data.api.IncomingReq
import com.example.playtracker.data.api.RetrofitInstance
import com.example.playtracker.data.model.User
import com.example.playtracker.ui.components.SearchBar
import com.example.playtracker.ui.components.UserListItem
import com.example.playtracker.ui.viewmodel.FriendState
import com.example.playtracker.ui.viewmodel.FriendsViewModel
import com.example.playtracker.ui.viewmodel.FriendsViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object TokenProvider {
    var token: String? = null
}

@Composable
fun SocialScreen(navController: NavHostController) {
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    val token by prefs.tokenFlow.collectAsState(initial = null)
    val bearer = token?.let { "Bearer $it" }

    val search = remember { mutableStateOf("") }
    val userList = remember { mutableStateListOf<User>() }
    val isSearching = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val friendsVm: FriendsViewModel = viewModel(factory = FriendsViewModelFactory())
    val friendsUi by friendsVm.ui.collectAsState()

    var showIncomingDialog by remember { mutableStateOf(false) }

    fun snack(msg: String) =
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

    // Cargar solicitudes entrantes reales al tener token
    LaunchedEffect(bearer) {
        val b = bearer ?: return@LaunchedEffect
        friendsVm.loadIncoming(b)
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(Modifier.padding(16.dp)) {

            SearchBar(
                value = search.value,
                onValueChange = { search.value = it },
                onSearch = {
                    scope.launch {
                        try {
                            val result = withContext(Dispatchers.IO) {
                                RetrofitInstance.userApi.searchUsers(search.value)
                            }
                            userList.clear()
                            userList.addAll(result)
                            isSearching.value = true

                            // Inicializa a NONE y luego hidrata con datos reales
                            friendsVm.ensureUsers(result.map { it.id })
                            bearer?.let { b ->
                                friendsVm.hydrateStatesForResults(result.map { it.id }, b)
                            }
                        } catch (_: Exception) {
                            snack("No se pudo buscar usuarios")
                        }
                    }
                }
            )

            Spacer(Modifier.height(12.dp))

            // Banner de solicitudes entrantes (solo si hay)
            if (friendsUi.incoming.isNotEmpty()) {
                IncomingRequestsBanner(
                    count = friendsUi.incoming.size,
                    onClick = { showIncomingDialog = true }
                )
                Spacer(Modifier.height(12.dp))
            }

            if (userList.isEmpty()) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(top = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isSearching.value) "No hay resultados" else "Busca cualquier persona...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(userList, key = { it.id }) { user ->
                        val isWorking = friendsUi.workingFor.contains(user.id)
                        val state = friendsUi.states[user.id] ?: FriendState.NONE

                        UserListItem(
                            user = user,
                            isWorking = isWorking,
                            state = state,
                            onMainButtonClick = {
                                val b = bearer ?: return@UserListItem
                                friendsVm.toggleFriendAction(
                                    userId = user.id,
                                    bearer = b,
                                ) { snack(it) }
                            },
                            onClick = { navController.navigate("user/${user.id}") }
                        )
                    }
                }
            }
        }

        // Diálogo ancho con avatar + iconos (usa datos reales)
        if (showIncomingDialog) {
            IncomingRequestsDialog(
                isOpen = showIncomingDialog,
                onDismiss = { showIncomingDialog = false },
                requests = friendsUi.incoming,
                working  = friendsUi.workingIncoming,
                onAccept = onAccept@ { fromUserId ->
                    val b = bearer ?: return@onAccept
                    friendsVm.acceptIncoming(fromUserId, b) { snack(it) }
                },
                onDecline = onDecline@ { fromUserId ->
                    val b = bearer ?: return@onDecline
                    friendsVm.declineIncoming(fromUserId, b) { snack(it) }
                }
            )
        }
    }
}

/* ===================== UI helpers ===================== */

@Composable
private fun IncomingRequestsBanner(
    count: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Solicitudes entrantes",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "Tienes $count pendientes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            FilledTonalButton(onClick = onClick) { Text("Ver") }
        }
    }
}

@Composable
private fun IncomingRequestsDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    requests: List<IncomingReq>,
    working: Set<Int>,
    onAccept: (fromUserId: Int) -> Unit,
    onDecline: (fromUserId: Int) -> Unit
) {
    if (!isOpen) return

    val config = LocalConfiguration.current
    val maxDialogHeight = config.screenHeightDp.dp * 0.8f // máx. 80% alto pantalla

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = maxDialogHeight)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Solicitudes entrantes", style = MaterialTheme.typography.titleLarge)
                    TextButton(onClick = onDismiss) { Text("Cerrar") }
                }

                Spacer(Modifier.height(12.dp))

                if (requests.isEmpty()) {
                    Text(
                        "No tienes solicitudes pendientes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = true),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(requests, key = { it.other_user.id }) { req ->
                            val uid = req.other_user.id
                            val isBusy = working.contains(uid)

                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.background
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Avatar (usa drawable por defecto; sustituye si cargas URL)
                                    Image(
                                        painter = painterResource(R.drawable.default_avatar),
                                        contentDescription = "Avatar",
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                    )

                                    Spacer(Modifier.width(14.dp))

                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            text = req.other_user.username ?: "Usuario $uid",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = "te ha enviado una solicitud",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        IconButton(
                                            onClick = { onDecline(uid) },
                                            enabled = !isBusy
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Rechazar",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                        IconButton(
                                            onClick = { onAccept(uid) },
                                            enabled = !isBusy
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Aceptar",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
