package com.example.matthias.myapplication.SyllableDetector;

import android.os.Environment;
import android.util.Log;

import com.example.matthias.myapplication.BuildConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import uk.me.berndporr.iirj.Butterworth;

/**
 * Created by Matthias on 13.07.2018.
 */

public class SyllableDetectorWorker {

    final String LOG_TAG = "SyllableDetectorWorker";
    private Butterworth[] mfilters;

    private double[] computeTrajectory(int numChunks, double[][] energyVectors) {
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
        return trajectoryValues;
    }

    public SyllableDetectorWorker(Butterworth[] filters){
        mfilters = filters;
    }

    private List<Double> computeTrajectory(List<ArrayList<Double>> energyVectors, int chunkSize, int elements){
        int numChunks = elements / chunkSize;
        ArrayList<Double> trajectory = new ArrayList<>();
        int numVectors = energyVectors.size();
        int numPairs = (int) (numVectors * (numVectors-1) * 0.5);
        for(int ix = 0; ix < numChunks; ix++)
        {
            double tSumOfPairProducts = 0.0d;
            for(int i = 0; i < numVectors - 1; i++)
            {
                for(int j = i; j < numVectors-2; j++)
                {
                    tSumOfPairProducts += energyVectors.get(i).get(ix) * energyVectors.get(j).get(ix);
                }
            }
            trajectory.add(tSumOfPairProducts / numPairs);
        }
        return trajectory;
    }

    public int peaksFromFiltered(List<ArrayList<Double>> filtered, int chunkSize, int elements, int debugFileCounter)
    {
        List<ArrayList<Double>> energyVectors = computeEnergyVectors(filtered, chunkSize, elements);
        List<Double> trajectory = computeTrajectory(energyVectors, chunkSize, elements);
        PeakResults peakResults = detectPeaks(trajectory);


        if(BuildConfig.DEBUG)
        {
            // write stuff to json
            if(BuildConfig.DEBUG)
            {
                String dirName = "debug-data";
                File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), dirName);
                if (!folder.mkdirs())
                    Log.d(LOG_TAG, "Directory not created");
                try {
                    File file = new File(folder, Integer.toString(debugFileCounter) + "-energy.txt");
                    FileWriter foo = new FileWriter(file);
                    for(ArrayList<Double> ld : energyVectors) {
                        for(double d : ld)
                        {
                            foo.write(Double.toString(d));
                            foo.write("\n");
                        }
                    }
                    foo.close();
                    File file2 = new File(folder, Integer.toString(debugFileCounter) + "-trajectory.txt");
                    FileWriter foo2 = new FileWriter(file2);
                    for(double d : trajectory)
                    {
                        foo2.write(Double.toString(d));
                        foo2.write("\n");
                    }
                    foo2.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return peakResults.getMaxima().size();
    }


    public List<ArrayList<Double>> filterBuffer(List<Short> content) {
        List<ArrayList<Double>> filtered = new ArrayList<>();
        long startTime = System.nanoTime();

        for(int j = 0; j < mfilters.length; j++) {
            ArrayList<Double> l = new ArrayList<>();
            for(int i = 0; i < content.size(); i++){
                double d = (double) content.get(i);
                l.add(mfilters[j].filter(d));
            }
            filtered.add(l);
        }
        long stopTime = System.nanoTime();
        long timeElapsed = stopTime - startTime;

        System.out.println("Execution time in milliseconds : " +
                timeElapsed / 1000000);
        Log.d(LOG_TAG, "Filtered " + content.size() + " elements in " + Long.toString(timeElapsed / 1000000) +"ms");
        return filtered;
    }

    private double[][] padSignal(double[][] filteredResults, int oSize, int paddedLength) {
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
        return filteredResultsWithPadding;
    }

    public List<ArrayList<Double>> computeEnergyVectors(List<ArrayList<Double>> filtered, int chunkSize, int elements) {
        if(elements % chunkSize != 0)
            Log.e(LOG_TAG, "number of elements not multiple of chunk size. either wait for more elements or add padding");

        List<ArrayList<Double>> energyVectors = new ArrayList<>();

        for(ArrayList<Double> l : filtered) {
            ArrayList<Double> energy = new ArrayList<>();
            double e = 0.0d;
            int elementsInChunk = 0;
            for(Double d : l){
                e += Math.pow(d, 2);
                elementsInChunk++;
                if (elementsInChunk == chunkSize) {
                    energy.add(e);
                    elementsInChunk = 0;
                    e = 0.0d;
                }
            }
            energyVectors.add(energy);
        }
        return energyVectors;
    }


    private class PeakResults {
        private List<Double> maxima;
        private List<Double> minima;
        private List<Integer> maximaPos;
        private List<Integer> minimaPos;

        public PeakResults( List<Double> maxima, List<Double> minima,  List<Integer> maximaPos, List<Integer> minimaPos){
            this.maxima = maxima;
            this.minima = minima;
            this.maximaPos = maximaPos;
            this.minimaPos = minimaPos;
        }

        public List<Double> getMaxima() {
            return maxima;
        }

        public List<Double> getMinima() {
            return minima;
        }

        public List<Integer> getMaximaPos() {
            return maximaPos;
        }

        public List<Integer> getMinimaPos() {
            return minimaPos;
        }
    }

    public PeakResults detectPeaks(List<Double> trajectoryValues) {
        double delta = 0.5;


        ArrayList<Double> maxima = new ArrayList<>();
        ArrayList<Double> minima = new ArrayList<>();

        ArrayList<Integer> maximaPos = new ArrayList<>();
        ArrayList<Integer> minimaPos = new ArrayList<>();

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
        return new PeakResults(maxima, minima, maximaPos, minimaPos);
    }
}
