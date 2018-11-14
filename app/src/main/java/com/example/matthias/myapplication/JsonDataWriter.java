package com.example.matthias.myapplication;

import android.util.JsonWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

public class JsonDataWriter {
    public static void write(OutputStream out, List<AnalyzedFile> files) throws IOException {
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
        writer.setIndent("  ");
        writeMessagesArray(writer, files);
        writer.close();
    }

    private static void writeMessagesArray(JsonWriter writer, List<AnalyzedFile> files) throws IOException {
        writer.beginArray();
        for (AnalyzedFile file : files) {
            writeMessage(writer, file);
        }
        writer.endArray();
    }

    private static void writeMessage(JsonWriter writer, AnalyzedFile file) throws IOException {
        writer.beginObject();
        writer.name("fID").value(file.getFileID());
        writer.name("syllables").value(file.getSyllables());
        writer.endObject();
    }
}
