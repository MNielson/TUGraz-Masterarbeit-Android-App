package com.example.matthias.myapplication;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Matthias on 20.03.2018.
 */


public class AudioWorker extends HandlerThread {

    private Handler mWorkerHandler;
    private short[] audioBuffer;
    private PitchDetector mPitchDetector;
    private int doublesPerSample = 1024;
    private Lock pitchResultLock;
    private LinkedList<Double> pitchResults;


    public AudioWorker(String name, PitchDetector pd) {
        super(name);
        mPitchDetector = pd;
        pitchResults = new LinkedList();
        pitchResultLock = new ReentrantLock();
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
            }
        };
        postTask(task);
    }

    public LinkedList<Double> processSamples(final double[] samples)
    {
        pitchResults.clear();
        int len = samples.length;
        int numSamples = (int) Math.ceil(len / doublesPerSample);
        for(int i = 0; i < numSamples; i++)
        {
            final int startSample = doublesPerSample * i;
            final int remainingSamples = len - startSample;
            double pitch = mPitchDetector.computePitch(samples, startSample, Math.min(1024, remainingSamples));
            pitchResults.add(pitch);
/*
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    // TODO: Figure out if we're losing samples or racing somewhere
                    pitchResultLock.lock();
                    double pitch = mPitchDetector.computePitch(samples, startSample, Math.min(1024, remainingSamples));

                    pitchResultLock.unlock();
                    //Log.d("PitchDetectorResult", "Calculated a pitch of "+ Double.toString(pitch) +" hz from " + Math.min(1024, remainingSamples) + " samples.");
                }
            };
            postTask(task);
*/
        }
        return pitchResults;

    }


    public void prepareHandler(){
        mWorkerHandler = new Handler(getLooper());
    }
}
