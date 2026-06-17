# phone-fractuals

A small, colourful, animated fractal demo for Android, built with **Kotlin + Jetpack Compose**.

It renders three classic fractals straight onto a `Canvas` — no images, no external
rendering libraries — and animates them continuously:

- **Tree** — a recursive branching tree that gently sways
- **Sierpinski** — a recursive triangle subdivision
- **Snowflake** — a recursive Koch snowflake that softly pulses

All three cycle through the full hue wheel over time, so the colours are always shifting.
A slider lets you change the recursion depth ("detail") live.

## Project layout

```
app/src/main/java/com/phonefractals/app/
  MainActivity.kt      - app entry point, hosts the Compose UI
  FractalScreen.kt      - UI: fractal picker, slider, animation driver (hue + sway)
  Fractals.kt           - pure drawing functions, one per fractal type
```

The drawing math is kept separate from the UI/animation state so each half stays small
and easy to follow.

## Running it

This is a standard Android Studio project:

1. Open the repo root in Android Studio (Kotlin/Compose support is built in).
2. Let it sync Gradle (needs internet access to Google's Maven repo for AndroidX/Compose
   dependencies).
3. Run the `app` configuration on a device or emulator (minSdk 24, i.e. Android 7.0+).

Or from the command line, with the Android SDK installed and `ANDROID_HOME` set:

```
./gradlew installDebug
```

## Notes

- minSdk 24 / targetSdk 34, so it runs on the vast majority of real Android phones.
- No third-party fractal/graphics libraries — everything is plain Compose `Canvas`
  drawing (`drawLine`, `drawPath`) plus `rememberInfiniteTransition` for the animation.
