package com.example.matthias.myapplication.SyllableDetector;

import android.util.Log;

import static com.example.matthias.myapplication.Constants.SAMPLE_RATE;


public class SyllableDetectorConfig {


    final String LOG_TAG = "SyllableDetectorConfig";

    public int secBetweenResults;
    public int chunkSize;
    public int numFilters;

    public SyllableDetectorConfig(int secondsBetweenResults, int chunkSize, int numFilters){
        this.secBetweenResults = secondsBetweenResults;
        // SAMPLE RATE must be divisible by chunk size
        if((SAMPLE_RATE * secondsBetweenResults)% chunkSize == 0)
            this.chunkSize = chunkSize;
        else
        {
            Log.e(LOG_TAG, "Sample Rate not divisible by chunk size. Using chunk size = 10 instead");
            this.chunkSize = 10;
        }
        this.numFilters = numFilters;

    }
}
