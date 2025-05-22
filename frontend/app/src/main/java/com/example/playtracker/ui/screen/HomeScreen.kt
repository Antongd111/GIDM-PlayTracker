package com.example.playtracker.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.playtracker.data.api.RetrofitInstance
import com.example.playtracker.data.model.Game
import com.example.playtracker.ui.components.GameCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.playtracker.ui.components.SearchBar
import com.example.playtracker.ui.components.PopularGamesSection
import com.example.playtracker.ui.components.GameSearchResults

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

    Column(modifier = Modifier.padding(16.dp)) {
        SearchBar(
            value = search.value,
            onValueChange = {
                search.value = it
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
            PopularGamesSection(games = gameList)
        } else {
            GameSearchResults(games = gameList)
        }
    }
}