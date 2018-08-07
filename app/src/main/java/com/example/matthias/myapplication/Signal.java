package com.example.matthias.myapplication;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Matthias on 29.07.2018.
 */

public class Signal {

    private Double[] signal;


    public Double[] getSignal() {
        return signal;
    }

    public Signal(Double[] sig)
    {
        this.signal = Arrays.copyOf(sig, sig.length);
    }

    public Signal(ArrayList<Double> sig)
    {
        this.signal = (Double[]) sig.toArray();
    }

    public Double computeEnergy()
    {
        Double e = 0.0;
        for (Double d : signal) {
            e += Math.pow(Math.abs(d), 2);
        }
        return e;
    }
}
