/*
 * JioSaavn Display Refresh Rate Extension Class
 * 
 * Provides high refresh rate logic called from bytecode patches.
 * Uses Android 11+ (API 30+) setFrameRate API and Android 13+ (API 33+) 
 * DisplayMode APIs for seamless high refresh rate support.
 */

package app.morphe.extension.jiosaavn.patches

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.Display
import android.view.Surface
import android.view.SurfaceHolder
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.window.DisplayArea

/**
 * High refresh rate display logic for JioSaavn.
 * Methods called from HighRefreshRatePatch bytecode injections.
 */
public class DisplayRefreshRate {

    companion object {
        
        // Minimum API levels for features
        private const val API_SET_FRAME_RATE = 30      // Android 11
        private const val API_DISPLAY_MODES = 33       // Android 13
        private const val API_SEAMLESS_SWITCH = 34     // Android 14
        
        /**
         * Called on Activity.onCreate - earliest point to set frame rate
         */
        @JvmStatic
        public fun onActivityCreate(activity: Activity) {
            if (!JioSaavnShared.isPatchEnabled("morphe_enable_high_refresh_rate")) return
            if (Build.VERSION.SDK_INT < API_SET_FRAME_RATE) return
            
            val window = activity.window
            applyHighRefreshRateToWindow(window)
        }
        
        /**
         * Called on Activity.onResume - good fallback point
         */
        @JvmStatic
        public fun onActivityResume(activity: Activity) {
            if (!JioSaavnShared.isPatchEnabled("morphe_enable_high_refresh_rate")) return
            if (Build.VERSION.SDK_INT < API_SET_FRAME_RATE) return
            
            val window = activity.window
            applyHighRefreshRateToWindow(window)
        }
        
        /**
         * Called on View.onAttachedToWindow - works for decor view
         */
        @JvmStatic
        public fun onViewAttachedToWindow(view: View) {
            if (!JioSaavnShared.isPatchEnabled("morphe_enable_high_refresh_rate")) return
            if (Build.VERSION.SDK_INT < API_SET_FRAME_RATE) return
            
            // Check if this is the decor view (root view of activity)
            if (view.parent == null) {
                val window = (view.context as? Activity)?.window
                window?.let { applyHighRefreshRateToWindow(it) }
            }
        }
        
        /**
         * Called on SurfaceHolder.Callback.surfaceCreated
         */
        @JvmStatic
        public fun onSurfaceCreated(holder: SurfaceHolder, surface: Surface) {
            if (!JioSaavnShared.isPatchEnabled("morphe_enable_high_refresh_rate")) return
            if (Build.VERSION.SDK_INT < API_SET_FRAME_RATE) return
            
            // Surface created - good time to set frame rate on the surface
            surface.setFrameRate(getPreferredRefreshRate(), Surface.FRAME_RATE_COMPATIBILITY_DEFAULT)
        }
        
        /**
         * Called on SurfaceHolder.Callback.surfaceChanged
         */
        @JvmStatic
        public fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            if (!JioSaavnShared.isPatchEnabled("morphe_enable_high_refresh_rate")) return
            if (Build.VERSION.SDK_INT < API_SET_FRAME_RATE) return
            
            val surface = holder.surface
            if (surface.isValid) {
                surface.setFrameRate(getPreferredRefreshRate(), Surface.FRAME_RATE_COMPATIBILITY_DEFAULT)
            }
        }
        
        /**
         * Apply high refresh rate settings to WindowManager.LayoutParams
         */
        @JvmStatic
        public fun applyHighRefreshRateParams(params: WindowManager.LayoutParams) {
            if (!JioSaavnShared.isPatchEnabled("morphe_enable_high_refresh_rate")) return
            if (Build.VERSION.SDK_INT < API_SET_FRAME_RATE) return
            
            // Set preferred display mode ID if available (Android 13+)
            if (Build.VERSION.SDK_INT >= API_DISPLAY_MODES) {
                val modeId = getPreferredDisplayModeId(params)
                if (modeId != -1) {
                    params.preferredDisplayModeId = modeId
                }
            }
            
            // Set frame rate hint (Android 11+)
            params.preferredFrameRate = getPreferredRefreshRate().toFloat()
        }
        
