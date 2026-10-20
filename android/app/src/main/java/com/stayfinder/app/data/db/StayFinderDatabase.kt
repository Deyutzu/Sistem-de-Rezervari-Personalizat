package com.stayfinder.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Baza de date Room a aplicatiei.
 * Include cele 3 tabele cerute in specificatie:
 *  - accommodations (cache offline de cazari)
 *  - favorites (cazarile salvate la favorite)
 *  - user_preferences (preferintele alese la onboarding)
 */
@Database(
    entities = [
        AccommodationEntity::class,
        FavoriteEntity::class,
        UserPreferenceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class StayFinderDatabase : RoomDatabase() {

    abstract fun accommodationDao(): AccommodationDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun userPreferenceDao(): UserPreferenceDao

    companion object {
        @Volatile
        private var INSTANCE: StayFinderDatabase? = null

        fun getInstance(context: Context): StayFinderDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    StayFinderDatabase::class.java,
                    "stayfinder.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
