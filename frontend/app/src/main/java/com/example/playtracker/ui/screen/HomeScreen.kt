package com.example.playtracker.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import com.example.playtracker.data.api.RetrofitInstance
import com.example.playtracker.data.model.Game
import com.example.playtracker.ui.components.GameCard
import com.example.playtracker.ui.components.GameSearchResults
import com.example.playtracker.ui.components.PopularGamesSection
import com.example.playtracker.ui.components.SearchBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen() {
    val search = remember { mutableStateOf("") }
    val gameList = remember { mutableStateListOf<Game>() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val games = withContext(Dispatchers.IO) {
                RetrofitInstance.gameApi.getPopularGames()
            }
            gameList.clear()
            gameList.addAll(games)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            SearchBar(
                value = search.value,
                onValueChange = {
                    search.value = it
                    if (it.isBlank()) {
                        coroutineScope.launch {
                            try {
                                val games = withContext(Dispatchers.IO) {
                                    RetrofitInstance.gameApi.getPopularGames()
                                }
                                gameList.clear()
                                gameList.addAll(games)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                },
                onSearch = {
                    coroutineScope.launch {
                        try {
                            val result = withContext(Dispatchers.IO) {
                                RetrofitInstance.gameApi.searchGames(search.value)
                            }
                            gameList.clear()
                            gameList.addAll(result)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (search.value.isBlank()) {
                Text(
                    text = "Juegos Populares",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                PopularGamesSection(games = gameList)
            } else {
                GameSearchResults(games = gameList)
            }
        }
    }
}
