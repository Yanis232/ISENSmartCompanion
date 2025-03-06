package fr.isen.goutalguerin.isensmartcompanion


import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EventPreferencesManager(private val context: Context) {
    // Create a DataStore with the name "event_preferences"
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "event_preferences")

    companion object {
        // Key for storing notification status for each event
        fun getNotificationKey(eventId: Long) = booleanPreferencesKey("notification_${eventId}")
    }

    // Save notification preference for a specific event
    suspend fun setEventNotificationPreference(eventId: Long, isNotificationEnabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[getNotificationKey(eventId)] = isNotificationEnabled
        }
    }

    // Get notification preference for a specific event
    fun getEventNotificationPreference(eventId: Long): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[getNotificationKey(eventId)] ?: false
        }
    }
}