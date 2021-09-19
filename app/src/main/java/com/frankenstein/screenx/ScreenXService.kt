package com.frankenstein.screenx

import android.app.*
import android.content.Intent
import android.os.IBinder
import com.frankenstein.screenx.helper.Logger

class ScreenXService : Service() {
    companion object {
        const val ACTION_STOP_SERVICE = "action_stop_service"
    }

    private var isRunning: Boolean = false
    private var _logger: Logger = Logger.getInstance("ScreenXService");

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        _logger.log("onStartCommand")
        if (isRunning) {
            return dispatchOnStartCommandAction(intent)
        } else {
            var serviceEnabled = true;
            if (serviceEnabled) {
                isRunning = true
                startScreenXService()
                return START_STICKY
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        if (isRunning) {
            isRunning = false
        }
        super.onDestroy()
    }

    private fun dispatchOnStartCommandAction(intent: Intent?): Int {
        if (intent == null)
            return START_NOT_STICKY
        when (intent.action) {
            ACTION_STOP_SERVICE -> {
                disableScreenXService()
                return START_NOT_STICKY
            }
        }
        return START_STICKY
    }

    private fun startScreenXService() {
        _logger.log("Starting ScreenX Service");
    }

    private fun disableScreenXService() {
        stopSelf()
    }
}
