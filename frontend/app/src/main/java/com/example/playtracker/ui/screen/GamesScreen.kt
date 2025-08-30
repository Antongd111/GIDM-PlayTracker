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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.playtracker.data.local.datastore.UserPreferences
import com.example.playtracker.ui.components.GameCard
import com.example.playtracker.ui.components.GameListItem
import com.example.playtracker.ui.components.GamePreviewCard
import com.example.playtracker.ui.components.SearchBar
import com.example.playtracker.ui.viewmodel.GamesViewModel
@Composable
fun GamesScreen(
    navController: NavController
) {
    // VM sin factory (crea deps internas)
    val viewModel: GamesViewModel = viewModel(key = "GamesVM")

    val listState = rememberLazyListState()
    val search = remember { mutableStateOf("") }

    val popular by viewModel.popular.collectAsState()
    val results by viewModel.searchResults.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val recommendations by viewModel.recommendations.collectAsState()

    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    val storedUserId by prefs.userIdFlow.collectAsState(initial = null)

    LaunchedEffect(Unit) { viewModel.loadPopular() }
    LaunchedEffect(storedUserId) {
//        storedUserId?.let { uid -> viewModel.loadRecommendations(uid) }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SearchBar(
                    value = search.value,
                    onValueChange = { search.value = it },
                    onSearch = { viewModel.search(search.value) }
                )
            }

            if (loading && !isSearching) {
                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            error?.let { msg ->
                item {
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            if (!isSearching) {
                if (recommendations.isNotEmpty()) {
                    item {
                        Text(
                            text = "Recomendados para ti",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(recommendations, key = { it.id }) { rec ->
                                GamePreviewCard(
                                    game = rec,
                                    onClick = { id -> navController.navigate("gameDetail/$id") }
                                )
                            }
                        }
                    }
                }

                if (popular.isNotEmpty()) {
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
                            items(popular, key = { it.id }) { game ->
                                GameCard(game = game, navController = navController)
                            }
                        }
                    }
                }
            } else {
                if (results.isEmpty() && !loading) {
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
                    items(results, key = { it.id }) { game ->
                        GameListItem(game = game, navController = navController)
                    }
                }
            }
        }
    }
}
