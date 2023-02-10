package com.example.fusedlocationtracker.Service

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Geocoder
import android.location.Location
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.fusedlocationtracker.Activity.LocationListActivity
import com.example.fusedlocationtracker.Activity.MainActivity.Companion.context
import com.example.fusedlocationtracker.Helper.*
import com.example.fusedlocationtracker.R

import com.example.fusedlocationtracker.RoomDB.LocationDataBase
import com.example.fusedlocationtracker.RoomDB.LocationEntity
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationCallback
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.util.*


class ForgroundServices : Service() {
    private var mcontext: Context? = null

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false
    private var mLocationCallback: LocationCallback? = null
    private var notification: Notification? = null
    private val mNotificationId = 123

    lateinit var locationdatabase: LocationDataBase

    override fun onBind(intent: Intent): IBinder? {
        log("Some component want to bind with the service")
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log("onStartCommand executed with startId: $startId")
        if (intent != null) {
            val action = intent.action
            log("using an intent with action $action")
            when (action) {
                ActionsEnum.START.name -> startService()
                ActionsEnum.STOP.name -> stopService()
                else -> log("This should never happen. No action in the received intent")
            }
        } else {
            log(
                "with a null intent. It has been probably restarted by the system."
            )
        }
        // by returning this we make sure the service is restarted if the system kills the service
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        log("The service has been created".uppercase())
        mcontext = context
        locationdatabase = LocationDataBase.getdatabase(applicationContext)
        //  setUpLocationListener_current_location2()
    }


    override fun onDestroy() {
        super.onDestroy()
        log("The service has been destroyed".toUpperCase())
        stopForeground(true)
        stopSelf()
        // Toast.makeText(this, "Service destroyed", Toast.LENGTH_SHORT).show()
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        val restartServiceIntent = Intent(applicationContext, ForgroundServices::class.java).also {
            it.setPackage(packageName)
        }
        val restartServicePendingIntent: PendingIntent =
            PendingIntent.getService(this, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT)
        applicationContext.getSystemService(Context.ALARM_SERVICE)
        val alarmService: AlarmManager =
            applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1000,
            restartServicePendingIntent
        )

    }

    private fun startService() {
        if (isServiceStarted) return
        log("Starting the foreground service task...")
        Toast.makeText(this, "Service starting its task", Toast.LENGTH_SHORT).show()
        isServiceStarted = true
        setServiceState(this, ServiceState.STARTED)

        // we need this lock so our service gets not affected by Doze Mode
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EndlessService::lock").apply {
                    acquire()
                }
            }
        SharedPrefrence.getInstance(applicationContext).saveInt("Count", 0)

        setUpLocationListener_current_location2()
    }

    private fun stopService() {
        log("Stopping the foreground service")
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }

            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            log("Service stopped without being started: ${e.message}")
        }
        isServiceStarted = false
        setServiceState(this, ServiceState.STOPPED)


        fusedLocationProviderClient.removeLocationUpdates(mLocationCallback)


        GlobalScope.launch {

            var collect = mutableListOf<LocationEntity>()
            collect.addAll(locationdatabase.locationdao().getlocation())


            var calculated_distance = 0.0
            for (i in 0..collect.size - 2) {
                var get_distance = 0.0


                get_distance = distance(
                    collect.get(i).lat.toDouble(),
                  collect.get(i + 1).lat.toDouble(),
                   collect.get(i).longitude.toDouble(),
                   collect.get(i + 1).longitude.toDouble()
                )

              //  if((get_distance*100)>=20) {
                    calculated_distance += get_distance
               // }
            }



            Log.d("chch", (calculated_distance).toString())


            val dec = DecimalFormat("#,###.00")

            locationdatabase.locationdao().updateItem(((calculated_distance ).toString()))

            Log.d("testing_dis", (calculated_distance).toString())

            SharedPrefrence.getInstance(applicationContext)
                .saveString("total_distance", (calculated_distance).toString())
            SharedPrefrence.getInstance(applicationContext).saveInt("Count", 0)

            //  locationdatabase.locationdao().truncateLocation()

            val i = Intent()
            i.setClass(applicationContext, LocationListActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(i)

        }

        //remove location update

        Handler().postDelayed(Runnable {

            Toast.makeText(
                applicationContext,
                SharedPrefrence.getInstance(applicationContext).getString("total_distance").toString() + " " + "km",
                Toast.LENGTH_SHORT
            ).show()


        }, 2000)
    }

    private fun getCompleteAddressString(latitude: Double, longitude: Double): String {
        var strAdd = ""
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null) {
                val returnedAddress = addresses[0]
                val strReturnedAddress = StringBuilder()
                for (i in 0..returnedAddress.maxAddressLineIndex) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
                }
                strAdd = strReturnedAddress.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return strAdd
    }

    private fun setUpLocationListener_current_location2() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRequest = LocationRequest().setInterval(20000).setFastestInterval(15000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        //.setSmallestDisplacement(10f)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for (location in locationResult.locations) {

                    Log.d("sdsd1", location.accuracy.toString())

                    Log.d("checking", "${location.latitude}" + " " + "${location.longitude}");

                    //  generateForegroundNotification()
                    getnotification(location.latitude, location.longitude)




                  //  Location_calculation(location)

                    GlobalScope.launch {


                            locationdatabase.locationdao().insertlocation(
                                (LocationEntity(
                                    0,
                                    location.latitude.toString(),
                                    location.longitude.toString(),
                                    location.accuracy.toString(),
                                    "not calculated"
                                ))
                            )

                    }


                }

            }
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            mLocationCallback,
            Looper.myLooper()
        )
    }

    private fun getnotification(latitude: Double, longitude: Double) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = getString(R.string.app_name)
            val description: String = getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel =
                NotificationChannel(getString(R.string.app_name), name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager: NotificationManager =
                getSystemService<NotificationManager>(
                    NotificationManager::class.java
                )
            notificationManager.createNotificationChannel(channel)
        }

        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, getString(R.string.app_name))
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setContentTitle("New Location Update")
                .setContentText(
                    "You are at " + getCompleteAddressString(
                        latitude,
                        longitude
                    )
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(
                    NotificationCompat.BigTextStyle().bigText(
                        "You are at " + getCompleteAddressString(
                            latitude,
                            longitude
                        )
                    )
                )
        notification = builder.build()
        startForeground(mNotificationId, notification)

    }

    fun distance(
        lat1: Double,
        lat2: Double,
        lon1: Double,
        lon2: Double
    ): Double {

        // The math module contains a function
        // named toRadians which converts from
        // degrees to radians.
        var lat1 = lat1
        var lat2 = lat2
        var lon1 = lon1
        var lon2 = lon2
        lon1 = Math.toRadians(lon1)
        lon2 = Math.toRadians(lon2)
        lat1 = Math.toRadians(lat1)
        lat2 = Math.toRadians(lat2)

        // Haversine formula
        val dlon = lon2 - lon1
        val dlat = lat2 - lat1
        val a = (Math.pow(Math.sin(dlat / 2), 2.0)
                + (Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dlon / 2), 2.0)))
        val c = 2 * Math.asin(Math.sqrt(a))

        // Radius of earth in kilometers. Use 3956
        // for miles
        val r = 6371.0

        // calculate the result
        return c * r
    }

}
