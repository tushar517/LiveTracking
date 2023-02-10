package com.example.fusedlocationtracker.Activity

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.library.BuildConfig
import com.example.fusedlocationtracker.*
import com.example.fusedlocationtracker.Helper.ActionsEnum
import com.example.fusedlocationtracker.Helper.ServiceState
import com.example.fusedlocationtracker.Helper.getServiceState
import com.example.fusedlocationtracker.Helper.log
import com.example.fusedlocationtracker.Service.ForgroundServices
import com.example.fusedlocationtracker.databinding.ActivityMainBinding


public class MainActivity : AppCompatActivity() {
private  lateinit var mainBinding:ActivityMainBinding

companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 999
        const val TAG = "LOCATION_FIND"
        const val  ACTION_STOP_FOREGROUND = "stopforeground"
         var context: Context? = null
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 99
        private const val MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION = 66

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context =this
        mainBinding=DataBindingUtil.setContentView(this, R.layout.activity_main)

        checkLocationPermission2()   // //check permission
        mainBinding.startLocation.setOnClickListener {
          //  startService(Intent(this, ForegroundLocationServices::class.java))
            actionOnService(ActionsEnum.START) }

        mainBinding.stopLocation.setOnClickListener {
          /*  val intentStop = Intent(this, ForegroundLocationServices::class.java)
            intentStop.action = ACTION_STOP_FOREGROUND
            startService(intentStop)*/
            actionOnService(ActionsEnum.STOP)




        }
    }




    private fun checkLocationPermission(): Boolean {


        val result3 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        val result4 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
         return result3 == PackageManager.PERMISSION_GRANTED &&
                result4 == PackageManager.PERMISSION_GRANTED

    }

    private fun actionOnService(action: ActionsEnum) {
        if (getServiceState(this) == ServiceState.STOPPED && action == ActionsEnum.STOP) return
        Intent(this, ForgroundServices::class.java).also {
            it.action = action.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                log("Starting the service in >=26 Mode")
                startForegroundService(it)

                return
            }
            log("Starting the service in < 26 Mode")
            startService(it)
        }
    }
    private fun checkLocationPermission2() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
            ) {
                AlertDialog.Builder(this)
                    .setTitle("Location Permission Needed")
                    .setMessage("This app needs the Location permission, please accept to use location functionality")
                    .setPositiveButton(
                        "OK") { _, _ ->
                        requestLocationPermission() }
                    .create()
                    .show()
            } else {
                requestLocationPermission()
            }
        } else {
            checkBackgroundLocation()
        }
    }
    private fun checkBackgroundLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            var  dialog2= Dialog(this)
            dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog2.setCancelable(false);
            dialog2.setCanceledOnTouchOutside(false);
            dialog2.setContentView(R.layout.info_dialog)
            dialog2.getWindow()!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);// set height width of dialog

            dialog2.getWindow()!!.setBackgroundDrawable(
                ColorDrawable(Color.TRANSPARENT)
            )
            dialog2.show()
            Handler().postDelayed(Runnable {

                dialog2.dismiss()
                requestBackgroundLocationPermission()
            }, 3000)

        }




    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
            ),
            MY_PERMISSIONS_REQUEST_LOCATION
        )
    }

    private fun requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {

                        // Now check background location

                        checkBackgroundLocation()
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show()
                    // Check if we are in a state where the user has denied the permission and
                    // selected Don't ask again
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    ) {
                    }
                }
                return
            }
            MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {

                        Toast.makeText(
                            this,
                            "Granted Background Location Permission",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
                }
                return

            }
        }
    }

}