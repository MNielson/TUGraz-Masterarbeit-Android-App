package com.example.matthias.myapplication;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.util.Arrays;

/**
 * Created by Matthias on 20.03.2018.
 */


public class AudioWorker extends HandlerThread {

    private Handler mWorkerHandler;
    private short[] audioBuffer;
    private PitchDetector mPitchDetector;
    private int doublesPerSample = 1024;

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
                double pitch = mPitchDetector.computePitch(sample, 0, Math.min(1024, sample.length));
                Log.d("PitchDetectorResult", "Calculated a pitch of "+ Double.toString(pitch) +" hz.");
                //TODO: report result of pitchdetection somehow
            }
        };
        postTask(task);
    }

    public void processSamples(final double[] samples)
    {
        double[] bar = Arrays.copyOfRange(samples, 1024 * 200, samples.length);

        int len = samples.length;
        int numSamples = (int) Math.ceil(len / doublesPerSample);
        for(int i = 0; i < numSamples; i++)
        {
            final int startSample = doublesPerSample * i;
            final int remainingSamples = len - startSample;

            Runnable task = new Runnable() {
                @Override
                public void run() {
                    double pitch = mPitchDetector.computePitch(samples, startSample, Math.min(1024, remainingSamples));
                    Log.d("PitchDetectorResult", "Calculated a pitch of "+ Double.toString(pitch) +" hz.");
                }
            };
            postTask(task);
        }


    }


    public void prepareHandler(){
        mWorkerHandler = new Handler(getLooper());
    }
}
