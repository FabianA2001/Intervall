package com.intervall

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    companion object {
        private val AUFWAERMEN_KEY = intPreferencesKey("aufwaermen")
        private val INTERVAL_KEY = intPreferencesKey("interval")
        private val PAUSE_KEY = intPreferencesKey("pause")
        private val ANZAHL_KEY = intPreferencesKey("anzahl")
        private val AUSLAUFEN_KEY = intPreferencesKey("auslaufen")
    }

    val aufwaermen: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[AUFWAERMEN_KEY] ?: 5
    }

    val interval: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[INTERVAL_KEY] ?: 2
    }

    val pause: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PAUSE_KEY] ?: 3
    }

    val anzahl: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[ANZAHL_KEY] ?: 2
    }

    val auslaufen: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[AUSLAUFEN_KEY] ?: 4
    }

    suspend fun saveAufwaermen(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[AUFWAERMEN_KEY] = value
        }
    }

    suspend fun saveInterval(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[INTERVAL_KEY] = value
        }
    }

    suspend fun savePause(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[PAUSE_KEY] = value
        }
    }

    suspend fun saveAnzahl(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[ANZAHL_KEY] = value
        }
    }

    suspend fun saveAuslaufen(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[AUSLAUFEN_KEY] = value
        }
    }
}

