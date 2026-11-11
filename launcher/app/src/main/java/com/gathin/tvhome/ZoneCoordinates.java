package com.gathin.tvhome;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * The place the sun's times are worked out for (wallpaper_moods, the sky themes): config.xml's latitude and longitude
 * when both are set, otherwise the reference city of the TV's own time zone, from res/raw/zone_coords.txt (the IANA tz
 * database's zone.tab, public domain). "Europe/Berlin" gives Berlin, "America/Chicago" Chicago: near enough for
 * morning, evening and night pictures, with no network, no permission and nothing to set. parse() has no Android
 * types, so it is tested on the JVM.
 */
final class ZoneCoordinates {
    private ZoneCoordinates() { }

    /** {latitude, longitude} as strings for WallpaperMood.sun; {"", ""} when neither config.xml nor the zone gives one. */
    static String[] place(Context context, ZoneId zone) {
        String lat = context.getString(R.string.latitude).trim();
        String lon = context.getString(R.string.longitude).trim();
        if (!lat.isEmpty() && !lon.isEmpty()) return new String[]{lat, lon};
        double[] found = zone == null ? null : read(context, zone.getId());
        return found == null ? new String[]{"", ""}
                : new String[]{Double.toString(found[0]), Double.toString(found[1])};
    }

    private static double[] read(Context context, String zoneId) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                context.getResources().openRawResource(R.raw.zone_coords), StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) lines.add(line);
        } catch (Exception e) {
            return null;
        }
        return parse(lines, zoneId);
    }

    /** The {latitude, longitude} on the line for zoneId ("&lt;zone&gt; &lt;lat&gt; &lt;lon&gt;"), or null without one. */
    static double[] parse(List<String> lines, String zoneId) {
        if (zoneId == null || zoneId.isEmpty()) return null;
        for (String line : lines) {
            if (line == null || line.startsWith("#")) continue;
            String[] parts = line.trim().split("\\s+");
            if (parts.length != 3 || !parts[0].equals(zoneId)) continue;
            try {
                double lat = Double.parseDouble(parts[1]), lon = Double.parseDouble(parts[2]);
                if (Math.abs(lat) > 90 || Math.abs(lon) > 180) return null;
                return new double[]{lat, lon};
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
