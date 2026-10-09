# ThaiSub Overlay — Android prototype

## What works
- User pastes an English subtitle in the app.
- ML Kit downloads English→Thai translation model once, then translates locally.
- Displays Thai subtitle as a draggable overlay above other apps (if Android permits overlays).
- Stop overlay from the app.

## What does NOT work yet
- No automatic Netflix subtitle extraction, OCR, audio capture, speech recognition, or subtitle synchronization.
- Netflix/DRM may prevent capture or overlays depending on device/version. Never claim universal Netflix support.

## Build
Open folder in Android Studio (JDK 17, Android SDK 35). Let Gradle sync, then Build > Build APK(s) or Run on your Android phone. Requires internet for initial ML Kit model download. Gradle wrapper is not included; Android Studio's installed Gradle may be used or generate a wrapper.

## Test
1. Grant Display over other apps permission.
2. Enter `I never thought we'd meet again.`
3. Tap Translate and show overlay.
4. Switch to another app and verify overlay; drag vertically.
5. Stop overlay from this app.

## Next phase
Validate whether overlay is visible above Netflix on the actual phone. If not, explore accessibility-compliant external subtitle sources rather than DRM bypass. For automatic text, use only authorized subtitle files or user-provided content. For audio capture, Android playback capture requires source-app permission and cannot be assumed to work with Netflix.
