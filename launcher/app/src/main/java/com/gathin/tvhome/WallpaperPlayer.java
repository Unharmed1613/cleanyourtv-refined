package com.gathin.tvhome;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Displays and rotates a theme's local wallpaper images while Home is visible. */
final class WallpaperPlayer {
    private final FrameLayout root;
    private final ImageView image;
    private final int backgroundColor;
    private final List<File> files;
    private final long interval;
    private final String order;
    private final SharedPreferences preferences;
    private final ExecutorService decoder = Executors.newSingleThreadExecutor();
    private final Random random = new Random();
    private final int width;
    /** config wallpaper_true_color: ARGB_8888 instead of RGB_565. */
    private final Bitmap.Config bitmapConfig;
    private final int height;
    /** The size each picture is decoded to cover: the screen, or the screen at the zoom's far end with drift. */
    private final int decodeWidth, decodeHeight;
    /** wallpaper_dim as an ARGB black, painted into each picture once instead of a full-screen shade view. */
    private final int dimColor;
    /** The picture on screen, released once the next one has been drawn. */
    private Bitmap current;
    /** How long a rotation's next picture takes to fade in over the last one. */
    private static final int FADE_MS = 900;
    /**
     * Where the light glass sits over the picture, as fractions of the screen (left, top, right, bottom): the dock
     * band in the lower third and the status pill at the top right. GlassTone tints the glass by their brightness.
     */
    private static final float[] DOCK_AREA = {0.06f, 0.69f, 0.94f, 0.90f};
    private static final float[] PILL_AREA = {0.74f, 0.04f, 0.95f, 0.13f};
    /** The tone listener and the last picture's {dock, pill} brightness (0 to 1; -1 unknown), measured in decode(). */
    private ToneListener toneListener;
    /** wallpaper_drift: the slow zoom over each picture, in on one and back out on the next. */
    private final boolean drift;
    private boolean zoomIn = true;
    private static final float DRIFT_SCALE = 1.06f;
    /**
     * The zoom moves the picture's edge by a tenth of a pixel per step, so 15 steps a second look as smooth as 60 and
     * Home is not redrawn at the full frame rate the whole time it is on screen.
     */
    private static final long DRIFT_STEP_MS = 66;
    private float driftFrom, driftTo;
    private long driftStart, driftLength;
    private final Runnable driftStep = new Runnable() {
        @Override
        public void run() {
            float f = driftLength <= 0 ? 1f
                    : Math.min(1f, (android.os.SystemClock.uptimeMillis() - driftStart) / (float) driftLength);
            float scale = driftFrom + (driftTo - driftFrom) * f;
            image.setScaleX(scale);
            image.setScaleY(scale);
            if (f < 1f) image.postDelayed(this, DRIFT_STEP_MS);
        }
    };
    private volatile float decodedDockTone = -1f, decodedPillTone = -1f;
    private float dockTone = -1f, pillTone = -1f;
    private boolean active;
    private boolean closed;
    private boolean loading;
    private String displayed = "";
    /** wallpaper_captions: the shown picture's name from wallpapers/captions.txt; null when off or there are none. */
    private final TextView caption;
    private final Map<String, String> names;
    /** wallpaper_moods: the collection's groups by time of day; empty rotates through all of it. */
    private final Map<String, Set<String>> moods;
    /** config.xml latitude and longitude for the sun's times, and today's {sunrise, sunset} once worked out. */
    private final String latitude, longitude;
    private LocalDate sunDay;
    private int[] sun;

