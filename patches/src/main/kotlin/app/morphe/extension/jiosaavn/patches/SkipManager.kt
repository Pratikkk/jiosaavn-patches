/*
 * JioSaavn Skip Manager Extension Class
 * 
 * Provides unlimited skips logic called from bytecode patches.
 */

package app.morphe.extension.jiosaavn.patches

import java.util.concurrent.TimeUnit

/**
 * Skip management logic for JioSaavn.
 * Methods called from UnlimitedSkipsPatch bytecode injections.
 */
public class SkipManager {

    companion object {
        
        // In-memory skip tracking (bypasses app's tracking)
        private var skipCount = 0
        private var lastSkipTime = 0L
        private const val SKIP_COOLDOWN_MS = 1000 // 1 second minimum between skips
        
        /**
         * Can user skip current track?
         */
        @JvmStatic
        public fun canSkip(originalCanSkip: Boolean): Boolean {
            if (JioSaavnShared.isPatchEnabled("morphe_unlimited_skips")) {
                return true
            }
            return originalCanSkip
        }
        
        /**
         * Should we count this skip towards the limit?
         */
        @JvmStatic
        public fun shouldCountSkip(): Boolean {
            return !JioSaavnShared.isPatchEnabled("morphe_unlimited_skips")
        }
        
        /**
         * Is skip limit exceeded?
         */
        @JvmStatic
        public fun isSkipLimitExceeded(originalExceeded: Boolean): Boolean {
            if (JioSaavnShared.isPatchEnabled("morphe_unlimited_skips")) {
                return false
            }
            
            // Check custom limit override
            val customLimit = JioSaavnShared.getIntPref("morphe_skip_limit_override", 0)
            if (customLimit > 0 && skipCount >= customLimit) {
                return true
            }
            
            return originalExceeded
        }
        
        /**
         * Is skip cooldown active?
         */
        @JvmStatic
        public fun isSkipCooldownActive(originalActive: Boolean): Boolean {
            if (JioSaavnShared.isPatchEnabled("morphe_remove_skip_cooldown")) {
                return false
            }
            
            val now = System.currentTimeMillis()
            val timeSinceLastSkip = now - lastSkipTime
            
            if (timeSinceLastSkip < SKIP_COOLDOWN_MS) {
                return true
            }
            
            lastSkipTime = now
            return originalActive
        }
        
        /**
         * Can skip in radio/station mode?
         */
        @JvmStatic
        public fun canSkipRadio(originalCanSkip: Boolean): Boolean {
            if (JioSaavnShared.isPatchEnabled("morphe_unlimited_radio_skips")) {
                return true
            }
            return originalCanSkip
        }
        
        /**
         * Is skip button enabled?
         */
        @JvmStatic
        public fun isSkipButtonEnabled(originalEnabled: Boolean): Boolean {
            if (JioSaavnShared.isPatchEnabled("morphe_unlimited_skips")) {
                return true
            }
            return originalEnabled
        }
        
        /**
         * Track a skip occurred (for custom limit tracking)
         */
        @JvmStatic
        public fun onSkipPerformed() {
            skipCount++
            lastSkipTime = System.currentTimeMillis()
        }
        
        /**
         * Reset skip count (e.g., hourly reset)
         */
        @JvmStatic
        public fun resetSkipCount() {
            skipCount = 0
        }
        
        /**
         * Get current skip count
         */
        @JvmStatic
        public fun getSkipCount(): Int {
            return skipCount
        }
    }
}