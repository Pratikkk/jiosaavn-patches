/*
 * JioSaavn Offline Download Patch
 * 
 * Enables offline downloads for free users:
 * - Removes premium requirement for downloads
 * - Unlocks high quality download options
 * - Removes download limits (number of songs, playlist size)
 * - Enables download over mobile data (not just WiFi)
 * - Bypasses download expiration/renewal checks
 * 
 * TODO: Replace fingerprints with actual JioSaavn method signatures
 */

package app.morphe.patches.jiosaavn.download

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.OpcodesFilter
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patches.jiosaavn.Constants.COMPATIBILITY_JIOSAAVN
import app.morphe.patches.jiosaavn.shared.JioSaavnSharedPatchBundle.{settingsPatch, versionCheckPatch, sharedExtensionPatch}
import app.morphe.patches.jiosaavn.shared.settings.PreferenceScreen
import app.morphe.patches.shared.misc.settings.preference.SwitchPreference
import app.morphe.patches.shared.misc.settings.preference.ListPreference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val EXTENSION_CLASS = "Lapp/morphe/extension/jiosaavn/patches/DownloadManager;"

private val offlineDownloadResourcePatch = resourcePatch {
    dependsOn(
        settingsPatch,
        versionCheckPatch,
        sharedExtensionPatch
    )
    
    execute {
        PreferenceScreen.MISC.addPreferences(
            SwitchPreference("morphe_enable_offline_download"),
            SwitchPreference("morphe_download_high_quality"),
            SwitchPreference("morphe_download_over_mobile_data"),
            SwitchPreference("morphe_remove_download_limits"),
            SwitchPreference("morphe_bypass_download_expiry"),
            ListPreference("morphe_preferred_download_quality"),
        )
    }
}

/**
 * Main offline download patch
 */
@Suppress("unused")
val offlineDownloadPatch = bytecodePatch(
    name = "Offline Download",
    description = "Enables high-quality offline downloads without premium"
) {
    dependsOn(
        offlineDownloadResourcePatch,
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
        // Search for in decompiled APK:
        // - "download" "Download" "DOWNLOAD" in manager/repository classes
        // - "canDownload" "isDownloadAllowed" "downloadEligible"
        // - "downloadQuality" "downloadBitrate" "offlineQuality"
        // - "downloadLimit" "maxDownloads" "playlistDownloadLimit"
        // - "wifiOnly" "mobileData" "networkType" in download logic
        // - "expiry" "expiration" "renewal" "license" in offline logic
        // - "DRM" "license" "widevine" "clearkey" for protected content
        // ============================================================
        
        // --- DOWNLOAD ELIGIBILITY CHECK ---
        // Hook the method that checks if user can download
        CanDownloadCheckFingerprint.method.apply {
            findInstructionIndicesReversedOrThrow(Opcode.RETURN).forEach { index ->
                val register = getInstruction<OneRegisterInstruction>(index).registerA
                addInstructionsWithLabels(
                    index,
                    """
                        invoke-static { v$register }, $EXTENSION_CLASS->canDownload(Z)Z
                        move-result v$register
                    """
                )
            }
        }
        
        // --- DOWNLOAD QUALITY SELECTION ---
        // Hook download quality/bitrate selection
        DownloadQualitySelectionFingerprint.method.apply {
            findInstructionIndicesReversedOrThrow(Opcode.RETURN).forEach { index ->
                val register = getInstruction<OneRegisterInstruction>(index).registerA
                addInstructionsWithLabels(
                    index,
                    """
                        invoke-static { v$register }, $EXTENSION_CLASS->getDownloadQuality(I)I
                        move-result v$register
                    """
                )
            }
        }
        
        // --- DOWNLOAD LIMIT CHECK ---
        // Hook download count/limit enforcement
        DownloadLimitCheckFingerprint.method.apply {
            findInstructionIndicesReversedOrThrow(Opcode.RETURN).forEach { index ->
                val register = getInstruction<OneRegisterInstruction>(index).registerA
                addInstructionsWithLabels(
                    index,
                    """
                        invoke-static { v$register }, $EXTENSION_CLASS->isDownloadLimitExceeded(Z)Z
                        move-result v$register
                    """
                )
            }
        }
        
        // --- NETWORK TYPE CHECK (WiFi vs Mobile) ---
        // Hook WiFi-only restriction
        NetworkTypeCheckFingerprint.method.apply {
            findInstructionIndicesReversedOrThrow(Opcode.RETURN).forEach { index ->
                val register = getInstruction<OneRegisterInstruction>(index).registerA
                addInstructionsWithLabels(
                    index,
                    """
                        invoke-static { v$register }, $EXTENSION_CLASS->isNetworkAllowedForDownload(Z)Z
                        move-result v$register
                    """
                )
            }
        }
        
        // --- DOWNLOAD EXPIRY/RENEWAL CHECK ---
        // Hook offline content expiration
        DownloadExpiryCheckFingerprint.method.apply {
            findInstructionIndicesReversedOrThrow(Opcode.RETURN).forEach { index ->
                val register = getInstruction<OneRegisterInstruction>(index).registerA
                addInstructionsWithLabels(
                    index,
                    """
                        invoke-static { v$register }, $EXTENSION_CLASS->isDownloadExpired(Z)Z
                        move-result v$register
                    """
                )
            }
        }
        
        // --- DRM/LICENSE CHECK ---
        // Hook DRM license validation for offline playback
        DrmLicenseCheckFingerprint.method.addInstructionsWithLabels(
            0,
            """
                invoke-static { p0 }, $EXTENSION_CLASS->validateOfflineLicense(Ljava/lang/Object;)Z
                move-result v0
                if-eqz v0, :license_ok
                return-void
                :license_ok
                nop
            """
        )
        
        // --- PLAYLIST/ALBUM DOWNLOAD ---
        // Hook batch download eligibility
        BatchDownloadCheckFingerprint.method.apply {
            findInstructionIndicesReversedOrThrow(Opcode.RETURN).forEach { index ->
                val register = getInstruction<OneRegisterInstruction>(index).registerA
                addInstructionsWithLabels(
                    index,
                    """
                        invoke-static { v$register }, $EXTENSION_CLASS->canBatchDownload(Z)Z
                        move-result v$register
                    """
                )
            }
        }
        
        // --- DOWNLOAD PATH/STORAGE ---
        // Allow custom download location
        DownloadPathFingerprint.method.addInstructionsWithLabels(
            0,
            """
                invoke-static { p0 }, $EXTENSION_CLASS->getCustomDownloadPath(Ljava/lang/String;)Ljava/lang/String;
                move-result-object p0
            """
        )
    }
}

