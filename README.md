# MP3 HUNTER

An Android music-discovery app with equal search opportunities for South African and international music.

## Current build
- Kotlin + Jetpack Compose.
- Artist spelling recognition and typo suggestions.
- Search provider architecture with an initial Internet Archive catalogue integration.
- Metadata and MP3 discovery; in-app downloads are enabled when a direct public HTTPS MP3 URL is exposed and verified as audio.
- Media3 playback for verified audio.
- Room-backed favourites and recent search history.
- GitHub Actions builds/tests the app and uploads a debug APK artifact.

## Build
Requires JDK 17 and Gradle 8.11.1:

```bash
gradle :app:testDebugUnitTest :app:assembleDebug
```

The debug APK is produced at `app/build/outputs/apk/debug/app-debug.apk`.

## Important
MP3 HUNTER does not bypass DRM, logins, paywalls, CAPTCHAs or other access controls. Catalogue coverage is not comprehensive: the initial provider searches Internet Archive, and commercial releases may not be available there. A download is offered when the source exposes a direct public HTTPS MP3 URL and its response passes an audio content-type check. MP3 HUNTER does not interpret public availability as a copyright licence; users are responsible for checking source terms and rights before downloading or sharing.

See [Product Specification](docs/PRODUCT_SPEC.md) and [Sprint Plan](docs/SPRINT_PLAN.md).
