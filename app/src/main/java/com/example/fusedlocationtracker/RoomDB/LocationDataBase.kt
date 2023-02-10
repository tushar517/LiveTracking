package com.example.fusedlocationtracker.RoomDB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities=[LocationEntity::class],version = 1)

abstract  class LocationDataBase:RoomDatabase() {

    abstract fun locationdao():LocationDao

       // lateinit var  converters:Converters
    companion object{
      /*  val migration_1_2=object: Migration(1,2){
            override fun migrate(database: SupportSQLiteDatabase) {
                //TODO("Not yet implemented")

                database.execSQL("ALTER TABLE contact ADD COLUMN isactive INTEGER NOT NULL DEFAULT (1)")
            }

        }
*/

        //volatile use--as soon as any value changed in instance ..thread get updated value
        @Volatile
        private  var Instance: LocationDataBase?=null
        fun getdatabase(context: Context):LocationDataBase{
            if(Instance==null){
                synchronized(this) {
                    Instance = Room.databaseBuilder(
                        context.applicationContext, LocationDataBase::class.java,
                        "contactDB"
                    )
                        .build()
                }

            }
            return  Instance!!
        }
    }
}