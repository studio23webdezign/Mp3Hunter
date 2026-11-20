package com.studio23webdezign.mp3hunter.recognition

import java.text.Normalizer
import java.util.Locale
import kotlin.math.max

data class Artist(val canonicalName: String, val aliases: List<String> = emptyList())
data class ArtistMatch(val artist: Artist, val confidence: Double)

class ArtistMatcher(private val artists: List<Artist>) {
    fun match(input: String): ArtistMatch? {
        val query = normalize(input)
        if (query.isBlank()) return null

        val ranked = artists.mapNotNull { artist ->
            val names = listOf(artist.canonicalName) + artist.aliases
            val score = names.maxOf { candidate ->
                val normalized = normalize(candidate)
                when {
                    query == normalized -> 1.0
                    else -> max(jaroWinkler(query, normalized), 1.0 - levenshtein(query, normalized).toDouble() / max(query.length, normalized.length).coerceAtLeast(1))
                }
            }
            ArtistMatch(artist, score)
        }.sortedByDescending { it.confidence }

        val best = ranked.firstOrNull() ?: return null
        val runnerUp = ranked.getOrNull(1)?.confidence ?: 0.0
        return best.takeIf { it.confidence >= 0.90 && it.confidence - runnerUp >= 0.06 }
    }

    fun suggestions(input: String, limit: Int = 5): List<ArtistMatch> {
        val query = normalize(input)
        if (query.isBlank()) return emptyList()
        return artists.map { artist ->
            val score = (listOf(artist.canonicalName) + artist.aliases).maxOf { candidate ->
                val normalized = normalize(candidate)
                max(jaroWinkler(query, normalized), 1.0 - levenshtein(query, normalized).toDouble() / max(query.length, normalized.length).coerceAtLeast(1))
            }
            ArtistMatch(artist, score)
        }.filter { it.confidence >= 0.76 }.sortedByDescending { it.confidence }.take(limit)
    }

    private fun normalize(value: String): String =
        Normalizer.normalize(value.lowercase(Locale.ROOT), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .replace(Regex("[^a-z0-9]+"), " ")
            .trim()
            .replace(Regex("\\s+"), " ")

    private fun jaroWinkler(a: String, b: String): Double {
        if (a == b) return 1.0
        if (a.isEmpty() || b.isEmpty()) return 0.0
        val range = (max(a.length, b.length) / 2 - 1).coerceAtLeast(0)
        val aMatch = BooleanArray(a.length)
        val bMatch = BooleanArray(b.length)
        var matches = 0
        for (i in a.indices) {
            for (j in (i - range).coerceAtLeast(0)..(i + range).coerceAtMost(b.lastIndex)) {
                if (!bMatch[j] && a[i] == b[j]) {
                    aMatch[i] = true; bMatch[j] = true; matches++; break
                }
            }
        }
        if (matches == 0) return 0.0
        var transpositions = 0
        var j = 0
        for (i in a.indices) if (aMatch[i]) {
            while (!bMatch[j]) j++
            if (a[i] != b[j]) transpositions++
            j++
        }
        val m = matches.toDouble()
        val jaro = (m / a.length + m / b.length + (m - transpositions / 2.0) / m) / 3.0
        val prefix = a.zip(b).takeWhile { it.first == it.second }.size.coerceAtMost(4)
        return jaro + prefix * 0.1 * (1.0 - jaro)
    }

    private fun levenshtein(a: String, b: String): Int {
        var previous = IntArray(b.length + 1) { it }
        for (i in a.indices) {
            val current = IntArray(b.length + 1)
            current[0] = i + 1
            for (j in b.indices) {
                val cost = if (a[i] == b[j]) 0 else 1
                current[j + 1] = minOf(current[j] + 1, previous[j + 1] + 1, previous[j] + cost)
            }
            previous = current
        }
        return previous[b.length]
    }
}
