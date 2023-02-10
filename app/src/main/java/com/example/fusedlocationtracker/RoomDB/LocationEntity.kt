package com.example.fusedlocationtracker.RoomDB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="location")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id:Int,
    val lat:String,
    val longitude:String,
    val accuracy:String,
    val total_distance:String)