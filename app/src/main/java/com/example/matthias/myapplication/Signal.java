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
        this.signal = sig.toArray(new Double[sig.size()]);
    }

    public Double computeEnergy()
    {
        Double e = 0.0;
        for (Double d : signal) {
            e += Math.pow(Math.abs(d), 2);
        }
        return e;
    }

    public Signal computeEnergyInChunks(int chunkSize)
    {
        //pad signal if needed
        int paddingNeeded = chunkSize - (signal.length % chunkSize);
        Double[] wSig = new Double[signal.length + paddingNeeded];
        for (int i = 0; i < wSig.length; i++)
        {
            wSig[i] = 0.0d;
        }


        System.arraycopy(signal, 0, wSig, 0, signal.length);
        int numChunks = wSig.length / chunkSize; //should always be an int because we padded the array
        Double[] d = new Double[numChunks];

        for(int i = 0; i < numChunks; i++)
        {
            Double e = 0.0;
            for(int j = 0; j < chunkSize; j++)
            {
                double temp = wSig[i * chunkSize + j];
                e += Math.pow(Math.abs(temp), 2);
            }
            d[i] = e;
        }
        Signal energySig = new Signal(d);
        return energySig;
    }

}
