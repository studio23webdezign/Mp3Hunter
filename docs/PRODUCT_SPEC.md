# DORK HUNTER — Product Specification

## Product promise
A mobile-first advanced search assistant that turns a user's topic into multiple targeted, ranked Google-compatible search queries. The dork builder is the default and primary application experience.

## Core experience
- One prominent search field for a topic, phrase, artist, track, album, genre or other subject.
- Search intent modes: general, artist, track, album and genre.
- Market targeting: global, South African and international.
- Ranked query generation with a clear query-family label and priority.
- One-tap copy for an individual search pattern.
- One-tap open in Google for an individual search pattern.
- Artist spelling recognition and curated aliases where a catalogue match exists.
- Responsive, dark interface with clear hierarchy, readable query cards and horizontally scrollable filters.

## Search operator strategy
Generate useful combinations from supported search-engine operators, including:
- Exact phrase matching.
- `site:` domain targeting.
- `filetype:` and extension targeting for public documents and media.
- Exclusion terms using the minus operator.
- Alternative spellings and related terms.
- Search-intent-specific combinations for artist, track, album and genre discovery.

The generated patterns are suggestions for Google-compatible search. Google executes the search and controls which indexed pages are returned. DORK HUNTER does not operate or claim to own Google's index.

## Scope and boundaries
- The dedicated MP3 search UI and MP3-first landing flow are removed from the primary app.
- The product is a general indexed-web dork search tool, not an MP3 downloader.
- The app does not claim complete internet coverage or access to private/unindexed pages.
- It does not bypass authentication, paywalls, CAPTCHAs, DRM or other access controls.
- Users must respect destination-site terms, privacy and applicable law.

## Architecture
Kotlin + Jetpack Compose Android application. Query generation is local and deterministic; Google is opened through an encoded search URL. Search-generation logic remains separate from UI so operator families and ranking can be expanded and unit-tested independently.

## UX principles
- Dork Search is the first screen and has no competing MP3 search tab.
- The search field is the strongest visual focus.
- Search category and region filters are easy to reach without crowding the screen.
- Results show the generated query, family and priority.
- Copy and Search Google actions are distinct and immediately visible.
- Use high-contrast text, restrained accent color, consistent card radius and spacing, and layouts that work on narrow Android screens.
