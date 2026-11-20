# Sprint Plan

Sprints are tracked against committed implementation. A green CI run is required before calling the code validated.

1. **Foundation — implemented:** Kotlin/Compose Android app, Gradle build and GitHub Actions CI.
2. **Artist intelligence — implemented:** canonical names, aliases, typo matching, suggestions and unit tests.
3. **Search foundation — implemented:** normalized search plan, equal South African/international market plan, provider interface and download eligibility model.
4. **Catalogue integration — implemented (initial provider):** Internet Archive Advanced Search + metadata lookup, MP3 filename discovery, permissive-license allowlist, HTTPS audio HEAD verification and Android DownloadManager queue. Coverage is limited to items available in that catalogue.
5. **Listening and personal library — implemented (baseline):** Media3 playback for verified audio, Room-backed favourites and recent search history.
6. **QA and packaging — CI configured:** unit tests, debug APK build and 14-day GitHub Actions artifact upload. Device QA, broader artist/catalogue coverage, signing configuration and release APK remain release-readiness tasks.

## Known limitations / next hardening
- The current source integration is Internet Archive only; it does not search Google, scrape search-result HTML, bypass access controls or guarantee availability of commercial South African releases.
- Only records with an explicit supported public-domain/CC0/CC BY/CC BY-SA license and a verified audio response get an in-app download action.
- Internet Archive metadata and file availability can vary; an absent licence or failed audio verification means no download button.
- Search is online; favourites/history are stored locally. Downloads are stored in the app-specific Music directory.
- Before public release: add licence attribution display, rate limiting/caching, pagination, robust network/error tests, device testing, app icon, signing secrets and a signed release workflow.
