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
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.example.matthias.myapplication.BufferProcessor.BufferProcessor;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    //////////////////
    //REQUEST CODES //
    /////////////////
    private static final int SINGLE_FILE    = 1;
    private static final int MULTIPLE_FILES = 3;

    final String LOG_TAG = "Audio-Recording";
    boolean mShouldContinue;

    private BufferProcessor mBufferProcessor;
    private Handler mHandler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mBufferProcessor = new BufferProcessor(this);
        mHandler = new Handler(Looper.getMainLooper()) {
            /*
         * handleMessage() defines the operations to perform when
         * the Handler receives a new Message to process.
         */
            @Override
            public void handleMessage(Message inputMessage) {
                short[] audioBuffer = (short[]) inputMessage.obj;
                mBufferProcessor.process(audioBuffer);

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
                        byte[] tbytes = IOUtils.toByteArray(is);
                        //skip wav header
                        byte[] bytes = new byte[tbytes.length - 44];
                        bytes = Arrays.copyOfRange(tbytes, 44, tbytes.length);
                        short[] content = new short[bytes.length / 2];
                        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(content);


                        int numBuffers = (int) Math.round(Math.ceil((double)content.length / Constants.ONE_BUFFER_LEN));
                        boolean lastNeedsPadding = (((double)content.length / Constants.ONE_BUFFER_LEN) % 1 != 0);
                        for(int i = 0; i < numBuffers; i++){
                            short[] b = new short[Constants.ONE_BUFFER_LEN];
                            if((i+1) == numBuffers && lastNeedsPadding) {
                                Arrays.fill(b, (short) 0);
                                b = Arrays.copyOfRange(content, i * Constants.ONE_BUFFER_LEN, content.length);
                            }
                            else
                                b = Arrays.copyOfRange(content, i * Constants.ONE_BUFFER_LEN, (i+1) * Constants.ONE_BUFFER_LEN);
                            mBufferProcessor.process(b);
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

                    break;
                }
            case(MULTIPLE_FILES):
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
    }

    void recordAudio() throws IOException {

        new Thread(new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

                // buffer size in bytes
                int bufferSize = AudioRecord.getMinBufferSize(Constants.SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);

                if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
                    bufferSize = Constants.ONE_BUFFER_LEN * 2;
                }
                if (bufferSize < Constants.ONE_BUFFER_LEN * 2)
                    bufferSize = Constants.ONE_BUFFER_LEN * 2;

                short[] audioBuffer = new short[bufferSize / 2];

                AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                        Constants.SAMPLE_RATE,
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
