package com.example.matthias.myapplication.BufferProcessor;

import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;



import java.util.Arrays;

import autovalue.shaded.org.apache.commons.lang.ArrayUtils;

public class BufferProcessor {
    HandlerThread myThread;
    Looper mLooper;
    MyBufferHandler mHandler;


    public BufferProcessor(){
        this( "Buffer Handling Thread");
    }

    public BufferProcessor(String name){
        myThread = new HandlerThread(name);
        myThread.start();
        mLooper = myThread.getLooper();
        mHandler = new MyBufferHandler(mLooper);
    }

    public void process(short[] content){
        Message msg = mHandler.obtainMessage();
        msg.obj = Arrays.asList(ArrayUtils.toObject(content));
        mHandler.sendMessage(msg);
    }

}
