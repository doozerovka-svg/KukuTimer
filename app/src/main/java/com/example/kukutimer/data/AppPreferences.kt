package com.example.kukutimer.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kuku_settings")

class AppPreferences(private val context: Context) {
    private val RESTRICTED_APPS = stringSetPreferencesKey("restricted_apps")
    private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    private val COOKING_TIME_MINUTES = intPreferencesKey("cooking_time_minutes")
    private val WINDOW_TIME_MINUTES = intPreferencesKey("window_time_minutes")
    private val AVOIDED_IMPULSES_COUNT = intPreferencesKey("avoided_impulses_count")

    val restrictedApps: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[RESTRICTED_APPS] ?: emptySet()
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[ONBOARDING_COMPLETED] ?: false
    }

    val cookingTimeMinutes: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[COOKING_TIME_MINUTES] ?: 10
    }

    val windowTimeMinutes: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[WINDOW_TIME_MINUTES] ?: 2
    }

    val avoidedImpulsesCount: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[AVOIDED_IMPULSES_COUNT] ?: 0
    }

    suspend fun incrementAvoidedImpulses() {
        context.dataStore.edit { prefs ->
            val current = prefs[AVOIDED_IMPULSES_COUNT] ?: 0
            prefs[AVOIDED_IMPULSES_COUNT] = current + 1
        }
    }

    suspend fun setCookingTimeMinutes(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[COOKING_TIME_MINUTES] = minutes
        }
    }

    suspend fun setWindowTimeMinutes(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[WINDOW_TIME_MINUTES] = minutes
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setAppRestricted(packageName: String, isRestricted: Boolean) {
        context.dataStore.edit { prefs ->
            val current = prefs[RESTRICTED_APPS] ?: emptySet()
            if (isRestricted) {
                prefs[RESTRICTED_APPS] = current + packageName
            } else {
                prefs[RESTRICTED_APPS] = current - packageName
            }
        }
    }
    
    // Store the absolute timestamp when the "cooking" ends
    fun getTimerEndTime(packageName: String): Flow<Long?> {
        val key = longPreferencesKey("timer_$packageName")
        return context.dataStore.data.map { prefs ->
            prefs[key]
        }
    }

    suspend fun setTimerEndTime(packageName: String, endTime: Long?) {
        val key = longPreferencesKey("timer_$packageName")
        context.dataStore.edit { prefs ->
            if (endTime != null) {
                prefs[key] = endTime
            } else {
                prefs.remove(key)
            }
        }
    }
    
    fun getSessionActive(packageName: String): Flow<Boolean> {
        val key = booleanPreferencesKey("session_$packageName")
        return context.dataStore.data.map { prefs ->
            prefs[key] ?: false
        }
    }

    suspend fun setSessionActive(packageName: String, isActive: Boolean) {
        val key = booleanPreferencesKey("session_$packageName")
        context.dataStore.edit { prefs ->
            if (isActive) {
                prefs[key] = true
            } else {
                prefs.remove(key)
            }
        }
    }

    suspend fun resetAllSessions() {
        context.dataStore.edit { prefs ->
            val keys = prefs.asMap().keys.filter { it.name.startsWith("session_") }
            for (key in keys) {
                prefs.remove(key)
            }
        }
    }
}
