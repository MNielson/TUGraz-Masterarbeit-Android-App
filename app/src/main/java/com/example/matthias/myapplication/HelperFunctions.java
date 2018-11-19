package com.example.matthias.myapplication;

import android.net.Uri;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Matthias on 20.03.2018.
 */

public class HelperFunctions {

    static double[] convertByteToDoubleViaShort(byte[] byteArray){
        return convertShortToDouble(convertByteToShort(byteArray));
    }

    static double[] convertByteToDouble(byte[] byteArray){
        int size = byteArray.length;
        double[] doubleArray = new double[size];
        for (int index = 0; index < size; index++)
            doubleArray[index] = (double) byteArray[index];
        return doubleArray;
    }

    static double[] convertShortToDouble(short[] shortArray){
        int size = shortArray.length;
        double[] doubleArray = new double[size];
        for (int index = 0; index < size; index++)
            doubleArray[index] = (double) shortArray[index];
        return doubleArray;
    }

    static short[] convertByteToShort(byte[] byteArrray)
    {
        if(byteArrray.length % 2 != 0) {
            Log.e("Conversion Error:", "Can only convert byte to short it number of bytes is even.");
            short[] shortArray = new short[byteArrray.length];
            for(int i = 0; i < byteArrray.length; i++)
                shortArray[i] = (short) byteArrray[i];
            return shortArray;
        }
        else
        {
            int shortArrayLen = byteArrray.length / 2;
            short[] shortArray = new short[shortArrayLen];
            for(int i = 0; i < shortArrayLen; i++)
            {
                //
                shortArray[i] = (short)((byteArrray[i*2]<<8) | (byteArrray[i*2+1]));
            }
            return shortArray;
        }

    }

    static byte[] convertShortToByte(short[] shortArray){

        ByteBuffer buffer = ByteBuffer.allocate(shortArray.length * 2);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.asShortBuffer().put(shortArray);
        byte[] bytes = buffer.array();
        return bytes;
    }

    static String generateTextResults(LinkedList<Double> pitches)
    {
        String result = "";
        for (Double pitch: pitches) {
            result += pitch.toString() + "hz\n";
        }
        return result;
    }

    static public String getJsonName(Uri uri) {
        String jsonFilename = uri.getPath();
        int cut = jsonFilename.lastIndexOf('/');
        if (cut != -1) {
            jsonFilename = jsonFilename.substring(0, cut);
        }
        int cut2 = jsonFilename.lastIndexOf('/');
        if (cut2 != -1) {
            jsonFilename = jsonFilename.substring(cut2+1);
        }
        return jsonFilename;
    }

    public static String getFileID(Uri uri) {
        String fileID = uri.getPath();
        int cut = fileID.lastIndexOf('/');
        if (cut != -1) {
            fileID = fileID.substring(cut + 1);
        }
        cut = fileID.lastIndexOf('.');
        if (cut != -1) {
            fileID = fileID.substring(0, cut);
        }
        return fileID;
    }




}
