package com.example.matthias.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.

    //post runables to this thread to run them on the worker thread
    private AudioWorker mAudioWorker;
    private AudioFileReader mAudioFileReader;
    private PitchDetector mPitchDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Example of a call to a native method
        TextView tv = findViewById(R.id.text_results);
        //tv.setText(stringFromJNI());
        mPitchDetector = new PitchDetector();
        mAudioWorker = new AudioWorker("foo", mPitchDetector);
        mAudioFileReader = new AudioFileReader(mAudioWorker, this);

    }

    public void analyseFile(View view){
        LinkedList<Double> pitches = mAudioFileReader.readPitchFromAudioFile();
        TextView tv = findViewById(R.id.text_results);
        tv.setText(HelperFunctions.generateTextResults(pitches));

    }


}
