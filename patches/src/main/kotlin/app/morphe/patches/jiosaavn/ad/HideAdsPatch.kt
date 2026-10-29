/*
 * JioSaavn Ad Removal Patch
 * 
 * This patch hides various types of ads in JioSaavn:
 * - Audio ads between songs
 * - Video ads
 * - Banner ads in UI
 * - Sponsored content in playlists/feeds
 * 
 * TODO: Replace fingerprints with actual JioSaavn method signatures
 * after reverse engineering the APK.
 */

package app.morphe.patches.jiosaavn.ad

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.OpcodesFilter
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patches.jiosaavn.Constants.COMPATIBILITY_JIOSAAVN
import app.morphe.patches.jiosaavn.shared.JioSaavnSharedPatchBundle.{settingsPatch, versionCheckPatch, sharedExtensionPatch}
import app.morphe.patches.jiosaavn.shared.settings.PreferenceScreen
import app.morphe.patches.shared.misc.settings.preference.SwitchPreference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val EXTENSION_CLASS = "Lapp/morphe/extension/jiosaavn/patches/AdFilter;"

private val hideAdsResourcePatch = resourcePatch {
    dependsOn(
        settingsPatch,
        versionCheckPatch,
        sharedExtensionPatch
    )
    
    execute {
        PreferenceScreen.ADS.addPreferences(
            SwitchPreference("morphe_hide_audio_ads"),
            SwitchPreference("morphe_hide_video_ads"),
            SwitchPreference("morphe_hide_banner_ads"),
            SwitchPreference("morphe_hide_sponsored_content"),
        )
    }
}

/**
 * Main ad hiding patch
 */
@Suppress("unused")
val hideAdsPatch = bytecodePatch(
    name = "Hide Ads",
    description = "Removes audio, video, and banner ads from JioSaavn"
) {
    dependsOn(
        hideAdsResourcePatch,
        settingsPatch,
        versionCheckPatch,
        sharedExtensionPatch
    )
    
    compatibleWith(COMPATIBILITY_JIOSAAVN)
    
    execute {
        // ============================================================
        // TODO: REPLACE THESE FINGERPRINTS WITH ACTUAL JIOSAAVN ONES
        // ============================================================
        // 
        // Steps to create real fingerprints:
        // 1. apktool d jiosaavn.apk -o jiosaavn
        // 2. grep -r "ad\|Ad\|AD" jiosaavn/smali/ --include="*.smali" | head -50
        // 3. Find methods like: loadAd, showAd, playAd, requestAd, isAdEnabled
        // 4. Create fingerprints using opcode patterns
        // 5. Test on multiple versions
        // ============================================================
        
        // --- AUDIO ADS ---
        // Hook method that loads/plays audio ads between songs
        AudioAdPlayerFingerprint.method.addInstructionsWithLabels(
            0,
            """
                invoke-static { }, $EXTENSION_CLASS->shouldHideAudioAds()Z
                move-result v0
                if-eqz v0, :play_audio_ad
                return-void
                :play_audio_ad
                nop
            """
        )
        
        // --- VIDEO ADS ---
        // Hook video ad playback
        VideoAdPlayerFingerprint.method.addInstructionsWithLabels(
            0,
            """
                invoke-static { }, $EXTENSION_CLASS->shouldHideVideoAds()Z
                move-result v0
                if-eqz v0, :play_video_ad
                return-void
                :play_video_ad
                nop
            """
        )
        
        // --- BANNER ADS ---
        // Hook banner ad view inflation/loading
        BannerAdLoaderFingerprint.method.addInstructionsWithLabels(
            0,
            """
                invoke-static { }, $EXTENSION_CLASS->shouldHideBannerAds()Z
                move-result v0
                if-eqz v0, :load_banner
                return-void
                :load_banner
                nop
            """
        )
        
        // --- SPONSORED CONTENT ---
        // Hook sponsored playlist/feed items
        SponsoredContentFilterFingerprint.method.addInstructionsWithLabels(
            0,
            """
                invoke-static { p0 }, $EXTENSION_CLASS->filterSponsoredContent(Ljava/util/List;)Ljava/util/List;
                move-result-object p0
            """
        )
        
        // --- AD REQUEST NETWORK CALLS ---
        // Block ad network requests at the HTTP client level
        AdNetworkRequestFingerprint.method.addInstructionsWithLabels(
            0,
            """
                invoke-static { p0 }, $EXTENSION_CLASS->isAdRequest(Ljava/lang/String;)Z
                move-result v0
                if-eqz v0, :allow_request
                return-void
                :allow_request
                nop
            """
        )
    }
}

