package com.frankenstein.screenx.helper;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;

import com.frankenstein.screenx.ScreenXApplication;
import com.frankenstein.screenx.database.DatabaseManager;
import com.frankenstein.screenx.database.ScreenShotDatabase;
import com.frankenstein.screenx.database.ScreenShotEntity;
import com.frankenstein.screenx.models.Screenshot;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

import static com.frankenstein.screenx.Constants.DB_THREAD_NAME;

public class TextHelper {
    private static TextHelper _mInstance;
    public static TextHelper getInstance() {
        return _mInstance;
    }

    public static void init(Context context) {
        if (_mInstance != null)
            return;
        _mInstance = new TextHelper(context);
    }

    private TextRecognizer _mOCRClient;
    private ScreenShotDatabase _mDBClient;
    private Context _mContext;
    private Logger _mLogger;
    private Handler _mHandler;
    private HandlerThread _mThread;
    private Map<String, String> _mCache = new HashMap<>();

    private TextHelper(Context context) {
        _mOCRClient = TextRecognition.getClient();
        _mContext = context;
        _mLogger = Logger.getInstance("TextHelper");
        _mDBClient = DatabaseManager.getInstance(_mContext);
         _mThread = new HandlerThread(DB_THREAD_NAME);
         _mThread.start();
         _mHandler = new Handler(_mThread.getLooper());
    }

    public void getDataForUIUpdate(File file, TextHelperListener listener) {
        _mHandler.post(() -> {
            String filename = file.getName();
            String text = _mCache.get(filename);
            if (text != null) {
                _mLogger.log("Found the data in Cache", filename);
                listener.onTextFetched(text);
            } else {
                String dataFromDB = textByFilenameDB(filename);
                if (dataFromDB != null ) {
                    _mLogger.log("Fetched the data from DB", filename);
                    _mCache.put(filename, dataFromDB);
                    listener.onTextFetched(dataFromDB);
                } else {
                    textByFileNameOCR(file, (String ocrText) -> {
                        // THIS IS RUNNING IN MAIN/UI THREAD
                        _mLogger.log("Scanned the data using OCR", filename);
                        // AS OCR CALLBACKS ARE RUN ON MAIN/UI THREAD, DB OPERATIONS NEED TO BE POSTED ON TO SEPARATE THREAD
                        _mHandler.post(() -> {
                            putScreenIntoDB(filename, ocrText);
                            _mCache.put(filename, ocrText);
                        });
                        // AS OCR CALLBACKS ARE RUN ON MAIN/UI THREAD, DIRECTLY INVOKING THE UI LISTENER HERE IS OKAY
                        listener.onTextFetched(ocrText);
                    });
                }
            }
        });
    }

    public String getUnParsedScreenshots() {
        List<ScreenShotEntity> parsedScreenList = _mDBClient.screenShotDao().getAll();
        ArrayList<Screenshot> allScreens = ScreenXApplication.screenFactory.screenshots.getValue();
        return null;
    }

    public String textByFilenameDB(String filename) {
        ScreenShotEntity screen = _mDBClient.screenShotDao().getScreenShotByName(filename);
        if (screen == null)
            return null;
        return screen.textContent;
    }

    public boolean putScreenIntoDB(String filename, String text) {
        final ScreenShotEntity screen = _mDBClient.screenShotDao().getScreenShotByName(filename);
        if (screen == null) {
            _mLogger.log("Inserting Data into DB", filename);
            _mDBClient.screenShotDao().putScreenShot(filename, text, "");
            return true;
        }
        return false;
    }

    public void textByFileNameOCR(File file, TextHelperListener listener) {
        try {
            InputImage image = InputImage.fromFilePath(_mContext, Uri.fromFile(file));
            // PROCESS IMAGE IS RUN ON DIFFERENT THREAD
            Task<Text> result = _mOCRClient.process(image)
                    // BUT THE SUCCESS AND FAILURE LISTENERS ARE RUN ON MAIN THREAD
                    .addOnSuccessListener(new OnSuccessListener<Text>() {
                        @Override
                        public void onSuccess(Text text) {
                            String output = text.getText();
                            _mLogger.log("OCR Task Completed Succesfully for file", file.getAbsolutePath());
                            listener.onTextFetched(output);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            _mLogger.log("OCR Failed to get data from image", file.getAbsolutePath());
                        }
                    });
        } catch (IOException e) {
            _mLogger.log("OCR: Failed to get data from image", file.getAbsolutePath());
        }
    }

    public interface TextHelperListener {
        public void onTextFetched(String text);
    }
}
