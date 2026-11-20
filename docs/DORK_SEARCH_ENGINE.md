# MP3 HUNTER — Dork Search Engine

The dork engine lives in its own package:
`app/src/main/java/com/studio23webdezign/mp3hunter/search/dork/`

It is intentionally separate from the existing search providers. It generates ranked Google-compatible query strings; a separate orchestration layer can execute those queries through supported search providers and inspect publicly accessible result pages.

## Capabilities

- Exact artist, track, album, genre, and general search intents.
- File filters: `filetype:mp3`, `ext:mp3`.
- Directory-index patterns: `intitle:"index of"`, `inurl:mp3`, `inurl:music`, `inurl:audio`.
- Filename, page-content, and common music-directory hints.
- Artist aliases and genre-context expansion.
- Optional release-year expansion.
- Ranking, case-insensitive deduplication, and a hard query cap (1–500).

## Example

```kotlin
val queries = DorkQueryEngine.generate(
    DorkSearchRequest(
        query = "Kabza De Small",
        intent = DorkIntent.ARTIST,
        aliases = listOf("Kabza"),
        market = DorkMarket.BOTH,
        maxQueries = 120
    )
)
```

## Search coverage design

The engine is a query generator, not a claim that Google exposes every indexed page or that every website is searchable. The next layer should support multiple legitimate search indexes/providers, pagination, provider-specific rate limits, result deduplication, and observable coverage metrics.

Only inspect pages that are publicly accessible. Do not bypass CAPTCHA, authentication, paywalls, robots restrictions, or anti-bot controls. A discovered audio URL is not proof that the file is licensed for download; retain source attribution and permission metadata where available.

## Planned next layer

1. Provider adapters for search indexes with documented public access.
2. Query batching and prioritization by result yield.
3. Accessible directory-page inspection and MP3 URL extraction.
4. Direct-audio verification with bounded requests.
5. Result ranking, caching, and diagnostics.
