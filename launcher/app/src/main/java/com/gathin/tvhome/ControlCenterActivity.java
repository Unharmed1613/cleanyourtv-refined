package com.gathin.tvhome;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * status_pill's Control Centre (control_center): the gear's one menu, a glass panel hanging under the pill on Home.
 * The volume (Left and Right on its row, where the box sets it itself); a row of round quick buttons for the clock
 * screensaver, Bluetooth, display, network and the system power menu (while HomeKeyService is bound); then the
 * list: Inputs (only on a set that has some), Reorder apps, Module cards (only when an app offers one), Launcher
 * settings and TV Settings. Everything the gear's hold menu had, without a hold, which some remotes never send.
 * Translucent, so Home stays behind it; Back, or anything else taking the screen, closes it.
 */
public final class ControlCenterActivity extends Activity {
    /** Where MainActivity's pill is: the panel's distance from the right edge and from the top, in px. */
    static final String EXTRA_END = "end", EXTRA_TOP = "top";
    /** What MainActivity found worth offering: inputs to switch to, apps to move, module cards, a vendor settings app. */
    static final String EXTRA_INPUTS = "inputs", EXTRA_ARRANGE = "arrange", EXTRA_MODULES = "modules",
            EXTRA_VENDOR_SETTINGS = "vendor_settings";

