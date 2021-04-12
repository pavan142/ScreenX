package com.frankenstein.screenx.coroutines

import androidx.lifecycle.Observer
import com.frankenstein.screenx.ScreenXApplication
import com.frankenstein.screenx.Utils
import com.frankenstein.screenx.helper.Logger
import com.frankenstein.screenx.models.Screenshot
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlin.collections.ArrayList
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class ParserCoroutine(): CoroutineScope {
    private val _logger = Logger.getInstance("ParserCoroutine");
    private val lifeCycleJob = Job()

    private var outgoing_mail: SendChannel<Unit>? = null
    private var screenshotschangedObserver = Observer<ArrayList<Screenshot>> {
        launch {
            queueAScan()
        }
    }

    // Default Dispatcher, because CPU intensive operations are run here
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + lifeCycleJob


    fun start() {
        _logger.log(Utils.timePassed(), "Starting")
        ScreenXApplication.screenFactory.screenshots.observeForever(screenshotschangedObserver)
    }

    fun resume() {
        _logger.log(Utils.timePassed(), "Resuming")
        outgoing_mail = actor (capacity = Channel.CONFLATED) {
            var incoming_mail = this.channel

            // Processing incoming mail
            for (msg in incoming_mail) {
                _logger.log(Utils.timePassed(), "Received an incoming mail")
                processUnParsedScreens()
            }
        }
    }

    fun pause() {
        ScreenXApplication.screenFactory.screenshots.removeObserver(screenshotschangedObserver)
        outgoing_mail?.close()
    }

    fun stop() {
        // There is supposed to be only one instance of parsercoroutine(a singleton) always alive during
        // the application lifecycle, so this code is only for decoration purposes and is never reached
        lifeCycleJob.cancel()
    }

    suspend fun processUnParsedScreens() {
        var unparsedScreenshots =  ScreenXApplication.textHelper.unParsedScreenshots
        _logger.log(Utils.timePassed(), "processing unparsed screens:: ", unparsedScreenshots.size)
        unparsedScreenshots.forEachIndexed{i, screen ->
            _logger.log("processing index", i, unparsedScreenshots.size)
            scanAndSave(screen)
        }
    }

    suspend fun queueAScan() {
        _logger.log(Utils.timePassed(), "Queuing a Scan")
        outgoing_mail?.send(Unit)
    }

    suspend fun scanAndSave(screen: Screenshot) {
        var text = scanText(screen)
        saveToDatabase(screen.name, text)
    }

    suspend fun saveToDatabase(filename: String, text: String) {
        withContext(Dispatchers.IO) {
            ScreenXApplication.textHelper.updateScreenText(filename, text);
        }
    }

    suspend  fun scanText(screen: Screenshot): String {
     return suspendCoroutine { continuation ->
            ScreenXApplication.textHelper.textByFileOCR(screen.file) { file, text ->
                continuation.resume(text)
            }
        }
    }
}
