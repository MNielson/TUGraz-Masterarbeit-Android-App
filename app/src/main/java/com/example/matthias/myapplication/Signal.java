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
        int len = signal.length;
        int numChunks = (int)Math.ceil( (double)len / (double)chunkSize);
        Double[] d = new Double[numChunks];
        boolean lastChunkSmall = false;
        int lastChunkSize = chunkSize;
        if (len % chunkSize != 0)
        {
            // last chunk is small
            lastChunkSmall = true;
            lastChunkSize = len % chunkSize;
        }

        for(int i = 0; i < numChunks; i++)
        {
            Double e = 0.0;
            int numElemensInChunk = chunkSize;
            if (i == (numChunks-1) && lastChunkSmall)
            {
                numElemensInChunk = lastChunkSize;
            }
            for(int j = 0; j < numElemensInChunk; j++)
            {
                double temp = signal[i * chunkSize + j];
                e += Math.pow(Math.abs(temp), 2);
            }
            d[i] = e;
        }
        Signal energySig = new Signal(d);
        return energySig;
    }

}
