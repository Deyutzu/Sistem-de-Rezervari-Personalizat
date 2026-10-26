package com.stayfinder.app.data

import com.stayfinder.app.model.Accommodation

/**
 * Contractul pentru sursa de date a cazarilor.
 *
 * De ce o interfata: ViewModel-urile depind de acest contract, nu de o implementare.
 * Acum folosim FakeAccommodationRepository (date mock); in fazele 3 si 5 adaugam
 * o implementare care combina Room (cache local) + Retrofit (backend),
 * FARA sa modificam ecranele sau ViewModel-urile.
 *
 * "suspend" = functia poate astepta (retea, baza de date) fara sa blocheze UI-ul.
 */
interface AccommodationRepository {

    /** Indică dacă ultimele date au fost servite din cache-ul offline Room. */
    val isOfflineMode: kotlinx.coroutines.flow.StateFlow<Boolean>
        get() = kotlinx.coroutines.flow.MutableStateFlow(false)

    /** Lista de cazari pentru feed. Arunca exceptie daca datele nu pot fi obtinute. */
    suspend fun getAccommodations(): List<Accommodation>

    /** O singura cazare; null daca nu exista. */
    suspend fun getAccommodation(id: Long): Accommodation?
}
