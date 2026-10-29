/*
 * JioSaavn Shared Extension Class
 * 
 * This class is compiled to DEX and injected into the patched app.
 * It provides shared utility functions used by multiple patches.
 * 
 * Place compiled .dex file in: src/main/dex/
 * Build with: kotlinc -d jiosaavn_shared.jar SharedExtension.kt
 * Then: d8 --release --output src/main/dex jiosaavn_shared.jar
 */

package app.morphe.extension.jiosaavn.patches

import android.content.Context
import android.content.SharedPreferences

/**
 * Shared extension functions for JioSaavn patches.
 * 
 * This class is loaded at runtime by the patched app.
 * All methods must be static and public.
 */
public class JioSaavnShared {

    companion object {
        private const val PREFS_NAME = "morphe_jiosaavn_prefs"
        private var prefs: SharedPreferences? = null
        private var context: Context? = null
        
        /**
         * Initialize the extension - called from onCreate hook
         */
        @JvmStatic
        public fun initialize(appContext: Context) {
            context = appContext
            prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
        
        /**
         * Check if a patch is enabled in settings
         */
        @JvmStatic
        public fun isPatchEnabled(patchKey: String): Boolean {
            return prefs?.getBoolean(patchKey, false) ?: false
        }
        
        /**
         * Get string preference value
         */
        @JvmStatic
        public fun getStringPref(key: String, defaultValue: String = ""): String {
            return prefs?.getString(key, defaultValue) ?: defaultValue
        }
        
        /**
         * Get int preference value
         */
        @JvmStatic
        public fun getIntPref(key: String, defaultValue: Int = 0): Int {
            return prefs?.getInt(key, defaultValue) ?: defaultValue
        }
        
        /**
         * Log debug message (only in debug builds)
         */
        @JvmStatic
        public fun log(tag: String, message: String) {
            if (BuildConfig.DEBUG) {
                android.util.Log.d("Morphe_JioSaavn_$tag", message)
            }
        }
    }
}