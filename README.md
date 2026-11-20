# MP3 HUNTER

An Android music-discovery app with equal search opportunities for South African and international music.

## Current build
- Kotlin + Jetpack Compose.
- Artist spelling recognition and typo suggestions.
- Search provider architecture with an initial Internet Archive catalogue integration.
- Metadata and MP3 discovery; in-app downloads are enabled only for supported permissive/public-domain licences after HTTPS audio verification.
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
MP3 HUNTER does not bypass DRM, logins, paywalls, CAPTCHAs or other access controls. Catalogue coverage is not comprehensive: the initial provider searches Internet Archive, and commercial releases may not be available there. A download is offered only when the item declares a supported permissive/public-domain licence and its direct MP3 URL passes an audio content-type check.

See [Product Specification](docs/PRODUCT_SPEC.md) and [Sprint Plan](docs/SPRINT_PLAN.md).
