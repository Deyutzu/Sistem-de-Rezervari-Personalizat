package com.stayfinder.app.data

import android.util.Log
import com.stayfinder.app.data.db.ExchangeRateCacheDao
import com.stayfinder.app.data.db.ExchangeRateCacheEntity
import com.stayfinder.app.data.db.WeatherCacheDao
import com.stayfinder.app.data.db.WeatherCacheEntity
import com.stayfinder.app.data.network.StayFinderApiService
import com.stayfinder.app.model.DailyWeather
import com.stayfinder.app.model.NearbyPlace
import com.stayfinder.app.model.WeatherForecast
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository pentru datele din API-urile externe apelate prin backend:
 *  1. Prognoza meteo (Open-Meteo) -> cache local Room (weather_cache) cu timestamp.
 *  2. Cursul valutar (Frankfurter) -> cache local Room (exchange_rate_cache) cu timestamp.
 *  3. Obiective turistice în apropiere (OpenTripMap) -> apel direct pe backend.
 *
 * Pattern Offline-First:
 * Aplicația afișează imediat datele salvate în Room (marcate cu "actualizat la ..."),
 * apoi încearcă actualizarea din rețea. Dacă nu există internet, datele locale rămân vizibile.
 */
class ExternalDataRepository(
    private val apiService: StayFinderApiService,
    private val weatherCacheDao: WeatherCacheDao,
    private val exchangeRateCacheDao: ExchangeRateCacheDao
) {

    private val tag = "ExternalDataRepo"

    /**
     * Observă prognoza meteo salvată în Room pentru cazarea dată.
     */
    fun observeWeather(accommodationId: Long): Flow<WeatherForecast?> {
        return weatherCacheDao.observeWeather(accommodationId).map { entity ->
            entity?.let {
                WeatherForecast(
                    accommodationId = it.accommodationId,
                    locationName = it.locationName,
                    note = it.note,
                    days = it.toDailyWeatherList(),
                    updatedAt = it.updatedAt
                )
            }
        }
    }

    /**
     * Actualizează prognoza meteo apelând endpoint-ul backend /api/accommodations/{id}/weather
     * și salvând rezultatul în Room cu timestamp-ul curent.
     */
    suspend fun refreshWeather(accommodationId: Long): Result<Unit> {
        return runCatching {
            val response = apiService.getWeather(accommodationId)
            val domainDays = response.days.map { d ->
                DailyWeather(
                    date = d.date,
                    minTemp = d.tempMin,
                    maxTemp = d.tempMax,
                    precipitation = d.precipitation,
                    description = d.description,
                    icon = d.icon
                )
            }
            val entity = WeatherCacheEntity.fromModel(
                accommodationId = response.accommodationId,
                locationName = response.locationName,
                note = response.note,
                days = domainDays,
                updatedAt = System.currentTimeMillis()
            )
            weatherCacheDao.saveWeather(entity)
        }.onFailure { ex ->
            Log.w(tag, "Eroare la actualizarea vremii pentru $accommodationId: ${ex.message}")
        }
    }

    /**
     * Observă cursul valutar memorat în Room (ex. EUR -> RON).
     */
    fun observeExchangeRate(base: String = "EUR"): Flow<ExchangeRateCacheEntity?> {
        return exchangeRateCacheDao.observeRate(base)
    }

    /**
     * Actualizează cursul valutar apelând /api/exchange-rates?base=EUR
     * și salvând rata RON în Room cu timestamp.
     */
    suspend fun refreshExchangeRate(base: String = "EUR"): Result<Unit> {
        return runCatching {
            val response = apiService.getExchangeRates(base)
            val ron = response.rates["RON"]
            if (ron != null) {
                val entity = ExchangeRateCacheEntity(
                    baseCurrency = base,
                    ronRate = ron,
                    updatedAt = System.currentTimeMillis()
                )
                exchangeRateCacheDao.saveRate(entity)
            }
        }.onFailure { ex ->
            Log.w(tag, "Eroare la actualizarea cursului valutar pentru $base: ${ex.message}")
        }
    }

    /**
     * Obține obiectivele turistice din apropierea cazării (rază 5 km).
     * Dacă serviciul nu este configurat pe backend sau este offline, returnează o listă vidă.
     */
    suspend fun getNearbyPlaces(accommodationId: Long): List<NearbyPlace> {
        return try {
            val response = apiService.getNearbyPlaces(accommodationId)
            response.map { item ->
                NearbyPlace(
                    name = item.name,
                    category = item.category,
                    distanceMeters = item.distanceMeters
                )
            }
        } catch (ex: Exception) {
            Log.w(tag, "Nu s-au putut încărca obiectivele turistice pentru $accommodationId: ${ex.message}")
            emptyList()
        }
    }
}
