package com.example.matthias.myapplication.SyllableDetector;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.JsonWriter;

import com.example.matthias.myapplication.AnalyzedFile;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import autovalue.shaded.org.apache.commons.lang.ArrayUtils;

public class SyllableDebugJsonWriter {
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void write(OutputStream out, List<SyllableResult> data) throws IOException {
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
        writer.setIndent("  ");
        writeMessagesArray(writer, data);
        writer.close();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static void writeMessagesArray(JsonWriter writer, List<SyllableResult> data) throws IOException {
        writer.beginArray();
        for (SyllableResult s : data) {
            writeMessage(writer, s.debugData);
        }
        writer.endArray();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static void writeMessage(JsonWriter writer, Optional<SyllableDetectorData> d) throws IOException {
        writer.beginObject();
        if(d.isPresent()) {
            writer.name("filename").value(d.get().getFilename());
            writer.name("content");
            writeShortArray(writer, d.get().getContent());
            writer.name("filteredResults");
            write2DDoublesArray(writer, d.get().getFilteredResults());
            writer.name("energyVectors");
            write2DDoublesArray(writer, d.get().getEnergyVectors());
            writer.name("trajectorsValues");
            writeDoublesArray(writer, d.get().getTrajectoryValues());
            writer.name("maxima");
            writeDoublesArray(writer, d.get().getMaxima());
            writer.name("minima");
            writeDoublesArray(writer, d.get().getMinima());
            writer.name("maximaPos");
            writeIntegerArray(writer, d.get().getMaximaPos());
            writer.name("minimaPos");
            writeIntegerArray(writer, d.get().getMinimaPos());
        }
        writer.endObject();
    }

    public static void write2DDoublesArray(JsonWriter writer, List<List<Double>> doubles2D) throws IOException {
        writer.beginArray();
        for (List<Double> doubles : doubles2D) {
            writeDoublesArray(writer, doubles);
        }
        writer.endArray();

    }

    public static void writeDoublesArray(JsonWriter writer, List<Double> doubles) throws IOException {
        writer.beginArray();
        for (Double value : doubles) {
            writer.value(value);
        }
        writer.endArray();
    }

    public static void writeIntegerArray(JsonWriter writer, List<Integer> ints) throws IOException {
        writer.beginArray();
        for (int i : ints) {
            writer.value(i);
        }
        writer.endArray();
    }

    public static void writeShortArray(JsonWriter writer, List<Short> shorts) throws IOException {
        writer.beginArray();
        for (short s : shorts) {
            writer.value(s);
        }
        writer.endArray();
    }
}
