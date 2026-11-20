# MP3 HUNTER — Product Specification

## Product promise
A mobile-first music discovery tool with equal priority for South African and international music. Users search artists, tracks and albums, identify likely spelling corrections, discover public catalogue records and manage direct MP3 downloads.

## Core requirements
- South African and international discovery receive equal ranking opportunity (50/50).
- South African genres include Amapiano, Maskandi, Gqom, House, Afro House, Deep House, 3-step, Kwaito, Gospel, Xitsonga/Shangaan disco, Mbaqanga, Isicathamiya, Afro-pop, traditional music, SA Hip-Hop and SA R&B.
- Artist aliases and typo-tolerant recognition; high-confidence corrections only. Ambiguous matches are suggestions, not silent substitutions.
- Known artist aliases are passed into every provider so the canonical name and up to three curated alternative spellings are queried separately.
- Internet Archive additionally searches exact phrases and all-token matches; genre metadata is an additional path for explicit local-genre searches, not a requirement for artist/title matches.
- Show source provenance and metadata.
- Offer a download action when the source exposes a direct public HTTPS MP3 URL and identifies it as MPEG audio. Rights metadata may be displayed but is not an in-app gate. Users are responsible for checking applicable source terms and rights.
- Never bypass DRM, authentication, paywalls, CAPTCHAs or access controls. Public accessibility does not itself grant copyright permission.
- Local search history, favourites, playback and offline library.

## Current catalogue sources
- **Internet Archive:** advanced metadata search plus item metadata resolution and public MP3 URL verification. Search includes phrase and token-based matching; curated alias variants are searched separately. Explicit South African genre queries can also use archive subject metadata.
- **Wikimedia Commons:** MediaWiki search API for audio files; searches the canonical artist name and curated aliases. Only direct HTTPS MP3 files with an MPEG audio MIME type are returned as downloadable results. File-page metadata is retained for source and rights display.
- **Openverse:** anonymous public audio-search API, filtered to music and MP3 results; canonical artist names and curated aliases are queried separately. The direct media URL is checked for an MPEG audio response before it is exposed. Openverse aggregates records from multiple public providers; its rights metadata is retained for display and does not gate the app action.
- All three sources are queried concurrently. A source failure does not discard results returned by the other sources.

These are public archives and aggregators, not comprehensive commercial music services. Current commercial South African releases may not be indexed or available. Coverage must not be described as complete.

## Architecture
Kotlin + Jetpack Compose, Room/SQLite, OkHttp, WorkManager, AndroidX Media3 and Android DownloadManager. Search providers are independent and can be added without introducing an app-owned backend or paid API dependency.
