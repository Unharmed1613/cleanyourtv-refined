# CleanYourTV, noch mehr wie Apple

[English](FORK.md)

Vorweg: [CleanYourTV](https://github.com/Gathin23/cleanyourtv) ist ein wirklich großartiges Projekt. Es nimmt einen
Fernseher, der Werbung zeigt, mitliest und mit jedem Update langsamer wird, und macht daraus wieder einen ruhigen,
schnellen Bildschirm, der einem selbst gehört. Ohne Root, ohne Flashen, und jeder Schritt lässt sich rückgängig
machen. Es hat den Alltag mit dem Fernseher für unglaublich viele Menschen besser gemacht. Danke an alle, die daran
bauen.

Ich bin Apple-Fan. Filme und Serien schaue ich aber meistens mit Kodi von einer USB-Festplatte. Weil ein Apple TV
keinen USB-Anschluss für USB-Sticks und Festplatten hat, muss ich eine Android-TV-Box nutzen. Also habe ich den Launcher von CleanYourTV darauf genommen und versucht, ihn noch mehr wie tvOS
wirken zu lassen: eine kleine Glas-Kapsel oben rechts, ein Kontrollzentrum, Kacheln, die sich neigen und das Licht
fangen, Fotos passend zur Tageszeit und darüber ein ruhiger Uhr-Bildschirmschoner.

Der Fork basiert auf CleanYourTV **3.4** (`versionCode` 13, Upstream-Commit `deba47ca`). Ausprobiert wurde alles
auf einer **Strong LEAP-S3+ V2** (Google TV, Android 14, Build `UKG3.260128.001.C3.0.13_20260929`).

> **Für Upstream:** Fast jede Änderung ist ein neuer Schalter, der standardmäßig aus ist. Ein Build dieses Forks
> verhält sich also wie das Original, bis ein Theme die Schalter einschaltet. Die Ausnahmen stehen unter
> [Was sich für jeden Fernseher ändert](#was-sich-für-jeden-fernseher-ändert).

---

## Teil 1: Die Übersicht

| # | Was | Schalter (jeweils `config.xml` und `config.json` eines Themes) |
|---|---|---|
| 1 | **Statuspille:** die ganze obere Leiste als eine kleine Glas-Kapsel oben rechts: Uhrzeit, Zahnrad und die `top_bar_apps` (z. B. Play Store). Oben links steht nichts. | `status_pill` |
| 2 | **Ein Zahnrad-Menü (Kontrollzentrum):** Ein Druck aufs Zahnrad öffnet ein Glas-Panel mit Lautstärke, runden Schnellknöpfen (Bildschirmschoner, Bluetooth, Bild, Netzwerk, Ausschalten), dann Quellen, Apps anordnen, Modulkarten, Launcher-Einstellungen und Einstellungen. Am Zahnrad gibt es keinen langen Druck mehr. | `control_center` |
| 3 | **Fokus wie bei tvOS:** Die fokussierte Kachel wird größer, hebt sich ab und wirft einen weiten, weichen Schatten statt eines Glas-Highlights. Darauf liegt ein Glanz, ein Lichtstreifen wandert darüber, und sie neigt sich von der Seite herein, aus der die Fernbedienung kam. | `glass_style: "shadow"` |
| 4 | **Glas passt sich dem Foto an:** Über hellen Bildern werden Dock-Band und Pille dunkler getönt, über dunklen bleiben sie hell. | mit `dock_glass` oder `status_pill` |
| 5 | **Ruhigerer Bildwechsel auf Home:** Überblendung statt hartem Schnitt, die Bildunterschrift unter der Pille, wahlweise ein langsamer Zoom (Ken Burns). | Überblendung immer; Zoom `wallpaper_drift` |
| 6 | **Bildtitel in mehreren Sprachen:** `captions-<sprache>.txt` neben `captions.txt`. | mit `wallpaper_captions` |
| 7 | **Schärfere Fotos:** Jedes Bild wird mit Spielraum für den Zoom geladen, der Zoom streckt es also nie. | mit dem Zoom |
| 8 | **Foto-Bildschirmschoner wie tvOS:** der Uhr-Bildschirmschoner über der Fotosammlung, mit Überblendung, langsamem Zoom, voller Farbtiefe, wahlweise Datum und einer ruhigen kleinen Uhr unten links mit dem Bildnamen darunter. | `clock_wallpaper`, `clock_date`, `clock_corner` |
| 9 | **Sanftes Erscheinen:** Home blendet beim Zurückkehren das Foto auf und lässt die Apps hochgleiten. | `home_entrance` |
| 10 | **Korrektur:** Mit Statuspille funktionieren die Input-Taste der Fernbedienung und die Quellen-Liste jetzt. Vorher hingen sie an der ausgeblendeten Quellen-Pille. | mit `status_pill` |
| 11 | **Theme-Werkzeuge:** `tools/theme.sh` und `tools/theme.ps1` übertragen auch übersetzte Bildtitel. | – |
| 12 | **Die Sonne ohne Einstellung:** Morgen-, Abend- und Nachtbilder und der Himmel folgen der echten Sonne an der Bezugsstadt der Zeitzone des Fernsehers (Berlin bei Europe/Berlin), berechnet auf dem Gerät, ohne Netzwerk. | mit `wallpaper_moods` oder `sky_intensity` |
| 13 | **Das Theme `refined`:** alles oben in einem Theme, mit 18 Unsplash-Fotos in 2560 × 1440 und Bildtiteln auf Englisch und Deutsch. | `themes/refined/` |

Keine neuen Berechtigungen, kein Netzwerkzugriff, keine neuen Bibliotheken, keine Hintergrunddienste. Die Texte gibt
es auf Englisch und Deutsch. Alle anderen Sprachen zeigen bei den neuen Zeilen den englischen Text, bis sie übersetzt
sind.

---

## Teil 2: Jede Änderung im Detail

### 1. Statuspille (`status_pill`, Standard `false`)

**Was man sieht**

- Die obere Leiste wird zu einer Kapsel oben rechts: 42 dp hoch, 3 dp Innenabstand, 24 dp Eckenradius.
- Darin von links nach rechts:
  - die Uhrzeit (`HH:mm`, auf 12-Stunden-Fernsehern `h:mm`; 15 sp, mittlere Schriftstärke, weiß)
  - das Zahnrad
  - die `top_bar_apps` als runde Symbole

  Auf Hebräisch (`rtl_menu_bar`) ist die Reihenfolge gespiegelt, und die Uhrzeit schließt die Reihe rechts ab.
- Jeder Knopf darin ist ein Kreis von 36 dp mit einem 22-dp-Symbol, 2 dp Abstand dazwischen. Ohne Fokus hat er keine
  Füllung. Mit Fokus wird er weiß (`#F5FFFFFF`) und das Symbol schwarz.
- Das Play-Store-Logo sitzt 3 dp rechts der Mitte. Ein nach rechts zeigendes Dreieck wirkt sonst nach links versetzt,
  wenn man seinen Rahmen zentriert.

**Was wegfällt**

- Der Block oben links mit Begrüßung, Uhr, Datum und Wetter wird ausgeblendet.
- Die Quellen-Pille, die Netzwerk-Pille (WLAN) und die VPN-Pille werden ausgeblendet. `vpn_pill` wird dabei
  ignoriert.
- Die Quellen-Liste hängt stattdessen unter dem Zahnrad (siehe 10).

**Glas**

- `status_pill_bg` ist dasselbe Milchglas wie das Dock-Band mit `dock_glass`: ein Weißverlauf von 14 % oben
  (`#24FFFFFF`) bis 5 % (`#0DFFFFFF`), dazu ein Rand von 1 dp in 10 % Weiß.
- Unter dem Glas liegt eine Schicht in der Farbe der fokussierten App, solange `ambient_glow` eine liefert (diese
  Farbe mit 22 % Deckkraft). Ohne sie wirkte die Kapsel in der dunklen Ecke einfach grau.
- Über hellen Fotos wird die Kapsel dunkler getönt (siehe 4).

**Zahnrad ohne `control_center`**

- Es öffnet die Fernseh-Einstellungen auf ihrer Hauptseite (mit `FLAG_ACTIVITY_CLEAR_TASK`). Vorher kamen sie auf der
  zuletzt offenen Unterseite zurück, zum Beispiel „Standard-Startbildschirm“.
- Gedrückt gehaltenes OK öffnet wie im Original weiter das Launcher-eigene Menü.

**Fokus**

- Nach oben aus dem Dock landet man auf dem ersten fokussierbaren Knopf der Kapsel.
- Das rechte Ende der oberen Reihe ist der letzte fokussierbare Knopf der Kapsel (`lastTopButton`), nicht das
  Zahnrad.

**Code:** `MainActivity.setUpStatusPill`, `tintStatusPill`, `lastTopButton`; `Config.statusPill`;
`res/drawable/status_pill_bg.xml`, `status_pill_segment_bg.xml`.

### 2. Zahnrad-Menü / Kontrollzentrum (`control_center`, Standard `false`, braucht `status_pill`)

**Das Zahnrad**

- Ein Druck öffnet das Kontrollzentrum.
- Langen Druck gibt es nicht (`setOnLongClickListener(null)`, `setLongClickable(false)`). Manche Fernbedienungen
  senden ihn nicht zuverlässig, deshalb steht alles aus dem früheren Halte-Menü in diesem Panel.
- Den eigenen Kontrollzentrum-Knopf neben dem Zahnrad aus einer früheren Version dieses Forks gibt es nicht mehr. In
  der Kapsel stehen nur Uhrzeit, Zahnrad und `top_bar_apps`.

**Das Panel** (`ControlCenterActivity`, `res/layout/activity_control_center.xml`)

- **Form:**
  - Eine durchsichtige Activity (`Theme.Translucent.NoTitleBar.Fullscreen`, nicht exportiert, `noHistory`,
    `excludeFromRecents`).
  - Sie hängt 10 dp unter der Pille, rechtsbündig mit ihr, 290 dp breit, 8 dp Innenabstand, 16 dp Erhebung.
  - Sie fällt in 200 ms aus 10 dp Höhe herein und blendet dabei auf, zum Ende hin langsamer.
- **Glas** (`control_center_bg`):
  - ein dunkler Verlauf von `#D92A2A2E` bis `#E6141416`, damit die Zeilen über jedem Foto lesbar bleiben
  - ein heller Glanz oben (`#14FFFFFF` bis durchsichtig)
  - ein Rand von 1 dp in 10 % Weiß und 24 dp Ecken wie bei der Pille
- **Schließen:** Zurück schließt es, ebenso ein Klick daneben (`scrim`). Auch alles andere, was den Bildschirm
  übernimmt, schließt es (`onPause` ruft `finish` auf): eine App, der Bildschirmschoner, das System-Ausschaltmenü.
- **Bildunterschrift:** Solange das Panel offen ist, ist sie auf Home ausgeblendet, weil sie genau dort sitzt. In
  `onResume` kommt sie zurück.

**Inhalt von oben nach unten**

1. **Lautstärke** (`STREAM_MUSIC`): eine 46-dp-Zeile mit Lautsprechersymbol und Balken.
   - Links und Rechts senken bzw. heben die Lautstärke um eine Stufe und zeichnen den Balken neu
     (`ADJUST_LOWER`/`ADJUST_RAISE`).
   - Auf einer Box mit fester Lautstärke (`isVolumeFixed`, z. B. HDMI mit vollem Pegel) ist die Zeile ausgeblendet.
   - Nach unten geht es zum ersten Schnellknopf, nicht zu dem, der zufällig mittig darunter sitzt.
2. **Schnellknöpfe:** eine Reihe Kreise von 46 dp mit 22-dp-Symbol und 5 dp Rand je Seite (`cc_round_bg`).
   - Ohne Fokus schwaches Glas (12 % Weiß), mit Fokus weiß mit schwarzem Symbol.
   - Unter der Reihe steht in 12 sp der Name des fokussierten Knopfs.
   - Die Knöpfe:
     - **Bildschirmschoner** (`ClockActivity`)
     - **Bluetooth** (Bluetooth-Einstellungen)
     - **Bild** (`SystemSettings.displayIntent`)
     - **Netzwerk** (WLAN-Einstellungen)
     - **Ausschalten…**: das System-Ausschaltmenü über `HomeKeyService.openPowerMenu`, nur solange der Home-Tasten-Helfer
       gebunden ist (`canOpenPowerMenu`)
   - Einstellungsseiten öffnen mit `FLAG_ACTIVITY_CLEAR_TASK`, also immer auf der gewünschten Seite.
3. **Liste** (Zeilen aus `item_input`):
   - **Quellen:** nur, wenn der Fernseher etwas zum Umschalten hat (`MainActivity.hasInputs()`). Das heißt: ein HDMI-
     oder AV-Anschluss (`isPassthroughInput` und nicht versteckt), die eigene Tuner-App des Fernsehers
     (`liveTvIntent`), eine Coocaa-Quellenliste oder ein `source_panel_service`. Eine Streaming-Box meldet nur die
     Kanal-Eingänge von Apps und bekommt die Zeile nicht. Sie öffnet die Original-Quellenliste unter dem Zahnrad.
   - **Apps anordnen:** nur, wenn sich mindestens zwei Kacheln bewegen lassen. Startet den Original-Anordnen-Modus,
     der das Dock mit umfasst.
   - **Modulkarten:** nur mit `module_cards` und wenn mindestens eine Karte angeboten wird.
   - **Launcher-Einstellungen:** über `MainActivity`, damit Home sich beim Schließen mit den neuen Einstellungen neu
     aufbaut.
   - **Hersteller-Einstellungen:** nur auf Firmware mit eigener Einstellungs-App neben der von AOSP
     (`SystemSettings.vendorIntent`, z. B. Xiaomi China).
   - **Einstellungen:** die Fernseh-Einstellungen (Google TV) auf ihrer Hauptseite.
4. **Fokus beim Öffnen:** auf der Lautstärke, oder auf dem ersten Schnellknopf, wenn die Lautstärke ausgeblendet ist.

**Wie Zeilen auf Home wirken**

- Quellen: Das Panel schickt `com.android.tv.action.VIEW_INPUTS` an `MainActivity`.
- Apps anordnen und Modulkarten: Es schickt die neue Aktion `com.gathin.tvhome.action.HOME_COMMAND` mit dem Extra
  `command = arrange | modules`.
- `MainActivity.runHomeCommandIfAsked` führt sie aus, sobald das Panel weg ist.

**Texte (en/de)**

| Schlüssel | Englisch | Deutsch |
|---|---|---|
| `control_center` | Control Centre | Kontrollzentrum |
| `cc_volume` | Volume | Lautstärke |
| `cc_screensaver` | Screensaver | Bildschirmschoner |
| `cc_power` | Power… | Ausschalten… |
| `cc_bluetooth` | Bluetooth | Bluetooth |

Zwei vorhandene Texte haben deutsche Fassungen bekommen: `quick_picture` „Bild“ und `quick_network` „Netzwerk“.

**Neue Grafiken:** `control_center_bg`, `cc_round_bg`, `volume_track`, `volume_fill`, `ic_volume`, `ic_moon`, `ic_power`,
`ic_bluetooth`.

### 3. Fokus wie bei tvOS (`glass_style: "shadow"`)

`glass_style` bekommt neben `glass`, `rim` und `halo` einen vierten Wert. `Config.glassStyle` liest ihn unabhängig von
Groß- und Kleinschreibung und ohne Leerzeichen am Rand.

- **Kein Highlight hinter der Kachel:** `GlassView` zeichnet in diesem Stil nichts.
- **Anheben und Schatten:**
  - Die fokussierte Kachel hebt sich auf 18 dp (`AppAdapter.focusLiftDp`; mit `advanced_effects` allein bleiben es
    8 dp, die anderen Stile bleiben flach).
  - Ihr Schatten ist die ganze Fokusmarkierung. Damit er auf dunklem Bildschirm sichtbar ist, setzt das
    Launcher-Theme `ambientShadowAlpha` auf 0,14 und `spotShadowAlpha` auf 0,55, statt Materials 0,039 und 0,19.
  - Nur die fokussierte Kachel hat eine Erhebung, sonst ändert sich nichts.
  - Ohne Animationen behält die Kachel Anhebung und Schatten, damit der Fokus sichtbar bleibt.
- **Glanz** (`TileSheen`, als Vordergrund der Kachel, auf ihre runden Ecken beschnitten):
  - ein weicher weißer Schimmer über die oberen 55 % der Kachel, bis 19 % (`0x30`)
  - beim Fokus wandert einmal ein schräger Lichtstreifen von 42 % Kachelbreite darüber, bis 26 % (`0x42`)
  - Er startet nach 18 % von 950 ms und wird zum Ende langsamer. Beim Fokusverlust blendet er in 160 ms aus.
- **Neigung:**
  - Die neu fokussierte Kachel startet um 10° geneigt, in Richtung der Steuerkreuz-Taste, die den Fokus bewegt hat:
    Rechts und Links drehen um die Y-Achse, Hoch und Runter um die X-Achse.
  - Beim Vergrößern legt sie sich in 420 ms flach. Die Kameradistanz beträgt 2600 × Dichte, damit die Drehung als
    Tiefe wirkt.
  - Der Lichtstreifen läuft in dieselbe Richtung, bei Links also von rechts nach links.
  - Aus bei reduzierter Bewegung oder ohne Animationen. Eine neu gebundene Kachel wird flach zurückgesetzt.

**Code:** `AppAdapter` (`navKey`, `TILT_DEG`, `Holder.lift`, `Holder.sheen`), `TileSheen.java` (neu), `GlassView`,
`Config.GLASS_SHADOW`, `res/values/styles.xml`. **Test:** `GlassStyleTest.shadowByNameAnyCase`.

### 4. Glas passt sich dem Foto an (mit `dock_glass` oder `status_pill`)

- **Messen:** Bei jedem Bild, das `WallpaperPlayer` lädt, misst er die mittlere Helligkeit (Rec. 709) dort, wo das
  helle Glas liegt. Das passiert einmal pro Bild, bevor es zur reinen GPU-Bitmap wird:
  - Dock-Band: 6–94 % der Breite, 69–90 % der Höhe, Raster aus 24 × 24 Punkten
  - Pille: 74–95 % der Breite, 4–13 % der Höhe, Raster aus 12 × 12 Punkten
- **Tönung** (`GlassTone.shadeAlpha`):
  - Bis Helligkeit 0,22 bleibt das Glas, wie es ist.
  - Ab 0,60 kommt die stärkste Tönung, 45 % Schwarz (`0x73`).
  - Dazwischen wächst sie gleichmäßig.
- **Zeichnen:**
  - `DockDecoration` zeichnet die Tönung unter sein helles Glas (`setShade`).
  - Die Pille bekommt sie als schwarze Schicht unter ihrem Glas, aber nur, solange keine App-Farbe darunter liegt.
  - Der Wechsel dauert 900 ms, so lang wie die Überblendung des Bildes. Ohne Animationen gilt er sofort.

**Code:** `GlassTone.java` (neu, ohne Android-Typen), `WallpaperPlayer` (`DOCK_AREA`, `PILL_AREA`, `ToneListener`,
`setToneListener`), `DockDecoration.setShade`, `MainActivity.applyGlassTone`. **Tests:** `GlassToneTest` (6 Tests).

### 5. Bildwechsel auf Home

- **Überblendung:**
  - Das nächste Bild einer Rotation blendet in 900 ms über das vorige (`TransitionDrawable`) statt hart zu
    schneiden. Das erste Bild von Home ist einfach da.
  - Die alte Bitmap wird 400 ms nach der Überblendung freigegeben, nachdem sie aus dem Drawable getauscht wurde.
- **Position der Bildunterschrift:**
  - Mit `status_pill` sitzt sie 80 dp unter dem oberen Rand, direkt unter der schmalen Pille. Ohne bleibt es bei
    den originalen 110 dp.
  - Solange das Kontrollzentrum offen ist, ist sie ausgeblendet.
- **Langsamer Zoom** (`wallpaper_drift`, Standard `false`):
  - Jedes Bild zoomt über sein Wechsel-Intervall um 6 %, bei einem Bild hinein, beim nächsten wieder heraus.
  - Er läuft nur, solange Home zu sehen ist. In `stop()` hält er an, in `start()` geht er an derselben Stelle
    weiter.
  - **Auf der Box gemessen:** Statt als Animation mit 60 Bildern pro Sekunde läuft er in 15 Schritten pro Sekunde
    (alle 66 ms). Jeder Schritt verschiebt den Bildrand um etwa ein Zehntel Pixel. Das wirkt genauso flüssig, und
    Home zeichnet im Ruhezustand 155 statt 605 Bilder in 10 s.
  - Aus bei einem Wechsel-Intervall von 0.
- **Bildersammlung nur für den Bildschirmschoner:**
  - Mit `"wallpaper": false` und eingeschaltetem `clock_wallpaper` wird die Sammlung trotzdem gelesen, aber nur für
    den Uhr-Bildschirmschoner. Home zeigt und wechselt sie nicht.
  - Vorher wurde die Sammlung nur mit `wallpaper` gelesen.

**Code:** `WallpaperPlayer` (`show(Bitmap, boolean)`, `FADE_MS`, `driftOn`, `driftStep`, `DRIFT_STEP_MS`,
`DRIFT_SCALE`, `fadeIn`, `hideCaption`, `makeCaption(..., underPill)`), `Config` (Bedingung für die Sammlung).

### 6. Bildtitel in mehreren Sprachen (mit `wallpaper_captions`)

- **Lesen:**
  - Ein Theme kann neben `wallpapers/captions.txt` Übersetzungen ablegen: `captions-de.txt`, `captions-fr.txt` usw.
    Die Sprache ist ein Code aus zwei oder drei Kleinbuchstaben.
  - Der Launcher liest zuerst die Datei seiner eigenen Sprache, folgt also `locale` und der Sprache des Fernsehers.
    Fehlt sie, ist sie leer oder nicht lesbar, nimmt er `captions.txt`.
  - `captions.txt` sollte deshalb englisch sein.
- **Übertragen:**
  - Der Theme-Provider nimmt `wallpapers/captions-<sprache>.txt` an (`WallpaperCaptions.isCaptionsFile`). Andere
    Namen wie `captions-DE.txt`, `captions-../x.txt` oder `notes.txt` lehnt er wie bisher ab.
  - Beim Leeren der Sammlung (`delete` auf `wallpapers`) werden jetzt auch alte Übersetzungen entfernt.
    `captions.txt` selbst bleibt wie bisher.
- **Dokumentation:** `themes/README.md` beschreibt das in der Zeile zu `wallpaper_captions`.

**Code:** `WallpaperCaptions` (`read(File, Locale)`, `fileName`, `isCaptionsFile`), `ThemeFiles`, `ThemeProvider`,
`WallpaperPlayer`, `ClockScene`. **Tests:** `WallpaperCaptionsTest` (Sprachwahl, Rückfall, Dateinamen) und
`ThemeFilesTest`.

### 7. Schärfere Fotos

- **Die Grenze der Box:** Viele Google-TV-Boxen zeichnen ihre ganze Oberfläche in 1920 × 1080 und skalieren sie auf
  4K hoch. Die Strong legt das in der Firmware fest (`ro.surface_flinger.max_graphics_width/height`), daran kann keine
  App etwas ändern.
- **Ohne den Fork:** Ein Bild wurde genau auf Bildschirmgröße geladen, und der Zoom streckte es dann um bis zu 6 %
  (im Bildschirmschoner 8 %).
- **Mit dem Fork:**
  - Bei eingeschaltetem Zoom wird jedes Bild so groß geladen, dass es den Bildschirm auch am Ende des Zooms füllt:
    `Bildschirm × DRIFT_SCALE`, auf Home 2036 × 1145, im Bildschirmschoner 2074 × 1167.
  - Der Zoom verkleinert also nur noch ein scharfes Bild auf den Bildschirm. Das kostet etwa 1 MB mehr pro Bild.
- **Farbtiefe im Bildschirmschoner:** Mit `wallpaper_true_color` lädt er in ARGB_8888 statt RGB_565, damit Himmel und
  Dämmerung keine Farbstufen zeigen.
- **Bilder für dieses Theme:** 2560 × 1440 ist der ideale Wert. Das lässt genug Spielraum für den Zoom und hält die
  Dateien klein.

**Code:** `WallpaperPlayer` (`decodeWidth`, `decodeHeight`), `ClockScene.loadWallpaper`, `ClockScene.decode(..., depth)`.

### 8. Foto-Bildschirmschoner wie tvOS

- **Fotos im Uhr-Bildschirmschoner** (`clock_wallpaper`):
  - Er startet beim Bild, das Home gerade zeigt. Ohne Home-Hintergrund startet er bei einem zufälligen Bild, so wie
    die Rotation es wählen würde.
  - Das nächste Bild blendet in 2,5 s über.
  - Jedes Bild zoomt langsam um 8 % (Ken Burns). Es wird einmal in eine Hardware-Ebene gezeichnet, der Zoom skaliert
    nur noch eine Textur.
- **`clock_date`** (Standard `true` wie im Original): `false` zeigt nur die Uhrzeit.
- **`clock_corner`** (Standard `false`, braucht Fotos):
  - Die Uhr ruht unten links (56 dp vom linken, 48 dp vom unteren Rand) in 76 sp, statt groß in der Mitte.
  - Darunter steht der Bildname aus den Bildtiteln (20 sp) und blendet beim Bildwechsel mit über.
  - Statt jede Minute an eine neue Stelle zu springen, rückt die Uhr alle fünf Minuten um ein paar Pixel weiter,
    über `{0,0} {6,-4} {12,0} {6,4} {0,-6} {10,-8}`. So leuchten nicht stundenlang dieselben Pixel.
- **Schatten** (`dream_clock.xml`, auch `layout-iw`):
  - Die Uhrzeit hat einen weichen dunklen Schatten (45 % Schwarz, Radius 28, Versatz 3), damit sie auch über hellen
    Fotos lesbar bleibt.
  - Datum und Bildtitel haben Radius 16 und Versatz 2.
  - Die Zeitanzeige hat in beiden Layouts die ID `dreamClock` bekommen, dazu kommt die neue Ansicht `dreamCaption`.

**Code:** `ClockScene` (`showCaption`, `placeClock`, `moveClock`, `drift`, `FADE_MS`, `DRIFT_SCALE`, `trueColor`),
`Config.clockDate` und `Config.clockCorner`, `res/layout/dream_clock.xml`, `res/layout-iw/dream_clock.xml`.

### 9. Sanftes Erscheinen (`home_entrance`, Standard `false`)

- **Auslöser:** jedes Mal, wenn Home auf den Bildschirm kommt (`onStart`: Einschalten, Rückkehr aus einer App, nach
  dem Bildschirmschoner).
- **Was sich bewegt:**
  - Das App-Raster blendet auf und gleitet 28 dp nach oben in Position. Es startet nach 120 ms, dauert 560 ms, wird
    zum Ende langsamer und wird in einer Hardware-Ebene gezeichnet.
  - Die obere Leiste blendet ab 300 ms in 420 ms auf.
  - Das Foto blendet in 520 ms aus Schwarz auf.
- **Wann nicht:** aus bei reduzierter Bewegung oder ohne Animationen.

**Code:** `MainActivity.playEntrance`, `WallpaperPlayer.fadeIn`.

### 10. Quellen mit Statuspille (Korrektur)

- **Vorher:** Das Original hängt die Quellen-Liste an die Quellen-Pille, mit `top_menu` an das Zahnrad. Mit
  `status_pill` ist diese Pille ausgeblendet, deshalb tat die Input-Taste der Fernbedienung (`VIEW_INPUTS`,
  `KEYCODE_TV`) nichts.
- **Jetzt:** Mit `status_pill` hängt die Liste unter dem Zahnrad, genau wie mit `top_menu`.

**Code:** `MainActivity.openInputsIfAsked`, `MainActivity.showInputs`.

### 11. Theme-Werkzeuge

- `tools/theme.sh` und `tools/theme.ps1` übertragen nach `captions.txt` jede `wallpapers/captions-<sprache>.txt`
  eines Themes.
- Dateien, die nicht `captions-<zwei oder drei Kleinbuchstaben>.txt` heißen, überspringen sie mit einem Hinweis.
- Das Leeren der Sammlung vor einer Übertragung oder einem Zurücksetzen (`rm`) entfernt jetzt auch `captions-*.txt`.
- `tools/manage.py set glass_style shadow` wird angenommen (`ENUMS`), weil der Launcher diesen Wert kennt.

### 12. Die Sonne aus der Zeitzone (mit `wallpaper_moods` oder `sky_intensity`)

- **Vorher:** Sonnenauf- und -untergang für `wallpaper_moods` und den Himmel kamen aus `latitude` und `longitude` in
  `config.xml`. Ein Theme kann sie nicht setzen, und kaum jemand baut den Launcher selbst. Deshalb waren sie auf fast
  jedem Fernseher leer: Die Stimmungen wechselten fest um 07:00 und 18:30, der Himmel nutzte einen erfundenen Tag mit
  Sonnenuntergang um 19:00.
- **Jetzt:** Sind die beiden Zeilen leer, nimmt der Launcher die Bezugsstadt der Zeitzone des Fernsehers:
  `Europe/Berlin` ergibt Berlin (52,50, 13,37), `America/Chicago` ergibt Chicago.
  - Die Liste ist `res/raw/zone_coords.txt`: 418 Zeilen `<zone> <breite> <länge>`, erzeugt aus `zone.tab` der
    IANA-Zeitzonendatenbank (gemeinfrei), 2 Nachkommastellen, etwa 12 KB.
  - Innerhalb einer Zeitzone liegt das an den Rändern bis zu etwa einer halben Stunde daneben. Bei Morgen-, Abend-
    und Nachtbildern sieht man das nicht.
  - Kein Netzwerk, keine Standort-Berechtigung, nichts einzustellen. Koordinaten in `config.xml` haben weiter Vorrang.
  - Eine Zeitzone, die nicht in der Liste steht (`Etc/UTC`, ein bloßer Versatz), behält den alten festen Tag.
- **Doku:** der Kommentar zu `latitude`/`longitude` in `config.xml`, `docs/sky.md` und die Absätze zu
  `wallpaper_moods` und dem Himmel in `themes/README.md`.

**Code:** `ZoneCoordinates.java` (neu: `place`, `parse`), genutzt von `WallpaperPlayer`, `ClockScene` und `SkyView`.
**Test:** `ZoneCoordinatesTest` (Lesen, kaputte Zeilen, Werte außerhalb des Bereichs, und Berlin, Wien und Sydney aus
der mitgelieferten Liste).

### 13. Das Theme `refined` (`themes/refined/`)

- `config.json` schaltet alles oben ein (siehe [Das Theme, das ich nutze](#das-theme-das-ich-nutze)) und überlässt
  Dock, Apps und Home-Umleitung jedem Fernseher selbst.
- `wallpapers/`: 18 Unsplash-Fotos in 2560 × 1440 (JPEG-Qualität 2, volle Farbauflösung, je etwa 0,25 bis 1,9 MB),
  dazu `captions.txt` (englisch) und `captions-de.txt`, über `wallpaper_moods` nach Morgen, Abend und Nacht sortiert.
- `CREDITS.md` nennt zu jedem Foto den Fotografen und die Seite. Die Fotos stehen unter der
  [Unsplash-Lizenz](https://unsplash.com/license), nicht unter MIT.
- `screenshot.jpg` von der Strong, 1280 × 720.
- Eine Zeile in `themes/README.md`; die Zahl der Themes auf den Startseiten (README in jeder Sprache) steigt von 29
  auf 30.

### 14. Doku und Tests, die mitgezogen wurden

- **Schlüssel-Tabelle in `themes/README.md`:** neue Zeilen für `status_pill`, `control_center`, `home_entrance`,
  `wallpaper_drift`, `clock_date` und `clock_corner`, dazu `shadow` in der Zeile `glass_style`.
- **`docs/switches.md`:** zu jedem neuen Schlüssel eine Zeile mit Screenshot (`docs/switches/control_center.jpg` und
  `clock_corner.jpg` sind neu; die anderen zeigen `themes/refined/screenshot.jpg`).
- **`docs/workflows/theme.md`, Schritt 2:** Ein Theme mit `wallpaper_drift` darf Bilder bis 2560 × 1440 und 2 MB
  mitbringen. `tests/test_theme_wallpapers.py` prüft diese Grenze für ein solches Theme und überspringt
  `captions-<sprache>.txt`.
- **`tests/test_theme_picture_credits.py`:** erkennt „Unsplash License“ als Lizenz in `CREDITS.md`.
- **Hebräisch:** die fünf Texte des Kontrollzentrums, damit `test_hebrew_resources` vollständig bleibt.
- **Tests, die für `latitude`/`longitude` auf `SkyView` zeigten** (`test_sky_place_note`, `test_theme_keys_config`),
  zeigen jetzt auf `ZoneCoordinates`, das sie liest. `test_theme_push_ownership` erlaubt `captions-*.txt` in der
  Lösch-Zeile, `test_shipped_config_values` erwartet `shadow`.
- **Unter Windows** scheitern 20 Tests der Python-Suite auch mit dem Original-Code (PowerShell-Pfade, Symlinks,
  `os.geteuid`). Die Upstream-CI führt sie unter Linux aus.

---

## Was sich für jeden Fernseher ändert

Diese drei Änderungen hängen an keinem eigenen Schalter:

1. **Die Sonne aus der Zeitzone** (siehe 12): Jeder Fernseher mit `wallpaper_moods` oder Himmel folgt jetzt der
   echten Sonne seiner Zeitzone statt eines festen Tages. `latitude` und `longitude` bleiben in `config.xml` leer, wie
   im Original.
2. **Überblendung** zwischen wechselnden Bildern auf Home (siehe 5).
3. **Der Rand des Dock-Bands mit `dock_glass`** ist schwächer, 10 % statt 22 % Weiß (`0x1A` statt `0x38`), passend zur
   Statuspille. Das betrifft jedes Theme mit `dock_glass`.

## Das Theme, das ich nutze

Mein Theme liegt in diesem Repository als [`themes/refined/`](themes/refined/). Es schaltet ein:

- **Oberfläche:** `status_pill`, `control_center`, `glass_style: "shadow"`, `dock_glass`, `home_entrance`.
- **Hintergrund:** `wallpaper` mit 18 Unsplash-Fotos. Sie wechseln jede Minute in zufälliger Reihenfolge, mit
  `wallpaper_moods` für Morgen, Abend und Nacht. Dazu `wallpaper_dim` 15, `wallpaper_true_color`, `wallpaper_drift`
  und `wallpaper_captions` (englisch, dazu deutsch in `captions-de.txt`).
- **Bildschirmschoner:** `clock_wallpaper` mit `clock_wallpaper_dim` 25, `clock_date` false, `clock_corner` und
  `home_idle_clock_minutes` 5.

Bei jedem Foto steht die Quellenangabe von Unsplash in `themes/refined/CREDITS.md`.

Auf einen Fernseher bringen: `TV_HOST=<tv ip> tools/theme.sh themes/refined` (unter Windows `tools/theme.ps1`).

## Größe und Prüfungen

- **Größe:** Die Release-APK hat **609.597 Byte**. Das liegt über der Upstream-Grenze von 589.824 Byte, die nur für
  Pull Requests an das Original gilt.
- **Prüfungen:** `testReleaseUnitTest`, `lintRelease` (0 Fehler) und `assembleRelease` laufen durch. Neue und
  erweiterte Tests: `GlassToneTest`, `GlassStyleTest`, `WallpaperCaptionsTest`, `ThemeFilesTest`,
  `ZoneCoordinatesTest`.
- **Flüssigkeit auf der Strong** (`dumpsys gfxinfo`, Steuerkreuz-Durchlauf übers Dock):
  - Vor dem Fork: 4,9 % ruckelnde Bilder, 99. Perzentil 85 ms.
  - Mit allem oben, über mehrere Durchläufe: 0–1,6 % ruckelnde Bilder, 99. Perzentil 31–46 ms.

## Noch nicht geprüft

- **Quellen auf einem echten Fernseher:** die Zeile „Quellen“ und die Input-Taste auf einem Fernseher mit
  HDMI-Anschlüssen oder Tuner. Die Strong hat keine.
- **Die echte Fernbedienung:** Bildschirmschoner, Ausschaltmenü und Kontrollzentrum wurden mit Tastendrücken über ADB
  geprüft.
