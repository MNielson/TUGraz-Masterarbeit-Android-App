package com.example.matthias.myapplication.SyllableDetector;

import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import java.util.Arrays;

import autovalue.shaded.org.apache.commons.lang.ArrayUtils;

public class SyllableDetector {
    HandlerThread myThread;
    Looper mLooper;
    MyHandler mHandler;


    public SyllableDetector(SyllableDetectorWorker syllableDetectorWorker, SyllableDetectorConfig config){
        this( "Syllable Detector Thread", syllableDetectorWorker, config);
    }

    public SyllableDetector(String name, SyllableDetectorWorker syllableDetectorWorker, SyllableDetectorConfig config){
        myThread = new HandlerThread(name);
        myThread.start();
        mLooper = myThread.getLooper();
        mHandler = new MyHandler(mLooper, syllableDetectorWorker, config);
    }

    public void process(short[] content){
        Message msg = mHandler.obtainMessage();
        msg.obj = Arrays.asList(ArrayUtils.toObject(content));
        mHandler.sendMessage(msg);
    }

}
