package com.example.matthias.myapplication.BufferProcessor;

import android.app.Activity;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import java.util.Arrays;

import autovalue.shaded.org.apache.commons.lang.ArrayUtils;

public class BufferProcessor {
    HandlerThread myThread;
    Looper mLooper;
    MyBufferHandler mHandler;


    public BufferProcessor(Activity activity){
        this("Buffer Handling Thread", activity);
    }

    public BufferProcessor(String name, Activity activity){
        myThread = new HandlerThread(name);
        myThread.start();
        mLooper = myThread.getLooper();
        mHandler = new MyBufferHandler(mLooper, activity);
    }

    public void process(short[] content){
        Message msg = mHandler.obtainMessage();
        msg.obj = Arrays.asList(ArrayUtils.toObject(content));
        mHandler.sendMessage(msg);
    }

}
