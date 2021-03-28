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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import static com.frankenstein.screenx.Constants.DB_THREAD_NAME;

public class TextHelper {
    private static TextHelper _mInstance;
    public static TextHelper getInstance() {
        return _mInstance;
    }

    public static TextHelper init(Context context) {
        if (_mInstance != null)
            return _mInstance;
        _mInstance = new TextHelper(context);
        return _mInstance;
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
         // As this class is a singleton that lives as long as application is alive,
         // adding observer Forever
         ScreenXApplication.screenFactory.screenshots.observeForever(this::syncFromUI);
    }

    public void getDataForUIUpdate(File file, TextHelperListener listener) {
        _mHandler.post(() -> {
            String filename = file.getName();
            String text = _mCache.get(filename);
            if (text != null) {
                _mLogger.log("Found the data in Cache", filename);
                // (TODO) This is wrong invoking UI Listener on background thread
                listener.onTextFetched(file, text);
            } else {
                String dataFromDB = textByFilenameDB(filename);
                if (dataFromDB != null ) {
                    _mLogger.log("Fetched the data from DB", filename);
                    _mCache.put(filename, dataFromDB);
                    listener.onTextFetched(file, dataFromDB);
                } else {
                    textByFileOCR(file, (File _file, String ocrText) -> {
                        // THIS IS RUNNING IN MAIN/UI THREAD
                        _mLogger.log("Scanned the data using OCR", filename);
                        // AS OCR CALLBACKS ARE RUN ON MAIN/UI THREAD, DB OPERATIONS NEED TO BE POSTED ON TO SEPARATE THREAD
                        _mHandler.post(() -> {
                            putScreenIntoDB(filename, ocrText);
                        });
                        // AS OCR CALLBACKS ARE RUN ON MAIN/UI THREAD, DIRECTLY INVOKING THE UI LISTENER HERE IS OKAY
                        listener.onTextFetched(_file, ocrText);
                    });
                }
            }
        });
    }

    public ArrayList<Screenshot> getUnParsedScreenshots() {
        List<ScreenShotEntity> parsedScreenList = _mDBClient.screenShotDao().getAll();
        _mLogger.log("Total Screenshots in database", parsedScreenList.size());
        Set<String> parsedScreens = new HashSet<String>();
        for(ScreenShotEntity screen: parsedScreenList) {
            parsedScreens.add(screen.filename);
        }
        ArrayList<Screenshot> allScreens = ScreenXApplication.screenFactory.screenshots.getValue();
        ArrayList<Screenshot> unParsedScreens = new ArrayList<>();
        if (allScreens == null)
            return unParsedScreens;
        for(Screenshot screen: allScreens) {
            if (!parsedScreens.contains(screen.name)) {
                unParsedScreens.add(screen);
            }
        }
        Collections.sort(unParsedScreens, new Comparator<Screenshot>() {
            @Override
            public int compare(Screenshot s1, Screenshot s2) {
                long result = (s2.lastModified - s1.lastModified);
                int output = (result >=0 ) ? 1: -1;
                return output;
            }
        });
        _mLogger.log("UnParsedScreenshots length", unParsedScreens.size());
        return unParsedScreens;
    }

    public LiveData<ArrayList<String>> searchScreenshots(String text) {
        MutableLiveData<ArrayList<String>> livescreens = new MutableLiveData<>();
        // This method is invoked by Main Thread, so we need to post the database operations
        // on to a separate thread
        _mHandler.post(() -> {
            // LIKE uses %query% format for pattern matching and
            // MATCH uses *query* format for pattern matching
            List<ScreenShotEntity> matchedList = _mDBClient.screenShotDao().findByContent("*"+text+"*");
            ArrayList<String> screens = new ArrayList<>();
            _mLogger.log("Total matched screenshots by search = ", matchedList.size());
            for (int i = 0; i < matchedList.size(); i++) {
                ScreenShotEntity item = matchedList.get(i);
                if (ScreenXApplication.screenFactory.findScreenByName(item.filename) != null)
                    screens.add(item.filename);
            }
            Collections.sort(screens, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    Screenshot s1 = ScreenXApplication.screenFactory.findScreenByName(o1);
                    Screenshot s2 = ScreenXApplication.screenFactory.findScreenByName(o2);
                    long result = (s2.lastModified - s1.lastModified);
                    int output = (result >=0 ) ? 1: -1;
                    return output;
                }
            });
            livescreens.postValue(screens);
        });
        return livescreens;
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
            _mCache.put(filename, text);
            return true;
        } else {
            _mDBClient.screenShotDao().updateScreenShotText(filename, text);
            return true;
        }
    }

    public void deleteScreenshotListFromUI(ArrayList<String> deleteList) {
        _mHandler.post(() -> {
            _mDBClient.screenShotDao().deleteMultipleScreenShots(deleteList.toArray(new String[deleteList.size()]));
        });
    }

    public void deleteScreenshotFromUI(String filename) {
        _mHandler.post(() -> {
            _mDBClient.screenShotDao().deleteScreenShot(filename);
        });
    }

    public void syncFromUI(ArrayList<Screenshot> screensOnDevice) {
        if (screensOnDevice.size() == 0) {
            // There must be an issue with storage permissions or some rogue scenario when this happens, so skipping it
            // On the other hand, if this is a genuine case, that the user actually deleted all the screenshots on their
            // phone, then that data will still be retained until a new screenshot appears, when all the old data will be deleted
            _mLogger.log("ScreensOnDevice is zero!!, has something gone wrong?");
            return;
        }
        _mHandler.post(() -> {
            List<ScreenShotEntity> screensOnDatabase = _mDBClient.screenShotDao().getAll();
            ArrayList<String> toBeDeleted = new ArrayList<>();
            for(ScreenShotEntity entity: screensOnDatabase) {
                String filename = (entity.filename);
                if (ScreenXApplication.screenFactory.findScreenByName(filename) == null) {
                    toBeDeleted.add(filename);
                }
            }
            for (String filename: toBeDeleted) {
                _mLogger.log("to be deleted", filename);
            }
            _mDBClient.screenShotDao().deleteMultipleScreenShots(toBeDeleted.toArray(new String[toBeDeleted.size()]));
        });
    }

    public void textByFileOCR(File file, TextHelperListener listener) {
        try {
            if (file == null) {
                _mLogger.log("got null for file", file);
                return;
            }
//            _mLogger.log("OCR Starting for", file.getName());
            InputImage image = InputImage.fromFilePath(_mContext, Uri.fromFile(file));
            // PROCESS IMAGE IS RUN ON DIFFERENT THREAD
            Task<Text> result = _mOCRClient.process(image)
                    // BUT THE SUCCESS AND FAILURE LISTENERS ARE RUN ON MAIN THREAD
                    .addOnSuccessListener(new OnSuccessListener<Text>() {
                        @Override
                        public void onSuccess(Text text) {
                            String output = text.getText();
//                            _mLogger.log("OCR Task Completed Succesfully for file", file.getName());
                            listener.onTextFetched(file, output);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            _mLogger.log("OCR Failed to get data from image", file.getAbsolutePath());
                        }
                    });
        } catch (Exception e) {
            _mLogger.log("OCR: Failed to get data from image", file.getAbsolutePath());
        }
    }

    public interface TextHelperListener {
        public void onTextFetched(File file, String text);
    }
}
