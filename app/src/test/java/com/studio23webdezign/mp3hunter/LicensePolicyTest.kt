package com.studio23webdezign.mp3hunter

import com.studio23webdezign.mp3hunter.search.LicensePolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LicensePolicyTest {
    @Test fun acceptsExplicitCreativeCommonsGrants() {
        assertTrue(LicensePolicy.isDownloadPermitted("https://creativecommons.org/publicdomain/zero/1.0/"))
        assertTrue(LicensePolicy.isDownloadPermitted("https://creativecommons.org/licenses/by/4.0/"))
        assertTrue(LicensePolicy.isDownloadPermitted("https://creativecommons.org/licenses/by-sa/4.0/"))
        assertTrue(LicensePolicy.isDownloadPermitted("CC BY 4.0"))
        assertTrue(LicensePolicy.isDownloadPermitted("CC BY-SA 4.0"))
    }

    @Test fun rejectsNonCommercialAndNoDerivativesLicences() {
        assertFalse(LicensePolicy.isDownloadPermitted("https://creativecommons.org/licenses/by-nc/4.0/"))
        assertFalse(LicensePolicy.isDownloadPermitted("https://creativecommons.org/licenses/by-nd/4.0/"))
        assertFalse(LicensePolicy.isDownloadPermitted("https://creativecommons.org/licenses/by-nc-sa/4.0/"))
    }

    @Test fun rejectsUnknownOrUnclearRightsMetadata() {
        assertFalse(LicensePolicy.isDownloadPermitted(""))
        assertFalse(LicensePolicy.isDownloadPermitted("copyrighted"))
        assertFalse(LicensePolicy.isDownloadPermitted("https://creativecommons.org/publicdomain/mark/1.0/"))
    }
}
