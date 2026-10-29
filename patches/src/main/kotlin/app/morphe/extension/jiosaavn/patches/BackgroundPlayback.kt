/*
 * JioSaavn Background Playback Extension Class
 * 
 * Provides background playback logic called from bytecode patches.
 */

package app.morphe.extension.jiosaavn.patches

import android.app.KeyguardManager
import android.content.Context

/**
 * Background playback logic for JioSaavn.
 * Methods called from BackgroundPlaybackPatch bytecode injections.
 */
public class BackgroundPlayback {

    companion object {
        
        /**
         * Is background playback allowed for this content?
         */
        @JvmStatic
        public fun isBackgroundPlaybackAllowed(originalAllowed: Boolean): Boolean {
            // If user enabled the patch, allow background playback
            if (JioSaavnShared.isPatchEnabled("morphe_enable_background_playback")) {
                return true
            }
            return originalAllowed
        }
        
        /**
         * Is background playback allowed for kids content?
         */
        @JvmStatic
        public fun isKidsContentBackgroundAllowed(originalAllowed: Boolean): Boolean {
            if (JioSaavnShared.isPatchEnabled("morphe_background_play_kids_content")) {
                return true
            }
            return originalAllowed
        }
        
        /**
         * Should pause playback when screen is locked?
         */
        @JvmStatic
        public fun shouldPauseOnScreenLock(originalShouldPause: Boolean): Boolean {
            val mode = JioSaavnShared.getStringPref("morphe_auto_pause_on_lock", "never")
            return when (mode) {
                "never" -> false
                "always" -> true
                "keyguard" -> isKeyguardLocked()
                else -> originalShouldPause
            }
        }
        
        /**
         * Check if keyguard (lock screen) is showing
         */
        private fun isKeyguardLocked(): Boolean {
            val context = JioSaavnShared.getContext() ?: return false
            val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager?
            return keyguardManager?.isKeyguardLocked() ?: false
        }
        
        /**
         * Handle media session callback for background state
         */
        @JvmStatic
        public fun onMediaSessionCallback(callback: android.media.session.MediaSession.Callback) {
            // Can be used to intercept media session callbacks
            // e.g., prevent pause on headphone unplug, etc.
        }
    }
}