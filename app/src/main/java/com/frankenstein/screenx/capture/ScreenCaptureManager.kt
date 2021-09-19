package com.frankenstein.screenx.capture

import android.app.Activity
import android.app.ActivityManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Context.WINDOW_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import com.frankenstein.screenx.Constants.SCREEN_DATE_FORMAT

import com.frankenstein.screenx.helper.Logger
import com.frankenstein.screenx.R
import com.frankenstein.screenx.helper.UsageStatsHelper.Companion.lastForegroundApp
import com.frankenstein.screenx.helper.FileHelper.CUSTOM_SCREENSHOT_DIR
import com.frankenstein.screenx.helper.FileHelper.createIfNot

import java.io.FileOutputStream
import java.time.LocalDateTime

interface ScreenCaptureListener {
    fun onScreenShotTaken(path: String)
}

class ScreenCaptureManager(context: Context, private val screenCapturePermissionIntent: Intent, private val screenCaptureListener: ScreenCaptureListener) {

    private val projectionManager: MediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    private var mediaProjection: MediaProjection? = null
    private var imageReader: ImageReader? = null
    private val workerHandler: Handler
    private val uiHandler: Handler
    private var defaultDisplay: Display
    private var virtualDisplay: VirtualDisplay? = null
    private val metrics: DisplayMetrics = DisplayMetrics()
    private val density: Int
    private var width = 0
    private var height = 0
    private val screenshotPath: String

    private val _logger: Logger = Logger.getInstance("ScreenCaptureManager");
    private val activityManager: ActivityManager;
    private val usm: UsageStatsManager;
    private val defaultPackageId: String;

    init {
        val screenshotDirectory = CUSTOM_SCREENSHOT_DIR;
        createIfNot(screenshotDirectory)
        screenshotPath = screenshotDirectory.absolutePath
        defaultPackageId = context.resources.getString(R.string.miscellaneous_app_name)
        val windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager
        activityManager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        defaultDisplay = windowManager.defaultDisplay
        defaultDisplay.getMetrics(metrics)
        density = metrics.densityDpi

        val handlerThread = HandlerThread("ScreenCaptureThread")
        handlerThread.start()
        val looper = handlerThread.looper
        workerHandler = Handler(looper)

        uiHandler = Handler(Looper.myLooper()!!)
    }

    fun captureScreen() {
        startProjection()
    }

    private fun startProjection() {
        uiHandler.post {
            try {
                mediaProjection = projectionManager.getMediaProjection(Activity.RESULT_OK, screenCapturePermissionIntent)
            } catch (exception: IllegalStateException) {
                return@post
            }
            createVirtualDisplay()
            mediaProjection?.registerCallback(MediaProjectionStopCallback(), workerHandler)
        }
    }

    private fun stopProjection() {
        workerHandler.post {
            mediaProjection?.stop()
        }
    }

    private fun createVirtualDisplay() {
        val size = Point()
        defaultDisplay.getRealSize(size)
        width = size.x
        height = size.y

        // start capture reader
        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        virtualDisplay = mediaProjection?.createVirtualDisplay("screen-capture",
                width, height, density, DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION, imageReader?.surface, null, workerHandler)
        imageReader?.setOnImageAvailableListener(ImageAvailableListener(), workerHandler)
    }

    private inner class ImageAvailableListener : ImageReader.OnImageAvailableListener {
        override fun onImageAvailable(reader: ImageReader) {
            imageReader?.setOnImageAvailableListener(null, null)

            val date: LocalDateTime = LocalDateTime.now();
            val dateString: String = date.format(SCREEN_DATE_FORMAT);
            val packageId = lastForegroundApp(usm);
            _logger.log("lastForegroundApp", packageId);
            val filePath: String = screenshotPath + "/Screenshot_" + dateString +"_" + packageId +".jpg"
//            var bitmap: Bitmap? = null
            var croppedBitmap: Bitmap? = null

            try {
                reader.acquireLatestImage()?.use {
                    val planes = it.planes
                    val buffer = planes[0].buffer
                    val pixelStride = planes[0].pixelStride
                    val rowStride = planes[0].rowStride
                    val rowPadding = rowStride - pixelStride * width
                    var bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888)
                    bitmap?.copyPixelsFromBuffer(buffer)

                    croppedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height)

                    _logger.log("Trying to put file in location", filePath);
                    FileOutputStream(filePath).use {
                        croppedBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, it)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                croppedBitmap?.recycle()
//                bitmap?.recycle()

                stopProjection()
            }

            uiHandler.post { screenCaptureListener.onScreenShotTaken(filePath) }
        }
    }

    private inner class MediaProjectionStopCallback : MediaProjection.Callback() {
        override fun onStop() {
            workerHandler.post {
                virtualDisplay?.release()
                imageReader?.setOnImageAvailableListener(null, null)
                mediaProjection?.unregisterCallback(this@MediaProjectionStopCallback)
            }
        }
    }
}
