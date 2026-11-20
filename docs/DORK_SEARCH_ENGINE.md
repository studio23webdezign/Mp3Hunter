# MP3 HUNTER — Dork Search Engine

The dork engine lives in its own package:
`app/src/main/java/com/studio23webdezign/mp3hunter/search/dork/`

It is intentionally separate from the existing search providers. It generates ranked Google-compatible query strings, while the web-discovery provider executes a limited, diversified selection against Mwmbl's documented public search API and inspects reachable public result pages for direct MP3 links.

## Capabilities

- Exact artist, track, album, genre, and general search intents.
- File filters: `filetype:mp3`, `ext:mp3`.
- Directory-index patterns: `intitle:"index of"`, `inurl:mp3`, `inurl:music`, `inurl:audio`.
- Filename, page-content, and common music-directory hints.
- Artist aliases and genre-context expansion.
- South African domain/context variants including `site:co.za`.
- Ranking, case-insensitive deduplication, and a hard query cap (1–500).
- Google URL builder for opening a generated dork in the user's browser.

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
val googleLinks = GoogleDorkUrlBuilder.buildAll(queries)
```

## Execution strategy

The web-discovery provider selects a diversified subset of generated query families rather than submitting every possible combination. This balances search coverage against provider quotas, request pacing, battery use, and mobile data. Mwmbl's anonymous API tier is documented as 1,000 requests/month at 1 request/second; quotas are shared by IP. A single MP3 HUNTER search therefore uses a bounded request budget and gracefully returns partial results when the provider limits requests.

Google links are generated for user-initiated browser searches. MP3 HUNTER does not automate Google SERP scraping or solve challenges.

## Coverage and access boundaries

The engine cannot guarantee every indexed page or every MP3 exists in a provider's index. Only inspect pages that are publicly accessible. Do not bypass CAPTCHA, authentication, paywalls, robots restrictions, or anti-bot controls. A discovered audio URL is not proof that the file is licensed for download; retain source attribution and permission metadata where available.

## Planned next layer

1. Additional search-index adapters with documented public access and suitable reuse terms.
2. Query batching and yield-based prioritization.
3. Accessible directory-page inspection and MP3 URL extraction.
4. Direct-audio verification with bounded requests and a GET-range fallback for hosts that reject HEAD.
5. Result ranking, caching, and diagnostics.
