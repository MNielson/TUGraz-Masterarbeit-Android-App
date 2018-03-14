package com.example.matthias.myapplication;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Matthias on 01.03.2018.
 */

public class AudioFileReader {

    static void readAudioFile()
    {
        File file = new File("--filePath--");
        int shortSizeInBytes = Short.SIZE / Byte.SIZE;
        int bufferSizeInBytes = (int)(file.length() / shortSizeInBytes);
        int i = 0;
        byte[] s = new byte[bufferSizeInBytes];

        try {
            final FileInputStream fin = new FileInputStream(file);
            final DataInputStream dis = new DataInputStream(fin);

            while ((i = dis.read(s, 0, bufferSizeInBytes)) > -1) {
                //read i bytes

            }

        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        } catch (Exception e) {

        }
    }
}
