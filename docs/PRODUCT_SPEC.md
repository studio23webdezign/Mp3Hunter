# MP3 HUNTER — Product Specification

## Product promise
A mobile-first music discovery tool with equal priority for South African and international music. Users search artists, tracks and albums, identify likely spelling corrections, discover sources and manage permitted downloads.

## Core requirements
- South African and international discovery receive equal ranking opportunity (50/50).
- South African genres include Amapiano, Maskandi, Gqom, House, Afro House, Deep House, 3-step, Kwaito, Gospel, Xitsonga/Shangaan disco, Mbaqanga, Isicathamiya, Afro-pop, traditional music, SA Hip-Hop and SA R&B.
- Artist aliases and typo-tolerant recognition; high-confidence corrections only. Ambiguous matches are suggestions, not silent substitutions.
- Show source provenance and metadata.
- Offer a download action when the source exposes a direct public HTTPS MP3 URL and the server confirms an audio response; rights metadata may be displayed but is not an in-app gate. Users are responsible for checking applicable source terms and rights.
- Never bypass DRM, authentication, paywalls, CAPTCHAs or access controls. Public accessibility does not itself grant copyright permission.
- Local search history, favourites, queue, playback and offline library.

## Architecture
Kotlin + Jetpack Compose, Room/SQLite, OkHttp, WorkManager, AndroidX Media3 and Android DownloadManager. No proprietary paid API is required for the initial foundation.
