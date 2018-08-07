package com.example.matthias.myapplication;


import java.util.ArrayList;
import java.util.List;

import uk.me.berndporr.iirj.Butterworth;

/**
 * Created by Matthias on 23.07.2018.
 */

public class Filterbank {
    private ArrayList<Butterworth> mFilters;
    private double mSampleRate;
    public Filterbank(double sampleRate){
        this.mFilters = new ArrayList<>();
        this.mSampleRate = sampleRate;

    }

    public void addFilter(int order, double centerFrequency, double widthFrequency){
        Butterworth b = new Butterworth();
        b.bandPass(order, mSampleRate, centerFrequency, widthFrequency);
        mFilters.add(b);
    }

    public ArrayList<Signal> filter(Signal in)
    {
        ArrayList<Signal> filteredResults = new ArrayList<>();
        for (Butterworth bandpass : mFilters)
        {
            ArrayList<Double> filtered = new ArrayList<>();

            for(int i = 0; i < in.getSignal().length; i++){
                filtered.add(bandpass.filter(in.getSignal()[i]));
            }
            Signal sig = new Signal(filtered);
            filteredResults.add(sig);
        }
        return filteredResults;
    }
}