    WallpaperPlayer(Context context, FrameLayout root, Config config) {
        this.root = root;
        backgroundColor = config.backgroundColor;
        // Without wallpaperEnabled the collection is the clock screensaver's alone (clock_wallpaper): Home neither shows
        // nor rotates it.
        files = config.wallpaperEnabled ? new ArrayList<>(config.wallpapers) : new ArrayList<>();
        interval = config.wallpaperRotationMinutes * 60_000L;
        drift = config.wallpaperDrift && interval > 0;
        bitmapConfig = config.wallpaperTrueColor ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        // The dim is painted into the picture (decode), so no full-screen shade layer is drawn on every frame.
        dimColor = config.wallpaperDim > 0 ? (config.wallpaperDim * 255 / 100) << 24 : 0;
        order = config.wallpaperOrder;
        preferences = context.getSharedPreferences("wallpaper_rotation", Context.MODE_PRIVATE);
        moods = config.wallpaperMoods;
        // config.xml's place, or the reference city of the TV's time zone (ZoneCoordinates).
        String[] place = moods.isEmpty() ? new String[]{"", ""} : ZoneCoordinates.place(context, ZoneId.systemDefault());
        latitude = place[0];
        longitude = place[1];
        // Playback state belongs to one collection. Changing the collection starts from its first
        // picture instead of resuming a filename that belonged to the previous one.
        String collection = collectionKey(files);
        if (!collection.equals(preferences.getString("collection", ""))) {
            preferences.edit().putString("collection", collection)
                    .remove("image").remove("changed").apply();
        }
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        width = metrics.widthPixels;
        height = metrics.heightPixels;
        // wallpaper_drift: decode the picture as large as it gets at the zoom's far end, so the zoom only ever
        // shrinks a sharp picture into the screen and never stretches one past its pixels.
        decodeWidth = drift ? (int) Math.ceil(width * DRIFT_SCALE) : width;
        decodeHeight = drift ? (int) Math.ceil(height * DRIFT_SCALE) : height;
        image = new ImageView(context);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        names = config.wallpaperCaptions && config.wallpaperEnabled && !files.isEmpty()
                ? WallpaperCaptions.read(files.get(0).getParentFile(),
                        context.getResources().getConfiguration().getLocales().get(0)) : Collections.emptyMap();
        caption = names.isEmpty() ? null : makeCaption(context, root, config.statusPill);
        if (config.wallpaperEnabled && !files.isEmpty()) {
            root.addView(image, 0, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            showFirst();
        }
    }

    /**
     * The picture Home opens with is decoded right here, on the main thread, as the single wallpaper always was:
     * Home's first frame already has it instead of drawing the plain colour and the picture popping in later.
     * Only the later changes of a rotation decode in the background.
     */
    private void showFirst() {
        String previous = preferences.getString("image", "");
        List<File> playable = playable();
        File first = WallpaperRotation.find(playable, previous);
        if (first == null) first = WallpaperRotation.pick(playable, previous, order, random);
        while (first != null) {
            Bitmap bitmap = decode(first);
            if (bitmap != null) {
                show(bitmap, false);
                // The picture covers the whole screen: the root's plain colour under it is overdraw.
                root.setBackground(null);
                displayed = first.getName();
                showCaption(false);
                if (!displayed.equals(previous)) {
                    preferences.edit().putString("image", displayed)
                            .putLong("changed", System.currentTimeMillis()).apply();
                }
                return;
            }
            files.remove(first);   // unreadable or over 4K: try another, or keep the plain colour
            playable = playable();
            first = WallpaperRotation.pick(playable, previous, order, random);
        }
    }

    void start() {
        active = true;
        update();
        if (drift && current != null) driftOn(false);
    }

    void stop() {
        active = false;
        image.removeCallbacks(driftStep);   // the zoom only runs while Home is on screen; start() carries it on
    }

    /** home_entrance: the picture fades in from black. */
    void fadeIn(int ms) {
        if (current == null) return;
        image.setAlpha(0f);
        image.animate().alpha(1f).setDuration(ms).start();
    }

    /**
     * wallpaper_drift: zoom towards the next end (1 or DRIFT_SCALE) over one interval. A new picture (next) turns the
     * direction round; a restart after a pause carries on the same way from where the picture stands.
     */
    private void driftOn(boolean next) {
        if (!drift) return;
        if (next) zoomIn = !zoomIn;
        driftTo = zoomIn ? DRIFT_SCALE : 1f;
        driftFrom = image.getScaleX();
        driftLength = Math.max(1000L, Math.round(interval * Math.abs(driftTo - driftFrom) / (DRIFT_SCALE - 1f)));
        driftStart = android.os.SystemClock.uptimeMillis();
        image.removeCallbacks(driftStep);
        image.post(driftStep);
    }

    /** Drops the wallpaper texture when Home is hidden and Android asks the process to free memory. */
    void releaseForMemoryPressure() {
        if (current == null) return;
        image.setImageDrawable(null);
        // The renderer may still have a queued frame that references this hardware bitmap. Drop our references and
        // let Android reclaim its native buffer after rendering releases it instead of recycling it mid-frame.
        current = null;
        displayed = "";
        root.setBackgroundColor(backgroundColor);
    }

    /** The pictures the rotation picks from now: wallpaper_moods' group for the time of day, or the whole collection. */
    private List<File> playable() {
        if (moods.isEmpty()) return files;
        LocalDateTime now = LocalDateTime.now();
        if (!now.toLocalDate().equals(sunDay)) {
            sunDay = now.toLocalDate();
            sun = WallpaperMood.sun(sunDay, latitude, longitude, ZoneId.systemDefault());
        }
        return WallpaperMood.select(files, moods, WallpaperMood.timeOfDay(now.getHour() * 60 + now.getMinute(), sun[0], sun[1]));
    }

    /** The Control Centre hangs where the caption sits: hidden while it is open, back once Home is in front again. */
    void hideCaption(boolean hidden) {
        if (caption != null) caption.setVisibility(hidden ? View.INVISIBLE : View.VISIBLE);
    }

    void close() {
        active = false;
        closed = true;
        decoder.shutdownNow();
    }

    void update() {
        if (!active || closed || loading || files.isEmpty()) return;
        String previous = preferences.getString("image", "");
        // A picture outside the time of day's group (the group changed) is replaced at once, not after the interval.
        List<File> playable = playable();
        File selected = WallpaperRotation.find(playable, previous);
        long changed = preferences.getLong("changed", 0);
        if (selected == null || WallpaperRotation.due(changed, System.currentTimeMillis(), interval)) {
            selected = WallpaperRotation.pick(playable, previous, order, random);
        }
        if (selected == null || selected.getName().equals(displayed)) return;
        File next = selected;
        loading = true;
        decoder.execute(() -> {
            Bitmap bitmap = decode(next);
            root.post(() -> {
                loading = false;
                if (closed || !active) {
                    if (bitmap != null) bitmap.recycle();
                    return;
                }
                if (bitmap == null) {
                    files.remove(next);
                    Log.w("TvHome.Wallpaper", "Skipping unreadable image: " + next.getName());
                    update();
                    return;
                }
                show(bitmap, true);
                displayed = next.getName();
                showCaption(true);
                if (!displayed.equals(previous) || changed == 0) {
                    preferences.edit().putString("image", displayed)
                            .putLong("changed", System.currentTimeMillis()).apply();
                }
            });
        });
    }

    /**
     * Small light text under the top-right buttons, above everything else and never focusable. It sits where the
     * top bar ends in every layout, so it does not cover the clock, the stats line or the tiles.
     */
    private static TextView makeCaption(Context context, FrameLayout root, boolean underPill) {
        float dp = context.getResources().getDisplayMetrics().density;
        TextView text = new TextView(context);
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        text.setTextColor(0xD9FFFFFF);
        text.setShadowLayer(6 * dp, 0, dp, 0xCC000000);
        text.setSingleLine(true);
        text.setEllipsize(TextUtils.TruncateAt.END);
        text.setMaxWidth(Math.round(520 * dp));
        text.setFocusable(false);
        text.setAlpha(0f);
        try {
            text.setTypeface(UiFont.get(context, R.string.font_regular));
        } catch (Exception ignored) {
            // the system font is fine
        }
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.END);
        // status_pill is a single slim row: the name sits just under it rather than under the taller header.
        params.topMargin = Math.round((underPill ? 80 : 110) * dp);
        params.setMarginEnd(Math.round(62 * dp));
        root.addView(text, params);
        return text;
    }

