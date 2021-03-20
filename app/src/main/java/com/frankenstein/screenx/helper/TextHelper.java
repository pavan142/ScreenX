package com.frankenstein.screenx.helper;

import android.content.Context;
import android.net.Uri;

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

public class TextHelper {
    private static TextHelper _mTextHelper;
    public static TextHelper getInstance(Context context) {
        if (_mTextHelper == null)
            _mTextHelper = new TextHelper(context);
        return _mTextHelper;
    }

    private TextRecognizer _mClient;
    private Context _mContext;
    private Logger _mLogger;
    private TextHelper(Context context) {
        _mClient = TextRecognition.getClient();
        _mContext = context;
        _mLogger = Logger.getInstance("FILES-OCR");
    }

    public void getData(File file) {
        try {
            InputImage image = InputImage.fromFilePath(_mContext, Uri.fromFile(file));
            Task<Text> result = _mClient.process(image)
                    .addOnSuccessListener(new OnSuccessListener<Text>() {
                        @Override
                        public void onSuccess(Text text) {
                            _mLogger.log("Task Completed Succesfully for file", file.getAbsolutePath());
                            _mLogger.log("the data is", text.getText());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            _mLogger.log("Task failed for file", file.getAbsolutePath());
                        }
                    });
        } catch (IOException e) {
            _mLogger.log("Failed to get data from image", file.getAbsolutePath());
        }
    }
}
