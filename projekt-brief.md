# Projekt-Brief: „Kana-Widget“ (Android, Pixel 10)

## Ziel
Eine kleine Android-App, die ein **Homescreen-Widget** bereitstellt. Das Widget zeigt ein japanisches Zeichen mit Lesung, Bedeutung und Beispielwort und wechselt automatisch alle 2 Stunden. Zuerst **Hiragana und Katakana**, später ein **Kanji-Modus**, der per Schalter aktiviert wird.

Das Widget soll auch auf der **Lockscreen-Widget-Seite** des Pixels funktionieren (Android 16 QPR2+, Einstellungen › Display & Touch › Sperrbildschirm › Widgets auf Sperrbildschirm). Dafür gelten dieselben Anforderungen wie für normale Homescreen-Widgets. Kein Wallpaper-Wechsel, kein Smartspacer, kein Shizuku.

## Rahmen
- Sprache: Kotlin, UI mit Jetpack Compose und **Jetpack Glance** (App Widget).
- Zielgerät: Google Pixel 10 (aktuelle Android-Version). minSdk, targetSdk und Bibliotheksversionen wählst du passend und aktuell.
- Komplett **offline**: keine Internetberechtigung, keine Konten, keine Tracker.
- Vorgesehene Bibliotheken: androidx.glance:glance-appwidget, WorkManager, DataStore (Preferences), kotlinx-serialization-json.
- Datenquelle: `app/src/main/assets/kana.json` (liegt bei, Schema siehe unten).

## Layout (3-Zeilen-Schema, wie im Vorbild-Video)
Links das Zeichen groß, rechts drei Textzeilen:

```
あ     a・Hiragana        ← Zeile 1 (klein): Romaji・Schriftart
       Regen               ← Zeile 2 (fett): Bedeutung des Beispielworts
       あめ・ame           ← Zeile 3 (klein): Beispielwort・Romaji
```

Im späteren Kanji-Modus ist es dasselbe Schema:

```
学     gaku・がく          ← Zeile 1: Lesung
       Studium             ← Zeile 2 (fett): Bedeutung
       学生・Student       ← Zeile 3: Beispielwort・Übersetzung
```

Vorgaben zur Gestaltung:
- Transparenter oder leicht abgedunkelter Hintergrund, weiße Schrift mit dezentem Schatten, damit es auf jedem Wallpaper lesbar bleibt. Alternativ Material-You-Farben (dynamic color).
- Japanische Schrift über die Systemschrift (Noto CJK JP) darstellen.
- **Responsive** (`SizeMode.Responsive`): klein (ca. 2×1) nur Zeichen und Romaji, mittel (ca. 4×2) volles Layout.
- Wenn `word` = `null` ist (nur bei seltenen Zeichen), Zeile 2 und 3 weglassen und stattdessen die Notiz (`note`) zeigen.
- Hat ein Eintrag ein `note`-Feld, kann es als Zusatzzeile erscheinen, wenn Platz ist.

## Verhalten
1. **Wechsel alle 2 Stunden** per WorkManager (`PeriodicWorkRequest`, Intervall einstellbar; WorkManager erlaubt mindestens 15 Minuten). Der Worker schaltet zum nächsten Eintrag und ruft `updateAll` auf.
2. **Tipp aufs Widget:** nächstes Zeichen sofort, über `actionRunCallback`. Das Widget darf dabei keine Activity starten (Lockscreen-tauglich).
3. **Reihenfolge:** „der Reihe nach“ (あ, い, う, …) oder „gemischt“. Beim gemischten Modus eine gemischte Warteschlange speichern, sodass sich kein Zeichen wiederholt, bevor alle einmal dran waren.
4. **Zustand** (Modus, Reihenfolge, Index, Warteschlange) in DataStore speichern. Nach einem Neustart läuft es weiter (WorkManager ist persistent).
5. Widget zeigt sofort nach dem Hinzufügen ein Zeichen, ohne auf den ersten Zyklus zu warten.

## Einstellungen (einfache Activity aus dem App-Icon)
- Modus: **Hiragana / Katakana / Beide / Kanji** (Kanji anfangs ausgegraut mit „kommt später“).
- Reihenfolge: der Reihe nach / gemischt.
- Intervall: 1 / 2 / 4 / 6 Stunden.
- Schalter: Dakuten/Handakuten (が, ぱ, …) einbeziehen (Standard: an).
- Schalter: seltene Zeichen (`rare: true`) einbeziehen (Standard: aus).
- Hinweis-Text zur Akku-Optimierung: „App von der Akku-Optimierung ausnehmen, damit der Wechsel pünktlich läuft.“

## Datenschema (`kana.json`)
```json
{
  "version": 1,
  "language": "de",
  "hiragana": [ { "char": "あ", "romaji": "a", "script": "Hiragana", "kind": "basic|dakuten",
                  "word": { "jp": "あめ", "romaji": "ame", "de": "Regen" } | null,
                  "rare": false, "note": "optional" } ],
  "katakana": [ ... gleiches Schema ... ],
  "kanji":    [ ]   // später, siehe unten
}
```
Enthalten sind je 46 Basiszeichen und 25 Dakuten-/Handakuten-Zeichen (Hiragana 71, Katakana 71). Kleine Kombinationen (きゃ, しゅ, …) kommen als zweite Ausbaustufe.

### Kanji-Erweiterung (später)
Eintragsschema für `kanji[]`:
```json
{ "char": "学", "reading": "がく", "romaji": "gaku", "meaning": "Studium",
  "word": { "jp": "学生", "reading": "がくせい", "de": "Student" }, "level": "N5" }
```
Die Kanji-Liste (zum Beispiel JLPT N5 und N4) wird als separate Datei erzeugt. Freie Quellen sind KANJIDIC2 und JMdict (Lizenz beachten, Namensnennung); Bedeutungen ins Deutsche übersetzen.

## Nicht im Umfang
Spaced Repetition, Audioausgabe, Anki-Anbindung, Cloud-Sync. Alles später erweiterbar.

## Abnahmekriterien
1. Widget lässt sich auf dem Homescreen platzieren und zeigt sofort ein Zeichen im 3-Zeilen-Layout.
2. Tipp aufs Widget wechselt zum nächsten Zeichen ohne App-Start.
3. Nach 2 Stunden (bzw. eingestelltem Intervall) wechselt es von selbst, auch nach einem Neustart des Geräts.
4. Moduswechsel Hiragana ↔ Katakana ↔ Beide wirkt sich beim nächsten Wechsel aus.
5. Dasselbe Widget lässt sich auf der Lockscreen-Widget-Seite platzieren und zeigt dort denselben Inhalt.
6. Keine Internetberechtigung im Manifest.

## Ablauf für dich (Putu)
1. Android Studio installieren, neues leeres Projekt anlegen, `kana.json` nach `app/src/main/assets/` kopieren.
2. Claude Code im Projektordner starten und diesen Brief als Auftrag geben.
3. Pixel per USB (Entwickleroptionen › USB-Debugging) anschließen und die App aus Android Studio installieren, oder einen Debug-APK übertragen.
4. Widget auf dem Homescreen hinzufügen; danach die App von der Akku-Optimierung ausnehmen.
