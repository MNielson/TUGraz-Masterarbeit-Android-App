package com.example.matthias.myapplication;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedList;

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


    public LinkedList<Double> readPitchFromAudioFile()
    {

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        InputStream is = mContext.getResources().openRawResource(R.raw.audio1000hzsine3sraw);
        byte[] temp = new byte[1024];
        int read;

        try {
            while((read = is.read(temp)) >= 0){
                buffer.write(temp, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] bytes = buffer.toByteArray();
        short[] shorts = new short[bytes.length/2];
        // to turn bytes to shorts as either big endian or little endian.
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
        // TODO: fix
        //LinkedList<Double> pitches = mAudioWorker.computePitches(HelperFunctions.convertShortToDouble(shorts));
        LinkedList<Double> pitches = new LinkedList<>();
        return pitches;
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
