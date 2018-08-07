package com.example.matthias.myapplication;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matthias on 13.07.2018.
 */

public class SyllableDetector {
    //private List<Double> pitches;
    private Double delta;

    public SyllableDetector(Double delta) {
        this.delta = delta;
        //pitches = new ArrayList<>();
    }

    public SyllableDetector(){
        this(0.0d);
    }
/*
    public void addPitch(Double pitch) {
        this.pitches.add(pitch);
    }
*/
    public void setDelta(Double delta) {
        this.delta = delta;
    }

    public int countPeaks(Signal sig)
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
            bla[k] = foo;
        }
        Signal bar = new Signal(bla);
        return bar;

    }
}
