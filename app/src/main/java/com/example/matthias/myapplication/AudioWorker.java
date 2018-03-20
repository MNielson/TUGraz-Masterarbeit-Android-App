package com.example.matthias.myapplication;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

/**
 * Created by Matthias on 20.03.2018.
 */


public class AudioWorker extends HandlerThread {

    private Handler mWorkerHandler;
    private short[] audioBuffer;
    private PitchDetector mPitchDetector;

    //TODO: figure out where pitch detection should happen

    public AudioWorker(String name, PitchDetector pd) {
        super(name);
        mPitchDetector = pd;
    }

    private void postTask(Runnable task){
        mWorkerHandler.post(task);
    }

    public void processSample(final double[] sample)
    {
        //generate a task
        //post task
        Runnable task = new Runnable() {
            @Override
            public void run() {
                double pitch = mPitchDetector.computePitch(sample, 0, sample.length);
                Log.d("PitchDetectorResult", "Calculated a pitch of "+ Double.toString(pitch) +" hz.");
                //TODO: report result of pitchdetection somehow
            }
        };
        postTask(task);
    }

    public void processAllSamples()
    {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                //TODO: do work with samples
                //TODO: pitch detection
            }
        };
        postTask(task);
    }


    public void prepareHandler(){
        mWorkerHandler = new Handler(getLooper());
    }
}
