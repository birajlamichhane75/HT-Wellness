package com.example.mood;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

// Handles saving/loading mood entries to a CSV file in the working directory.
public class MoodStorage {
    private static final String FILE_NAME = "htwellness_entries.csv";

    // Save one entry (appends)
    public static void saveEntry(MoodEntry entry) throws IOException {
        Path p = Paths.get(FILE_NAME);
        // Ensure parent exists (we're writing to current dir so fine)
        String line = entry.toCSVLine();
        Files.write(p, Collections.singletonList(line), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    // Load all entries (most recent last)
    public static List<MoodEntry> loadEntries() throws IOException {
        Path p = Paths.get(FILE_NAME);
        if (!Files.exists(p)) return new ArrayList<>();
        List<String> lines = Files.readAllLines(p);
        return lines.stream()
                .map(line -> new MoodEntry(line))
                .collect(Collectors.toList());
    }

    // Delete file (clear history)
    public static void clearEntries() throws IOException {
        Path p = Paths.get(FILE_NAME);
        Files.deleteIfExists(p);
    }

    // Export entries to a CSV path (overwrite)
    public static void exportTo(Path target) throws IOException {
        List<MoodEntry> entries = loadEntries();
        List<String> lines = entries.stream().map(MoodEntry::toCSVLine).collect(Collectors.toList());
        Files.write(target, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
