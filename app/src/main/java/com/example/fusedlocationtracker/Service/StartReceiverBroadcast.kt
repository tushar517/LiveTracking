package com.example.fusedlocationtracker.Service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.fusedlocationtracker.Helper.ActionsEnum
import com.example.fusedlocationtracker.Helper.ServiceState
import com.example.fusedlocationtracker.Helper.getServiceState
import com.example.fusedlocationtracker.Helper.log

class StartReceiverBroadcast : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED && getServiceState(context) == ServiceState.STARTED) {
            Intent(context, ForgroundServices::class.java).also {
                it.action = ActionsEnum.START.name
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    log("Starting the service in >=26 Mode from a BroadcastReceiver")
                    context.startForegroundService(it)
                    return
                }
                log("Starting the service in < 26 Mode from a BroadcastReceiver")
                context.startService(it)
            }
        }
    }
}
