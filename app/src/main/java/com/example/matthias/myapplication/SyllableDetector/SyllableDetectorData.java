package com.example.matthias.myapplication.SyllableDetector;

import java.util.List;

public class SyllableDetectorData {
    private List<Short> content;
    private List<List<Double>> filteredResults;
    private List<List<Double>> energyVectors;
    private List<Double> trajectoryValues;
    private List<Double> maxima;
    private List<Integer> maximaPos;
    private List<Double> minima;
    private List<Integer> minimaPos;
    private String filename;

    SyllableDetectorData(String filename){
        this.filename = filename;
    }


    public List<Integer> getMaximaPos() {
        return maximaPos;
    }

    public void setMaximaPos(List<Integer> maximaPos) {
        this.maximaPos = maximaPos;
    }


    public List<Short> getContent() {
        return content;
    }

    public void setContent(List<Short> content) {
        this.content = content;
    }

    public List<List<Double>> getFilteredResults() {
        return filteredResults;
    }

    public void setFilteredResults(List<List<Double>> filteredResults) {
        this.filteredResults = filteredResults;
    }

    public List<List<Double>> getEnergyVectors() {
        return energyVectors;
    }

    public void setEnergyVectors(List<List<Double>> energyVectors) {
        this.energyVectors = energyVectors;
    }

    public List<Double> getTrajectoryValues() {
        return trajectoryValues;
    }

    public void setTrajectoryValues(List<Double> trajectoryValues) {
        this.trajectoryValues = trajectoryValues;
    }

    public List<Double> getMaxima() {
        return maxima;
    }

    public void setMaxima(List<Double> maxima) {
        this.maxima = maxima;
    }

    public List<Double> getMinima() {
        return minima;
    }

    public void setMinima(List<Double> minima) {
        this.minima = minima;
    }

    public List<Integer> getMinimaPos() {
        return minimaPos;
    }

    public void setMinimaPos(List<Integer> minimaPos) {
        this.minimaPos = minimaPos;
    }

    public String getFilename() {
        return filename;
    }
}
