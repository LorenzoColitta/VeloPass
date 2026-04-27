package com.velopass.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.velopass.app.models.BikeEntity

@Database(
    entities = [BikeEntity::class],
    version = 1,
    exportSchema = false
)
abstract class VeloPassDatabase : RoomDatabase() {
    abstract fun bikeDao(): BikeDao
}
