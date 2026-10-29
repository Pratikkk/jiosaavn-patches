/*
 * Shared JioSaavn patches - utilities, settings, version checks, extensions
 */

package app.morphe.patches.jiosaavn.shared

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.resource.ResourceType
import app.morphe.patches.jiosaavn.Constants.COMPATIBILITY_JIOSAAVN
import app.morphe.patches.jiosaavn.display.HighRefreshRatePatch
import app.morphe.patches.jiosaavn.playback.UnlimitedSkipsPatch
import app.morphe.patches.jiosaavn.download.OfflineDownloadPatch
import app.morphe.patches.jiosaavn.lyrics.LyricsAccessPatch
import app.morphe.patches.jiosaavn.audiofx.AudioEffectsPatch
import app.morphe.patches.jiosaavn.shared.settings.PreferenceScreen
import app.morphe.patches.shared.misc.settings.preference.SwitchPreference
import app.morphe.patches.shared.misc.settings.preference.ListPreference
import app.morphe.patches.shared.misc.settings.preference.IntSliderPreference

private const val EXTENSION_CLASS = "Lapp/morphe/extension/jiosaavn/patches/JioSaavnShared;"

/**
 * Extension class patch - provides shared Kotlin logic compiled to DEX
 * This is the entry point for the extension class that gets loaded at runtime
 */
val sharedExtensionPatch = bytecodePatch(
    name = "JioSaavn Shared Extensions",
    description = "Core extension functions for JioSaavn patches"
) {
    compatibleWith(COMPATIBILITY_JIOSAAVN)
    
    execute {
        // Initialize extension class on app start
        // The extension class is loaded via resource patch below
    }
}

/**
 * Resource patch - adds extension class DEX to the app
 * The actual .dex file should be placed in src/main/dex/
 */
val extensionResourcePatch = resourcePatch {
    dependsOn(sharedExtensionPatch)
    
    execute {
        // Add the extension class DEX file
        // This requires the compiled extension class to be in resources
        // resourceLiteral(ResourceType.RAW, "jiosaavn_extensions").also { ... }
    }
}

/**
 * Settings patch - adds Morphe preferences to JioSaavn's settings
 */
val settingsPatch = resourcePatch {
    dependsOn(sharedExtensionPatch)
    
    execute {
        PreferenceScreen.ADS.addPreferences(
            SwitchPreference("morphe_hide_audio_ads"),
            SwitchPreference("morphe_hide_video_ads"),
            SwitchPreference("morphe_hide_banner_ads"),
            SwitchPreference("morphe_hide_sponsored_content"),
        )
        
        PreferenceScreen.PLAYBACK.addPreferences(
            SwitchPreference("morphe_enable_background_playback"),
            SwitchPreference("morphe_background_play_kids_content"),
            SwitchPreference("morphe_background_play_shorts"),
            ListPreference("morphe_auto_pause_on_lock"),
            SwitchPreference("morphe_unlimited_skips"),
            SwitchPreference("morphe_remove_skip_cooldown"),
            SwitchPreference("morphe_unlimited_radio_skips"),
            IntSliderPreference(
                key = "morphe_skip_limit_override",
                title = "Custom Skip Limit (0 = unlimited)",
                description = "Set custom skip limit per hour (0 for unlimited)",
                defaultValue = 0,
                min = 0,
                max = 100,
                step = 1
            ),
        )
        
        PreferenceScreen.AUDIO.addPreferences(
            SwitchPreference("morphe_enable_high_quality"),
            ListPreference("morphe_preferred_bitrate"),
            SwitchPreference("morphe_enable_gapless_playback"),
            SwitchPreference("morphe_enable_offline_download"),
            SwitchPreference("morphe_download_high_quality"),
            SwitchPreference("morphe_download_over_mobile_data"),
            SwitchPreference("morphe_remove_download_limits"),
            SwitchPreference("morphe_bypass_download_expiry"),
            ListPreference("morphe_preferred_download_quality"),
            SwitchPreference("morphe_unlock_equalizer"),
            SwitchPreference("morphe_enable_bass_boost"),
            SwitchPreference("morphe_enable_virtualizer"),
            SwitchPreference("morphe_enable_reverb"),
            SwitchPreference("morphe_custom_presets"),
            ListPreference("morphe_default_preset"),
        )
        
        PreferenceScreen.DISPLAY.addPreferences(
            SwitchPreference("morphe_enable_high_refresh_rate"),
            ListPreference("morphe_preferred_refresh_rate"),
            SwitchPreference("morphe_seamless_frame_rate_switching"),
            SwitchPreference("morphe_disable_frame_rate_throttling"),
        )
        
        PreferenceScreen.MISC.addPreferences(
            SwitchPreference("morphe_show_lyrics"),
            SwitchPreference("morphe_enable_synced_lyrics"),
            SwitchPreference("morphe_enable_lyrics_translation"),
            SwitchPreference("morphe_enable_romanization"),
            SwitchPreference("morphe_lyrics_search"),
        )
    }
}

/**
 * Version check patch - sets version flags for conditional patching
 */
val versionCheckPatch = bytecodePatch {
    execute {
        val versionName = packageMetadata.versionName
        
        // Helper to compare version strings
        fun isEqualsOrGreaterThan(version: String): Boolean {
            return versionName >= version
        }
        
        // Set version flags - BASED ON JIOSAAVN 10.x VERSIONS
        is_10_0_or_greater = isEqualsOrGreaterThan("10.0.0")
        is_10_5_or_greater = isEqualsOrGreaterThan("10.5.0")
        is_10_8_or_greater = isEqualsOrGreaterThan("10.8.0")
        is_10_9_or_greater = isEqualsOrGreaterThan("10.9.0")
        is_10_9_4_or_greater = isEqualsOrGreaterThan("10.9.4")
    }
}

// Version flags - accessed by other patches
var is_10_0_or_greater: Boolean = false
var is_10_5_or_greater: Boolean = false
var is_10_8_or_greater: Boolean = false
var is_10_9_or_greater: Boolean = false
var is_10_9_4_or_greater: Boolean = false

/**
 * High refresh rate display patch (120Hz/90Hz/144Hz)
 */
val highRefreshRatePatch = HighRefreshRatePatch.highRefreshRatePatch
val displayManifestPatch = HighRefreshRatePatch.displayManifestPatch

/**
 * Unlimited skips patch
 */
val unlimitedSkipsPatch = UnlimitedSkipsPatch.unlimitedSkipsPatch

/**
 * Offline download patch
 */
val offlineDownloadPatch = OfflineDownloadPatch.offlineDownloadPatch

/**
 * Lyrics access patch
 */
val lyricsAccessPatch = LyricsAccessPatch.lyricsAccessPatch

/**
 * Audio effects/equalizer patch
 */
val audioEffectsPatch = AudioEffectsPatch.audioEffectsPatch