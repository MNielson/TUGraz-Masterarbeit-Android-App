package com.example.matthias.myapplication.SyllableDetector;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.example.matthias.myapplication.BuildConfig;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import autovalue.shaded.org.apache.commons.lang.ArrayUtils;
import uk.me.berndporr.iirj.Butterworth;

import static com.example.matthias.myapplication.MainActivity.SAMPLE_RATE;
import static java.lang.System.gc;

/**
 * Created by Matthias on 13.07.2018.
 */

public class SyllableDetector {


    private Butterworth[] mfilters;
/*
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


    private Signal computeTrajectory(ArrayList<Signal> energySignals)
    {
        int N = energySignals.size();
        Double M = N * (N-1) * 0.5;

        Double[] trajectoryValues = new Double[energySignals.get(0).getSignal().length];


        for(int k = 0; k < trajectoryValues.length; k++)
        {
            Double foo = 0.0;
            for (int i = 0; i < N-2; i++)
            {
                for (int j = i+1; j < N-1; j++)
                {
                    foo += energySignals.get(i).getSignal()[k] * energySignals.get(j).getSignal()[k];
                }
            }
            trajectoryValues[k] = foo / M;
        }
        Signal trajectory = new Signal(trajectoryValues);
        return trajectory;

    }

    public Signal countSyllables(Signal sig)
    {
        ArrayList<Signal> filteredSignals = mFilterbank.filter(sig);
        ArrayList<Signal> energySigs = new ArrayList<>();
        for(Signal fSig : filteredSignals)
        {
            energySigs.add(fSig.computeEnergyInChunks(mChunkSize));
        }
        Signal trajectory = computeTrajectory(energySigs);
        int numPeaks = countPeaks(trajectory);
        return trajectory;
    }


*/

    public SyllableDetector(Butterworth[] filters){
        mfilters = filters;
    }

    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(api = Build.VERSION_CODES.N)
    public SyllableResult getNumPeaks(InputStream is, String filename) throws IOException {

        Optional<SyllableDetectorData> debugData = Optional.of(new SyllableDetectorData(filename));

        byte[] bytes = IOUtils.toByteArray(is);
        short[] content = new short[bytes.length / 2];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(content);
        if(BuildConfig.DEBUG) {
            debugData.get().setContent(Arrays.asList(ArrayUtils.toObject(content)));
        }
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // filter content
        double[][] filteredResults = new double[19][content.length];
        double t = 0.0;
        for(int i = 0; i < content.length; i++)
        {
            for(int j = 0; j < mfilters.length; j++)
            {
                filteredResults[j][i] = mfilters[j].filter((double)content[i]);
            }

        }


        //pad signal if needed
        int oSize = content.length;
        content = null;

        int chunkSize = SAMPLE_RATE / 100;
        int paddingNeeded = chunkSize - (oSize % chunkSize);
        int paddedLength = oSize+paddingNeeded;
        double[][] filteredResultsWithPadding = new double[19][paddedLength];

        for(int j = 0; j < mfilters.length; j++)
        {
            for (int i = 0; i < (paddedLength); i++)
            {
                if(i < oSize) //add content
                    filteredResultsWithPadding[j][i] = filteredResults[j][i];
                else//add padding
                    filteredResultsWithPadding[j][i] = 0.0d;
            }
        }
        if(BuildConfig.DEBUG){
            List<List<Double>> x = new ArrayList<>();
            for(int j = 0; j < mfilters.length; j++)
            {
                double[] tArr = new double[paddedLength];
                for (int i = 0; i < (paddedLength); i++)
                {
                    tArr[i] = filteredResultsWithPadding[j][i];
                }
                x.add(Arrays.asList(ArrayUtils.toObject(tArr)));
            }
            debugData.get().setFilteredResults(x);
        }
        filteredResults = null;

        // compute energy in chunks

        int numChunks = paddedLength / chunkSize; //should always be an int because we padded the array
        double[][] energyVectors = new double[mfilters.length][numChunks];
        double temp = 0.0d;
        double e = 0.0d;
        for(int k = 0; k < mfilters.length; k++)
        {
            for (int i = 0; i < numChunks; i++)
            {
                e = 0.0d;
                for (int j = 0; j < chunkSize; j++)
                {
                    temp = filteredResultsWithPadding[k][i * chunkSize + j];
                    e += Math.pow(temp, 2);
                }
                energyVectors[k][i] = e;
            }
        }

        if(BuildConfig.DEBUG){
            List<List<Double>> x = new ArrayList<>();
            for(int k = 0; k < mfilters.length; k++)
            {
                double[] tArr = new double[numChunks];
                for (int i = 0; i < numChunks; i++)
                {
                    tArr[i] = energyVectors[k][i];
                }
                x.add(Arrays.asList(ArrayUtils.toObject(tArr)));
            }
            debugData.get().setEnergyVectors(x);
        }
        filteredResultsWithPadding = null;


        // compute trajectory
        double[] trajectoryValues = new double[numChunks];

        int N = mfilters.length;
        double M = N * (N-1) * 0.5;
        for(int k = 0; k < trajectoryValues.length; k++)
        {
            Double foo = 0.0;
            for (int i = 0; i < N-2; i++)
            {
                for (int j = i+1; j < N-1; j++)
                {
                    foo += energyVectors[i][k] * energyVectors[j][k];
                }
            }
            trajectoryValues[k] = foo / M;
        }
        if(BuildConfig.DEBUG){
            debugData.get().setTrajectoryValues(Arrays.asList(ArrayUtils.toObject(trajectoryValues)));
        }
        energyVectors = null;

        //detect peaks

        double delta = 0.5;

        List<Double> maxima = new ArrayList<>();
        List<Double> minima = new ArrayList<>();

        List<Integer> maximaPos = new ArrayList<>();
        List<Integer> minimaPos = new ArrayList<>();

        Double maximum = null;
        Double minimum = null;

        boolean lookForMax = true;


        Integer pos = 0;
        int maximaP = 0;
        int minimaP = 0;
        for (double trajectoryValue : trajectoryValues) {
            if (maximum == null || trajectoryValue > maximum) {
                maximum = trajectoryValue;
                maximaP = pos;
            }

            if (minimum == null || trajectoryValue < minimum) {
                minimum = trajectoryValue;
                minimaP = pos;
            }

            if (lookForMax) {
                if (trajectoryValue < maximum - delta) {
                    maxima.add(trajectoryValue);
                    minimum = trajectoryValue;
                    maximaPos.add(maximaP);
                    minimaP = pos;
                    lookForMax = false;
                }
            } else {
                if (trajectoryValue > minimum + delta) {
                    minima.add(trajectoryValue);
                    maximum = trajectoryValue;
                    minimaPos.add(minimaP);
                    maximaP = pos;
                    lookForMax = true;
                }
            }
            pos++;
        }

        if(BuildConfig.DEBUG){
            debugData.get().setMaxima(maxima);
            debugData.get().setMaximaPos(maximaPos);
            debugData.get().setMinima(minima);
            debugData.get().setMinimaPos(minimaPos);
        }


        int numPeaks = maxima.size();
        trajectoryValues = null;
        gc();
        return new SyllableResult(numPeaks, debugData);
    }




}
