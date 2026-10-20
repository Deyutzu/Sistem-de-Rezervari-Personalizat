package com.stayfinder.app.data

/**
 * "Containerul" de dependente al aplicatiei (Dependency Injection manual).
 *
 * De ce: ViewModel-urile au nevoie de repository-uri, dar nu trebuie sa le
 * construiasca singure. Le cream O SINGURA DATA aici si le impartim.
 * Am ales DI manual in locul unui framework (Hilt) ca sa fie usor de explicat:
 * e doar o clasa cu proprietati.
 *
 * Pentru a trece la date reale (fazele 3 si 5), schimbam doar implementarile de mai jos.
 */
class AppContainer {
    val accommodationRepository: AccommodationRepository = FakeAccommodationRepository()
    val favoritesRepository: FavoritesRepository = InMemoryFavoritesRepository()
}