/*
 * ================================================================
 * FINGERPRINTS - REPLACE WITH ACTUAL JIOSAAVN METHOD SIGNATURES
 * ================================================================
 * 
 * How to create fingerprints:
 * 1. Decompile JioSaavn APK: apktool d jiosaavn.apk
 * 2. Search for ad-related classes: 
 *    grep -r "ad\|Ad\|AD" jiosaavn/smali/ -l | xargs grep -l "class" | head -20
 * 3. Look for method signatures like:
 *    - Lcom/jio/media/jiosavn/ads/AdPlayer;->playAudioAd()V
 *    - Lcom/jio/media/jiosavn/ads/AdLoader;->loadBannerAd()V
 *    - Lcom/jio/media/jiosavn/network/ApiClient;->requestAd()L...
 * 4. Use `app.morphe.patcher.Fingerprint` with opcode patterns
 * 
 * Example fingerprint structure:
 * internal object MyAdMethodFingerprint : Fingerprint(
 *     accessFlags = listOf(AccessFlags.PUBLIC),
 *     returnType = "V",
 *     parameters = listOf(),
 *     filters = OpcodesFilter.opcodesToFilters(
 *         Opcode.INVOKE_VIRTUAL,
 *         Opcode.CONST_STRING,
 *         Opcode.INVOKE_STATIC,
 *         // ... unique opcode sequence
 *     )
 * )
 */

// Audio ad player - plays ads between songs
internal object AudioAdPlayerFingerprint : Fingerprint(
    accessFlags = listOf(),
    returnType = "V",
    parameters = listOf(),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.INVOKE_VIRTUAL,
        Opcode.CONST_STRING,
        Opcode.INVOKE_STATIC,
        Opcode.RETURN_VOID
    )
)

// Video ad player - plays video ads
internal object VideoAdPlayerFingerprint : Fingerprint(
    accessFlags = listOf(),
    returnType = "V",
    parameters = listOf(),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.INVOKE_VIRTUAL,
        Opcode.CONST_STRING,
        Opcode.INVOKE_STATIC,
        Opcode.RETURN_VOID
    )
)

// Banner ad loader - loads banner ad views
internal object BannerAdLoaderFingerprint : Fingerprint(
    accessFlags = listOf(),
    returnType = "V",
    parameters = listOf(),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.INVOKE_VIRTUAL,
        Opcode.NEW_INSTANCE,
        Opcode.INVOKE_DIRECT,
        Opcode.RETURN_VOID
    )
)

// Sponsored content filter - filters sponsored items from feeds
internal object SponsoredContentFilterFingerprint : Fingerprint(
    accessFlags = listOf(),
    returnType = "Ljava/util/List;",
    parameters = listOf("Ljava/util/List;"),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.INVOKE_INTERFACE,
        Opcode.CHECK_CAST,
        Opcode.INVOKE_STATIC,
        Opcode.RETURN_OBJECT
    )
)

// Ad network request - blocks ad API calls
internal object AdNetworkRequestFingerprint : Fingerprint(
    accessFlags = listOf(),
    returnType = "V",
    parameters = listOf("Ljava/lang/String;"),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.INVOKE_VIRTUAL,
        Opcode.CONST_STRING,
        Opcode.IF_EQ,
        Opcode.RETURN_VOID
    )
)