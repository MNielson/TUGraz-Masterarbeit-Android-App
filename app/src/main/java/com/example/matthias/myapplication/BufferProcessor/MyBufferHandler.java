package com.example.matthias.myapplication.BufferProcessor;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.example.matthias.myapplication.FFT;
import com.example.matthias.myapplication.PitchDetector;

import java.util.List;

public class MyBufferHandler extends Handler {

    private final PitchDetector mPitchDetector;
    private final FFT fft;
    private final int ONE_BUFFER_LEN = 2048;
    private final int MIN_EXTREMA_X_DIFF = ONE_BUFFER_LEN / 2;

    // Number of processed buffers
    int buffercounter = 0;

    // Local extrema algorithm variables
    double p0_elem = -1, p1_elem = 0;
    int p0_index = 0, p1_index = 0, p2_index = 0;
    int wordpart = 0;

    public MyBufferHandler(Looper looper) {
        super(looper);
        mPitchDetector = new PitchDetector();
        fft = new FFT(ONE_BUFFER_LEN);


    }

    public void handleMessage(Message msg) {
        List<Short> buffer = (List<Short>) msg.obj;

        double pitch = computePitch(buffer);

        // perform FFT
        double[] re = new double[ONE_BUFFER_LEN];
        double[] im = new double[ONE_BUFFER_LEN];
        for(int i = 0; i < buffer.size(); i++){
            re[i] = buffer.get(i).doubleValue();
            im[i] = 0;
        }
        fft.fft(re, im);

        // Algorithm: https://ieeexplore.ieee.org/document/4317582/ (Subband-Based Correlation Approach)
        // Step 1: Morgan and Fosler-Lussier, compute a trajectory that is the average product over all pairs of compressed sub-band energy trajectories.
        // Use 4 bands max, otherwise NON-real-time (quadratic computational complexity)
        double y = 0;
        int n_bands = 20;
        int L = ONE_BUFFER_LEN / 2 / n_bands;
        for (int i=0; i<n_bands-1; i++) {
            for (int j=i+1; j<n_bands; j++) {
                double sum_i = 0;
                double sum_j = 0;
                for (int k=i*L; k<i*L+L; k++)
                    sum_i += Math.pow(re[k], 2.0);
                for (int k=j*L; k<j*L+L; k++)
                    sum_j += Math.pow(re[k], 2.0);
                y += sum_i * sum_j;
            }
        }

        // Step 2: Syllable recognition algorithm (local extrema finding). Online algorithm.
        // Extremas are either /\ or \/ --> Estimated by: p0_elem --> p1_elem --> y.
        float MIN_EXTREMA_Y_DIFF = 0;

        p2_index += buffer.size();
        if (p0_elem <= p1_elem && p1_elem <= y) { p1_elem = y; p1_index = p2_index; }
        else if (p0_elem <= p1_elem && p1_elem > y && pitch >= 60 && pitch < 400) {
            if ( Math.abs(p1_elem-y) > MIN_EXTREMA_Y_DIFF ) {
                if ((p1_index > MIN_EXTREMA_X_DIFF || p0_elem < 0) && p2_index-p1_index > MIN_EXTREMA_X_DIFF) {
                    /*
#ifdef DEBUG_OUTPUT
                    printf("RateS,%d,%f\n", buffercounter,p1_elem); fflush(stdout);  // local max found: (p1_index, p1_elem)
#endif
                    */
                    process_maximum(p1_index, p1_elem);

                    p0_elem = p1_elem; p1_elem = y;
                    int offset = p1_index;
                    p0_index = p1_index - offset; p1_index = p2_index - offset; p2_index = p2_index - offset;
                }
            }
        }
        else if (p0_elem >= p1_elem && p1_elem >= y) { p1_elem = y; p1_index = p2_index; }
        else if (p0_elem >= p1_elem && p1_elem < y) {
            if ( Math.abs(p1_elem-y) > MIN_EXTREMA_Y_DIFF ) {
                if ((p1_index > MIN_EXTREMA_X_DIFF || p0_elem < 0) && p2_index-p1_index > MIN_EXTREMA_X_DIFF) {
                /*
#ifdef DEBUG_OUTPUT
                    printf("RateF,%d,%f\n", buffercounter,p1_elem); fflush(stdout); // local min found: (p1_index, p1_elem)
#endif
                */
                    process_minimum(p1_index, p1_elem);

                    p0_elem = p1_elem; p1_elem = y;
                    int offset = p1_index;
                    p0_index = p1_index - offset; p1_index = p2_index - offset; p2_index = p2_index - offset;
                }
            }
        }
    }

    private void process_maximum(int index, double value) {
        wordpart++;
    }

    private void process_minimum(int index, double value) {
        double pause = ((double)index) / 44100;
    }



    private double computePitch(List<Short> buffer)
    {
        int len = buffer.size();
        double[] primSamples = new double[len];
        for(int i = 0; i < len; i++)
            primSamples[i] = buffer.get(i).doubleValue();
        return mPitchDetector.computePitch(primSamples, 0, len);
    }
}