    private AudioManager audio;
    private View volumeFill;
    private View volumeTrack;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LauncherLocale.wrap(base));   // locale
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_control_center);
        View panel = findViewById(R.id.panel);
        Intent from = getIntent();
        FrameLayout.LayoutParams position = (FrameLayout.LayoutParams) panel.getLayoutParams();
        if (from.hasExtra(EXTRA_END)) position.setMarginEnd(Math.max(0, from.getIntExtra(EXTRA_END, 0)));
        if (from.hasExtra(EXTRA_TOP)) position.topMargin = Math.max(0, from.getIntExtra(EXTRA_TOP, 0));
        panel.setLayoutParams(position);
        findViewById(R.id.scrim).setOnClickListener(v -> finish());

        audio = getSystemService(AudioManager.class);
        View volume = findViewById(R.id.volume);
        volumeTrack = findViewById(R.id.volumeTrack);
        volumeFill = findViewById(R.id.volumeFill);
        if (audio == null || audio.isVolumeFixed()) {
            // The box passes its sound on at full level (HDMI) and the TV's own volume is the one that counts.
            volume.setVisibility(View.GONE);
        } else {
            volume.setContentDescription(getString(R.string.cc_volume));
            volume.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode != KeyEvent.KEYCODE_DPAD_LEFT && keyCode != KeyEvent.KEYCODE_DPAD_RIGHT) return false;
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
                            ? AudioManager.ADJUST_RAISE : AudioManager.ADJUST_LOWER, 0);
                    showVolume();
                }
                return true;   // the panel's only column: Left and Right have nowhere else to go
            });
            volumeTrack.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> showVolume());
        }

        panel.setContentDescription(getString(R.string.control_center));
        LinearLayout quick = findViewById(R.id.quick);
        quickLabel = findViewById(R.id.quickLabel);
        addQuick(quick, R.drawable.ic_moon, R.string.cc_screensaver, () -> start(new Intent(this, ClockActivity.class)));
        addQuick(quick, R.drawable.ic_bluetooth, R.string.cc_bluetooth,
                () -> startFresh(SystemSettings.intent(this, Settings.ACTION_BLUETOOTH_SETTINGS)));
        addQuick(quick, R.drawable.ic_picture, R.string.quick_picture, () -> startFresh(SystemSettings.displayIntent(this)));
        addQuick(quick, R.drawable.ic_wifi, R.string.quick_network,
                () -> startFresh(SystemSettings.intent(this, Settings.ACTION_WIFI_SETTINGS)));
        if (HomeKeyService.canOpenPowerMenu()) {
            addQuick(quick, R.drawable.ic_power, R.string.cc_power, HomeKeyService::openPowerMenu);
        }

        LinearLayout rows = findViewById(R.id.rows);
        // Rows for Home itself go back through MainActivity, which acts once this panel has gone.
        if (from.getBooleanExtra(EXTRA_INPUTS, false)) {
            addRow(rows, R.drawable.ic_input, R.string.inputs, () -> start(
                    new Intent(this, MainActivity.class).setAction(MainActivity.ACTION_VIEW_INPUTS)));
        }
        if (from.getBooleanExtra(EXTRA_ARRANGE, false)) {
            addRow(rows, R.drawable.ic_swap_vert, R.string.reorder, () -> homeCommand(MainActivity.COMMAND_ARRANGE));
        }
        if (from.getBooleanExtra(EXTRA_MODULES, false)) {
            addRow(rows, R.drawable.ic_apps, R.string.module_cards, () -> homeCommand(MainActivity.COMMAND_MODULES));
        }
        // Through Home, so it rebuilds with the new choices when Launcher settings closes.
        addRow(rows, R.drawable.ic_home_settings, R.string.launcher_settings, () -> start(
                new Intent(this, MainActivity.class).setAction(MainActivity.ACTION_LAUNCHER_SETTINGS)));
        if (from.getBooleanExtra(EXTRA_VENDOR_SETTINGS, false)) {
            addRow(rows, R.drawable.ic_settings, R.string.native_settings, () -> {
                Intent vendor = SystemSettings.vendorIntent(this);
                if (vendor != null) start(vendor);
            });
        }
        // TV Settings on its main page, as the gear used to open it.
        addRow(rows, R.drawable.ic_settings, R.string.settings,
                () -> startFresh(SystemSettings.intent(this, Settings.ACTION_SETTINGS)));
        // Down from the volume lands on the first quick button, not the one that happens to sit under the middle.
        View firstQuick = quick.getChildAt(0);
        firstQuick.setId(View.generateViewId());
        volume.setNextFocusDownId(firstQuick.getId());
        (volume.getVisibility() == View.VISIBLE ? volume : firstQuick).requestFocus();

        // Drops in from the pill.
        float rise = 10 * getResources().getDisplayMetrics().density;
        panel.setAlpha(0f);
        panel.setTranslationY(-rise);
        panel.animate().alpha(1f).translationY(0f).setDuration(200)
                .setInterpolator(new DecelerateInterpolator()).start();
    }

    /** Anything else taking the screen (an app, a screensaver, the power menu's own window) closes the panel. */
    @Override
    @SuppressWarnings("deprecation")
    protected void onPause() {
        super.onPause();
        finish();
        overridePendingTransition(0, 0);
    }

    private void showVolume() {
        int max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int now = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        int width = volumeTrack.getWidth();
        ViewGroup.LayoutParams lp = volumeFill.getLayoutParams();
        int fill = max <= 0 ? 0 : Math.round((float) width * now / max);
        if (lp.width != fill) {
            lp.width = fill;
            volumeFill.setLayoutParams(lp);
        }
    }

    private TextView quickLabel;

    /** A round quick button; its name shows under the row while it has focus. */
    private void addQuick(LinearLayout quick, int icon, int title, Runnable action) {
        float density = getResources().getDisplayMetrics().density;
        ImageView button = new ImageView(this);
        int size = Math.round(46 * density), pad = Math.round(12 * density), gap = Math.round(5 * density);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
        lp.setMarginStart(gap);
        lp.setMarginEnd(gap);
        button.setLayoutParams(lp);
        button.setPadding(pad, pad, pad, pad);
        button.setImageResource(icon);
        // pill_text's colours: dark on the white focused circle, white on the glass.
        button.setImageTintList(new android.content.res.ColorStateList(
                new int[][]{new int[]{android.R.attr.state_focused}, new int[]{}},
                new int[]{0xFF111111, 0xFFFFFFFF}));
        button.setBackgroundResource(R.drawable.cc_round_bg);
        button.setFocusable(true);
        button.setClickable(true);
        String name = getString(title);
        button.setContentDescription(name);
        button.setOnFocusChangeListener((v, has) -> {
            if (has) quickLabel.setText(name);
            else if (name.contentEquals(quickLabel.getText())) quickLabel.setText("");
        });
        button.setOnClickListener(v -> {
            action.run();
            finish();
        });
        quick.addView(button);
    }

    private void homeCommand(String command) {
        start(new Intent(this, MainActivity.class).setAction(MainActivity.ACTION_HOME_COMMAND)
                .putExtra(MainActivity.EXTRA_COMMAND, command));
    }

    private void addRow(LinearLayout rows, int icon, int title, Runnable action) {
        View row = getLayoutInflater().inflate(R.layout.item_input, rows, false);
        ((ImageView) row.findViewById(R.id.icon)).setImageResource(icon);
        ((TextView) row.findViewById(R.id.title)).setText(title);
        row.findViewById(R.id.dot).setVisibility(View.GONE);
        row.findViewById(R.id.status).setVisibility(View.GONE);
        row.setOnClickListener(v -> {
            action.run();
            finish();
        });
        rows.addView(row);
    }

    /** TV Settings on the page asked for: opened plainly it comes back on whatever page its task was left at. */
    private void startFresh(Intent target) {
        start(target.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    private void start(Intent target) {
        try {
            startActivity(target.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (ActivityNotFoundException | SecurityException e) {
            Toast.makeText(this, R.string.not_available, Toast.LENGTH_SHORT).show();
        }
    }
}
