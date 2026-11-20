# MP3 HUNTER

An Android music-discovery app with equal search opportunities for South African and international music.

## Current build
- Kotlin + Jetpack Compose.
- Artist spelling recognition and typo suggestions.
- Concurrent public-catalogue search across Internet Archive, Wikimedia Commons and Openverse.
- Broader South African discovery queries so local records are not excluded just because genre/country tags are missing.
- MP3 discovery from direct public HTTPS files; the download action appears only for verified MPEG audio URLs.
- Media3 playback for discovered audio.
- Room-backed favourites and recent search history.
- GitHub Actions builds/tests the app and uploads a debug APK artifact.

## Build
Requires JDK 17 and Gradle 8.11.1:

```bash
gradle :app:testDebugUnitTest :app:assembleDebug
```

The debug APK is produced at `app/build/outputs/apk/debug/app-debug.apk`.

## Catalogue coverage
Internet Archive, Wikimedia Commons and Openverse are public catalogues, not complete commercial music services. Openverse aggregates audio from multiple providers, but none of these sources will reliably contain every current Amapiano, Maskandi, Gqom, House or international chart release. MP3 HUNTER queries all three sources concurrently and keeps results from a healthy source if another is unavailable.

## Important
MP3 HUNTER does not bypass DRM, logins, paywalls, CAPTCHAs or other access controls. A download is offered when a source exposes a direct public HTTPS URL and the response is verified as MPEG audio. Public accessibility does not itself grant copyright permission; users are responsible for checking source terms and rights before downloading or sharing.

See [Product Specification](docs/PRODUCT_SPEC.md) and [Sprint Plan](docs/SPRINT_PLAN.md).
