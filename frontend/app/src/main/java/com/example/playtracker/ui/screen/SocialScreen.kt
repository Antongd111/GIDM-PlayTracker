package com.example.playtracker.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.playtracker.data.UserPreferences
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

    fun snack(msg: String) =
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

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

                            // Inicializa a NONE y luego hidrata con datos reales (amigos/pendientes)
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

            Spacer(Modifier.height(16.dp))

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
    }
}
