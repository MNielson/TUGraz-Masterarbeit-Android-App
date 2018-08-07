package com.example.matthias.myapplication;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Filter;

/**
 * Created by Matthias on 13.07.2018.
 */

public class SyllableDetector {
    //private List<Double> pitches;
    private Double delta;
    private Filterbank mFilterbank;

    public void setmChunkSize(int chunkSize) {
        this.mChunkSize = chunkSize;
    }

    private int mChunkSize = 4410;

    public SyllableDetector(Double delta, Filterbank filterbank) {
        this.delta = delta;
        this.mFilterbank = filterbank;
        //pitches = new ArrayList<>();
    }

    public SyllableDetector(Filterbank filterbank) {
        this.delta = 0.0d;
        this.mFilterbank = filterbank;
    }

    public void setDelta(Double delta) {
        this.delta = delta;
    }

    private int countPeaks(Signal sig)
    {
        Pair<List<Double>, List<Double>> peaks = detectPeaks(sig);
        return peaks.first.size();
    }

    private Pair< List<Double>, List<Double> > detectPeaks(Signal sig)
    {
        List<Double> maxima = new ArrayList<>();
        List<Double> minima = new ArrayList<>();

        Double maximum = null;
        Double minimum = null;

        boolean lookForMax = true;

        for (Double d : sig.getSignal()) {
            if (maximum == null || d > maximum) {
                maximum = d;
            }

            if (minimum == null || d < minimum) {
                minimum = d;
            }

            if (lookForMax) {
                if (d < maximum - delta) {
                    maxima.add(d);
                    minimum = d;
                    lookForMax = false;
                }
            } else {
                if (d > minimum + delta) {
                    minima.add(d);
                    maximum = d;
                    lookForMax = true;
                }
            }
        }

        return new Pair(maxima, minima);
    }


    private Signal computeTrajectory(ArrayList<Signal> sigs)
    {
        int N = sigs.size();
        Double M = N * (N-1) * 0.5;

        Double[] bla = new Double[sigs.get(0).getSignal().length];


        for(int k = 0; k < bla.length; k++)
        {
            Double foo = 0.0;
            for (int i = 1; i < N-1; i++)
            {
                for (int j = i+1; j < N; j++)
                {
                    foo += sigs.get(i).getSignal()[k] * sigs.get(j).getSignal()[k];
                }
            }
            bla[k] = foo / M;
        }
        Signal bar = new Signal(bla);
        return bar;

    }

    public int countSyllables(Signal sig)
    {
        ArrayList<Signal> filteredSignals = mFilterbank.filter(sig);
        ArrayList<Signal> energySigs = new ArrayList<>();
        for(Signal fSig : filteredSignals)
        {
            energySigs.add(fSig.computeEnergyInChunks(mChunkSize));
        }
        Signal trajectory = computeTrajectory(energySigs);
        int numPeaks = countPeaks(trajectory);
        return numPeaks;
    }
}
