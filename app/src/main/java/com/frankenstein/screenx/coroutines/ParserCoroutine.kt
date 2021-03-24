package com.frankenstein.screenx.coroutines

import com.frankenstein.screenx.ScreenXApplication
import com.frankenstein.screenx.helper.Logger
import com.frankenstein.screenx.models.Screenshot
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class ParserCoroutine() : CoroutineScope {
    companion object {
        private val _logger = Logger.getInstance("ParserCoroutine");
    }

    private val parentJob = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + parentJob

    fun start() {
        launch {
            _logger.log("Starting Parse")
            var unparsedScreenshots =  ScreenXApplication.textHelper.unParsedScreenshots
            unparsedScreenshots.forEachIndexed{i, screen ->
                _logger.log("processing index", i, unparsedScreenshots.size)
                scanAndSave(screen)
            }
        }
    }

    suspend fun scanAndSave(screen: Screenshot) {
        var text = scanText(screen)
        saveToDatabase(screen.name, text)
    }

    suspend fun saveToDatabase(filename: String, text: String) {
        withContext(Dispatchers.IO) {
            ScreenXApplication.textHelper.putScreenIntoDB(filename, text);
        }
    }

    suspend  fun scanText(screen: Screenshot): String {
     return suspendCoroutine { continuation ->
            ScreenXApplication.textHelper.textByFileOCR(screen.file) { file, text ->
                continuation.resume(text)
            }
        }
    }

    fun stop() {
        parentJob.cancel()
    }
}
