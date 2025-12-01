package com.example.mood;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// Simple helper to save/load moods to a text file
public class MoodStorage {
    private static final String FILE_NAME = "moods.txt";
    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void saveMood(String mood) {
        String time = LocalDateTime.now().format(TF);
        String line = time + " - " + mood;
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(FILE_NAME), 
                java.nio.file.StandardOpenOption.CREATE, 
                java.nio.file.StandardOpenOption.APPEND)) {
            bw.write(line);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> loadMoods() {
        List<String> lines = new ArrayList<>();
        Path p = Paths.get(FILE_NAME);
        if (!Files.exists(p)) return lines;
        try {
            lines = Files.readAllLines(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    public static void clearMoods() {
        try {
            Files.deleteIfExists(Paths.get(FILE_NAME));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
