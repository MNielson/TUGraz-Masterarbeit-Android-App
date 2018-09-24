package com.example.matthias.myapplication;

import android.Manifest;
import android.content.Context;
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
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
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
    private Filterbank mFilterbank;
    private Butterworth[] filters = new Butterworth[19];

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

        mBandpass = new Butterworth();
        mBandpass.bandPass(2, SAMPLE_RATE, (FH - FL) / 2, FH - FL);

        /*
        mFilterbank = new Filterbank(SAMPLE_RATE);
        mFilterbank.addFilter(2,  240, 120 );
        mFilterbank.addFilter(2,  360, 120 );
        mFilterbank.addFilter(2,  480, 120 );
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
        mSyllableDetector = new SyllableDetector(mFilterbank);
        */

        Butterworth b1 = new Butterworth();
        Butterworth b2 = new Butterworth();
        Butterworth b3 = new Butterworth();
        Butterworth b4 = new Butterworth();
        Butterworth b5 = new Butterworth();
        Butterworth b6 = new Butterworth();
        Butterworth b7 = new Butterworth();
        Butterworth b8 = new Butterworth();
        Butterworth b9 = new Butterworth();
        Butterworth b10 = new Butterworth();
        Butterworth b11 = new Butterworth();
        Butterworth b12 = new Butterworth();
        Butterworth b13 = new Butterworth();
        Butterworth b14 = new Butterworth();
        Butterworth b15 = new Butterworth();
        Butterworth b16 = new Butterworth();
        Butterworth b17 = new Butterworth();
        Butterworth b18 = new Butterworth();
        Butterworth b19 = new Butterworth();


        b1.bandPass(2, SAMPLE_RATE, 240, 120);
        b2.bandPass(2, SAMPLE_RATE, 360, 120);
        b3.bandPass(2, SAMPLE_RATE, 480, 120);
        b4.bandPass(2, SAMPLE_RATE, 600, 120);
        b5.bandPass(2, SAMPLE_RATE, 720, 120);
        b6.bandPass(2, SAMPLE_RATE, 840, 120);
        b7.bandPass(2, SAMPLE_RATE, 1000, 150);
        b8.bandPass(2, SAMPLE_RATE, 1150, 150);
        b9.bandPass(2, SAMPLE_RATE, 1300, 150);
        b10.bandPass(2, SAMPLE_RATE, 1450, 150);
        b11.bandPass(2, SAMPLE_RATE, 1600, 150);
        b12.bandPass(2, SAMPLE_RATE, 1800, 200);
        b13.bandPass(2, SAMPLE_RATE, 2000, 200);
        b14.bandPass(2, SAMPLE_RATE, 2200, 200);
        b15.bandPass(2, SAMPLE_RATE, 2400, 200);
        b16.bandPass(2, SAMPLE_RATE, 2700, 300);
        b17.bandPass(2, SAMPLE_RATE, 3000, 300);
        b18.bandPass(2, SAMPLE_RATE, 3300, 300);
        b19.bandPass(2, SAMPLE_RATE, 3750, 500);

        filters[1 -1] = b1;
        filters[2 -1] = b2;
        filters[3 -1] = b3;
        filters[4 -1] = b4;
        filters[5 -1] = b5;
        filters[6 -1] = b6;
        filters[7 -1] = b7;
        filters[8 -1] = b8;
        filters[9 -1] = b9;
        filters[10-1] = b10;
        filters[11-1] = b11;
        filters[12-1] = b12;
        filters[13-1] = b13;
        filters[14-1] = b14;
        filters[15-1] = b15;
        filters[16-1] = b16;
        filters[17-1] = b17;
        filters[18-1] = b18;
        filters[19-1] = b19;



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
        //LinkedList<Double> pitches = mAudioFileReader.readPitchFromAudioFile();
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
                InputStream is = null;
                try {
                    is = getContentResolver().openInputStream(uri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                byte[] tcontent = new byte[0];
                byte[] content = new byte[0];
                ArrayList<Short> content2 = new ArrayList<>();
                try {
                    //content = convertStreamToByteArray(is);
                    DataInputStream dis = new DataInputStream(is);

                    while(dis.available() > 0)
                    {
                        //reverse byte order in wav
                        content2.add(Short.reverseBytes(dis.readShort()));
                    }

                    content = IOUtils.toByteArray(is); //includes wav header (size 40 bytes?)

                    //content = Arrays.copyOfRange(tcontent, 40, tcontent.length);



                    File sdCard = Environment.getExternalStorageDirectory();
                    File dir = new File (sdCard.getAbsolutePath() + "/dir1");
                    File file = new File(dir, "shorts");

                    if(!file.getParentFile().exists())
                        if(!file.getParentFile().mkdirs())
                            Log.e("FOO", "Failed to create directory");

                    if(!file.exists())
                        if(!file.createNewFile())
                            Log.e("FOO", "Failed to create file");
/*
                    double[] d = HelperFunctions.convertByteToDoubleViaShort(content);
                    try {
                        FileOutputStream outputStream = new FileOutputStream(file);
                        OutputStreamWriter os = new OutputStreamWriter(outputStream);
                        for (double du : d) {
                            os.write(String.valueOf(du) + "\n");
                        }
                        outputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
*/

                    try {
                        FileOutputStream outputStream = new FileOutputStream(file);
                        OutputStreamWriter os = new OutputStreamWriter(outputStream);
                        for (Short s : content2) {
                            os.write(String.valueOf(s) + "\n");
                        }
                        outputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }



                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                /*
                int byte_len = content.length;
                double[] d = HelperFunctions.convertByteToDoubleViaShort(content);
                int double_len = d.length;
                */
                // filter content
                double[][] filteredResults = new double[19][content2.size()];
                double t = 0.0;
                for(int i = 0; i < content2.size(); i++)
                {
                    t = content2.get(i).doubleValue();
                    for(int j = 0; j < filters.length; j++)
                    {
                        filteredResults[j][i] = filters[j].filter(t);
                    }

                }

                //pad signal if needed

                int chunkSize = SAMPLE_RATE / 10;
                int paddingNeeded = chunkSize - (content2.size() % chunkSize);
                int paddedLength = content2.size()+paddingNeeded;
                double[][] filteredResultsWithPadding = new double[19][paddedLength];

                for(int j = 0; j < filters.length; j++)
                {
                    for (int i = 0; i < (paddedLength); i++)
                    {
                        if(i < content2.size()) //add content
                            filteredResultsWithPadding[j][i] = filteredResults[j][i];
                        else//add padding
                            filteredResultsWithPadding[j][i] = 0.0d;
                    }
                }

                // compute energy in chunks

                int numChunks = paddedLength / chunkSize; //should always be an int because we padded the array
                double[][] energyVectors = new double[filters.length][numChunks];
                double temp = 0.0d;
                double e = 0.0d;
                for(int k = 0; k < filters.length; k++)
                {
                    for (int i = 0; i < numChunks; i++)
                    {
                        e = 0.0d;
                        for (int j = 0; j < chunkSize; j++)
                        {
                            temp = filteredResultsWithPadding[k][i * chunkSize + j];
                            e += Math.pow(temp, 2);
                        }
                        energyVectors[k][i] = e;
                    }
                }


                // compute trajectory
                double[] trajectoryValues = new double[numChunks];

                int N = filters.length;
                double M = N * (N-1) * 0.5;
                for(int k = 0; k < trajectoryValues.length; k++)
                {
                    Double foo = 0.0;
                    for (int i = 0; i < N-2; i++)
                    {
                        for (int j = i+1; j < N-1; j++)
                        {
                            foo += energyVectors[i][k] * energyVectors[j][k];
                        }
                    }
                    trajectoryValues[k] = foo / M;
                }

                //detect peaks

                double delta = 0.5;

                List<Double> maxima = new ArrayList<>();
                List<Double> minima = new ArrayList<>();

                Double maximum = null;
                Double minimum = null;

                boolean lookForMax = true;



                for (double trajectoryValue : trajectoryValues) {
                    if (maximum == null || trajectoryValue > maximum) {
                        maximum = trajectoryValue;
                    }

                    if (minimum == null || trajectoryValue < minimum) {
                        minimum = trajectoryValue;
                    }

                    if (lookForMax) {
                        if (trajectoryValue < maximum - delta) {
                            maxima.add(trajectoryValue);
                            minimum = trajectoryValue;
                            lookForMax = false;
                        }
                    } else {
                        if (trajectoryValue > minimum + delta) {
                            minima.add(trajectoryValue);
                            maximum = trajectoryValue;
                            lookForMax = true;
                        }
                    }
                }

                int numPeaks = maxima.size();
                Log.d("PEAKS", "Detected "+numPeaks + " syllables in File");



                /*
                Signal s = new Signal(d);
                Signal foo = mSyllableDetector.countSyllables(s);

                Log.d("foo", "bar");
                for(int i = 0; i < foo.getSignal().length; i++)
                {
                    mSeries.appendData(new DataPoint(i, foo.getSignal()[i]), true, foo.getSignal().length);
                }
                */




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
                    Double f[] = new Double[audioBuffer.length];
                    for(int i = 0; i < audioBuffer.length; i++)
                    {
                        f[i] = mBandpass.filter(workBuffer[i]);
                    }
                    Signal filteredSignal = new Signal(f);

                    Double pitch = new Double(mAudioWorker.computePitch(filteredSignal));
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
        byte[] buff = new byte[100];
        int i = Integer.MAX_VALUE;
        while ((i = is.read(buff, 0, buff.length)) > 0) {
            baos.write(buff, 0, i);
        }

        return baos.toByteArray(); // be sure to close InputStream in calling function
    }

}
