package com.stayfinder.app.data.network

/**
 * Configurarea rețelei pentru comunicarea cu backend-ul Spring Boot.
 *
 * De ce http://10.0.2.2:8080/:
 * În Android Emulator, adresa 10.0.2.2 este un alias special către 127.0.0.1 (localhost-ul PC-ului gazdă).
 * Dacă testezi pe un telefon fizic conectat prin Wi-Fi, înlocuiește cu IP-ul local al calculatorului
 * (ex. "http://192.168.1.15:8080/").
 */
object NetworkConfig {
    const val BASE_URL = "http://10.0.2.2:8080/"
}
