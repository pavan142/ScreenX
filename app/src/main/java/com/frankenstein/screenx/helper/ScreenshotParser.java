package com.frankenstein.screenx.helper;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.frankenstein.screenx.Utils;
import com.frankenstein.screenx.coroutines.ParserCoroutine;

public class ScreenshotParser {

    private static ScreenshotParser _instance;
    public static final String HANDLER_THREAD_NAME="screenshotparser-handler-thread";

    private static final Logger _mLogger = Logger.getInstance("ScreenshotParser");
    private Handler _mHandler;
    private HandlerThread _mHandlerThread;
    public static ScreenshotParser init(Context context) {
        ScreenshotParser._instance = new ScreenshotParser(context);
        return ScreenshotParser._instance;
    }

    public static ScreenshotParser getInstance() {
        return ScreenshotParser._instance;
    }

    Context _mContext;
    ParserCoroutine _mParserCoroutine;

    public ScreenshotParser(Context context) {
        this._mContext = context;
        _mHandlerThread = new HandlerThread(HANDLER_THREAD_NAME);
        _mHandlerThread.start();
        _mHandler = new Handler(_mHandlerThread.getLooper());
        _mLogger.log("creating parser coroutine", Utils.getInstance().timePassed());
        ParserCoroutine parserCoroutine = new ParserCoroutine();
        parserCoroutine.start();
        parserCoroutine.resume();
    }
}
