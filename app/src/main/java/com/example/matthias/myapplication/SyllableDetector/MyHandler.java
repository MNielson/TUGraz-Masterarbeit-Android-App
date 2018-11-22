package com.example.matthias.myapplication.SyllableDetector;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static com.example.matthias.myapplication.MainActivity.SAMPLE_RATE;

public class MyHandler extends Handler {
    private static final String LOG_TAG = "MyHandler";
    private SyllableDetectorWorker msyllableDetectorWorker;
    private SyllableDetectorConfig mconfig;
    private int filteredElements;


    public MyHandler(Looper myLooper, SyllableDetectorWorker syllableDetectorWorker, SyllableDetectorConfig config) {
        super(myLooper);
        msyllableDetectorWorker = syllableDetectorWorker;
        mconfig = config;
        filteredElements = 0;
    }

    public void handleMessage(Message msg) {
        List<Short> buffer = (List<Short>) msg.obj;
        List<ArrayList<Double>> filteredBuffers = msyllableDetectorWorker.filterBuffer(buffer);
        filteredElements += buffer.size();

        if(filteredElements >= (SAMPLE_RATE * mconfig.secBetweenResults))
        {
            // take secBetweenResults * SAMPLE_RATE elements
            List<ArrayList<Double>> toProcess = new ArrayList<>();
            for(ArrayList<Double> l : filteredBuffers ){
                // add only samples to be processed to processinglist
                toProcess.add(new ArrayList<>(l.subList(0, SAMPLE_RATE * mconfig.secBetweenResults+1)));
                // remove elements added to processing list
                l.subList(0, SAMPLE_RATE * mconfig.secBetweenResults+1).clear();
                filteredElements -= SAMPLE_RATE * mconfig.secBetweenResults;
            }
            // process X seconds of filtered results
            int numPeaks = msyllableDetectorWorker.peaksFromFiltered(toProcess, mconfig.chunkSize, SAMPLE_RATE * mconfig.secBetweenResults);
            Log.d(LOG_TAG, "Detected " + Integer.toString(numPeaks) + " peaks");
            // TODO: do something with result
        }





        /*
        FolderToAnalyze f = (FolderToAnalyze) msg.obj;
        ArrayList<Uri> files = f.getfiles();
        int i = 0;
        List<SyllableResult> results = new ArrayList<>();
        for(Uri file : files){
            try {
                i++;
                Log.d(LOG_TAG, "Analyzing file " + Integer.toString(i) + "/" + Integer.toString(files.size()));
                InputStream is = mcontext.getContentResolver().openInputStream(file);
                SyllableResult s = msyllableDetectorWorker.syllablesFromFile(is, HelperFunctions.getFileID(file));
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
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
            Log.e(LOG_TAG, "Can't write to external storage");
        */

    }

    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}