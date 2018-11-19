package com.example.matthias.myapplication.SyllableDetector;

import java.util.Optional;

public class SyllableResult {
    int numSyllables;
    Optional<SyllableDetectorData> debugData;

    SyllableResult(int n, Optional<SyllableDetectorData> d){
        numSyllables = n;
        debugData = d;
    }
}
