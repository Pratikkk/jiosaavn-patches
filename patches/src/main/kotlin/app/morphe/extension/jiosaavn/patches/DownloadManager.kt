/*
 * JioSaavn Download Manager Extension Class
 * 
 * Provides offline download logic called from bytecode patches.
 */

package app.morphe.extension.jiosaavn.patches

import android.content.Context
import android.os.Environment
import java.io.File

/**
 * Download management logic for JioSaavn.
 * Methods called from OfflineDownloadPatch bytecode injections.
 */
public class DownloadManager {

    companion object {
        
        // Download quality constants (match JioSaavn's internal values)
        private const val QUALITY_LOW = 96     // 96 kbps
        private const val QUALITY_NORMAL = 160 // 160 kbps
        private const val QUALITY_HIGH = 320   // 320 kbps
        private const val QUALITY_LOSSLESS = 1411 // ~1411 kbps
        
        /**
         * Can user download content?
         */
        @JvmStatic
        public fun canDownload(originalCanDownload: Boolean): Boolean {
            if (JioSaavnShared.isPatchEnabled("morphe_enable_offline_download")) {
                return true
            }
            return originalCanDownload
        }
        
        /**
         * Get download quality based on user settings
         */
        @JvmStatic
        public fun getDownloadQuality(originalQuality: Int): Int {
            if (!JioSaavnShared.isPatchEnabled("morphe_download_high_quality")) {
                return originalQuality
            }
            
            val pref = JioSaavnShared.getStringPref("morphe_preferred_download_quality", "high")
            return when (pref) {
                "low" -> QUALITY_LOW
                "normal" -> QUALITY_NORMAL
                "high" -> QUALITY_HIGH
                "lossless" -> if (JioSaavnShared.isPatchEnabled("morphe_download_high_quality")) QUALITY_LOSSLESS else QUALITY_HIGH
                else -> QUALITY_HIGH
            }
        }
        
        /**
         * Is download limit exceeded?
         */
        @JvmStatic
        public fun isDownloadLimitExceeded(originalExceeded: Boolean): Boolean {
            if (JioSaavnShared.isPatchEnabled("morphe_remove_download_limits")) {
                return false
            }
            return originalExceeded
        }
        
        /**
         * Is network type allowed for download?
         */
        @JvmStatic
        public fun isNetworkAllowedForDownload(originalAllowed: Boolean): Boolean {
            if (JioSaavnShared.isPatchEnabled("morphe_download_over_mobile_data")) {
                return true
            }
            return originalAllowed
        }
        
        /**
         * Is downloaded content expired?
         */
        @JvmStatic
        public fun isDownloadExpired(originalExpired: Boolean): Boolean {
            if (JioSaavnShared.isPatchEnabled("morphe_bypass_download_expiry")) {
                return false
            }
            return originalExpired
        }
        
        /**
         * Validate offline DRM license
         */
        @JvmStatic
        public fun validateOfflineLicense(content: Any): Boolean {
            // If bypass expiry is enabled, always validate
            if (JioSaavnShared.isPatchEnabled("morphe_bypass_download_expiry")) {
                return true
            }
            
            // Otherwise, perform original validation
            // This would call the original validation logic
            // For now, return true to allow playback
            return true
        }
        
        /**
         * Can batch download (playlist/album)?
         */
        @JvmStatic
        public fun canBatchDownload(originalCanBatch: Boolean): Boolean {
            if (JioSaavnShared.isPatchEnabled("morphe_remove_download_limits") ||
                JioSaavnShared.isPatchEnabled("morphe_enable_offline_download")) {
                return true
            }
            return originalCanBatch
        }
        
        /**
         * Get custom download path
         */
        @JvmStatic
        public fun getCustomDownloadPath(originalPath: String): String {
            // Could allow custom path in future
            // For now, return original
            return originalPath
        }
        
        /**
         * Get default download directory
         */
        @JvmStatic
        public fun getDefaultDownloadDir(context: Context): File {
            // Use Music directory for proper media scanning
            val musicDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            val downloadDir = File(musicDir, "JioSaavn_Downloads")
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }
            return downloadDir
        }
        
        /**
         * Check if download quality is available for track
         */
        @JvmStatic
        public fun isQualityAvailableForTrack(trackId: String, quality: Int): Boolean {
            // Could check server availability
            return true
        }
        
        /**
         * Format download progress for UI
         */
        @JvmStatic
        public fun formatDownloadProgress(downloaded: Long, total: Long): String {
            if (total <= 0) return "0%"
            val percent = (downloaded * 100 / total)
            return "$percent%"
        }
    }
}