    /** The displayed picture's name, faded across a change; a picture without a name hides the caption. */
    private void showCaption(boolean fade) {
        if (caption == null) return;
        String name = names.get(displayed);
        caption.animate().cancel();
        if (name == null) {
            caption.setAlpha(0f);
            return;
        }
        caption.setContentDescription(name);
        if (!fade || caption.getAlpha() == 0f) {
            caption.setText(name);
            caption.setAlpha(1f);
            return;
        }
        caption.animate().alpha(0f).setDuration(250).withEndAction(() -> {
            caption.setText(name);
            caption.animate().alpha(1f).setDuration(350).start();
        }).start();
    }

    /** Names plus sizes: enough to notice a different collection without hashing every image. */
    private static String collectionKey(List<File> images) {
        StringBuilder key = new StringBuilder();
        for (File file : images) key.append(file.getName()).append(':').append(file.length()).append(';');
        return key.toString();
    }

    /** Told the brightness behind the dock and the pill each time a picture goes on screen. */
    interface ToneListener {
        void onWallpaperTone(float dock, float pill);
    }

    /** Sets the listener and tells it the picture already on screen, if there is one. */
    void setToneListener(ToneListener listener) {
        toneListener = listener;
        if (listener != null && current != null) listener.onWallpaperTone(dockTone, pillTone);
    }

