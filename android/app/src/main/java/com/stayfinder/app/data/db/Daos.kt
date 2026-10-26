package com.stayfinder.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) pentru cache-ul local de cazari.
 * Room genereaza automat codul SQL la compilare prin KSP.
 */
@Dao
interface AccommodationDao {

    @Query("SELECT * FROM accommodations")
    suspend fun getAll(): List<AccommodationEntity>

    @Query("SELECT * FROM accommodations WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): AccommodationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<AccommodationEntity>)

    @Query("SELECT COUNT(*) FROM accommodations")
    suspend fun count(): Int
}

/**
 * DAO pentru favorite: expune un Flow<List<Long>> pe care Room il re-emite
 * automat ori de cate ori tabela "favorites" se modifica.
 */
@Dao
interface FavoriteDao {

    @Query("SELECT accommodationId FROM favorites ORDER BY createdAt DESC")
    fun observeFavoriteIds(): Flow<List<Long>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE accommodationId = :id)")
    suspend fun isFavorite(id: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE accommodationId = :id")
    suspend fun deleteById(id: Long)
}

/**
 * DAO pentru preferintele utilizatorului (onboarding + profil).
 */
@Dao
interface UserPreferenceDao {

    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    fun observe(): Flow<UserPreferenceEntity?>

    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    suspend fun get(): UserPreferenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: UserPreferenceEntity)
}

/**
 * DAO pentru cache-ul prognozei meteo.
 */
@Dao
interface WeatherCacheDao {

    @Query("SELECT * FROM weather_cache WHERE accommodationId = :id LIMIT 1")
    fun observeWeather(id: Long): Flow<WeatherCacheEntity?>

    @Query("SELECT * FROM weather_cache WHERE accommodationId = :id LIMIT 1")
    suspend fun getWeatherOnce(id: Long): WeatherCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWeather(entity: WeatherCacheEntity)
}

/**
 * DAO pentru cache-ul cursului valutar.
 */
@Dao
interface ExchangeRateCacheDao {

    @Query("SELECT * FROM exchange_rate_cache WHERE baseCurrency = :baseCurrency LIMIT 1")
    fun observeRate(baseCurrency: String): Flow<ExchangeRateCacheEntity?>

    @Query("SELECT * FROM exchange_rate_cache WHERE baseCurrency = :baseCurrency LIMIT 1")
    suspend fun getRateOnce(baseCurrency: String): ExchangeRateCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRate(entity: ExchangeRateCacheEntity)
}
