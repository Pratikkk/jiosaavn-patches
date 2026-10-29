/*
 * JioSaavn Preference Screens
 * Defines the settings categories shown in Morphe Manager
 */

package app.morphe.patches.jiosaavn.shared.settings

import app.morphe.patches.shared.misc.settings.PreferenceScreen as BasePreferenceScreen

/**
 * Preference screens for JioSaavn patches.
 * These appear in Morphe Manager's patch configuration UI.
 */
object PreferenceScreen {
    /** Ad-related settings */
    val ADS = BasePreferenceScreen("ads", "Ads & Sponsored Content")
    
    /** Playback settings */
    val PLAYBACK = BasePreferenceScreen("playback", "Playback & Background")
    
    /** Audio quality settings */
    val AUDIO = BasePreferenceScreen("audio", "Audio Quality & Effects")
    
    /** Display settings */
    val DISPLAY = BasePreferenceScreen("display", "Display & Refresh Rate")
    
    /** Miscellaneous settings */
    val MISC = BasePreferenceScreen("misc", "Additional Features")
}