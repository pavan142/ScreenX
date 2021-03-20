package com.frankenstein.screenx.helper;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;

import com.frankenstein.screenx.database.DatabaseManager;
import com.frankenstein.screenx.database.ScreenShotDatabase;
import com.frankenstein.screenx.database.ScreenShotEntity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;

import static com.frankenstein.screenx.Constants.DB_THREAD_NAME;

public class TextHelper {
    private static TextHelper _mTextHelper;
    public static TextHelper getInstance(Context context) {
        if (_mTextHelper == null)
            _mTextHelper = new TextHelper(context);
        return _mTextHelper;
    }

    private TextRecognizer _mOCRClient;
    private ScreenShotDatabase _mDBClient;
    private Context _mContext;
    private Logger _mLogger;
    private Handler _mHandler;
    private HandlerThread _mThread;
    private TextHelper(Context context) {
        _mOCRClient = TextRecognition.getClient();
        _mContext = context;
        _mLogger = Logger.getInstance("FILES-TEXT-HELPER");
        _mDBClient = DatabaseManager.getInstance(_mContext);
         _mThread = new HandlerThread(DB_THREAD_NAME);
         _mThread.start();
         _mHandler = new Handler(_mThread.getLooper());
    }

    public void getData(File file, TextHelperListener listener) {
        _mHandler.post(() -> {
            String filename = file.getName();
            ScreenShotEntity sso = _mDBClient.screenShotDao().getScreenShotByName(filename);
            if (sso !=null ) {
                _mLogger.log("Successfully got data from DB", filename);
                listener.onTextFetched(sso.textContent);
            } else {
                getDataFromOCR(file, (String text) -> {
                    _mLogger.log("Successfully got data from OCR", filename);
                    _mHandler.post(() -> {
                        final ScreenShotEntity new_sso = _mDBClient.screenShotDao().getScreenShotByName(filename);
                        if (new_sso == null) {
                            _mLogger.log("INSERTING got data from DB", filename);
                            _mDBClient.screenShotDao().putScreenShot(filename, text, "");
                        }
                        listener.onTextFetched(text);
                    });
                });
            }
        });
    }

    public void getDataFromOCR(File file, TextHelperListener listener) {
        try {
            InputImage image = InputImage.fromFilePath(_mContext, Uri.fromFile(file));
            Task<Text> result = _mOCRClient.process(image)
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
