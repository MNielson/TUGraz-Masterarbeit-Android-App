package com.example.matthias.myapplication;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matthias on 13.07.2018.
 */

public class SyllableDetector {
    private List<Double> pitches;
    private Double delta;

    public SyllableDetector(Double delta) {
        this.delta = delta;
        pitches = new ArrayList<>();
    }

    public SyllableDetector(){
        this(0.0d);
    }

    public void addPitch(Double pitch) {
        this.pitches.add(pitch);
    }

    public void setDelta(Double delta) {
        this.delta = delta;
    }

    public int countPeaks()
    {
        Pair<List<Double>, List<Double>> peaks = detectPeaks();
        return peaks.first.size();
    }

    private Pair< List<Double>, List<Double> > detectPeaks()
    {
        List<Double> maxima = new ArrayList<>();
        List<Double> minima = new ArrayList<>();

        Double maximum = null;
        Double minimum = null;

        boolean lookForMax = true;

        for (Double pitch : pitches) {
            if (maximum == null || pitch > maximum) {
                maximum = pitch;
            }

            if (minimum == null || pitch < minimum) {
                minimum = pitch;
            }

            if (lookForMax) {
                if (pitch < maximum - delta) {
                    maxima.add(pitch);
                    minimum = pitch;
                    lookForMax = false;
                }
            } else {
                if (pitch > minimum + delta) {
                    minima.add(pitch);
                    maximum = pitch;
                    lookForMax = true;
                }
            }
        }

        return new Pair(maxima, minima);
    }
}
