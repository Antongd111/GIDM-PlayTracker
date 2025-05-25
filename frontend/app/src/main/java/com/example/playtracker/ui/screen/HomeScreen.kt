package com.example.playtracker.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.playtracker.data.api.RetrofitInstance
import com.example.playtracker.data.model.Game
import com.example.playtracker.ui.components.GameCard
import com.example.playtracker.ui.components.GameListItem
import com.example.playtracker.ui.components.SearchBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(navController: NavController) {
    val search = remember { mutableStateOf("") }
    val gameList = remember { mutableStateListOf<Game>() }
    val isSearching = remember { mutableStateOf(false) }  // <-- NUEVO FLAG
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        try {
            val games = withContext(Dispatchers.IO) {
                RetrofitInstance.gameApi.getPopularGames()
            }
            gameList.clear()
            gameList.addAll(games)
            isSearching.value = false  // <-- aseguramos que no estamos en modo búsqueda inicial
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(WindowInsets.systemBars.asPaddingValues()),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SearchBar arriba fijo
            item {
                SearchBar(
                    value = search.value,
                    onValueChange = { search.value = it },
                    onSearch = {
                        coroutineScope.launch {
                            try {
                                val result = withContext(Dispatchers.IO) {
                                    RetrofitInstance.gameApi.searchGames(search.value)
                                }
                                gameList.clear()
                                gameList.addAll(result)
                                isSearching.value = true
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                )
            }

            if (!isSearching.value) {
                // SECCIÓN: Juegos Populares
                item {
                    Text(
                        text = "Juegos Populares",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(gameList) { game ->
                            GameCard(game = game, navController = navController)
                        }
                    }
                }

                // SECCIÓN: Basado en tus preferencias
                item {
                    Text(
                        text = "Basado en tus preferencias",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(gameList) { game ->
                            GameCard(game = game, navController = navController)
                        }
                    }
                }

                // SECCIÓN: Favoritos de tus amigos
                item {
                    Text(
                        text = "Los favoritos de tus amigos",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(gameList) { game ->
                            GameCard(game = game, navController = navController)
                        }
                    }
                }
            } else {
                if (gameList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay resultados",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                } else {
                    // Mostrar resultados de búsqueda en lista vertical
                    items(gameList) { game ->
                        GameListItem(game)
                    }
                }
            }
        }
    }
}
