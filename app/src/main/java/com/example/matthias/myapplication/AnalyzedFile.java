package com.example.matthias.myapplication;

public class AnalyzedFile {
    public String getFilename() {
        return fname;
    }

    public int getSyllables() {
        return syl;
    }

    private String fname;
    private int syl;


    public AnalyzedFile(String filename, int syllables)
    {
        fname = filename;
        syl = syllables;
    }

    public AnalyzedFile(String filename)
    {
        fname = filename;
        syl = 0;
    }
}
