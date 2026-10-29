/*
 * JioSaavn Audio Quality Extension Class
 * 
 * Provides high quality audio logic called from bytecode patches.
 */

package app.morphe.extension.jiosaavn.patches

/**
 * Audio quality logic for JioSaavn.
 * Methods called from HighQualityAudioPatch bytecode injections.
 */
public class AudioQuality {

    companion object {
        
        // Bitrate constants (match JioSaavn's internal values)
        private const val BITRATE_LOW = 64    // 64 kbps
        private const val BITRATE_NORMAL = 128 // 128 kbps
        private const val BITRATE_HIGH = 256   // 256 kbps
        private const val BITRATE_VERY_HIGH = 320 // 320 kbps
        private const val BITRATE_LOSSLESS = 1411 // ~1411 kbps (CD quality)
        
        /**
         * Get preferred bitrate based on user settings
         */
        @JvmStatic
        public fun getPreferredBitrate(originalBitrate: Int): Int {
            if (!JioSaavnShared.isPatchEnabled("morphe_enable_high_quality")) {
                return originalBitrate
            }
            
            val pref = JioSaavnShared.getStringPref("morphe_preferred_bitrate", "very_high")
            return when (pref) {
                "low" -> BITRATE_LOW
                "normal" -> BITRATE_NORMAL
                "high" -> BITRATE_HIGH
                "very_high" -> BITRATE_VERY_HIGH
                "lossless" -> if (JioSaavnShared.isPatchEnabled("morphe_enable_lossless")) BITRATE_LOSSLESS else BITRATE_VERY_HIGH
                else -> BITRATE_VERY_HIGH
            }
        }
        
        /**
         * Is high quality audio allowed for this user?
         */
        @JvmStatic
        public fun isHighQualityAllowed(originalAllowed: Boolean): Boolean {
            if (JioSaavnShared.isPatchEnabled("morphe_enable_high_quality")) {
                return true
            }
            return originalAllowed
        }
        
        /**
         * Modify stream URL to request higher quality
         */
        @JvmStatic
        public fun modifyStreamUrlForQuality(originalUrl: String): String {
            if (!JioSaavnShared.isPatchEnabled("morphe_enable_high_quality")) {
                return originalUrl
            }
            
            // Modify URL parameters to request higher bitrate
            // This depends on JioSaavn's streaming URL format
            // Common patterns: ?bitrate=320, &quality=high, /hd/, etc.
            
            var url = originalUrl
            
            // Try to replace bitrate parameter
            if (url.contains("bitrate=")) {
                val preferred = getPreferredBitrate(0)
                url = url.replace(Regex("bitrate=\\d+"), "bitrate=$preferred")
            }
            
            // Try to replace quality parameter
            if (url.contains("quality=")) {
                url = url.replace(Regex("quality=\\w+"), "quality=high")
            }
            
            // Add HD/lossless indicators if supported
            if (JioSaavnShared.isPatchEnabled("morphe_enable_lossless") && 
                !url.contains("lossless") && !url.contains("hd")) {
                if (url.contains("?")) {
                    url += "&format=lossless"
                } else {
                    url += "?format=lossless"
                }
            }
            
            return url
        }
        
        /**
         * Is gapless playback enabled?
         */
        @JvmStatic
        public fun isGaplessEnabled(): Boolean {
            return JioSaavnShared.isPatchEnabled("morphe_enable_gapless_playback")
        }
        
        /**
         * Get preferred audio codec
         * Returns codec ID matching JioSaavn's internal constants
         */
        @JvmStatic
        public fun getPreferredCodec(originalCodec: Int): Int {
            // Codec IDs (these must match JioSaavn's internal values):
            // 0 = AAC, 1 = Opus, 2 = MP3, 3 = FLAC, etc.
            // You'll need to reverse engineer these values
            
            if (!JioSaavnShared.isPatchEnabled("morphe_enable_high_quality")) {
                return originalCodec
            }
            
            // Prefer Opus for efficiency, FLAC for lossless
            if (JioSaavnShared.isPatchEnabled("morphe_enable_lossless")) {
                return 3 // FLAC (verify actual ID)
            }
            return 1 // Opus (verify actual ID)
        }
    }
}