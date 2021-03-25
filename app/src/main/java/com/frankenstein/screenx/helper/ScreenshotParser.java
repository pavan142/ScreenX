package com.frankenstein.screenx.helper;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.frankenstein.screenx.coroutines.ParserCoroutine;

public class ScreenshotParser {

    private static ScreenshotParser _instance;
    public static final String HANDLER_THREAD_NAME="screenshotparser-handler-thread";

    private static final Logger _mLogger = Logger.getInstance("ScreenshotParser");
    private Handler _mHandler;
    private HandlerThread _mHandlerThread;
    public static void init(Context context) {
        ScreenshotParser._instance = new ScreenshotParser(context);
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
        _mParserCoroutine = new ParserCoroutine();
        _mHandler.postDelayed(() -> {
            _mParserCoroutine.start();
        }, 12000);
    }
}
