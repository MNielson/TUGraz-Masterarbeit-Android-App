package com.example.matthias.myapplication;

import android.content.Context;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Matthias on 01.03.2018.
 */

public class AudioFileReader {

    private AudioWorker mAudioWorker;
    private Context mContext;
    public AudioFileReader(AudioWorker aw, Context c){
        mAudioWorker = aw;
        mContext = c;
    }


    public void readAudioFile()
    {

        InputStream ins = mContext.getResources().openRawResource(R.raw.audio1000hzsine3s);
        try {
            byte[] foo = readBytes(ins);
            mAudioWorker.processSample(HelperFunctions.convertByteToDouble(foo));
        } catch (IOException e) {
            Log.d("readAudioFile", "IOException");
            e.printStackTrace();
        }

    }

    public byte[] readBytes(InputStream inputStream) throws IOException {
        // this dynamically extends to take the bytes you read
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        // this is storage overwritten on each iteration with bytes
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        // we need to know how may bytes were read to write them to the byteBuffer
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray();
    }
}
