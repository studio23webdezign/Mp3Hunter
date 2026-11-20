package com.studio23webdezign.mp3hunter.search

/**
 * Conservative allowlist for licences that explicitly permit redistribution.
 * The Public Domain Mark is intentionally not treated as a licence: it is a
 * rights-status label, not a grant of permissions.
 */
object LicensePolicy {
    fun isDownloadPermitted(value: String): Boolean {
        val normalized = value.trim().lowercase()
        if (normalized.isBlank()) return false

        val compact = normalized
            .replace(" ", "")
            .replace("_", "-")

        return compact.contains("creativecommons.org/publicdomain/zero/") ||
            compact.contains("creativecommons.org/licenses/by/") ||
            compact.contains("creativecommons.org/licenses/by-sa/") ||
            compact == "cc0" ||
            compact.startsWith("cc0-1.0") ||
            compact.matches(Regex("cc-by(-| )?[0-9].*")) ||
            compact.matches(Regex("cc-by-sa(-| )?[0-9].*"))
    }
}
