package com.example.matthias.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.example.matthias.myapplication.SyllableDetector.SyllableDetector;
import com.example.matthias.myapplication.SyllableDetector.SyllableDetectorConfig;
import com.example.matthias.myapplication.SyllableDetector.SyllableDetectorWorker;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

import uk.me.berndporr.iirj.Butterworth;

public class MainActivity extends AppCompatActivity {

    //////////////////
    //REQUEST CODES //
    /////////////////
    private static final int SINGLE_FILE      = 1;
    private static final int MULTIPLE_FILES = 3;

    public static final int SAMPLE_RATE = 44100; // 16k for speech. 44.1k for music.
    final String LOG_TAG = "Audio-Recording";
    private Handler mHandler;
    boolean mShouldContinue;

    private AudioWorker mAudioWorker;
    private AudioFileReader mAudioFileReader;
    private PitchDetector mPitchDetector;
    private SyllableDetectorWorker mSyllableDetectorWorker;

    private ArrayList<AnalyzedFile> analyzedFiles = new ArrayList<>();

    private ArrayList<short[]> audioBuffers = new ArrayList<>();

    private double mGraphLastXValue = 0;
    private LineGraphSeries<DataPoint> mSeries;

    // Bandpass Frequencies
    final int FL = 50;
    final int FH = 50;
    private Butterworth mBandpass;
    private Filterbank mFilterbank;
    //private Butterworth[] filters = new Butterworth[19];

    private SyllableDetector syllableWorker;

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
        requestStoragePermission();

        mBandpass = new Butterworth();
        mBandpass.bandPass(2, SAMPLE_RATE, (FH - FL) / 2, FH - FL);

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

        Butterworth[] filters = new Butterworth[19];
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
        SyllableDetectorWorker syl = new SyllableDetectorWorker(filters);

        SyllableDetectorConfig config = new SyllableDetectorConfig(2, SAMPLE_RATE / 50, 19);
        syllableWorker = new SyllableDetector(syl, config);


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
                short[] audioBuffer = (short[]) inputMessage.obj;
                syllableWorker.process(audioBuffer);
                mAudioWorker.computePitch(audioBuffer);
                /*
                Bundle bundle = (Bundle) inputMessage.obj;
                short[] audioBuffer = bundle.getShortArray("AudioBuffer");
                double pitch = bundle.getDouble("Pitch");
                //mSyllableDetectorWorker.addPitch(pitch);
                audioBuffers.add(audioBuffer);
                mSeries.appendData(new DataPoint(mGraphLastXValue, pitch+1d), true, 1000);
                mGraphLastXValue += 0.5d;
                Log.d(LOG_TAG, "Stuff");
                */
            }
        };
    }

    //onclick button
    public void analyseFilesInFolder(View view){
        // doesn't work on all phones...
        // works on emulated Pixel 2XL & Nexus 5X
        // doesn't work on real Galaxy S6
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Files"), MULTIPLE_FILES);
    }

    //onclick button
    public void analyseFile(View view){
        //LinkedList<Double> pitches = mAudioFileReader.readPitchFromAudioFile();
        //TextView tv = findViewById(R.id.text_results);
        //tv.setText(HelperFunctions.generateTextResults(pitches));

        Intent intent_upload = new Intent();
        intent_upload.setType("audio/*");
        intent_upload.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent_upload,SINGLE_FILE);
        return;
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        switch(requestCode){
            case(SINGLE_FILE):
                if(resultCode == RESULT_OK){
                    Uri uri = data.getData();
                    InputStream is;
                    try {
                        is = getContentResolver().openInputStream(uri);
                        byte[] bytes = IOUtils.toByteArray(is);
                        short[] content = new short[bytes.length / 2];
                        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(content);
                        int bufferSize = Math.min(SAMPLE_RATE + 400, content.length);
                        int numBuffers = (int) Math.round(Math.ceil((double)content.length / bufferSize));
                        boolean lastNeedsPadding = (((double)content.length / bufferSize) % 1 != 0);
                        for(int i = 0; i < numBuffers; i++){
                            short[] b = new short[bufferSize];
                            if((i+1) == numBuffers && lastNeedsPadding) {
                                Arrays.fill(b, (short) 0);
                                b = Arrays.copyOfRange(content, i * bufferSize, content.length);
                            }
                            else
                                b = Arrays.copyOfRange(content, i * bufferSize, (i+1) * bufferSize);
                            syllableWorker.process(b);
                            mAudioWorker.computePitch(b);
                            try {
                                Thread.sleep(5);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //the selected audio.
                    /*
                    Uri uri = data.getData();
                    try {
                        InputStream is = getContentResolver().openInputStream(uri);
                        int numPeaks = syllablesFromFile(is);
                        analyzedFiles.add(new AnalyzedFile(uri.getLastPathSegment(), numPeaks));
                        Log.d("PEAKS", "Detected "+numPeaks + " syllables in File");
                        is.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    */
                    break;
                }
            case(MULTIPLE_FILES):
                /*
                String jsonFilename = "";
                if(data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    int currentItem = 0;
                    // get directory as json filename
                    Uri uri = data.getClipData().getItemAt(currentItem).getUri();
                    jsonFilename = HelperFunctions.getJsonName(uri);
                    ArrayList<Uri> files = new ArrayList<>();
                    while(currentItem < count) {
                        Log.d("PeakResults-Data", "Analyzing file " + String.valueOf(currentItem+1) + " / " + String.valueOf(count));
                        uri = data.getClipData().getItemAt(currentItem).getUri();
                        files.add(uri);
                        currentItem = currentItem + 1;
                    }
                    FolderToAnalyze foo = new FolderToAnalyze(jsonFilename, files);
                    syllableWorker.sendMessage(foo);
                } else if(data.getData() != null) {
                    Uri uri = data.getData();
                    jsonFilename = HelperFunctions.getJsonName(uri);
                    FolderToAnalyze foo = new FolderToAnalyze(jsonFilename, uri);
                    syllableWorker.sendMessage(foo);
                }
                */
                break;
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
        //int numPeaks = mSyllableDetectorWorker.countPeaks();
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

                    Message msg = mHandler.obtainMessage();
                    msg.obj = audioBuffer;
                    mHandler.sendMessage(msg);
                    /*
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
                    */
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

    private void requestStoragePermission() {
        //check API version, do nothing if API version < 23!
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion > android.os.Build.VERSION_CODES.LOLLIPOP){

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
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
    // Checks if external storage is available for read and write
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
*/
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
