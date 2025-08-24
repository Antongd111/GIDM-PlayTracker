package com.example.playtracker.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Crea el DataStore llamado "user_prefs"
val Context.userPrefs by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        private val USER_ID = intPreferencesKey("user_id")
        private val TOKEN = stringPreferencesKey("token")
    }

    // Guardar userId
    suspend fun saveUserId(id: Int) {
        context.userPrefs.edit { it[USER_ID] = id }
    }

    // Guardar token
    suspend fun saveToken(token: String) {
        context.userPrefs.edit { it[TOKEN] = token }
    }

    // Leer userId
    val userIdFlow: Flow<Int?> = context.userPrefs.data.map { it[USER_ID] }

    // Leer token
    val tokenFlow: Flow<String?> = context.userPrefs.data.map { it[TOKEN] }

    // Limpiar todo (logout)
    suspend fun clear() {
        context.userPrefs.edit { it.clear() }
    }
}
