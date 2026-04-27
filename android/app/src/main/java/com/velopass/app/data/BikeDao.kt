package com.velopass.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.velopass.app.models.BikeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BikeDao {
    @Insert
    suspend fun insertBike(bike: BikeEntity)

    @Update
    suspend fun updateBike(bike: BikeEntity)

    @Query("SELECT * FROM bikes WHERE bikeId = :bikeId")
    suspend fun getBike(bikeId: String): BikeEntity?

    @Query("SELECT * FROM bikes WHERE ownerId = :ownerId ORDER BY registeredAt DESC")
    fun getBikesByOwner(ownerId: String): Flow<List<BikeEntity>>

    @Query("SELECT * FROM bikes WHERE ownerId = :ownerId ORDER BY registeredAt DESC")
    suspend fun getBikesByOwnerList(ownerId: String): List<BikeEntity>

    @Query("DELETE FROM bikes WHERE bikeId = :bikeId")
    suspend fun deleteBike(bikeId: String)
}
