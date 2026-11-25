# AutoStartTV

Android-TV-App, die nach dem Einschalten des Fernsehers automatisch eine gewählte App öffnet.
Sprachen: Deutsch und Englisch (folgt der Systemsprache).

## Bedienung

1. AutoStartTV öffnen.
2. **App: …** wählen und die gewünschte App auswählen.
3. **Autostart aktiviert** einschalten (geschieht bei der App-Auswahl automatisch).
4. Optional **Verzögerung** einstellen (0–30 Sekunden nach dem Start).
5. Falls angezeigt: **Berechtigung erteilen** → „Über anderen Apps einblenden“ erlauben.
   Ab Android 10 darf eine App nur mit dieser Berechtigung andere Apps im Hintergrund starten.
   Hat der Fernseher dafür keine Einstellung, per Computer:
   ```
   adb shell appops set de.autostarttv SYSTEM_ALERT_WINDOW allow
   ```
6. Mit **Jetzt testen** prüfen, ob die App startet.

## Bauen

```
./gradlew assembleRelease
```
Die APK liegt danach unter `app/build/outputs/apk/release/`.
Bei jedem Push baut GitHub Actions die APK automatisch (Reiter *Actions* → Artefakt *AutoStartTV*).

Installation auf dem Fernseher z. B. per `adb install app-release.apk`.

---

# AutoStartTV (English)

Android TV app that automatically opens a chosen app after the TV has booted.
Choose the app, enable autostart, optionally set a delay, and grant the
"Display over other apps" permission (required on Android 10+).