    /**
     * Puts a picture on screen and releases the previous one once it is no longer drawn. A rotation's next picture
     * (fade) fades in over the last one; Home's first picture is simply there.
     */
    private void show(Bitmap bitmap, boolean fade) {
        Bitmap old = current;
        current = bitmap;
        // The wallpaper covers the whole root; avoid drawing the fallback colour under it.
        root.setBackground(null);
        BitmapDrawable next = new BitmapDrawable(root.getResources(), bitmap);
        Drawable previous = image.getDrawable();
        if (previous instanceof TransitionDrawable) previous = ((TransitionDrawable) previous).getDrawable(1);
        if (fade && previous != null && old != null) {
            TransitionDrawable crossFade = new TransitionDrawable(new Drawable[]{previous, next});
            crossFade.setCrossFadeEnabled(true);
            image.setImageDrawable(crossFade);
            crossFade.startTransition(FADE_MS);
            // The finished fade still holds the old picture: swap it for the new one alone before recycling it.
            image.postDelayed(() -> {
                if (image.getDrawable() == crossFade) image.setImageDrawable(next);
                if (old != current) old.recycle();
            }, FADE_MS + 400);
        } else {
            image.setImageDrawable(next);
            if (old != null && old != bitmap) image.postDelayed(old::recycle, 500);
        }
        if (drift && active) driftOn(fade);
        dockTone = decodedDockTone;
        pillTone = decodedPillTone;
        if (toneListener != null) toneListener.onWallpaperTone(dockTone, pillTone);
    }

    private Bitmap decode(File file) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getPath(), options);
            int width = options.outWidth, height = options.outHeight;
            if (width <= 0 || height <= 0) return null;
            int sample = WallpaperSize.sampleSize(width, height, decodeWidth, decodeHeight);
            if (!WallpaperSize.accepted(width / sample, height / sample)) return null;
            options.inJustDecodeBounds = false;
            options.inSampleSize = sample;
            int[] scale = WallpaperSize.coverScale(width / sample, height / sample, decodeWidth, decodeHeight);
            if (scale != null) {
                options.inScaled = true;
                options.inDensity = scale[0];
                options.inTargetDensity = scale[1];
            }
            options.inPreferredConfig = bitmapConfig;
            options.inMutable = dimColor != 0;
            Bitmap decoded = BitmapFactory.decodeFile(file.getPath(), options);
            if (decoded == null) return null;
            if (dimColor != 0) new Canvas(decoded).drawColor(dimColor);
            // Measured here, while the picture is still a software bitmap: a hardware one cannot be read back.
            int w = decoded.getWidth(), h = decoded.getHeight();
            decodedDockTone = GlassTone.regionLuminance(w, h, DOCK_AREA[0], DOCK_AREA[1], DOCK_AREA[2], DOCK_AREA[3], 24,
                    decoded::getPixel);
            decodedPillTone = GlassTone.regionLuminance(w, h, PILL_AREA[0], PILL_AREA[1], PILL_AREA[2], PILL_AREA[3], 12,
                    decoded::getPixel);
            // One copy in GPU memory instead of a software bitmap plus its texture; the decode stays off the UI
            // thread for rotations, and Home's return no longer uploads 4-8 MB to the GPU.
            Bitmap gpu = decoded.copy(Bitmap.Config.HARDWARE, false);
            if (gpu == null) return decoded;
            decoded.recycle();
            return gpu;
        } catch (Exception | OutOfMemoryError failure) {
            return null;
        }
    }
}
