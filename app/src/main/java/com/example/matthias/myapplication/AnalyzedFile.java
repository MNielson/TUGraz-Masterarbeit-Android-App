package com.example.matthias.myapplication;

public class AnalyzedFile {
    public String getFileID() {
        return fID;
    }

    public int getSyllables() {
        return syl;
    }

    private String fID;
    private int syl;


    public AnalyzedFile(String fileID, int syllables)
    {
        fID = fileID;
        syl = syllables;
    }

    public AnalyzedFile(String fileID)
    {
        fID = fileID;
        syl = 0;
    }
}
