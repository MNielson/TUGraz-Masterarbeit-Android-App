package com.example.matthias.myapplication.SyllableDetector;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.example.matthias.myapplication.BuildConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import autovalue.shaded.org.apache.commons.lang.ArrayUtils;

import static com.example.matthias.myapplication.MainActivity.SAMPLE_RATE;

public class MyHandler extends Handler {
    private static final String LOG_TAG = "MyHandler";
    private SyllableDetectorWorker msyllableDetectorWorker;
    private SyllableDetectorConfig mconfig;
    List<ArrayList<Double>> filteredBuffers;
    private int filteredElements;
    private int debugFileCounter;
    public MyHandler(Looper myLooper, SyllableDetectorWorker syllableDetectorWorker, SyllableDetectorConfig config) {
        super(myLooper);
        msyllableDetectorWorker = syllableDetectorWorker;
        mconfig = config;
        filteredElements = 0;
        debugFileCounter = 0;
        filteredBuffers = new ArrayList<>();
        for(int i = 0; i < config.numFilters; i++){
            filteredBuffers.add(new ArrayList<Double>());
        }
    }

    public void handleMessage(Message msg) {
        List<Short> buffer = (List<Short>) msg.obj;
        List<ArrayList<Double>> tfilteredBuffers = msyllableDetectorWorker.filterBuffer(buffer);
        for(int i = 0; i < tfilteredBuffers.size(); i++) {
            filteredBuffers.get(i).addAll(tfilteredBuffers.get(i));
        }
        filteredElements += buffer.size();
        if(BuildConfig.DEBUG)
        {
            String dirName = "debug-data";
            File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), dirName);
            if (!folder.mkdirs())
                Log.d(LOG_TAG, "Directory not created");
            try {
                File file = new File(folder, Integer.toString(debugFileCounter) + "-file-content.txt");
                FileWriter foo = new FileWriter(file, true);
                for(Short s : buffer){
                    foo.write(Short.toString(s));
                    foo.write("\n");
                }
                foo.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(filteredElements >= (SAMPLE_RATE * mconfig.secBetweenResults))
        {
            // take secBetweenResults * SAMPLE_RATE elements
            List<ArrayList<Double>> toProcess = new ArrayList<>();
            for(ArrayList<Double> l : filteredBuffers ){
                // add only samples to be processed to processinglist
                toProcess.add(new ArrayList<>(l.subList(0, SAMPLE_RATE * mconfig.secBetweenResults)));
                // remove elements added to processing list
                l.subList(0, SAMPLE_RATE * mconfig.secBetweenResults).clear();
            }
            filteredElements -= SAMPLE_RATE * mconfig.secBetweenResults;

            if(BuildConfig.DEBUG)
            {
                String dirName = "debug-data";
                File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), dirName);
                if (!folder.mkdirs())
                    Log.d(LOG_TAG, "Directory not created");
                try {
                    File file = new File(folder, Integer.toString(debugFileCounter) + "-"+ Integer.toString(toProcess.size()) +"filtered.txt");
                    FileWriter foo = new FileWriter(file);
                    for(ArrayList<Double> ld : toProcess) {
                        for(double d : ld)
                        {
                            foo.write(Double.toString(d));
                            foo.write("\n");
                        }
                    }
                    foo.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // process X seconds of filtered results
            int numPeaks = msyllableDetectorWorker.peaksFromFiltered(toProcess, mconfig.chunkSize, SAMPLE_RATE * mconfig.secBetweenResults, debugFileCounter);
            Log.d(LOG_TAG, "Detected " + Integer.toString(numPeaks) + " peaks");
            // TODO: do something with result
            debugFileCounter++;
        }
    }

    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}