package com.example.mood;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Simple data holder for a mood entry
public class MoodEntry {
    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final LocalDateTime timestamp;
    private final String moodText;
    private final int rating; // 1..5
    private final String reflection;

    public MoodEntry(LocalDateTime timestamp, String moodText, int rating, String reflection) {
        this.timestamp = timestamp;
        this.moodText = moodText == null ? "" : moodText;
        this.rating = rating;
        this.reflection = reflection == null ? "" : reflection;
    }

    public MoodEntry(String csvLine) {
        // Expect CSV quoted fields: "timestamp","mood","rating","reflection"
        // Very simple parser for our saved format:
        String[] parts = splitCsvLine(csvLine);
        LocalDateTime ts = LocalDateTime.now();
        String mood = "";
        int r = 0;
        String refl = "";
        try {
            if (parts.length > 0 && !parts[0].isEmpty()) ts = LocalDateTime.parse(unquote(parts[0]), TF);
            if (parts.length > 1) mood = unquote(parts[1]);
            if (parts.length > 2) r = Integer.parseInt(unquote(parts[2]));
            if (parts.length > 3) refl = unquote(parts[3]);
        } catch (Exception e) {
            // if parsing fails, keep reasonable defaults
        }
        this.timestamp = ts;
        this.moodText = mood;
        this.rating = r;
        this.reflection = refl;
    }

    private static String[] splitCsvLine(String line) {
        // extremely simple split that assumes our saving uses quoted fields and no embedded quotes
        // We'll split on "," sequences that are outside quotes - but for simplicity here, we assume format is consistent
        return line.split("\",\"|-?"); // fallback (won't be used often)
    }

    private static String unquote(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            s = s.substring(1, s.length()-1);
        }
        return s.replace("\\n", "\n");
    }

    public String toCSVLine() {
        // produce: "yyyy-MM-dd HH:mm:ss","mood text","rating","reflection"
        return quote(timestamp.format(TF)) + "," + quote(moodText) + "," + quote(Integer.toString(rating)) + "," + quote(reflection);
    }

    private static String quote(String s) {
        if (s == null) s = "";
        // escape newline for safer single-line CSV; keep simple
        s = s.replace("\n", "\\n").replace("\"", "'");
        return "\"" + s + "\"";
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public String getMoodText() { return moodText; }
    public int getRating() { return rating; }
    public String getReflection() { return reflection; }

    @Override
    public String toString() {
        return timestamp.format(TF) + " - (" + rating + ") " + moodText + (reflection.isEmpty() ? "" : " — " + reflection);
    }
}
