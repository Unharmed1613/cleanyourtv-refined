package com.gathin.tvhome;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Names for a wallpaper collection (wallpaper_captions): {@code wallpapers/captions.txt}, one
 * "filename<TAB>name" per line. Blank lines, lines starting with # and lines without a tab are skipped; a name
 * is cut to {@link #MAX_LENGTH} characters. A theme can add the names in other languages beside it, as
 * {@code captions-<language>.txt} ({@code captions-de.txt}, {@code captions-fr.txt}); the one for the launcher's
 * language wins, and {@code captions.txt} is the fallback. No Android types, so it is tested on the JVM.
 */
final class WallpaperCaptions {
    static final String FILE_NAME = "captions.txt";
    static final int MAX_LENGTH = 80;
    /** captions.txt, or a translation of it named by a two or three letter language code. */
    private static final Pattern NAME = Pattern.compile("captions(-[a-z]{2,3})?\\.txt");

    private WallpaperCaptions() { }

    static Map<String, String> parse(List<String> lines) {
        Map<String, String> names = new HashMap<>();
        for (String line : lines) {
            if (line == null || line.trim().isEmpty() || line.trim().startsWith("#")) continue;
            int tab = line.indexOf('\t');
            if (tab <= 0) continue;
            String file = line.substring(0, tab).trim();
            String name = line.substring(tab + 1).trim();
            if (file.isEmpty() || name.isEmpty()) continue;
            if (name.length() > MAX_LENGTH) name = name.substring(0, MAX_LENGTH).trim();
            names.put(file, name);
        }
        return names;
    }

    /** True for the names of a collection's captions file and its translations. */
    static boolean isCaptionsFile(String name) {
        return name != null && NAME.matcher(name).matches();
    }

    /** The translated file's name for this language, e.g. "captions-de.txt", or null when there is none. */
    static String fileName(Locale locale) {
        String language = locale == null ? "" : locale.getLanguage().toLowerCase(Locale.ROOT);
        String name = "captions-" + language + ".txt";
        return language.isEmpty() || !isCaptionsFile(name) ? null : name;
    }

    /**
     * The names in {@code folder} for this language: its captions-&lt;language&gt;.txt when the theme has one,
     * otherwise captions.txt; none when neither can be read.
     */
    static Map<String, String> read(File folder, Locale locale) {
        String translated = fileName(locale);
        if (folder != null && translated != null) {
            Map<String, String> names = readFile(new File(folder, translated));
            if (!names.isEmpty()) return names;
        }
        return read(folder);
    }

    /** The names in {@code folder}/captions.txt, or none when the file is missing or unreadable. */
    static Map<String, String> read(File folder) {
        return folder == null ? Collections.emptyMap() : readFile(new File(folder, FILE_NAME));
    }

    private static Map<String, String> readFile(File file) {
        if (file == null || !file.isFile() || file.length() > 256 * 1024) return Collections.emptyMap();
        try {
            return parse(Files.readAllLines(file.toPath(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}
