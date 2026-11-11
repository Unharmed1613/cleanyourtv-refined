package com.gathin.tvhome;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GlassToneTest {
    @Test
    public void darkPicturesLeaveTheGlassClear() {
        assertEquals(0, GlassTone.shadeAlpha(0.05f));
        assertEquals(0, GlassTone.shadeAlpha(GlassTone.CLEAR_UNTIL));
    }

    @Test
    public void brightPicturesGetTheFullTint() {
        assertEquals(GlassTone.MAX_ALPHA, GlassTone.shadeAlpha(GlassTone.FULL_FROM));
        assertEquals(GlassTone.MAX_ALPHA, GlassTone.shadeAlpha(1f));
    }

    @Test
    public void inBetweenGrowsSteadily() {
        int mid = GlassTone.shadeAlpha((GlassTone.CLEAR_UNTIL + GlassTone.FULL_FROM) / 2f);
        assertEquals(Math.round(GlassTone.MAX_ALPHA / 2f), mid, 1);
    }

    @Test
    public void unknownBrightnessLeavesTheGlassClear() {
        assertEquals(0, GlassTone.shadeAlpha(-1f));
        assertEquals(0, GlassTone.shadeAlpha(Float.NaN));
    }

    @Test
    public void regionReadsOnlyItsOwnPart() {
        // White in the bottom half, black above it.
        GlassTone.PixelSource half = (x, y) -> y >= 50 ? 0xFFFFFFFF : 0xFF000000;
        assertEquals(1f, GlassTone.regionLuminance(100, 100, 0f, 0.5f, 1f, 1f, 8, half), 0.001f);
        assertEquals(0f, GlassTone.regionLuminance(100, 100, 0f, 0f, 1f, 0.45f, 8, half), 0.001f);
    }

    @Test
    public void emptyRegionIsUnknown() {
        assertEquals(-1f, GlassTone.regionLuminance(0, 0, 0f, 0f, 1f, 1f, 8, (x, y) -> 0), 0f);
        assertEquals(-1f, GlassTone.regionLuminance(100, 100, 0.5f, 0.5f, 0.5f, 0.6f, 8, (x, y) -> 0), 0f);
    }
}
