# CleanYourTV, made more Apple-like

[Deutsch](FORK.de.md)

First things first: [CleanYourTV](https://github.com/Gathin23/cleanyourtv) is a brilliant project. It takes a TV
that shows you ads, tracks you and slows down with every update, and turns it back into a calm, fast screen that
belongs to you, with no root, nothing flashed, and every step reversible. It has made life with the TV better for
an enormous number of people. Thank you to everyone who builds it.

I am an Apple fan, but I mostly watch films and series with Kodi from a USB hard drive. An Apple TV has no USB port
for USB sticks and hard drives, so I have to use an Android TV box. So I took CleanYourTV's launcher on that box and tried to make it feel even more like tvOS: a small glass capsule at the top right, a
Control Centre, tiles that tilt and catch the light, photos that change with the time of day, and a calm clock
screensaver over them.

This fork is based on CleanYourTV **3.4** (`versionCode` 13, upstream commit `deba47ca`). Everything here was tried
on a **Strong LEAP-S3+ V2** (Google TV, Android 14, build `UKG3.260128.001.C3.0.13_20260929`).

> **For upstream:** almost every change is a new switch that is off by default, so a build of this fork behaves like
> the original until a theme turns the switches on. The exceptions are listed under
> [What changes for every TV](#what-changes-for-every-tv).

---

## Part 1: The overview

| # | What | Switch (all `config.xml` and theme `config.json`) |
|---|---|---|
| 1 | **Status pill:** the whole top bar as one small glass capsule at the top right: the time, the gear and the `top_bar_apps` (e.g. the Play Store). Nothing at the top left. | `status_pill` |
| 2 | **One gear menu (Control Centre):** a press on the gear opens one glass panel with volume, round quick buttons (screensaver, Bluetooth, display, network, power), then Inputs, Reorder apps, Module cards, Launcher settings and TV Settings. No long press anywhere on the gear. | `control_center` |
| 3 | **tvOS focus:** the focused tile grows, rises and casts a wide soft shadow instead of a glass highlight; a sheen sits on it and a band of light sweeps across; it tilts in from the side the remote moved from. | `glass_style: "shadow"` |
| 4 | **Glass that adapts to the photo:** over a bright picture the dock band and the pill are tinted darker, over a dark one they stay light. | with `dock_glass` or `status_pill` |
| 5 | **Smoother photo rotation on Home:** pictures cross-fade instead of cutting, the caption sits under the pill, and optionally zoom slowly (Ken Burns). | crossfade always; zoom `wallpaper_drift` |
| 6 | **Captions in several languages:** `captions-<language>.txt` next to `captions.txt`. | with `wallpaper_captions` |
| 7 | **Sharper photos:** each picture is loaded with room for the zoom, so the zoom never stretches it. | with the zoom |
| 8 | **Photo screensaver like tvOS:** the clock screensaver over the photo collection, with cross-fades, a slow zoom, full colour depth, an optional date, and a calm small clock at the bottom left with the photo's name under it. | `clock_wallpaper`, `clock_date`, `clock_corner` |
| 9 | **Soft entrance:** Home fades its photo in and slides the apps up when it comes back on screen. | `home_entrance` |
| 10 | **Fix:** with the status pill, the remote's Input button and the Inputs list now work (they hung on the hidden Inputs pill before). | with `status_pill` |
| 11 | **Theme tools:** `tools/theme.sh` and `tools/theme.ps1` also push caption translations. | – |
| 12 | **The sun without a setting:** morning, evening and night pictures and the sky follow the real sun at the reference city of the TV's time zone (Berlin for Europe/Berlin), worked out on the TV with no network. | with `wallpaper_moods` or `sky_intensity` |
| 13 | **The `refined` theme:** all of the above in one theme, with 18 Unsplash photos at 2560 × 1440 and English and German captions. | `themes/refined/` |

No new permissions, no network access, no new libraries, no background services. Strings are in English and German;
every other language shows the English text for the new rows until it is translated.

---

## Part 2: Every change in detail

### 1. Status pill (`status_pill`, default `false`)

**What you see**

- The top bar becomes one capsule at the top right, 42 dp high with 3 dp padding and 24 dp corners.
- Inside it, from left to right: the time (`HH:mm`, or `h:mm` on 12-hour TVs; 15 sp, medium weight, white), then the
  gear, then the `top_bar_apps` as round icons. In Hebrew (`rtl_menu_bar`) the order is mirrored and the time closes
  the row on the right.
- Every button inside is a 36 dp circle with a 22 dp icon and 2 dp gaps. Unfocused it has no fill. Focused it turns
  white (`#F5FFFFFF`) and the icon turns black.
- The Play Store logo sits 3 dp right of centre, because a right-pointing triangle looks off-centre when its box is
  centred.

**What goes**

- The greeting, clock, date and weather block at the top left is hidden.
- The Inputs pill, the network (Wi-Fi) pill and the VPN pill are hidden, and `vpn_pill` is ignored.
- The Inputs list hangs under the gear instead (see 10).

**Glass**

- `status_pill_bg`: the same frosted glass as the dock band with `dock_glass`. A white gradient runs from 14 % at
  the top (`#24FFFFFF`) to 5 % (`#0DFFFFFF`), with a 1 dp edge at 10 % white.
- Under the glass lies a layer in the focused app's colour while `ambient_glow` gives one (that colour at 22 %
  alpha). Without it the capsule read as plain grey in the dark corner.
- Over a bright photo the capsule is tinted darker (see 4).

**The gear without `control_center`**

- It opens TV Settings on its main page, with `FLAG_ACTIVITY_CLEAR_TASK`. Before, Settings came back on whatever page
  its task was last left at, for example "Default home app".
- Holding OK on it still opens the launcher's own menu, as in the original.

**Focus**

- Up from the dock lands on the first focusable button in the capsule.
- The right edge of the top row is the capsule's last focusable button (`lastTopButton`), not the gear.

**Code:** `MainActivity.setUpStatusPill`, `tintStatusPill`, `lastTopButton`; `Config.statusPill`;
`res/drawable/status_pill_bg.xml`, `status_pill_segment_bg.xml`.

### 2. Gear menu / Control Centre (`control_center`, default `false`, needs `status_pill`)

**The gear**

- A press opens the Control Centre.
- There is no long press: `setOnLongClickListener(null)` and `setLongClickable(false)`. Some remotes never send a
  reliable hold, so everything the hold menu used to offer is in this panel.
- The separate Control Centre button that an earlier version of this fork had beside the gear is gone. The capsule
  holds only the time, the gear and the `top_bar_apps`.

**The panel** (`ControlCenterActivity`, `res/layout/activity_control_center.xml`)

- **Shape:**
  - A translucent activity: `Theme.Translucent.NoTitleBar.Fullscreen`, not exported, `noHistory`,
    `excludeFromRecents`.
  - It hangs 10 dp under the pill, right-aligned with it, 290 dp wide, with 8 dp padding and 16 dp elevation.
  - It drops in from 10 dp above while fading in over 200 ms, decelerating.
- **Glass** (`control_center_bg`):
  - A dark gradient, `#D92A2A2E` to `#E6141416`, so the rows read over any photo.
  - A light top sheen (`#14FFFFFF` to clear).
  - A 1 dp edge at 10 % white and 24 dp corners like the pill.
- **Closing:** Back closes it, and so does a click beside it (`scrim`). Anything else that takes the screen also
  closes it (`onPause` calls `finish`), whether an app, the screensaver or the system power menu.
- **Caption:** While the panel is open, Home's photo caption is hidden, because it sits right where the panel hangs.
  It comes back in `onResume`.

**Contents, from top to bottom**

1. **Volume** (`STREAM_MUSIC`): a 46 dp row with a speaker icon and a bar.
   - Left and Right lower and raise the volume by one step and redraw the bar (`ADJUST_LOWER`/`ADJUST_RAISE`).
   - On a box with fixed volume (`isVolumeFixed`, e.g. HDMI at full level) the row is hidden.
   - Down from it goes to the first quick button, not the one that happens to sit under the middle.
2. **Quick buttons:** a row of 46 dp circles with a 22 dp icon and 5 dp margin on each side (`cc_round_bg`).
   - Unfocused they are faint glass at 12 % white. Focused they turn white with a black icon.
   - The focused button's name shows in 12 sp under the row.
   - The buttons:
     - **Screensaver** (`ClockActivity`)
     - **Bluetooth** (Bluetooth settings)
     - **Display** (`SystemSettings.displayIntent`)
     - **Network** (Wi-Fi settings)
     - **Power…**: the system power menu through `HomeKeyService.openPowerMenu`. This one shows only while the Home key
       helper is bound (`canOpenPowerMenu`).
   - Settings pages open with `FLAG_ACTIVITY_CLEAR_TASK`, so they always start on the page asked for.
3. **List** (rows from `item_input`):
   - **Inputs:** only when the set has something to switch to (`MainActivity.hasInputs()`). That means an HDMI or AV
     port (`isPassthroughInput` and not hidden), the TV's own tuner app (`liveTvIntent`), a Coocaa source list or a
     `source_panel_service`. A streaming box lists only apps' channel inputs, so it does not get this row. The row
     opens the original Inputs list under the gear.
   - **Reorder apps:** only when at least two tiles can move. It starts the original reorder mode, which covers the
     dock too.
   - **Module cards:** only with `module_cards` on and at least one card offered.
   - **Launcher settings:** through `MainActivity`, so Home rebuilds with the new choices when it closes.
   - **Vendor settings:** only on firmware that ships its own settings app next to the AOSP one
     (`SystemSettings.vendorIntent`, e.g. Xiaomi China).
   - **Settings:** TV Settings (Google TV) on its main page.
4. **Focus on opening:** the volume row, or the first quick button when the volume row is hidden.

**How rows act on Home**

- Inputs: the panel sends `com.android.tv.action.VIEW_INPUTS` to `MainActivity`.
- Reorder apps and Module cards: it sends the new `com.gathin.tvhome.action.HOME_COMMAND` with the extra
  `command = arrange | modules`.
- `MainActivity.runHomeCommandIfAsked` then acts once the panel has gone.

**Strings (en/de)**

| Key | English | German |
|---|---|---|
| `control_center` | Control Centre | Kontrollzentrum |
| `cc_volume` | Volume | Lautstärke |
| `cc_screensaver` | Screensaver | Bildschirmschoner |
| `cc_power` | Power… | Ausschalten… |
| `cc_bluetooth` | Bluetooth | Bluetooth |

Two existing strings got German texts: `quick_picture` "Bild" and `quick_network` "Netzwerk".

**New drawables:** `control_center_bg`, `cc_round_bg`, `volume_track`, `volume_fill`, `ic_volume`, `ic_moon`,
`ic_power`, `ic_bluetooth`.

### 3. tvOS focus (`glass_style: "shadow"`)

`glass_style` gets a fourth value next to `glass`, `rim` and `halo`. `Config.glassStyle` reads it in any case, with
spaces trimmed.

- **No highlight behind the tile:** `GlassView` draws nothing in this style.
- **Lift and shadow:**
  - The focused tile rises to an elevation of 18 dp (`AppAdapter.focusLiftDp`; `advanced_effects` alone keeps its
    8 dp, other styles stay flat).
  - Its shadow is the whole focus mark. The launcher theme makes it visible on a dark screen with
    `ambientShadowAlpha` 0.14 and `spotShadowAlpha` 0.55, against Material's 0.039 and 0.19.
  - Only the focused tile has an elevation, so nothing else changes.
  - Without animations the tile keeps the lift and the shadow, so the focus stays visible.
- **Sheen** (`TileSheen`, the tile's foreground drawable, clipped to its rounded corners):
  - A soft white sheen over the top 55 % of the tile, at up to 19 % (`0x30`).
  - On focus, a slanted band of light 42 % of the tile wide sweeps across it once, at up to 26 % (`0x42`).
  - The band starts after 18 % of 950 ms and slows as it leaves. It fades out over 160 ms when the tile loses focus.
- **Tilt:**
  - The newly focused tile starts tilted by 10° towards the D-pad key that moved the focus: Right and Left turn it
    around Y, Up and Down around X.
  - It settles flat while it grows, over 420 ms. Camera distance is 2600 × density, so the turn reads as depth.
  - The sweep follows the same direction: Left makes it run right to left.
  - Off with reduced motion or without animations. A rebound tile is reset to flat.

**Code:** `AppAdapter` (`navKey`, `TILT_DEG`, `Holder.lift`, `Holder.sheen`), `TileSheen.java` (new), `GlassView`,
`Config.GLASS_SHADOW`, `res/values/styles.xml`. **Test:** `GlassStyleTest.shadowByNameAnyCase`.

### 4. Glass that adapts to the photo (with `dock_glass` or `status_pill`)

- **Measuring:** each time `WallpaperPlayer` decodes a picture, it measures the picture's mean brightness (Rec. 709)
  where the light glass sits. It does this once per picture, before the bitmap becomes a GPU-only hardware bitmap:
  - The dock band, at 6–94 % of the width and 69–90 % of the height, on a grid of 24 × 24 points.
  - The pill, at 74–95 % of the width and 4–13 % of the height, on a grid of 12 × 12 points.
- **Tint** (`GlassTone.shadeAlpha`):
  - Up to a brightness of 0.22 the glass stays as it is.
  - From 0.60 on it gets the strongest tint, 45 % black (`0x73`).
  - In between the tint grows linearly.
- **Drawing:**
  - `DockDecoration` draws the tint under its light glass (`setShade`).
  - The pill gets it as a black layer under its glass, but only while there is no ambient app colour.
  - The change fades over 900 ms, the same time as the picture's cross-fade, or applies at once without animations.

**Code:** `GlassTone.java` (new, no Android types), `WallpaperPlayer` (`DOCK_AREA`, `PILL_AREA`, `ToneListener`,
`setToneListener`), `DockDecoration.setShade`, `MainActivity.applyGlassTone`. **Tests:** `GlassToneTest` (6 tests).

### 5. Photo rotation on Home

- **Cross-fade:**
  - The next picture of a rotation fades in over the last one in 900 ms (`TransitionDrawable`) instead of cutting.
    Home's first picture is simply there.
  - The old bitmap is recycled 400 ms after the fade, once it has been swapped out of the drawable.
- **Caption position:**
  - With `status_pill` the caption sits 80 dp from the top, just under the slim pill. Without it, it keeps the
    original 110 dp.
  - It is hidden while the Control Centre is open.
- **Slow zoom** (`wallpaper_drift`, default `false`):
  - Each picture zooms by 6 % over its rotation interval, in on one picture and back out on the next.
  - It runs only while Home is on screen. It stops in `stop()` and carries on from where it was in `start()`.
  - **Measured on the box:** it runs in 15 steps a second (every 66 ms) rather than as a 60 fps animation. Each step
    moves the picture's edge by about a tenth of a pixel, so it looks just as smooth, and Home in its idle state draws
    155 frames in 10 s instead of 605.
  - Off with a rotation interval of 0.
- **Screensaver collection without a Home wallpaper:**
  - With `"wallpaper": false` and `clock_wallpaper` on, the collection is still read, but only for the clock
    screensaver. Home shows nothing and does not rotate it.
  - Before, the collection was read only when `wallpaper` was on.

**Code:** `WallpaperPlayer` (`show(Bitmap, boolean)`, `FADE_MS`, `driftOn`, `driftStep`, `DRIFT_STEP_MS`,
`DRIFT_SCALE`, `fadeIn`, `hideCaption`, `makeCaption(..., underPill)`), `Config` (collection condition).

### 6. Captions in several languages (with `wallpaper_captions`)

- **Reading:**
  - A theme can put translations next to `wallpapers/captions.txt`: `captions-de.txt`, `captions-fr.txt` and so on.
    The language is a two- or three-letter code in lower case.
  - The launcher reads the file for its own language first, so it follows `locale` and the TV's language. If that
    file is missing, empty or unreadable, it falls back to `captions.txt`.
  - So `captions.txt` should be written in English.
- **Pushing:**
  - The theme provider accepts `wallpapers/captions-<language>.txt` (`WallpaperCaptions.isCaptionsFile`). Any other
    name, such as `captions-DE.txt`, `captions-../x.txt` or `notes.txt`, is refused as before.
  - Clearing the collection (a `delete` of `wallpapers`) now also removes old translations. `captions.txt` itself
    stays, as before.
- **Documentation:** `themes/README.md` describes this in the `wallpaper_captions` row.

**Code:** `WallpaperCaptions` (`read(File, Locale)`, `fileName`, `isCaptionsFile`), `ThemeFiles`, `ThemeProvider`,
`WallpaperPlayer`, `ClockScene`. **Tests:** `WallpaperCaptionsTest` (language choice, fallback, file names) and
`ThemeFilesTest`.

### 7. Sharper photos

- **The box limit:** many Google TV boxes draw their whole interface at 1920 × 1080 and scale it to 4K. The Strong
  sets this in firmware (`ro.surface_flinger.max_graphics_width/height`), so no app can change it.
- **Without the fork:** a picture was decoded exactly to screen size, and the zoom then stretched it by up to 6 %
  (8 % in the screensaver).
- **With the fork:**
  - With the zoom on, each picture is decoded large enough to cover the screen at the zoom's far end:
    `screen × DRIFT_SCALE`, 2036 × 1145 on Home and 2074 × 1167 in the screensaver.
  - The zoom therefore only ever shrinks a sharp picture into the screen. That costs about 1 MB more per picture.
- **Screensaver colour depth:** with `wallpaper_true_color` the screensaver decodes in ARGB_8888 instead of
  RGB_565, so skies and dusk gradients do not band.
- **Pictures for this theme:** 2560 × 1440 is the sweet spot. It leaves enough room for the zoom and keeps the files
  small.

**Code:** `WallpaperPlayer` (`decodeWidth`, `decodeHeight`), `ClockScene.loadWallpaper`, `ClockScene.decode(..., depth)`.

### 8. Photo screensaver like tvOS

- **Photos in the clock screensaver** (`clock_wallpaper`):
  - The screensaver starts on the picture Home is showing. Without a Home wallpaper it starts on a random picture,
    the way the rotation would pick it.
  - The next picture fades in over 2.5 s.
  - Each picture zooms slowly by 8 % (Ken Burns), drawn into a hardware layer once, so the zoom only scales a
    texture.
- **`clock_date`** (default `true`, as in the original): `false` shows the time alone.
- **`clock_corner`** (default `false`, needs photos):
  - The clock rests at the bottom left (56 dp from the left, 48 dp from the bottom), at 76 sp instead of large in
    the middle.
  - The picture's name from the captions sits under it (20 sp) and fades across a picture change.
  - Instead of jumping somewhere new every minute, the clock shifts a few pixels every five minutes, through
    `{0,0} {6,-4} {12,0} {6,4} {0,-6} {10,-8}`, so the same pixels are not lit for hours.
- **Shadows** (`dream_clock.xml`, also `layout-iw`):
  - The time has a soft dark shadow: 45 % black, radius 28, offset 3, so it stays readable over bright photos.
  - Date and caption have radius 16, offset 2.
  - The time view got the id `dreamClock` in both layouts, plus the new view `dreamCaption`.

**Code:** `ClockScene` (`showCaption`, `placeClock`, `moveClock`, `drift`, `FADE_MS`, `DRIFT_SCALE`, `trueColor`),
`Config.clockDate` and `Config.clockCorner`, `res/layout/dream_clock.xml`, `res/layout-iw/dream_clock.xml`.

### 9. Soft entrance (`home_entrance`, default `false`)

- **Trigger:** each time Home comes on screen (`onStart`: power on, back from an app, after the screensaver).
- **What moves:**
  - The app grid fades in and rises 28 dp into place, starting after 120 ms and taking 560 ms, decelerating, drawn
    in a hardware layer.
  - The top bar fades in from 300 ms over 420 ms.
  - The photo fades in from black over 520 ms.
- **When not:** off with reduced motion or without animations.

**Code:** `MainActivity.playEntrance`, `WallpaperPlayer.fadeIn`.

### 10. Inputs with the status pill (fix)

- **Before:** the original hangs the Inputs list on the Inputs pill, or on the gear with `top_menu`. With
  `status_pill` that pill is hidden, so the remote's Input button (`VIEW_INPUTS`, `KEYCODE_TV`) did nothing.
- **Now:** with `status_pill` the list hangs under the gear, the same way it does with `top_menu`.

**Code:** `MainActivity.openInputsIfAsked`, `MainActivity.showInputs`.

### 11. Theme tools

- `tools/theme.sh` and `tools/theme.ps1` push every `wallpapers/captions-<language>.txt` of a theme after
  `captions.txt`. They skip and name any file that is not `captions-<two or three lower-case letters>.txt`.
- Emptying the collection before a push or a reset (`rm`) now also removes `captions-*.txt`.
- `tools/manage.py set glass_style shadow` is accepted (`ENUMS`), as the launcher keeps that value.

### 12. The sun from the time zone (with `wallpaper_moods` or `sky_intensity`)

- **Before:** the sunrise and sunset behind `wallpaper_moods` and the sky came from `latitude` and `longitude` in
  `config.xml`. A theme cannot set them, and most people never build the launcher themselves, so on almost every TV
  they were empty: the moods switched at a fixed 07:00 and 18:30 and the sky used a made-up day with sunset at 19:00.
- **Now:** when those two lines are empty, the launcher takes the reference city of the TV's own time zone:
  `Europe/Berlin` gives Berlin (52.50, 13.37), `America/Chicago` gives Chicago.
  - The list is `res/raw/zone_coords.txt`: 418 lines of `<zone> <latitude> <longitude>`, made from the IANA tz
    database's `zone.tab` (public domain), 2 decimals, about 12 KB.
  - Across one time zone that is off by up to about half an hour at its edges, which morning, evening and night
    pictures do not show.
  - No network, no location permission, nothing to set. Coordinates set in `config.xml` still win.
  - A zone that is not in the list (`Etc/UTC`, a bare offset) keeps the old fixed day.
- **Docs:** the `latitude`/`longitude` comment in `config.xml`, `docs/sky.md` and the `wallpaper_moods` and sky
  paragraphs in `themes/README.md`.

**Code:** `ZoneCoordinates.java` (new: `place`, `parse`), used by `WallpaperPlayer`, `ClockScene` and `SkyView`.
**Test:** `ZoneCoordinatesTest` (parsing, broken lines, out-of-range values, and Berlin, Vienna and Sydney read from
the shipped list).

### 13. The `refined` theme (`themes/refined/`)

- `config.json` turns on everything above (see [The theme I use](#the-theme-i-use)) and leaves the dock, the apps
  and the Home redirect to each TV.
- `wallpapers/`: 18 Unsplash photos at 2560 × 1440 (JPEG quality 2, full chroma, about 0.25 to 1.9 MB each), with
  `captions.txt` (English) and `captions-de.txt`, sorted into morning, evening and night by `wallpaper_moods`.
- `CREDITS.md` names each photographer and photo page. The photos are under the
  [Unsplash License](https://unsplash.com/license), not MIT.
- `screenshot.jpg` from the Strong, 1280 × 720.
- A row in `themes/README.md`, and the theme counts on the front pages (README in every language) went from 29 to 30.

### 14. Docs and tests that came along

- **`themes/README.md` Keys table:** new rows for `status_pill`, `control_center`, `home_entrance`, `wallpaper_drift`,
  `clock_date` and `clock_corner`, and `shadow` in the `glass_style` row.
- **`docs/switches.md`:** a screenshot row for each new key (`docs/switches/control_center.jpg` and
  `clock_corner.jpg` are new; the others show `themes/refined/screenshot.jpg`).
- **`docs/workflows/theme.md`, step 2:** a theme with `wallpaper_drift` may ship pictures up to 2560 × 1440 and
  2 MB each. `tests/test_theme_wallpapers.py` checks that limit for such a theme and skips `captions-<language>.txt`.
- **`tests/test_theme_picture_credits.py`:** accepts "Unsplash License" as a licence in `CREDITS.md`.
- **Hebrew:** the five Control Centre strings, so `test_hebrew_resources` stays complete.
- **Tests that pointed at `SkyView` for `latitude`/`longitude`** (`test_sky_place_note`, `test_theme_keys_config`)
  now point at `ZoneCoordinates`, which reads them. `test_theme_push_ownership` allows `captions-*.txt` in the
  clear line, and `test_shipped_config_values` expects `shadow`.
- **On Windows** 20 tests of the Python suite fail with the original code too (PowerShell paths, symlinks,
  `os.geteuid`). The upstream CI runs them on Linux.

---

## What changes for every TV

These three changes do not sit behind a switch of their own:

1. **The sun from the time zone** (see 12): every TV with `wallpaper_moods` or the sky now follows the real sun of
   its time zone instead of a fixed day. `latitude` and `longitude` stay empty in `config.xml`, as in the original.
2. **Cross-fades** between rotating pictures on Home (see 5).
3. **The dock band's edge with `dock_glass`** is fainter, at 10 % white instead of 22 % (`0x1A` instead of `0x38`),
   to match the status pill. That affects every theme with `dock_glass`.

## The theme I use

My theme is in this repository as [`themes/refined/`](themes/refined/). It turns on `status_pill`, `control_center`, `glass_style: "shadow"`, `dock_glass`, `wallpaper` with 18
Unsplash photos (rotation every minute, shuffled, `wallpaper_moods` for morning, evening and night), `wallpaper_dim`
15, `wallpaper_true_color`, `wallpaper_captions` (English, plus German in `captions-de.txt`), `wallpaper_drift`,
`clock_wallpaper` with `clock_wallpaper_dim` 25, `clock_date` false, `clock_corner`, `home_entrance` and
`home_idle_clock_minutes` 5. Each photo keeps its Unsplash credit in `themes/refined/CREDITS.md`.

To put it on a TV: `TV_HOST=<tv ip> tools/theme.sh themes/refined` (or `tools/theme.ps1` on Windows).

## Size and checks

- **Size:** the release APK is **609,597 bytes**. That is over the upstream size gate of 589,824 bytes, which only
  matters for pull requests to the original.
- **Checks:** `testReleaseUnitTest`, `lintRelease` (0 errors) and `assembleRelease` pass. New and extended tests:
  `GlassToneTest`, `GlassStyleTest`, `WallpaperCaptionsTest`, `ThemeFilesTest`, `ZoneCoordinatesTest`.
- **Smoothness on the Strong** (`dumpsys gfxinfo`, D-pad run over the dock):
  - Before the fork: 4.9 % janky frames, 99th percentile 85 ms.
  - With all of the above, over several runs: 0–1.6 % janky frames, 99th percentile 31–46 ms.

## Not yet checked

- **Inputs on a real TV:** the Inputs row and the Input button on a TV with HDMI ports or a tuner. The Strong has
  none.
- **The real remote:** the screensaver, the power menu and the Control Centre were checked with ADB key presses.
