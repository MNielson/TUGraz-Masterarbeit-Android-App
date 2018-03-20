package com.example.matthias.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.

    //post runables to this thread to run them on the worker thread
    private AudioWorker mAudioWorker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Example of a call to a native method
        TextView tv = findViewById(R.id.sample_text);
        //tv.setText(stringFromJNI());
        PitchDetector pd = new PitchDetector();

        //double[] bar = {0, 0.5, 1, 0.5, 0, -0.5, -1, -0.5, 0, 0.5, 1, 0.5, 0, -0.5, -1, -0.5};
        //double foo = pd.computePitch(bar, 0, bar.length);
        //tv.setText(String.valueOf(foo));

        mAudioWorker = new AudioWorker("myWorkerThread", pd);
        mAudioWorker.start();
        mAudioWorker.prepareHandler();

        AudioFileReader audioFileReader = new AudioFileReader(mAudioWorker, this);
        Log.d("myTag", "myMessage");
        audioFileReader.readAudioFile();


    }


}
