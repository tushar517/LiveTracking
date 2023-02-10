package com.example.fusedlocationtracker.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fusedlocationtracker.Adapter.LocationAdapter
import com.example.fusedlocationtracker.R
import com.example.fusedlocationtracker.RoomDB.LocationDataBase
import com.example.fusedlocationtracker.RoomDB.LocationEntity
import com.example.fusedlocationtracker.databinding.ActivityLocationListBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LocationListActivity : AppCompatActivity() {

    lateinit var  locationdatabase: LocationDataBase
    private  lateinit var activitylocationbinding: ActivityLocationListBinding
      var collect=mutableListOf<LocationEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitylocationbinding=DataBindingUtil.setContentView(this,
            R.layout.activity_location_list
        )
        locationdatabase= LocationDataBase.getdatabase(applicationContext)
        activitylocationbinding.locationRecycler.layoutManager=LinearLayoutManager(this)

        GlobalScope.launch {
            // Log.d("testing_list",locationdatabase.locationdao().getlastlocation().toString())
            /*  locationA.latitude="%.3f".format(locationdatabase.locationdao().getfirstlocation()[0].lat.toDouble()).toDouble()
              locationA.longitude="%.3f".format(locationdatabase.locationdao().getfirstlocation()[0].longitude.toDouble()).toDouble()
              locationB.latitude="%.3f".format( locationdatabase.locationdao().getlastlocation()[0].lat.toDouble()).toDouble()
              locationB.longitude= "%.3f".format(locationdatabase.locationdao().getlastlocation()[0].longitude.toDouble()).toDouble()
              distance = locationA.distanceTo(locationB)
         */

            var collect = mutableListOf<LocationEntity>()
            collect.addAll(locationdatabase.locationdao().getlocation())
            val adapter = LocationAdapter(collect)

            // Setting the Adapter with the recyclerview
            activitylocationbinding.locationRecycler.adapter = adapter

           locationdatabase.locationdao().truncateLocation()
        }





     /*   val adapter = LocationAdapter(collect)
        activitylocationbinding.locationRecycler.adapter = adapter*/
    }
}