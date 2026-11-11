package com.gathin.tvhome;

/**
 * How dark the light glass (the dock band with dock_glass, the status pill) is tinted over a wallpaper picture. White
 * glass over a dark photo reads as frosted glass; over a bright one (snow, sand, a white sky) it vanishes into the
 * picture. WallpaperPlayer measures the picture's brightness where the dock and the pill sit, once per picture, and
 * this turns it into a black tint under the glass. No Android types, so it is tested on the JVM.
 */
final class GlassTone {
    /** Up to this brightness (0 to 1, after wallpaper_dim) the glass stays as it is. */
    static final float CLEAR_UNTIL = 0.22f;
    /** From this brightness on the tint is at its strongest. */
    static final float FULL_FROM = 0.60f;
    /** The strongest tint: 45 % black. */
    static final int MAX_ALPHA = 0x73;

    private GlassTone() { }

    /** The black tint's alpha (0 to MAX_ALPHA) for a region of this mean brightness (0 to 1); below 0 means unknown. */
    static int shadeAlpha(float luminance) {
        if (luminance < 0f || Float.isNaN(luminance)) return 0;
        float t = (luminance - CLEAR_UNTIL) / (FULL_FROM - CLEAR_UNTIL);
        t = Math.max(0f, Math.min(1f, t));
        return Math.round(MAX_ALPHA * t);
    }

    /** Relative luminance (Rec. 709) of an ARGB colour, 0 to 1. */
    static float luminance(int argb) {
        int r = (argb >> 16) & 0xFF, g = (argb >> 8) & 0xFF, b = argb & 0xFF;
        return (0.2126f * r + 0.7152f * g + 0.0722f * b) / 255f;
    }

    /**
     * Mean luminance of a region given as fractions of the picture (left, top, right, bottom), sampled on a grid of
     * about steps × steps points through {@code pixel}, which returns the ARGB colour at (x, y).
     */
    static float regionLuminance(int width, int height, float left, float top, float right, float bottom, int steps,
                                 PixelSource pixel) {
        if (width <= 0 || height <= 0 || steps <= 0) return -1f;
        int x0 = clamp(Math.round(width * left), width), x1 = clamp(Math.round(width * right), width);
        int y0 = clamp(Math.round(height * top), height), y1 = clamp(Math.round(height * bottom), height);
        if (x1 <= x0 || y1 <= y0) return -1f;
        float sum = 0f;
        int count = 0;
        for (int i = 0; i < steps; i++) {
            int y = y0 + (int) ((y1 - y0) * (i + 0.5f) / steps);
            for (int j = 0; j < steps; j++) {
                int x = x0 + (int) ((x1 - x0) * (j + 0.5f) / steps);
                sum += luminance(pixel.at(x, y));
                count++;
            }
        }
        return sum / count;
    }

    private static int clamp(int v, int size) {
        return Math.max(0, Math.min(size - 1, v));
    }

    interface PixelSource {
        int at(int x, int y);
    }
}
