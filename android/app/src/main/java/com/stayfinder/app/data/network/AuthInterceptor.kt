package com.stayfinder.app.data.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor OkHttp:
 * 1. Atașează automat antetul `Authorization: Bearer <jwt>` cererilor HTTP către backend.
 * 2. Dacă backend-ul întoarce HTTP 401 Unauthorized (token expirat sau invalid),
 *    șterge automat token-ul local și notifică aplicația pentru a redirecționa la ecranul de Login.
 */
class AuthInterceptor(
    private val tokenProvider: () -> String?,
    private val onUnauthorized: () -> Unit
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = tokenProvider()

        val newRequest = if (!token.isNullOrBlank() && originalRequest.header("Authorization") == null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        val response = chain.proceed(newRequest)

        if (response.code == 401) {
            onUnauthorized()
        }

        return response
    }
}
