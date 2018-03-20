package com.example.matthias.myapplication;

/**
 * Created by Matthias on 20.03.2018.
 */

public class HelperFunctions {

    static double[] convertByteToDouble(byte[] byteArray){
        int size = byteArray.length;
        double[] doubleArray = new double[size];
        for (int index = 0; index < size; index++)
            doubleArray[index] = (double) byteArray[index];
        return doubleArray;
    }
}
