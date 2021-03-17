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
import com.frankenstein.screenx.helper.PermissionHelper
import com.frankenstein.screenx.overlay.CaptureButtonController
import com.frankenstein.screenx.ui.ScreenXToast

class ScreenXService : Service(), CaptureButtonController.ClickListener, ScreenCaptureListener {
    companion object {
        // TODO: temp id
        private const val ID_FOREGROUND = 9487
        private const val ID_SCREENSHOT_DETECTED = 9488

        const val ACTION_CAPTURE_SCREEN = "action_capture"

        /** Disable service and no succession dialog will be shown */
        const val ACTION_DISABLE_SERVICE = "action_disable_service"
        /** Disable service while allowing to show a prompt-enable dialog in the future  */
        const val ACTION_DISABLE_SERVICE_SOFTLY = "action_disable_service_softly"

        /** Action indicating user has explicitly enabled the service */
        const val ACTION_ENABLE_SERVICE = "action_enable_service"

        const val ACTION_ENABLE_CAPTURE_BUTTON = "action_enable_capture_button"
        const val ACTION_DISABLE_CAPTURE_BUTTON = "action_disable_capture_button"

        private const val DELAY_CAPTURE_NOTIFICATION = 1000L
        private const val DELAY_CAPTURE_FAB = 0L

        // Broadcast sent from ScryerService
        const val EVENT_TAKE_SCREENSHOT = "org.mozilla.scryer.take_screenshot"
    }

    private var isRunning: Boolean = false
    private var captureButtonController: CaptureButtonController? = null

    private var _logger: Logger? = Logger.getInstance("FILES-SERVICE");

    private var screenCapturePermissionIntent: Intent? = null
    private var screenCaptureManager: ScreenCaptureManager? = null
    private lateinit var requestCaptureFilter: IntentFilter
    private lateinit var requestCaptureReceiver: BroadcastReceiver

    private val toast: ScreenXToast by lazy {
        ScreenXToast(this)
    }

//    private val fileMonitor: FileMonitor by lazy {
//        //FileMonitor(FileObserverDelegate(Handler(Looper.getMainLooper())))
//        FileMonitor(MediaProviderDelegate(this, Handler(Looper.getMainLooper())))
//    }

