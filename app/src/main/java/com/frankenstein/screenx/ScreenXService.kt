package com.frankenstein.screenx

import android.annotation.TargetApi
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.text.TextUtils
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.frankenstein.screenx.capture.RequestCaptureActivity
import com.frankenstein.screenx.capture.ScreenCaptureListener
import com.frankenstein.screenx.capture.ScreenCaptureManager
import com.frankenstein.screenx.helper.Logger
import com.frankenstein.screenx.helper.PermissionHelper
import com.frankenstein.screenx.overlay.CaptureButtonController
import com.frankenstein.screenx.ui.ScreenXToast

class ScreenXService : Service(), CaptureButtonController.ClickListener, ScreenCaptureListener {
    companion object {
        private const val ID_FOREGROUND = 1730

        const val ACTION_STOP_SERVICE = "action_stop_service"
        const val ACTION_ENABLE_CAPTURE_BUTTON = "action_enable_capture_button"
        const val ACTION_DISABLE_CAPTURE_BUTTON = "action_disable_capture_button"

        private const val DELAY_CAPTURE_FAB = 0L
    }

    private var isRunning: Boolean = false
    private var isFloatingButtonVisible: Boolean = false;
    private var captureButtonController: CaptureButtonController? = null

    private var _logger: Logger = Logger.getInstance("ScreenXService");

    private var screenCapturePermissionIntent: Intent? = null
    private var screenCaptureManager: ScreenCaptureManager? = null
    private lateinit var requestCaptureFilter: IntentFilter
    private lateinit var requestCaptureReceiver: BroadcastReceiver
    private val handler = Handler(Looper.getMainLooper())

    private val toast: ScreenXToast by lazy {
        ScreenXToast(this)
    }

    private val bringTaskToFrontIntent: Intent by lazy {
        Intent(Intent.ACTION_MAIN).apply {
            setClass(this@ScreenXService, MainActivity::class.java)
            addCategory(Intent.CATEGORY_LAUNCHER)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

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
            destroyFloatingButton()
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
            ACTION_ENABLE_CAPTURE_BUTTON -> initFloatingButton()
            ACTION_DISABLE_CAPTURE_BUTTON -> destroyFloatingButton()
        }
        return START_STICKY
    }

    private fun startScreenXService() {
        _logger.log("Starting ScreenX Service");
        startForeground(getForegroundNotificationId(), getForegroundNotification())
    }

    private fun disableScreenXService() {
        stopSelf()
    }

    private fun initFloatingButton() {
        if (isFloatingButtonVisible)
            return;
        _logger.log("Checking for Floating Button Permissions");
        var enabled = true;
        if (!enabled || !PermissionHelper.hasOverlayPermission(this)) {
            return
        } else {
            _logger.log("Yay! You have permissions for floating button");
        }

        if (captureButtonController != null)
            return;

        captureButtonController?: run {
            captureButtonController = CaptureButtonController(applicationContext)
            captureButtonController?.setOnClickListener(this)
            captureButtonController?.init()
        }
        isFloatingButtonVisible = true;
    }

    private fun destroyFloatingButton() {
        captureButtonController?.destroy()
        captureButtonController = null
        isFloatingButtonVisible = false;
    }

    override fun onScreenshotButtonClicked() {
        handler.postDelayed({
            takeScreenshot()
        }, DELAY_CAPTURE_FAB)
    }

    override fun onScreenshotButtonDismissed() {
        destroyFloatingButton()
    }

    private fun takeScreenshot() {
        captureButtonController?.hide()
        if (screenCapturePermissionIntent != null) {
            _logger.log("We have Screen Capture permission, taking screenshot now ");
            screenCaptureManager?.captureScreen()
        } else {
            _logger.log("We do not have Screen Capture permission , initiating permissions");
            requestCaptureFilter = IntentFilter(RequestCaptureActivity.getResultBroadcastAction(applicationContext))
            requestCaptureReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(requestCaptureReceiver)

                    val resultCode = intent.getIntExtra(RequestCaptureActivity.RESULT_EXTRA_CODE, Activity.RESULT_CANCELED)
                    if (resultCode != Activity.RESULT_OK) {
                        onScreenShotTaken("")
                        return
                    }

                    screenCapturePermissionIntent = intent.getParcelableExtra(RequestCaptureActivity.RESULT_EXTRA_DATA)
                    screenCapturePermissionIntent?.let {
                        screenCaptureManager = ScreenCaptureManager(applicationContext, it, this@ScreenXService)

                        if (intent.getBooleanExtra(RequestCaptureActivity.RESULT_EXTRA_PROMPT_SHOWN, true)) {
                            // Delay capture until after the permission dialog is gone.
                            handler.postDelayed({ screenCaptureManager?.captureScreen() }, 500)
                        } else {
                            screenCaptureManager?.captureScreen()
                        }
                    }
                }
            }

            androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(applicationContext).registerReceiver(requestCaptureReceiver, requestCaptureFilter)
            val intent = Intent(applicationContext, RequestCaptureActivity::class.java)
            intent.flags = intent.flags or Intent.FLAG_ACTIVITY_NEW_TASK
            applicationContext.startActivity(intent)
        }
    }

    override fun onScreenShotTaken(path: String) {
        captureButtonController?.show()
        toast.show(getString(R.string.screenshot_feedback), Toast.LENGTH_SHORT)
        if (!TextUtils.isEmpty(path)) {
            MediaScannerConnection.scanFile(applicationContext, arrayOf(path), null, null)
        }
        _logger.log("ScreenXService: Screenshot taken", path);
        ScreenXApplication.screenFactory.onScreenAdded(this, path)
    }

    private fun getForegroundNotificationId(): Int {
        return ID_FOREGROUND
    }

    private fun getForegroundNotification(): Notification? {
        _logger.log("Getting Foreground Notification");
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createForegroundChannel()
        } else {
            ""
        }

        val openAppPendingIntent = PendingIntent.getActivity(this, 0,
                bringTaskToFrontIntent, 0)

        val stopIntent = Intent(ACTION_STOP_SERVICE)
        stopIntent.setClass(this, ScreenXService::class.java)
        val stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, 0)
        val stopAction = NotificationCompat.Action(0, getString(R.string.notification_action_stop),
                stopPendingIntent)

        val style = NotificationCompat.BigTextStyle()
        style.bigText(getString(R.string.notification_display_text))
        return NotificationCompat.Builder(this, channelId)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setSmallIcon(R.drawable.gallery_outline_64bit)
                .setColor(ContextCompat.getColor(this, R.color.foreground_notification))
                .setContentTitle(getString(R.string.notification_default_title))
                .setContentText(getString(R.string.notification_display_text))
                .setContentIntent(openAppPendingIntent)
                .setStyle(style)
                .build()
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createForegroundChannel(): String {
        val channelId = "foreground_channel"
        val channelName = "ScreenX Service"
        val channel = NotificationChannel(channelId, channelName,
                NotificationManager.IMPORTANCE_LOW)

        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
        return channelId
    }
}
