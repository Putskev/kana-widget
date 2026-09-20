# Kana-Widget: Japanisch lernen direkt auf dem Homescreen

Ein kleines Android-Widget, das alle paar Stunden ein neues japanisches Zeichen oder Wort zeigt: Hiragana, Katakana oder Alltagswörter mit Kanji. Ein Tipp aufs Widget springt sofort zum nächsten Wort, falls du das aktuelle schon kennst.

Die Idee stammt aus einem TikTok-Video (Konto @moji.learn) mit einem iOS-Lockscreen-Widget. Diese Version läuft auf Android, komplett offline, ohne Konto und ohne Tracking.

<!-- Optional: Screenshot als docs/screenshot.png ablegen und hier einbinden -->

## Funktionen

- **Vier Modi:** Hiragana, Katakana, beide zusammen oder Kanji-Wörter (271 häufige Alltagswörter).
- **Reihenfolge:** der Reihe nach oder gemischt. Im gemischten Modus kommt kein Eintrag doppelt, bevor alle einmal dran waren.
- **Automatischer Wechsel** im Hintergrund (1, 2, 4 oder 6 Stunden, Standard 2 Stunden).
- **Tipp aufs Widget** zeigt sofort das nächste Wort, ohne die App zu öffnen.
- **Anzeige Kana:** Zeichen groß, dazu Romaji sowie Beispielwort mit deutscher Bedeutung.
- **Anzeige Kanji-Wörter:** Kanji-Wort groß, dazu `romaji・kana` und die deutsche Übersetzung.
- **Größenanpassung:** Startgröße 4×1 Kacheln, aber frei in Breite und Höhe skalierbar (bis hinunter zu 1 Zeile Höhe). Layout und Schriftgröße richten sich nach der tatsächlichen Widget-Größe, die Übersetzung ist in jeder Größe sichtbar.
- **Einstellungen:** Modus, Reihenfolge, Intervall, Dakuten/Handakuten (が, ぱ …) ein/aus, seltene Zeichen (ぢ, づ, ヲ, ヂ, ヅ) ein/aus.
- **Datenschutz:** keine Internetberechtigung, alle Daten liegen in der App.

## Installation

### Option A: fertige APK (für Freunde, kein Android Studio nötig)

1. Öffne auf dem Android-Handy die Seite **Releases** dieses Repositories und lade `KanaWidget.apk` herunter.
2. Öffne die Datei. Android fragt, ob dein Browser oder Dateimanager Apps installieren darf: erlauben. Zeigt Google Play Protect einen Hinweis zu einer nicht überprüften App, bestätige mit „Trotzdem installieren“. Die App ist eine Debug-Version und nicht im Play Store.
3. Widget hinzufügen: Homescreen lange drücken › **Widgets** › **Kana Widget** auswählen und platzieren.
4. App öffnen, Modus und Reihenfolge wählen.
5. Damit der Wechsel pünktlich läuft: Einstellungen › Apps › Kana Widget › Akkunutzung › **Unbeschränkt**.

Voraussetzung: Android 7.0 (API 24) oder neuer.

### Option B: selbst bauen

Voraussetzungen: [Android Studio](https://developer.android.com/studio) (aktuelle stabile Version) und ein Android-Handy mit aktiviertem USB-Debugging.

1. Repository klonen: `git clone(https://github.com/Putskev/kana-widget)`
2. In Android Studio **Open** wählen, den Projektordner öffnen und den Gradle-Sync abwarten.
3. Handy per USB anschließen (Einstellungen › Über das Telefon › 7× auf „Build-Nummer“ tippen, dann unter Entwickleroptionen „USB-Debugging“ aktivieren).
4. Oben in Android Studio das Handy als Gerät wählen und auf **Run** klicken.

Alternativ auf der Kommandozeile: `./gradlew assembleDebug` (Windows: `gradlew.bat assembleDebug`). Die APK liegt danach unter `app/build/outputs/apk/debug/`. Java (JAVA_HOME) muss dafür gesetzt sein, zum Beispiel auf das JDK von Android Studio (Ordner `jbr`).

## Lockscreen

Auf einem Pixel mit Android 16 QPR2 oder neuer lässt sich das Widget auch auf die Lockscreen-Widget-Seite legen: Einstellungen › Display & Touch › Sperrbildschirm › Widgets auf Sperrbildschirm aktivieren, dann auf dem Sperrbildschirm nach links wischen und das Widget hinzufügen. Diese Variante ist nur wenig getestet. Auf anderen Handys gibt es diese Seite nicht, das Widget läuft dort auf dem Homescreen.

## Eigene Wörter hinzufügen

Alle Inhalte liegen als JSON in `app/src/main/assets/`. Nach einer Änderung neu bauen und installieren.

`kana.json` enthält Hiragana und Katakana:

```json
{ "char": "あ", "romaji": "a", "script": "Hiragana", "kind": "basic",
  "word": { "jp": "あめ", "romaji": "ame", "de": "Regen" },
  "rare": false }
```

`kanji.json` enthält die Alltagswörter mit Kanji:

```json
{ "kanji": "水", "kana": "みず", "romaji": "mizu", "de": "Wasser", "cat": "Natur" }
```

Die Übersetzungen sind bewusst kurz gehalten. Bei mehreren Bedeutungen stehen sie getrennt durch „;“, zum Beispiel „Tag; Sonne“.

## Technik

- Kotlin, minSdk 24 (Android 7.0)
- Jetpack Compose + Material 3 für die Einstellungen-Activity, Jetpack Glance für das Widget selbst
- WorkManager für den zeitgesteuerten Wechsel im Hintergrund
- AndroidX DataStore (Preferences) für Einstellungen und den aktuell angezeigten Eintrag
- kotlinx.serialization zum Einlesen der Wortlisten aus `kana.json`/`kanji.json`
- Keine Netzwerkzugriffe, keine Analyse- oder Werbe-Bibliotheken

## Bekannte Einschränkungen

- Android drosselt Hintergrundaufgaben. Der Wechsel ist deshalb nicht sekundengenau, und ohne Ausnahme von der Akku-Optimierung kann er sich verzögern.
- Kein Audio, keine Wiederholungsplanung (Spaced Repetition) und keine Anki-Anbindung.
- Im Modus „der Reihe nach“ läuft die Kanji-Liste kategorieweise durch (Menschen, Zeit, Natur …). Für Abwechslung den Modus „gemischt“ wählen.
- Nicht im Play Store, die APK ist mit dem Debug-Schlüssel signiert.

## Entstehung

Das Projekt entstand im Gespräch mit Claude: zuerst die Auswertung des Vorbild-Videos, dann die Frage, wie sich das auf einem Pixel umsetzen lässt. Der Pixel-Sperrbildschirm erlaubt Widgets nicht direkt unter der Uhr. Geprüft wurden ein automatisch wechselndes Lockscreen-Wallpaper, Smartspacer (At a Glance) und Benachrichtigungen. Am Ende fiel die Wahl auf ein normales Homescreen-Widget, das sich auch auf der Lockscreen-Widget-Seite platzieren lässt. Der Code wurde mit Claude Code nach dem Auftrag in `projekt-brief.md` geschrieben und anschließend in mehreren Runden angepasst: Kanji-Modus ergänzt, Beschriftung „Hiragana/Katakana“ entfernt, Übersetzung in jeder Größe sichtbar, größere Schrift bei kleinen Widgets.