/*
 * ================================================================
 * FINGERPRINTS - REPLACE WITH ACTUAL JIOSAAVN METHOD SIGNATURES
 * ================================================================
 * 
 * Search patterns in decompiled smali:
 * 
 * # Download eligibility
 * grep -r "canDownload\|isDownloadAllowed\|downloadEligible" jiosaavn/smali/ -i
 * grep -r "premium.*download\|download.*premium" jiosaavn/smali/ -i
 * 
 * # Download quality
 * grep -r "downloadQuality\|downloadBitrate\|offlineQuality" jiosaavn/smali/ -i
 * grep -r "320\|256\|128" jiosaavn/smali/ | grep -i download
 * 
 * # Download limits
 * grep -r "downloadLimit\|maxDownloads\|playlistDownload" jiosaavn/smali/ -i
 * grep -r "downloadCount\|downloadedSongs" jiosaavn/smali/ -i
 * 
 * # Network restrictions
 * grep -r "wifiOnly\|mobileData\|networkType" jiosaavn/smali/ -i
 * grep -r "ConnectivityManager\|NetworkCapabilities" jiosaavn/smali/ | head -20
 * 
 * # Expiry/renewal
 * grep -r "expiry\|expiration\|renewal\|license.*expire" jiosaavn/smali/ -i
 * grep -r "offline.*expire\|download.*expire" jiosaavn/smali/ -i
 * 
 * # DRM/License
 * grep -r "DRM\|Widevine\|ClearKey\|MediaDrm" jiosaavn/smali/ -i
 * grep -r "license\|License" jiosaavn/smali/ | grep -i offline
 * 
 * # Batch download
 * grep -r "playlistDownload\|albumDownload\|batchDownload" jiosaavn/smali/ -i
 * 
 * # Storage path
 * grep -r "downloadPath\|downloadDir\|storagePath" jiosaavn/smali/ -i
 */

// Download eligibility check
internal object CanDownloadCheckFingerprint : Fingerprint(
    accessFlags = listOf(),
    returnType = "Z",
    parameters = listOf(),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.INVOKE_VIRTUAL,
        Opcode.IF_EQZ,
        Opcode.CONST_4,
        Opcode.RETURN
    )
)

// Download quality selection
internal object DownloadQualitySelectionFingerprint : Fingerprint(
    accessFlags = listOf(),
    returnType = "I",
    parameters = listOf(),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.IGET,
        Opcode.IF_LT,
        Opcode.CONST_16,
        Opcode.RETURN
    )
)

// Download limit check
internal object DownloadLimitCheckFingerprint : Fingerprint(
    accessFlags = listOf(),
    returnType = "Z",
    parameters = listOf(),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.IGET,
        Opcode.IF_GE,
        Opcode.CONST_4,
        Opcode.RETURN
    )
)

// Network type check (WiFi only)
internal object NetworkTypeCheckFingerprint : Fingerprint(
    accessFlags = listOf(),
    returnType = "Z",
    parameters = listOf(),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.INVOKE_VIRTUAL,
        Opcode.CHECK_CAST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.IF_EQ,
        Opcode.RETURN
    )
)

// Download expiry check
internal object DownloadExpiryCheckFingerprint : Fingerprint(
    accessFlags = listOf(),
    returnType = "Z",
    parameters = listOf(),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.IGET,
        Opcode.INVOKE_VIRTUAL,
        Opcode.LCMP,
        Opcode.IF_LT,
        Opcode.RETURN
    )
)

// DRM license check
internal object DrmLicenseCheckFingerprint : Fingerprint(
    accessFlags = listOf(),
    returnType = "Z",
    parameters = listOf("Ljava/lang/Object;"),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.INVOKE_VIRTUAL,
        Opcode.IF_EQZ,
        Opcode.RETURN
    )
)

// Batch download check
internal object BatchDownloadCheckFingerprint : Fingerprint(
    accessFlags = listOf(),
    returnType = "Z",
    parameters = listOf(),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.INVOKE_VIRTUAL,
        Opcode.IF_EQZ,
        Opcode.CONST_4,
        Opcode.RETURN
    )
)

// Download path
internal object DownloadPathFingerprint : Fingerprint(
    accessFlags = listOf(),
    returnType = "Ljava/lang/String;",
    parameters = listOf("Ljava/lang/String;"),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN_OBJECT
    )
)