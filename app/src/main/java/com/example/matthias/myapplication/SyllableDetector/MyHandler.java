package com.example.matthias.myapplication.SyllableDetector;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.example.matthias.myapplication.AnalyzedFile;
import com.example.matthias.myapplication.BuildConfig;
import com.example.matthias.myapplication.FolderToAnalyze;
import com.example.matthias.myapplication.HelperFunctions;
import com.example.matthias.myapplication.JsonDataWriter;
import com.example.matthias.myapplication.SyllableDetector.SyllableDetector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MyHandler extends Handler {
    private static final String LOG_TAG = "MyHandler";
    private ArrayList<AnalyzedFile> analyzedFiles = new ArrayList<>();
    private Context mcontext;
    private SyllableDetector msyllableDetector;

    public MyHandler(Looper myLooper, Context context, SyllableDetector syllableDetector) {
        super(myLooper);
        mcontext = context;
        msyllableDetector = syllableDetector;
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void handleMessage(Message msg) {
        FolderToAnalyze f = (FolderToAnalyze) msg.obj;
        ArrayList<Uri> files = f.getfiles();
        int i = 0;
        List<SyllableResult> results = new ArrayList<>();
        for(Uri file : files){
            try {
                i++;
                Log.d(LOG_TAG, "Analyzing file " + Integer.toString(i) + "/" + Integer.toString(files.size()));
                InputStream is = mcontext.getContentResolver().openInputStream(file);
                SyllableResult s = msyllableDetector.getNumPeaks(is, HelperFunctions.getFileID(file));
                results.add(s);
                analyzedFiles.add(new AnalyzedFile(HelperFunctions.getFileID(file), s.numSyllables));
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(isExternalStorageWritable())
        {
            String dirName = "someDir";
            // Get the directory for the user's public Downloads directory.
            File folder = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), dirName);
            if (!folder.mkdirs()) {
                Log.d(LOG_TAG, "Directory not created");
            }
            try {
                File file = new File(folder, f.getoutFileName() + ".json");
                FileOutputStream fOut = new FileOutputStream(file);
                JsonDataWriter.write(fOut, analyzedFiles);
                if(BuildConfig.DEBUG){
                    File debugInfoFile = new File(folder, f.getoutFileName() +"_debug.json");
                    FileOutputStream fOutDebug = new FileOutputStream(debugInfoFile);
                    SyllableDebugJsonWriter.write(fOutDebug, results);
                    Log.d(LOG_TAG, "Finished writing debug JSON");
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
            Log.e(LOG_TAG, "Can't write to external storage");

    }

    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}