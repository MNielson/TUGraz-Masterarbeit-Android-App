package com.example.matthias.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import uk.me.berndporr.iirj.Butterworth;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.

    final int SAMPLE_RATE = 44100; // 16k for speech. 44.1k for music.
    final String LOG_TAG = "Audio-Recording";
    private Handler mHandler;
    boolean mShouldContinue;

    private AudioWorker mAudioWorker;
    private AudioFileReader mAudioFileReader;
    private PitchDetector mPitchDetector;
    private SyllableDetector mSyllableDetector;

    private ArrayList<short[]> audioBuffers = new ArrayList<>();

    private double mGraphLastXValue = 0;
    private LineGraphSeries<DataPoint> mSeries;

    // Bandpass Frequencies
    final int FL = 50;
    final int FH = 50;
    private Butterworth mBandpass;

    // Filterbank
    private Filterbank mFilterbank;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        //TextView tv = findViewById(R.id.text_results);
        //tv.setText(stringFromJNI());
        mSyllableDetector = new SyllableDetector(5.0d);
        mPitchDetector = new PitchDetector();
        mAudioWorker = new AudioWorker("foo", mPitchDetector);
        mAudioFileReader = new AudioFileReader(mAudioWorker, this);
        requestRecordAudioPermission();

        mBandpass = new Butterworth();
        mBandpass.bandPass(2, SAMPLE_RATE, (FH - FL) / 2, FH - FL);

        mFilterbank = new Filterbank(SAMPLE_RATE);
        mFilterbank.addFilter(2,  240, 120 );
        mFilterbank.addFilter(2,  480, 120 );
        mFilterbank.addFilter(2,  360, 120 );
        mFilterbank.addFilter(2,  600, 120 );
        mFilterbank.addFilter(2,  720, 120 );
        mFilterbank.addFilter(2,  840, 120 );
        mFilterbank.addFilter(2, 1000, 150 );
        mFilterbank.addFilter(2, 1150, 150 );
        mFilterbank.addFilter(2, 1300, 150 );
        mFilterbank.addFilter(2, 1450, 150 );
        mFilterbank.addFilter(2, 1600, 150 );
        mFilterbank.addFilter(2, 1800, 200 );
        mFilterbank.addFilter(2, 2000, 200 );
        mFilterbank.addFilter(2, 2200, 200 );
        mFilterbank.addFilter(2, 2400, 200 );
        mFilterbank.addFilter(2, 2700, 300 );
        mFilterbank.addFilter(2, 3000, 300 );
        mFilterbank.addFilter(2, 3300, 300 );
        mFilterbank.addFilter(2, 3750, 500 );

        GraphView graph = findViewById(R.id.graph);
        mSeries = new LineGraphSeries<>();
        graph.addSeries(mSeries);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(40);
        graph.getGridLabelRenderer().setTextSize(20.0f);


        mHandler = new Handler(Looper.getMainLooper()) {
            /*
         * handleMessage() defines the operations to perform when
         * the Handler receives a new Message to process.
         */
            @Override
            public void handleMessage(Message inputMessage) {
                Bundle bundle = (Bundle) inputMessage.obj;
                short[] audioBuffer = bundle.getShortArray("AudioBuffer");
                double pitch = bundle.getDouble("Pitch");
                //mSyllableDetector.addPitch(pitch);
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
        Intent intent_upload = new Intent();
        intent_upload.setType("audio/*");
        intent_upload.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent_upload,1);

    }
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){

        if(requestCode == 1){

            if(resultCode == RESULT_OK){

                //the selected audio.
                Uri uri = data.getData();
                try {
                    InputStream is = getContentResolver().openInputStream(uri);
                    byte[] content = new byte[is.available()];
                    content = convertStreamToByteArray(is);

                    double pitch = mAudioWorker.computePitch(HelperFunctions.convertByteToDoubleViaShort(content));
                    Log.d("foo", "bar");

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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
        //int numPeaks = mSyllableDetector.countPeaks();
        //Log.d(LOG_TAG, "Detected " +numPeaks+ " peaks / syllables? in recorded audio.");
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


                    //filter microphone input
                    Double filtered[] = new Double[audioBuffer.length];
                    for(int i = 0; i < audioBuffer.length; i++)
                    {
                        filtered[i] = mBandpass.filter(workBuffer[i]);
                    }

                    Double pitch = new Double(mAudioWorker.computePitch(filtered));
                    Log.d(LOG_TAG, "W-Pitch:" + pitch);
                    Bundle bundle = new Bundle();
                    bundle.putShortArray("AudioBuffer", workBuffer);
                    bundle.putDouble("Pitch", pitch);
                    Message msg = new Message();
                    msg.obj = bundle;
                    mHandler.sendMessage(msg);

                }

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



    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
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

    public static byte[] convertStreamToByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buff = new byte[10240];
        int i = Integer.MAX_VALUE;
        while ((i = is.read(buff, 0, buff.length)) > 0) {
            baos.write(buff, 0, i);
        }

        return baos.toByteArray(); // be sure to close InputStream in calling function
    }

}
