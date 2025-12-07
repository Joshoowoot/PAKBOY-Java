import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class Leaderboard {
    private static final String FILE_PATH = "highscore.txt";
    private static final int MAX_ENTRIES = 10;

    public static final class Entry {
        public final String name;
        public final int score;

        public Entry(String name, int score) {
            this.name = name;
            this.score = score;
        }
    }

    private Leaderboard() {}

    public static List<Entry> load() {
        List<Entry> entries = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return entries;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Entry parsed = parseLine(line);
                if (parsed != null) {
                    entries.add(parsed);
                }
            }
        } catch (IOException ignored) {
        }

        sortEntries(entries);
        return capEntries(entries);
    }

    public static void recordScore(String name, int score) {
        List<Entry> entries = load();
        entries.add(new Entry(sanitizeName(name), Math.max(0, score)));
        sortEntries(entries);
        save(capEntries(entries));
    }

    public static int getHighscore() {
        List<Entry> entries = load();
        return entries.isEmpty() ? 0 : entries.get(0).score;
    }

    public static String formatForDisplay() {
        List<Entry> entries = load();
        if (entries.isEmpty()) {
            return "No scores yet.";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < entries.size(); i++) {
            Entry e = entries.get(i);
            sb.append(String.format("#%d  %s  -  %d", i + 1, e.name, e.score));
            if (i < entries.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private static Entry parseLine(String rawLine) {
        String line = rawLine.trim();
        if (line.isEmpty()) {
            return null;
        }

        String[] commaParts = line.split(",");
        if (commaParts.length == 2) {
            try {
                int score = Integer.parseInt(commaParts[1].trim());
                return new Entry(sanitizeName(commaParts[0]), score);
            } catch (NumberFormatException ignored) {
            }
        }

        String[] parts = line.split("\\s+");
        if (parts.length >= 2) {
            try {
                int score = Integer.parseInt(parts[1].trim());
                return new Entry(sanitizeName(parts[0]), score);
            } catch (NumberFormatException ignored) {
            }
        }

        try {
            int score = Integer.parseInt(line);
            return new Entry("???", score);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static List<Entry> capEntries(List<Entry> entries) {
        if (entries.size() <= MAX_ENTRIES) {
            return new ArrayList<>(entries);
        }
        return new ArrayList<>(entries.subList(0, MAX_ENTRIES));
    }

    private static void sortEntries(List<Entry> entries) {
        Collections.sort(entries, new Comparator<Entry>() {
            @Override
            public int compare(Entry a, Entry b) {
                return Integer.compare(b.score, a.score);
            }
        });
    }

    private static void save(List<Entry> entries) {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(FILE_PATH)))) {
            for (Entry e : entries) {
                writer.println(e.name + "," + e.score);
            }
        } catch (IOException ignored) {
        }
    }

    private static String sanitizeName(String raw) {
        if (raw == null) {
            return "???";
        }
        String cleaned = raw.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        if (cleaned.isEmpty()) {
            cleaned = "???";
        }
        if (cleaned.length() < 3) {
            while (cleaned.length() < 3) {
                cleaned += "?";
            }
            return cleaned;
        }
        return cleaned.substring(0, 3);
    }
}

// JOSHOOWOOT 12/07/2025