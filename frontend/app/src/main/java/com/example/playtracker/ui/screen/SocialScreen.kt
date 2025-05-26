package com.example.playtracker.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.playtracker.data.api.RetrofitInstance
import com.example.playtracker.data.model.User
import com.example.playtracker.ui.components.SearchBar
import com.example.playtracker.ui.components.UserListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SocialScreen(navController: NavHostController) {
    val search = remember { mutableStateOf("") }
    val userList = remember { mutableStateListOf<User>() }
    val isSearching = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // SearchBar como en HomeScreen
            SearchBar(
                value = search.value,
                onValueChange = { search.value = it },
                onSearch = {
                    coroutineScope.launch {
                        try {
                            val result = withContext(Dispatchers.IO) {
                                RetrofitInstance.userApi.searchUsers(search.value)
                            }
                            userList.clear()
                            userList.addAll(result)
                            isSearching.value = true
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (userList.isEmpty()) {
                Box(
                    modifier = Modifier
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
                    items(userList) { user ->
                        UserListItem(user = user)
                    }
                }
            }
        }
    }
}