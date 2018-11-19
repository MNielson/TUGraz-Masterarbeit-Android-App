package com.example.matthias.myapplication.SyllableDetector;

import android.content.Context;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.example.matthias.myapplication.FolderToAnalyze;
import com.example.matthias.myapplication.SyllableDetector.MyHandler;
import com.example.matthias.myapplication.SyllableDetector.SyllableDetector;

public class Foo {
    HandlerThread myThread;
    Looper mLooper;
    MyHandler mHandler;


    public Foo(Context context, SyllableDetector syllableDetector){
        myThread = new HandlerThread("Unnamed Worker Thread");
        myThread.start();
        mLooper = myThread.getLooper();
        mHandler = new MyHandler(mLooper, context, syllableDetector);
    }

    public Foo(String name, Context context, SyllableDetector syllableDetector){
        myThread = new HandlerThread(name);
        myThread.start();
        mLooper = myThread.getLooper();
        mHandler = new MyHandler(mLooper, context, syllableDetector);
    }


    public void sendMessage(FolderToAnalyze f){
        Message msg = mHandler.obtainMessage();
        msg.obj = f;
        mHandler.sendMessage(msg);
    }
}
