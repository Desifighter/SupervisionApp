package com.supervisionapp.lab

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.personalSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "personal_supervision_settings")

data class PersonalRuleSettings(
    val instagramDailyLimitMinutes: Int = 30,
    val resetHour: Int = 16,
    val resetMinute: Int = 0,
    val instagramUsedMinutes: Int = 0,
    val pornFilterEnabled: Boolean = true,
    val socialBlockingEnabled: Boolean = true
)

class PersonalSettingsStore(private val context: Context) {
    companion object {
        private val INSTAGRAM_DAILY_LIMIT = intPreferencesKey("instagram_daily_limit_minutes")
        private val RESET_HOUR = intPreferencesKey("instagram_reset_hour")
        private val RESET_MINUTE = intPreferencesKey("instagram_reset_minute")
        private val INSTAGRAM_USED_MINUTES = intPreferencesKey("instagram_used_minutes")
        private val PORN_FILTER_ENABLED = booleanPreferencesKey("porn_filter_enabled")
        private val SOCIAL_BLOCKING_ENABLED = booleanPreferencesKey("social_blocking_enabled")
    }

    suspend fun read(): PersonalRuleSettings {
        val prefs = context.personalSettingsDataStore.data.first()
        return PersonalRuleSettings(
            instagramDailyLimitMinutes = prefs[INSTAGRAM_DAILY_LIMIT] ?: 30,
            resetHour = prefs[RESET_HOUR] ?: 16,
            resetMinute = prefs[RESET_MINUTE] ?: 0,
            instagramUsedMinutes = prefs[INSTAGRAM_USED_MINUTES] ?: 0,
            pornFilterEnabled = prefs[PORN_FILTER_ENABLED] ?: true,
            socialBlockingEnabled = prefs[SOCIAL_BLOCKING_ENABLED] ?: true
        )
    }

    suspend fun save(settings: PersonalRuleSettings) {
        context.personalSettingsDataStore.edit { prefs ->
            prefs[INSTAGRAM_DAILY_LIMIT] = settings.instagramDailyLimitMinutes
            prefs[RESET_HOUR] = settings.resetHour
            prefs[RESET_MINUTE] = settings.resetMinute
            prefs[INSTAGRAM_USED_MINUTES] = settings.instagramUsedMinutes
            prefs[PORN_FILTER_ENABLED] = settings.pornFilterEnabled
            prefs[SOCIAL_BLOCKING_ENABLED] = settings.socialBlockingEnabled
        }
    }

    suspend fun setUsedMinutes(value: Int) {
        context.personalSettingsDataStore.edit { prefs ->
            prefs[INSTAGRAM_USED_MINUTES] = value.coerceIn(0, 240)
        }
    }

    suspend fun resetUsage() {
        context.personalSettingsDataStore.edit { prefs ->
            prefs[INSTAGRAM_USED_MINUTES] = 0
        }
    }
}
