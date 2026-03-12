package com.example.module4

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("reminder_prefs")

class ReminderDataStore(private val context: Context) {
    companion object {
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
    }

    val isEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[REMINDER_ENABLED] ?: false
        }

    suspend fun setEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[REMINDER_ENABLED] = enabled
        }
    }
}