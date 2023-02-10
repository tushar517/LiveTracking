package com.example.fusedlocationtracker.RoomDB

import androidx.room.*

@Dao
interface LocationDao {
    @Insert
    suspend fun insertlocation(Locationentity:LocationEntity)  //'suspend' due to execute on background

    @Update
    suspend fun updatelocation(Locationentity:LocationEntity)

    @Delete
    suspend fun deletelocation(Locationentity:LocationEntity)

    @Query("DELETE FROM location")
    fun truncateLocation()


    @Query("UPDATE location SET total_distance = :distnace where  id=(SELECT MAX(id)from location)")
    fun updateItem( distnace: String)

   // @Query("INSERT INTO location where  id=(SELECT MAX(id)from location)")

/*
    @Query("UPDATE SQLITE_SEQUENCE SET seq = 1 WHERE name = 'WHEATHERS'")
    fun clearPrimaryKey()*/

   /* @Query("DROP TABLE location ")
    fun truncateLocation(): List<LocationEntity>*/

    //  @Query("SELECT * from contactss")  ///compile time verification of table name in room
    //mostly used live data for 'getting' ..we need not to put suspend as prefix...here is live data so...it automatically understand
    @Query("SELECT * from location ")
    fun getlocation(): List<LocationEntity>

    @Query("SELECT * from location where  id=(SELECT MAX(id)from location)")
    fun getlastlocation(): List<LocationEntity>
   // fun getlastlocation():  LiveData<List<LocationEntity>>

    @Query("SELECT * from location where  id=(SELECT MIN(id)from location)")
    fun getfirstlocation(): List<LocationEntity>
}