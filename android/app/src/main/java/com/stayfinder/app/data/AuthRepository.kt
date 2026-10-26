package com.stayfinder.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.stayfinder.app.data.network.LoginRequestDto
import com.stayfinder.app.data.network.RegisterRequestDto
import com.stayfinder.app.data.network.StayFinderApiService
import com.stayfinder.app.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private val Context.authDataStore by preferencesDataStore(name = "stayfinder_auth")

interface AuthRepository {
    val isLoggedIn: StateFlow<Boolean>
    val currentUser: StateFlow<User?>
    val currentToken: String?

    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String, displayName: String): Result<User>
    suspend fun logout()
    fun clearTokenSync()
}

class DefaultAuthRepository(
    private val context: Context,
    private val apiServiceProvider: () -> StayFinderApiService
) : AuthRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private val KEY_TOKEN = stringPreferencesKey("jwt_token")
        private val KEY_USER_ID = longPreferencesKey("user_id")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_USER_NAME = stringPreferencesKey("user_display_name")
        private val KEY_USER_ROLE = stringPreferencesKey("user_role")
    }

    // Cache în memorie pentru acces sincron ultra-rapid în AuthInterceptor
    @Volatile
    private var inMemoryToken: String? = null

    override val currentToken: String?
        get() = inMemoryToken

    override val currentUser: StateFlow<User?> = context.authDataStore.data
        .map { prefs ->
            val id = prefs[KEY_USER_ID]
            val email = prefs[KEY_USER_EMAIL]
            val name = prefs[KEY_USER_NAME]
            val role = prefs[KEY_USER_ROLE]
            if (id != null && email != null && name != null && role != null) {
                User(id = id, email = email, displayName = name, role = role)
            } else {
                null
            }
        }
        .stateIn(scope, SharingStarted.Eagerly, null)

    override val isLoggedIn: StateFlow<Boolean> = context.authDataStore.data
        .map { prefs ->
            val token = prefs[KEY_TOKEN]
            inMemoryToken = token
            !token.isNullOrBlank()
        }
        .stateIn(scope, SharingStarted.Eagerly, false)

    init {
        // Inițializăm cache-ul din memorie la pornire
        scope.launch {
            context.authDataStore.data.firstOrNull()?.let { prefs ->
                inMemoryToken = prefs[KEY_TOKEN]
            }
        }
    }

    override suspend fun login(email: String, password: String): Result<User> = runCatching {
        val response = apiServiceProvider().login(LoginRequestDto(email, password))
        saveAuthSession(response.token, response.user.toModel())
        response.user.toModel()
    }

    override suspend fun register(email: String, password: String, displayName: String): Result<User> = runCatching {
        val response = apiServiceProvider().register(RegisterRequestDto(email, password, displayName))
        saveAuthSession(response.token, response.user.toModel())
        response.user.toModel()
    }

    override suspend fun logout() {
        inMemoryToken = null
        context.authDataStore.edit { prefs ->
            prefs.clear()
        }
    }

    override fun clearTokenSync() {
        inMemoryToken = null
        scope.launch {
            context.authDataStore.edit { prefs ->
                prefs.clear()
            }
        }
    }

    private suspend fun saveAuthSession(token: String, user: User) {
        inMemoryToken = token
        context.authDataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
            prefs[KEY_USER_ID] = user.id
            prefs[KEY_USER_EMAIL] = user.email
            prefs[KEY_USER_NAME] = user.displayName
            prefs[KEY_USER_ROLE] = user.role
        }
    }
}
