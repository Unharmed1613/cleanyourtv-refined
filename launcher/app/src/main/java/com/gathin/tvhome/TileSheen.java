package com.gathin.tvhome;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.view.animation.DecelerateInterpolator;

/**
 * glass_style shadow: the light on the focused tile, as its foreground. A soft white sheen over the top of the tile,
 * and on focus one band of light that sweeps across it once, like a glossy card turned towards a lamp. Unfocused
 * tiles draw nothing. The card clips it to its rounded corners (clipToOutline), so it stays on the GPU.
 */
final class TileSheen extends Drawable {

    /** The sheen's strength at the top edge, and the band's at its brightest. */
    private static final int TOP_ALPHA = 0x30, BAND_ALPHA = 0x42;
    private static final int IN_MS = 950, OUT_MS = 160;

    private final Paint top = new Paint();
    private final Paint band = new Paint();
    private final Matrix bandMove = new Matrix();
    /** 0 hidden to 1 fully shown. */
    private float level;
    /** Where the band is, 0 (off the left edge) to 1 (off the right edge); below 0 it is not drawn. */
    private float sweep = -1f;
    private float bandWidth;
    private ValueAnimator animator;

    @Override
    protected void onBoundsChange(Rect b) {
        top.setShader(new LinearGradient(0, b.top, 0, b.top + b.height() * 0.55f,
                0xFFFFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP));
        // A slanted band: brightest in its middle, fading to nothing at both sides.
        bandWidth = b.width() * 0.42f;
        band.setShader(new LinearGradient(0, 0, bandWidth, bandWidth * 0.35f,
                new int[]{0x00FFFFFF, 0xFFFFFFFF, 0x00FFFFFF}, null, Shader.TileMode.CLAMP));
    }

    @Override
    public void draw(Canvas canvas) {
        if (level <= 0f) return;
        Rect b = getBounds();
        top.setAlpha(Math.round(TOP_ALPHA * level));
        canvas.drawRect(b, top);
        if (sweep >= 0f) {
            float travel = reverse ? 1f - sweep : sweep;
            float x = b.left - bandWidth * 1.3f + (b.width() + bandWidth * 2.6f) * travel;
            bandMove.setTranslate(x, b.top);
            band.getShader().setLocalMatrix(bandMove);
            band.setAlpha(Math.round(BAND_ALPHA * level));
            canvas.drawRect(b, band);
        }
    }

    /** Right to left: the focus came from the right (Left pressed), so the light crosses the tile the same way. */
    private boolean reverse;

    /**
     * Focus gained or lost; animated shows the sweep and fades, otherwise the sheen just appears or goes. fromRight
     * runs the sweep right to left, following the remote.
     */
    void focus(boolean focused, boolean animated, boolean fromRight) {
        if (focused) reverse = fromRight;
        if (animator != null) animator.cancel();
        animator = null;
        if (!animated) {
            level = focused ? 1f : 0f;
            sweep = -1f;
            invalidateSelf();
            return;
        }
        float from = level;
        ValueAnimator a = ValueAnimator.ofFloat(0f, 1f);
        a.setDuration(focused ? IN_MS : OUT_MS);
        if (focused) {
            a.addUpdateListener(v -> {
                float f = v.getAnimatedFraction();
                level = Math.min(1f, from + (1f - from) * f / 0.25f);
                // The band starts once the tile has nearly grown, and slows as it leaves.
                float t = (f - 0.18f) / 0.82f;
                sweep = t < 0f || f >= 1f ? -1f : 1f - (1f - t) * (1f - t);
                invalidateSelf();
            });
        } else {
            sweep = -1f;
            a.addUpdateListener(v -> {
                level = from * (1f - v.getAnimatedFraction());
                invalidateSelf();
            });
        }
        a.setInterpolator(focused ? null : new DecelerateInterpolator());
        animator = a;
        a.start();
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