    private val bringTaskToFrontIntent: Intent by lazy {
        Intent(Intent.ACTION_MAIN).apply {
            setClass(this@ScreenXService, MainActivity::class.java)
            addCategory(Intent.CATEGORY_LAUNCHER)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    private val bringMainActivityToFrontIntent: Intent by lazy {
        Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    private val handler = Handler(Looper.getMainLooper())

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        _logger!!.log("onStartCommand ->")
        if (null == intent) {
            stopSelf()
            return Service.START_NOT_STICKY
        }

        if (isRunning) {
            return dispatchOnStartCommandAction(intent)

        } else {
//            val serviceEnabled = ScryerApplication.getSettingsRepository().serviceEnabled
            var serviceEnabled = true;
            if (serviceEnabled) {
                isRunning = true
                val startedByUser = (intent.action == ACTION_ENABLE_SERVICE)
                startScreenXSerrvice(startedByUser)
                return START_STICKY
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        if (isRunning) {
            isRunning = false
            destroyFloatingButton()
//            fileMonitor.stopMonitor()
//            stopHeartbeatTracking()
        }
        super.onDestroy()
    }

    private fun dispatchOnStartCommandAction(intent: Intent): Int {
        _logger!!.log("onStartCommandAction ->", intent.action);
        when (intent.action) {
            ACTION_DISABLE_SERVICE -> {
                disableScreenXService(true)
                return START_NOT_STICKY
            }

            ACTION_DISABLE_SERVICE_SOFTLY -> {
//                PreferenceWrapper(this).setShouldPromptEnableService(true)
                disableScreenXService(false)
//                TelemetryWrapper.stopCaptureService(TelemetryWrapper.Value.NOTIFICATION)
                return START_NOT_STICKY
            }

            ACTION_CAPTURE_SCREEN -> {
                postTakeScreenshot(DELAY_CAPTURE_NOTIFICATION)
//                TelemetryWrapper.captureViaNotification()
            }

            ACTION_ENABLE_CAPTURE_BUTTON -> initFloatingButton()
            ACTION_DISABLE_CAPTURE_BUTTON -> destroyFloatingButton()
        }
        return START_STICKY
    }

    private fun startScreenXSerrvice(startedExplicitly: Boolean) {
        _logger!!.log("Starting ScreenX Service, going to start floating button");
        if (startedExplicitly) {
            toast.show(getString(R.string.snackbar_enable), Toast.LENGTH_SHORT)
        }
        startForeground(getForegroundNotificationId(), getForegroundNotification())
//        initFileMonitors()
        initFloatingButton()
//        startHeartbeatTracking()
    }

    private fun disableScreenXService(showToast: Boolean) {
        if (showToast) {
            toast.show(getString(R.string.snackbar_disable), Toast.LENGTH_SHORT)
        }
//        ScryerApplication.getSettingsRepository().serviceEnabled = false
        stopSelf()
    }

    private fun initFloatingButton() {
        _logger!!.log("Checking for Floating Button Permissions");
//        val enabled = ScryerApplication.getSettingsRepository().floatingEnable
        var enabled = true;
        if (!enabled || !PermissionHelper.hasOverlayPermission(this)) {
            return
        } else {
            _logger!!.log("Yay! You have permissions for floating button");
        }

        captureButtonController?: run {
            captureButtonController = CaptureButtonController(applicationContext)
            captureButtonController?.setOnClickListener(this)
            captureButtonController?.init()
        }
    }

//    private fun startHeartbeatTracking() {
//        stopHeartbeatTracking()
//
//        val heartbeatWorkBuilder = PeriodicWorkRequest
//                .Builder(CaptureServiceHeartbeatWorker::class.java, 24, TimeUnit.HOURS)
//        heartbeatWorkBuilder.addTag(CaptureServiceHeartbeatWorker.TAG)
//        WorkManager.getInstance().enqueue(heartbeatWorkBuilder.build())
//    }

//    private fun stopHeartbeatTracking() {
//        WorkManager.getInstance().cancelAllWorkByTag(CaptureServiceHeartbeatWorker.TAG)
//    }

    private fun destroyFloatingButton() {
        captureButtonController?.destroy()
        captureButtonController = null
    }

//    private fun initFileMonitors() {
//        fileMonitor.startMonitor(object : FileMonitor.ChangeListener {
//            override fun onChangeFinish(path: String) {
//                postNotification(getScreenshotDetectedNotification())
//                val model = ScreenshotModel(path,
//                        System.currentTimeMillis(),
//                        CollectionModel.UNCATEGORIZED)
//                launchIO {
//                    ScryerApplication.getScreenshotRepository().addScreenshot(listOf(model))
//                }
//
//                TelemetryWrapper.captureViaExternal()
//            }
//        })
//    }

    override fun onScreenshotButtonClicked() {
        postTakeScreenshot(DELAY_CAPTURE_FAB)
//        TelemetryWrapper.captureViaFab()
    }

    override fun onScreenshotButtonDismissed() {
        destroyFloatingButton()
//        ScryerApplication.getSettingsRepository().floatingEnable = false
//        TelemetryWrapper.closeFAB()
    }

    private fun postTakeScreenshot(delayed: Long) {
        handler.postDelayed({
            captureButtonController?.hide()
            takeScreenshot()
        }, delayed)
    }

    private fun takeScreenshot() {
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(EVENT_TAKE_SCREENSHOT))

        if (screenCapturePermissionIntent != null) {
            screenCaptureManager?.captureScreen()
        } else {
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
        if (!TextUtils.isEmpty(path)) {
//            startSortingPanelActivity(path)
            MediaScannerConnection.scanFile(applicationContext, arrayOf(path), null, null)
//            Promoter.onScreenshotTaken(this)
        }
    }

//    private fun startSortingPanelActivity(path: String) {
//        val intent = SortingPanelActivity.sortNewScreenshot(this, path, ScryerApplication.getSettingsRepository().addToCollectionEnable)
//        intent.flags = intent.flags or Intent.FLAG_ACTIVITY_NEW_TASK
//        startActivity(intent)
//    }

    private fun getForegroundNotificationId(): Int {
        return ID_FOREGROUND
    }

    private fun getForegroundNotification(): Notification? {
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createForegroundChannel()
        } else {
            ""
        }

        val tapIntent = Intent(ACTION_CAPTURE_SCREEN)
        tapIntent.setClass(this, ScreenXService::class.java)
        val tapPendingIntent = PendingIntent.getService(this, 0, tapIntent, 0)

        val openAppPendingIntent = PendingIntent.getActivity(this, 0,
                bringTaskToFrontIntent, 0)
        val openAppAction = NotificationCompat.Action(0, getString(R.string.notification_action_open),
                openAppPendingIntent)

        val stopIntent = Intent(ACTION_DISABLE_SERVICE_SOFTLY)
        stopIntent.setClass(this, ScreenXService::class.java)
        val stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, 0)
        val stopAction = NotificationCompat.Action(0, getString(R.string.notification_action_stop),
                stopPendingIntent)

        val style = NotificationCompat.BigTextStyle()
        style.bigText(getString(R.string.notification_action_capture))
        return NotificationCompat.Builder(this, channelId)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setColor(ContextCompat.getColor(this, R.color.foreground_notification))
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_action_capture))
                .setContentIntent(tapPendingIntent)
                .setStyle(style)
                .addAction(openAppAction)
                .addAction(stopAction)
                .build()
    }

//    private fun getScreenshotDetectedNotification(): Notification {
//        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            createMessageChannel()
//        } else {
//            ""
//        }
//
//        val intent = SortingPanelActivity.sortCollection(this, CollectionModel.UNCATEGORIZED)
//        intent.flags = intent.flags or Intent.FLAG_ACTIVITY_NEW_TASK
//        val tapPendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
//
//        return NotificationCompat.Builder(this, channelId)
//                .setCategory(Notification.CATEGORY_PROMO)
//                .setSmallIcon(R.drawable.ic_stat_notify_sort)
//                .setColor(ContextCompat.getColor(this, R.color.foreground_notification))
//                .setContentTitle(getString(R.string.notification_action_collect_title))
//                .setContentText(getString(R.string.notification_action_collect))
//                .setContentIntent(tapPendingIntent)
//                .setAutoCancel(true)
//                .build()
//    }

    private fun postNotification(notification: Notification) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(ID_SCREENSHOT_DETECTED, notification)
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createForegroundChannel(): String {
        val channelId = "foreground_channel"
        val channelName = "ScreenshotPlus Service"
        val channel = NotificationChannel(channelId, channelName,
                NotificationManager.IMPORTANCE_NONE)

        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
        return channelId
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createMessageChannel(): String {
        val channelId = "message_channel"
        val channelName = "ScreenshotPlus Message"
        val channel = NotificationChannel(channelId, channelName,
                NotificationManager.IMPORTANCE_HIGH)

        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
        return channelId
    }
}
