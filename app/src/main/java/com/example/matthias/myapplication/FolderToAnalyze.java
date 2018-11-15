package com.example.matthias.myapplication;

import android.net.Uri;

import java.util.ArrayList;

public class FolderToAnalyze {
    private String moutFileName;
    private ArrayList<Uri> mfiles;

    public FolderToAnalyze(String outFileName, ArrayList<Uri> files){
        mfiles = files;
        moutFileName = outFileName;
    }

    public FolderToAnalyze(String outFileName, Uri file){
        ArrayList<Uri> foo = new ArrayList<>();
        foo.add(file);
        mfiles = foo;
        moutFileName = outFileName;
    }


    public String getoutFileName() {
        return moutFileName;
    }

    public ArrayList<Uri> getfiles() {
        return mfiles;
    }
}
