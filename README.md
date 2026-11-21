# DORK HUNTER

An Android-first advanced search operator builder. Dork Search is the app's primary and default experience.

## What it does
- Turns a topic, phrase, artist, track, album or genre into ranked Google-compatible search patterns.
- Combines exact phrases, site targeting, file-type filters, exclusions and other search operators.
- Supports general, artist, track, album and genre search intents.
- Supports global, South African and international search markets.
- Uses curated artist aliases and spelling suggestions when a known artist is searched.
- Lets users copy an individual query or open it directly in Google.
- Keeps the query builder local to the app; Google performs the actual indexed-web search.

## Product scope
The dedicated MP3 search experience has been removed from the primary app navigation and launcher flow. This project is now focused on broad indexed-web discovery through search-engine operators. It does not claim to crawl the entire internet or access private/unindexed content.

Dork HUNTER does not bypass logins, paywalls, CAPTCHAs, DRM or other access controls. Search results remain subject to the destination website's terms and applicable law.

## Build
Requires JDK 17 and Gradle 8.11.1:

```bash
gradle :app:testDebugUnitTest :app:assembleDebug
```

The debug APK is produced at `app/build/outputs/apk/debug/app-debug.apk`.

## CI
GitHub Actions runs unit tests and builds a debug APK. The APK is uploaded as the `mp3-hunter-debug-apk` workflow artifact.
