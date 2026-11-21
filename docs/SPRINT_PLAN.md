# Sprint Plan

Sprints are tracked against committed implementation. A green CI run is required before calling the code validated.

1. **Android foundation — implemented:** Kotlin/Jetpack Compose app, Gradle build and GitHub Actions CI.
2. **Dork query engine — existing foundation:** Google-compatible query generation, ranked patterns, search intents and market targeting.
3. **Primary app redesign — implemented in this change:** Dork Search replaces the MP3 search flow as the launcher experience. The main activity no longer exposes MP3 search, playback, favourites or download UI.
4. **Dork builder UX — implemented in this change:** refreshed visual hierarchy, focused search input, clear category and market filters, live pattern count, artist suggestions, query copy and direct Google search actions.
5. **QA and packaging — pending CI:** run unit tests and assemble the debug APK; address any failures before release.

## Next improvements
- Expand operator families for public documents, code, public datasets, media, academic sources and domain discovery.
- Add a curated domain/source picker and custom `site:` targeting.
- Add saved searches and recent query history.
- Add query editing and a multi-query launch workflow while respecting Google rate limits and normal access controls.
- Add tests for generated operator combinations, escaping and URL encoding.
- Verify layout on small phones and larger Android devices.

## Boundaries
DORK HUNTER generates search patterns for public search engines. It does not crawl the whole internet, promise that every result is indexed, or bypass authentication, paywalls, CAPTCHAs, DRM or other access controls.
