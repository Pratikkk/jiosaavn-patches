/*
 * JioSaavn Patch Bundle
 * 
 * This bundle provides patches for JioSaavn music streaming app.
 * Package name: com.jio.media.jiobeats (verified from APK)
 */

package app.morphe.patches.jiosaavn

import app.morphe.patcher.patch.PatchBundle
import app.morphe.patches.jiosaavn.ad.HideAdsPatch
import app.morphe.patches.jiosaavn.backgroundplayback.BackgroundPlaybackPatch
import app.morphe.patches.jiosaavn.audio.HighQualityAudioPatch
import app.morphe.patches.jiosaavn.display.HighRefreshRatePatch
import app.morphe.patches.jiosaavn.playback.UnlimitedSkipsPatch
import app.morphe.patches.jiosaavn.download.OfflineDownloadPatch
import app.morphe.patches.jiosaavn.lyrics.LyricsAccessPatch
import app.morphe.patches.jiosaavn.audiofx.AudioEffectsPatch
import app.morphe.patches.jiosaavn.shared.JioSaavnSharedPatchBundle

/**
 * Main patch bundle for JioSaavn.
 * 
 * UID: 1001 (must be unique across all patch bundles in Morphe Manager)
 * Version: 1.0.0
 */
val jiosaavnPatchBundle = PatchBundle(
    name = "JioSaavn Patches",
    uid = 1001,
    version = "1.0.0",
    patches = listOf(
        // Shared patches (utilities, settings, extensions)
        JioSaavnSharedPatchBundle.sharedExtensionPatch,
        JioSaavnSharedPatchBundle.settingsPatch,
        JioSaavnSharedPatchBundle.versionCheckPatch,
        JioSaavnSharedPatchBundle.highRefreshRatePatch,
        JioSaavnSharedPatchBundle.displayManifestPatch,
        JioSaavnSharedPatchBundle.unlimitedSkipsPatch,
        JioSaavnSharedPatchBundle.offlineDownloadPatch,
        JioSaavnSharedPatchBundle.lyricsAccessPatch,
        JioSaavnSharedPatchBundle.audioEffectsPatch,
        
        // Feature patches
        HideAdsPatch.hideAdsPatch,
        BackgroundPlaybackPatch.backgroundPlaybackPatch,
        HighQualityAudioPatch.highQualityAudioPatch,
        HighRefreshRatePatch.highRefreshRatePatch,
        HighRefreshRatePatch.displayManifestPatch,
        UnlimitedSkipsPatch.unlimitedSkipsPatch,
        OfflineDownloadPatch.offlineDownloadPatch,
        LyricsAccessPatch.lyricsAccessPatch,
        AudioEffectsPatch.audioEffectsPatch,
        
        // Add more patches here as you develop them
        // CrossfadePatch.crossfadePatch,
        // SleepTimerPatch.sleepTimerPatch,
        // WidgetUnlockPatch.widgetUnlockPatch,
    )
)

// Compatibility constants
object Constants {
    /** Target package name - VERIFIED FROM APK: com.jio.media.jiobeats */
    const val PACKAGE_NAME = "com.jio.media.jiobeats"
    
    /** Compatibility target for patcher */
    const val COMPATIBILITY_JIOSAAVN = "jiosaavn"
    
    /** Minimum supported app version */
    const val MIN_VERSION = "10.0.0"
    
    /** Maximum tested app version */
    const val MAX_VERSION = "10.9.4"
}