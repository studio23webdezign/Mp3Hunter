# Sprint Plan

Sprints are tracked against committed implementation. A green CI run is required before calling the code validated.

1. **Foundation — implemented:** Kotlin/Compose Android app, Gradle build and GitHub Actions CI.
2. **Artist intelligence — implemented:** canonical names, aliases, typo matching, suggestions and unit tests.
3. **Search foundation — implemented:** normalized search plan, equal South African/international market plan, provider interface and download eligibility model.
4. **Catalogue integration — initial provider implemented:** Internet Archive Advanced Search + metadata lookup, MP3 filename discovery, explicit Creative Commons CC0/CC BY/CC BY-SA allowlist, HTTPS audio HEAD verification and Android DownloadManager queue. Coverage is limited to items available in that catalogue.
5. **Listening and personal library — baseline implemented:** Media3 playback for verified audio, Room-backed favourites and recent search history.
6. **QA and packaging — CI configured:** unit tests, debug APK build and 14-day GitHub Actions artifact upload. Device QA, broader artist/catalogue coverage, signing configuration and release APK remain release-readiness tasks.

## Latest hardening

- Added a centralized conservative licence policy and tests.
- Downloads are permitted only for explicit CC0, CC BY or CC BY-SA declarations; NonCommercial, NoDerivatives, Public Domain Mark-only and unknown rights metadata are rejected.
- Licence metadata is retained in each live search result and displayed to the user for attribution/verification.
- Download eligibility still requires a verified HTTPS audio response. A licence declaration alone never enables the download button.

## Remaining release-readiness work

- Add additional authorised catalogues with meaningful South African coverage; do not imply Internet Archive is comprehensive.
- Add rate limiting, result pagination and caching.
- Add network/error handling tests and run physical-device QA.
- Add app icon, secure signing configuration and a signed release workflow.
- Verify attribution obligations for each provider and display required creator/source credits where supplied.
