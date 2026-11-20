# Sprint Plan

Sprints are tracked against committed implementation. A green CI run is required before calling the code validated.

1. **Foundation — implemented:** Kotlin/Compose Android app, Gradle build and GitHub Actions CI.
2. **Artist intelligence — expanded in code, awaiting CI:** curated South African and international artist names, aliases, high-confidence matching and unit tests for Maskandi/Gqom/local spelling variants.
3. **Search foundation — expanded in code, awaiting CI:** normalized search plan now carries curated alternate artist spellings to providers.
4. **Catalogue coverage — expanded in code, awaiting CI:** Internet Archive phrase + token matching and genre-subject discovery; Wikimedia Commons and Openverse now query artist aliases; concurrent multi-provider aggregation.
5. **Listening and personal library — baseline implemented:** Media3 playback for discovered audio, Room-backed favourites and recent search history.
6. **QA and packaging — CI configured:** unit tests, debug APK build and GitHub Actions artifact upload. Device QA, catalogue coverage research, signing configuration and release APK remain release-readiness tasks.

## Catalogue coverage constraints

- Internet Archive, Wikimedia Commons and Openverse are public archives/aggregators, not complete commercial music catalogues.
- Current commercial Amapiano, Maskandi, Gqom, House and international releases may not be indexed.
- Search must preserve equal South African/international opportunity and show source provenance.
- Download eligibility is based on a direct public HTTPS MP3 file verified as MPEG audio; licence/rights metadata is displayed when supplied but is not an in-app gate.
- Never bypass DRM, authentication, paywalls, CAPTCHAs or other access controls. Public accessibility does not itself grant copyright permission.

## Remaining work

- Run CI on the expanded artist and provider implementation and fix any build/test failures.
- Add result pagination, caching, request throttling and more robust cancellation/error handling.
- Research additional catalogue providers with meaningful South African coverage and no paid API dependency.
- Add provider attribution/rights presentation where source metadata is supplied.
- Run physical-device QA, add app icon, secure signing configuration and a signed release workflow.
