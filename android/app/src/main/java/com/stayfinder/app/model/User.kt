package com.stayfinder.app.model

/**
 * Utilizatorul autentificat curent.
 */
data class User(
    val id: Long,
    val email: String,
    val displayName: String,
    val role: String
)
