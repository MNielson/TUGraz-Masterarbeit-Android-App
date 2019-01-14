package com.example.matthias.myapplication;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.util.LinkedList;

/**
 * Created by Matthias on 20.03.2018.
 */

/*
public class AudioWorker extends HandlerThread {

    private Handler mWorkerHandler;
    private short[] audioBuffer;
    private PitchDetector mPitchDetector;
    private int doublesPerSample = 1024;
    //private Lock pitchResultLock;
    private LinkedList<Double> pitchResults;
    final String LOG_TAG = "AudioWorker";


    public AudioWorker(String name, PitchDetector pd) {
        super(name);
        mPitchDetector = pd;
        pitchResults = new LinkedList();
        //pitchResultLock = new ReentrantLock();
    }

    private void postTask(Runnable task){
        mWorkerHandler.post(task);
    }

    public double computePitch(Signal signal)
    {

        //unbox samples
        int len = signal.getSignal().length;
        double[] primSamples = new double[len];
        for(int i = 0; i < len; i++)
            primSamples[i] = signal.getSignal()[i].doubleValue();
        double pitch = mPitchDetector.computePitch(primSamples, 0, signal.getSignal().length);
        return pitch;
    }

    public double computePitch(short[] content)
    {
        int len = content.length;
        double[] primSamples = new double[len];
        for(int i = 0; i < len; i++)
            primSamples[i] = (double) content[i];
        double pitch = mPitchDetector.computePitch(primSamples, 0, len);
        Log.d(LOG_TAG, "Detected " + Double.toString(pitch) + "hz pitch");
        return pitch;
    }

    public LinkedList<Double> computePitches(final Double[] samples)
    {
        pitchResults.clear();
        int len = samples.length;
        int numSamples = (int) Math.ceil(len / doublesPerSample);

        //unbox samples
        double[] primSamples = new double[samples.length];
        for(int i = 0; i < samples.length; i++)
            primSamples[i] = samples[i].doubleValue();

        for(int i = 0; i < numSamples; i++)
        {
            int startSample = doublesPerSample * i;
            int remainingSamples = len - startSample;

            double pitch = mPitchDetector.computePitch(primSamples, startSample, Math.min(1024, remainingSamples));
            pitchResults.add(pitch);
        }
        return pitchResults;

    }


    public void prepareHandler(){
        mWorkerHandler = new Handler(getLooper());
    }
}
*/