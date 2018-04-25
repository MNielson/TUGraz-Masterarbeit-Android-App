package com.example.matthias.myapplication;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;

/**
 * Created by Matthias on 20.03.2018.
 */

public class HelperFunctions {

    static Double[] convertByteToDoubleViaShort(byte[] byteArray){
        return convertShortToDouble(convertByteToShort(byteArray));
    }

    static double[] convertByteToDouble(byte[] byteArray){
        int size = byteArray.length;
        double[] doubleArray = new double[size];
        for (int index = 0; index < size; index++)
            doubleArray[index] = (double) byteArray[index];
        return doubleArray;
    }

    static Double[] convertShortToDouble(short[] shortArray){
        int size = shortArray.length;
        Double[] doubleArray = new Double[size];
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


}
