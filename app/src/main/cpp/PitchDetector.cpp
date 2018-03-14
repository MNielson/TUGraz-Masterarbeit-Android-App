//
// Created by Matthias on 01.03.2018.
//

#include "PitchDetector.h"
#include "dywapitchtrack.h"

static dywapitchtracker* mdywapitchtracker;

PitchDetector::PitchDetector() {
    mdywapitchtracker = new dywapitchtracker;
}

double PitchDetector::computePitch(double * samples, int startsample, int samplecount) {
    return dywapitch_computepitch(mdywapitchtracker, samples, startsample, samplecount);
}

double PitchDetector::testMe(double * samples, int samplecount) {
    return dywapitch_tester(samples, samplecount);
}