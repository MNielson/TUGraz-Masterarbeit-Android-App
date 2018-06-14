package com.example.matthias.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.

    final int SAMPLE_RATE = 16000; // 16k for speech. 44.1k for music.
    final String LOG_TAG = "Audio-Recording";
    private Handler mHandler;
    boolean mShouldContinue;

    private AudioWorker mAudioWorker;
    private AudioFileReader mAudioFileReader;
    private PitchDetector mPitchDetector;

    private ArrayList<short[]> audioBuffers = new ArrayList<>();

    private double mGraphLastXValue = 0;
    private LineGraphSeries<DataPoint> mSeries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        //TextView tv = findViewById(R.id.text_results);
        //tv.setText(stringFromJNI());
        mPitchDetector = new PitchDetector();
        mAudioWorker = new AudioWorker("foo", mPitchDetector);
        mAudioFileReader = new AudioFileReader(mAudioWorker, this);
        requestRecordAudioPermission();



        GraphView graph = (GraphView) findViewById(R.id.graph);
        mSeries = new LineGraphSeries<>();
        graph.addSeries(mSeries);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(40);

        // set manual Y bounds
        //graph.getViewport().setYAxisBoundsManual(true);
        //graph.getViewport().setMinY(0);
        //graph.getViewport().setMaxY(300);

        graph.getGridLabelRenderer().setTextSize(20.0f);
        //mSeries.appendData(new DataPoint(-3.0, 5000), false, 1000);
        //mSeries.appendData(new DataPoint(-2.0, 1000), false, 1000);
        //mSeries.appendData(new DataPoint(-1.0,  300), false, 1000);


        mHandler = new Handler(Looper.getMainLooper()) {
            /*
         * handleMessage() defines the operations to perform when
         * the Handler receives a new Message to process.
         */
            @Override
            public void handleMessage(Message inputMessage) {
                // Gets the image task from the incoming Message object.
                Bundle bundle = (Bundle) inputMessage.obj;
                short[] audioBuffer = bundle.getShortArray("AudioBuffer");
                double pitch = bundle.getDouble("Pitch");
                audioBuffers.add(audioBuffer);
                mSeries.appendData(new DataPoint(mGraphLastXValue, pitch+1d), true, 1000);
                mGraphLastXValue += 0.5d;
                Log.d(LOG_TAG, "Stuff");
            }
        };
    }

    //onclick button
    public void analyseFile(View view){
        LinkedList<Double> pitches = mAudioFileReader.readPitchFromAudioFile();
        //TextView tv = findViewById(R.id.text_results);
        //tv.setText(HelperFunctions.generateTextResults(pitches));
    }

    //onclick button
    public void onClickStartAudioRecording(View view) {
        mShouldContinue = true;
        try {
            recordAudio();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //onclick button
    public void onClickStopAudioRecording(View view) {
        mShouldContinue = false;
        audioBuffers.size(); //audioBuffers always contains the same data here.
    }


    void recordAudio() throws IOException {
        /*
        File folder = getPublicMusicStorageDir("test");
        final File outputFile = new File(folder, "myRawAudioFile.raw");
        // if file doesnt exists, then create it
        if (!outputFile.exists()) {
            outputFile.createNewFile();
            if (!outputFile.exists()) {
                Log.e("FOO", "Tried to create file and failed somehow.");
            }
        }
        final FileOutputStream outputStream = new FileOutputStream(outputFile);
        */
        new Thread(new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

                // buffer size in bytes
                int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);

                if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
                    bufferSize = SAMPLE_RATE * 2;
                }

                short[] audioBuffer = new short[bufferSize / 2];

                AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize);

                if (record.getState() != AudioRecord.STATE_INITIALIZED) {
                    Log.e(LOG_TAG, "Audio Record can't initialize!");
                    return;
                }
                record.startRecording();

                Log.v(LOG_TAG, "Start recording.");

                long shortsRead = 0;
                while (mShouldContinue) {
                    int numberOfShort = record.read(audioBuffer, 0, audioBuffer.length);

                    short[] workBuffer = new short[audioBuffer.length];

                    System.arraycopy( audioBuffer, 0, workBuffer, 0, audioBuffer.length );

                    shortsRead += numberOfShort;
                    /*
                    try {
                        outputStream.write(HelperFunctions.convertShortToByte(audioBuffer));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    */
                    // Do something with the audioBuffer
                    Random r = new Random();
                    if (r.nextInt(10) == 9)
                    {
                        Log.v("", ""); //break here to randomly check audioBuffer. audioBuffer contains different data every time
                    }
                    Double pitch = new Double(mAudioWorker.computePitch(HelperFunctions.convertShortToDouble(workBuffer)));
                    Log.d(LOG_TAG, "W-Pitch:" + pitch);
                    Bundle bundle = new Bundle();
                    bundle.putShortArray("AudioBuffer", workBuffer);
                    bundle.putDouble("Pitch", pitch);
                    Message msg = new Message();
                    msg.obj = bundle;
                    mHandler.sendMessage(msg);

                }
                /*
                try {
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                */
                record.stop();
                record.release();

                Log.v(LOG_TAG, String.format("Recording stopped. Samples read: %d", shortsRead));
            }
        }).start();
    }



    private void requestRecordAudioPermission() {
        //check API version, do nothing if API version < 23!
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion > android.os.Build.VERSION_CODES.LOLLIPOP){

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.d("Activity", "Granted!");

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d("Activity", "Denied!");
                    finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
/*
    public void someFunction(Double pitch)
    {
        final String someText = "\n\n\n foo             " + pitch.toString() +"hz";
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tv = findViewById(R.id.text_results);
                tv.setText(someText);
            }
        });
    }
*/


    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public File getPublicMusicStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.

        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MUSIC), albumName);
        if (!file.mkdirs()) {
            Log.e(LOG_TAG, "Directory not created");
        }
        return file;
    }
}