        /**
         * Select highest available refresh rate mode
         */
        @JvmStatic
        public fun selectHighestRefreshRateMode(currentModeId: Int): Int {
            if (!JioSaavnShared.isPatchEnabled("morphe_enable_high_refresh_rate")) {
                return currentModeId
            }
            if (Build.VERSION.SDK_INT < API_DISPLAY_MODES) return currentModeId
            
            // This would need a Display reference - in practice, 
            // the fingerprint would need to pass the Display object
            return currentModeId
        }
        
        /**
         * Apply high refresh rate to a window
         */
        private fun applyHighRefreshRateToWindow(window: Window) {
            val decorView = window.decorView
            val display = decorView.display
            
            if (Build.VERSION.SDK_INT >= API_DISPLAY_MODES) {
                // Android 13+: Use DisplayMode API for precise control
                setHighRefreshRateMode(display, window)
            } else if (Build.VERSION.SDK_INT >= API_SET_FRAME_RATE) {
                // Android 11-12: Use setFrameRate on decor view
                val rate = getPreferredRefreshRate().toFloat()
                decorView.setFrameRate(rate, Surface.FRAME_RATE_COMPATIBILITY_DEFAULT)
            }
            
            // Also apply to window attributes
            val params = window.attributes
            applyHighRefreshRateParams(params)
            window.attributes = params
        }
        
        /**
         * Android 13+: Set preferred display mode
         */
        private fun setHighRefreshRateMode(display: Display, window: Window) {
            val modes = display.supportedModes
            val preferredMode = findBestMode(modes)
            
            if (preferredMode != null) {
                // Set preferred display mode on window
                window.preferredDisplayModeId = preferredMode.modeId
                
                // Also request frame rate on decor view for seamless switching
                if (Build.VERSION.SDK_INT >= API_SEAMLESS_SWITCH &&
                    JioSaavnShared.isPatchEnabled("morphe_seamless_frame_rate_switching")) {
                    window.decorView.setFrameRate(
                        preferredMode.refreshRate,
                        Surface.FRAME_RATE_COMPATIBILITY_FIXED_SOURCE
                    )
                } else {
                    window.decorView.setFrameRate(
                        preferredMode.refreshRate,
                        Surface.FRAME_RATE_COMPATIBILITY_DEFAULT
                    )
                }
            }
        }
        
        /**
         * Find the best display mode based on user preferences
         */
        private fun findBestMode(modes: Array<Display.Mode>): Display.Mode? {
            val preferredRate = getPreferredRefreshRate()
            
            // Filter modes by preferred refresh rate
            val candidates = modes.filter { mode ->
                val rate = mode.refreshRate
                when (preferredRate) {
                    120 -> rate >= 119 && rate <= 121
                    90 -> rate >= 89 && rate <= 91
                    144 -> rate >= 143 && rate <= 145
                    else -> rate >= preferredRate - 1 && rate <= preferredRate + 1
                }
            }
            
            // Return highest refresh rate match, or highest overall
            return candidates.maxByOrNull { it.refreshRate } 
                ?: modes.maxByOrNull { it.refreshRate }
        }
        
        /**
         * Get preferred refresh rate from user settings
         */
        private fun getPreferredRefreshRate(): Int {
            val pref = JioSaavnShared.getStringPref("morphe_preferred_refresh_rate", "highest")
            return when (pref) {
                "60" -> 60
                "90" -> 90
                "120" -> 120
                "144" -> 144
                "highest" -> 120  // Default to 120, will be capped by device capability
                else -> 120
            }
        }
        
        /**
         * Get preferred display mode ID for WindowManager.LayoutParams
         */
        private fun getPreferredDisplayModeId(params: WindowManager.LayoutParams): Int {
            // This requires a Display reference which we don't have in LayoutParams
            // In practice, the fingerprint would need to pass the Display/Window
            return -1
        }
        
        /**
         * Should throttle frame rate? (For Choreographer hook)
         */
        @JvmStatic
        public fun shouldThrottleFrameRate(): Boolean {
            return !JioSaavnShared.isPatchEnabled("morphe_disable_frame_rate_throttling")
        }
    }
}