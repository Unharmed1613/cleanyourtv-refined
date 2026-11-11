package com.gathin.tvhome;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class ZoneCoordinatesTest {
    private static final List<String> LINES = Arrays.asList(
            "# comment",
            "Europe/Berlin 52.50 13.37",
            "America/New_York 40.71 -74.01",
            "Broken/Line 12",
            "Bad/Number north 13.37",
            "Far/Away 95.00 13.37");

    @Test
    public void findsTheZonesCity() {
        assertArrayEquals(new double[]{52.50, 13.37}, ZoneCoordinates.parse(LINES, "Europe/Berlin"), 1e-9);
        assertArrayEquals(new double[]{40.71, -74.01}, ZoneCoordinates.parse(LINES, "America/New_York"), 1e-9);
    }

    @Test
    public void unknownOrBrokenGivesNothing() {
        assertNull(ZoneCoordinates.parse(LINES, "Etc/UTC"));
        assertNull(ZoneCoordinates.parse(LINES, ""));
        assertNull(ZoneCoordinates.parse(LINES, null));
        assertNull(ZoneCoordinates.parse(LINES, "Broken/Line"));
        assertNull(ZoneCoordinates.parse(LINES, "Bad/Number"));
        assertNull(ZoneCoordinates.parse(LINES, "Far/Away"));
        assertNull(ZoneCoordinates.parse(LINES, "# comment"));
    }

    @Test
    public void shippedListHasCommonZones() throws Exception {
        File raw = new File("src/main/res/raw/zone_coords.txt");
        List<String> shipped = Files.readAllLines(raw.toPath(), StandardCharsets.UTF_8);
        assertArrayEquals(new double[]{52.50, 13.37}, ZoneCoordinates.parse(shipped, "Europe/Berlin"), 1e-9);
        assertArrayEquals(new double[]{48.22, 16.33}, ZoneCoordinates.parse(shipped, "Europe/Vienna"), 1e-9);
        assertArrayEquals(new double[]{-33.87, 151.22}, ZoneCoordinates.parse(shipped, "Australia/Sydney"), 1e-9);
    }
}
