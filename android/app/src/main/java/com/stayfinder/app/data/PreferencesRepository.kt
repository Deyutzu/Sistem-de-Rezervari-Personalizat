package com.stayfinder.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.stayfinder.app.data.db.UserPreferenceDao
import com.stayfinder.app.data.db.UserPreferenceEntity
import com.stayfinder.app.model.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private val Context.dataStore by preferencesDataStore(name = "stayfinder_prefs")

/**
 * Gestioneaza preferintele utilizatorului:
 *  - Room (`user_preferences`): tipurile de destinatie, bugetul min/max, all-inclusive, tipul de calatorie;
 *  - DataStore (`stayfinder_prefs`): flag-ul simplu `onboarding_completed` si (din Faza 5) `jwt_token`.
 *
 * De ce ambele: conform cerintei proiectului, DataStore e ideal pentru flag-uri / token-uri
 * cheie-valoare, iar Room stocheaza structurat tabelul de preferinte alaturi de cache si favorite.
 */
interface PreferencesRepository {
    val onboardingCompleted: StateFlow<Boolean?>
    val userPreferences: StateFlow<UserPreferences>
    suspend fun savePreferences(preferences: UserPreferences)
}

class LocalPreferencesRepository(
    private val context: Context,
    private val userPreferenceDao: UserPreferenceDao,
    private val apiServiceProvider: (() -> com.stayfinder.app.data.network.StayFinderApiService)? = null
) : PreferencesRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_JWT_TOKEN = stringPreferencesKey("jwt_token")
    }

    /**
     * null = DataStore inca se citeste de pe disc;
     * false = utilizatorul nu a parcurs onboarding-ul;
     * true = onboarding finalizat -> intram direct in Feed.
     */
    override val onboardingCompleted: StateFlow<Boolean?> =
        context.dataStore.data
            .map { prefs -> prefs[KEY_ONBOARDING_COMPLETED] ?: false }
            .stateIn(scope, SharingStarted.Eagerly, null)

    override val userPreferences: StateFlow<UserPreferences> =
        userPreferenceDao.observe()
            .map { entity -> entity?.toModel() ?: UserPreferences() }
            .stateIn(scope, SharingStarted.Eagerly, UserPreferences())

    override suspend fun savePreferences(preferences: UserPreferences) {
        userPreferenceDao.upsert(UserPreferenceEntity.fromModel(preferences))
        context.dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_COMPLETED] = true
        }

        // Sincronizare automată cu backend-ul dacă există conexiune și serviciu API disponibil
        try {
            apiServiceProvider?.invoke()?.updatePreferences(
                com.stayfinder.app.data.network.NetworkUserPreferenceDto.fromModel(preferences)
            )
        } catch (_: Exception) {
            // Dacă utilizatorul este offline, preferințele rămân salvate local în Room
        }
    }

    /** Pregatit pentru Faza 5 (autentificare JWT). */
    val jwtTokenFlow: Flow<String?> = context.dataStore.data.map { it[KEY_JWT_TOKEN] }

    suspend fun saveJwtToken(token: String?) {
        context.dataStore.edit { prefs ->
            if (token == null) prefs.remove(KEY_JWT_TOKEN) else prefs[KEY_JWT_TOKEN] = token
        }
    }
}
