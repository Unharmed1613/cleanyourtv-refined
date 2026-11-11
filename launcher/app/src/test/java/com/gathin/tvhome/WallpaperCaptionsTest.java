package com.gathin.tvhome;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;

public class WallpaperCaptionsTest {

    @Test
    public void readsFilenameTabName() {
        Map<String, String> names = WallpaperCaptions.parse(Arrays.asList(
                "001.jpg\tRed Spider Nebula · Hubble",
                "002.jpg\t  Falcon 9 at sunset  "));
        assertEquals("Red Spider Nebula · Hubble", names.get("001.jpg"));
        assertEquals("Falcon 9 at sunset", names.get("002.jpg"));
    }

    @Test
    public void skipsCommentsBlankAndMalformedLines() {
        Map<String, String> names = WallpaperCaptions.parse(Arrays.asList(
                "# captions for the collection", "", "   ", "no-tab-here.jpg Dusk", "\tno file", "003.jpg\t", null));
        assertTrue(names.isEmpty());
    }

    @Test
    public void cutsVeryLongNames() {
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 30; i++) longName.append("word ");
        Map<String, String> names = WallpaperCaptions.parse(Arrays.asList("004.jpg\t" + longName));
        assertTrue(names.get("004.jpg").length() <= WallpaperCaptions.MAX_LENGTH);
    }

    @Test
    public void laterLineWinsForTheSameFile() {
        Map<String, String> names = WallpaperCaptions.parse(Arrays.asList("005.jpg\tOld", "005.jpg\tNew"));
        assertEquals("New", names.get("005.jpg"));
        assertFalse(names.containsKey("006.jpg"));
    }

    @Test
    public void missingFolderHasNoNames() {
        assertTrue(WallpaperCaptions.read(null).isEmpty());
        assertTrue(WallpaperCaptions.read(null, Locale.GERMAN).isEmpty());
    }

    @Test
    public void translationsAreNamedByLanguage() {
        assertEquals("captions-de.txt", WallpaperCaptions.fileName(Locale.GERMANY));
        assertEquals("captions-pt.txt", WallpaperCaptions.fileName(new Locale("pt", "BR")));
        assertEquals(null, WallpaperCaptions.fileName(Locale.ROOT));
        assertTrue(WallpaperCaptions.isCaptionsFile("captions.txt"));
        assertTrue(WallpaperCaptions.isCaptionsFile("captions-de.txt"));
        assertFalse(WallpaperCaptions.isCaptionsFile("captions-DE.txt"));
        assertFalse(WallpaperCaptions.isCaptionsFile("captions-../x.txt"));
        assertFalse(WallpaperCaptions.isCaptionsFile("captionsxtxt"));
    }

    @Test
    public void theLaunchersLanguageWinsAndCaptionsTxtIsTheFallback() throws Exception {
        File folder = Files.createTempDirectory("captions").toFile();
        try {
            Files.write(new File(folder, "captions.txt").toPath(),
                    Arrays.asList("001.jpg\tKing penguins"), StandardCharsets.UTF_8);
            Files.write(new File(folder, "captions-de.txt").toPath(),
                    Arrays.asList("001.jpg\tKönigspinguine"), StandardCharsets.UTF_8);
            assertEquals("Königspinguine", WallpaperCaptions.read(folder, Locale.GERMANY).get("001.jpg"));
            assertEquals("King penguins", WallpaperCaptions.read(folder, Locale.FRANCE).get("001.jpg"));
            assertEquals("King penguins", WallpaperCaptions.read(folder, null).get("001.jpg"));
        } finally {
            for (File f : folder.listFiles()) f.delete();
            folder.delete();
        }
    }
}
