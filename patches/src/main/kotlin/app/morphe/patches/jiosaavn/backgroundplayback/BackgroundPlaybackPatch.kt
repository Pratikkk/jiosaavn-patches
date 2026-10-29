/*
 * JioSaavn Background Playback Patch
 * 
 * Enables background playback for all content including:
 * - Regular songs (premium feature)
 * - Kids content (normally restricted)
 * - Radio/podcast content
 * 
 * TODO: Replace fingerprints with actual JioSaavn method signatures
 */

package app.morphe.patches.jiosaavn.backgroundplayback

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.OpcodesFilter
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.jiosaavn.Constants.COMPATIBILITY_JIOSAAVN
import app.morphe.patches.jiosaavn.shared.JioSaavnSharedPatchBundle.{settingsPatch, versionCheckPatch, sharedExtensionPatch, is_10_0_or_greater}
import app.morphe.patches.jiosaavn.shared.settings.PreferenceScreen
import app.morphe.patches.shared.misc.settings.preference.SwitchPreference
import app.morphe.patches.shared.misc.settings.preference.ListPreference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val EXTENSION_CLASS = "Lapp/morphe/extension/jiosaavn/patches/BackgroundPlayback;"

val backgroundPlaybackPatch = bytecodePatch(
    name = "Background Playback",
    description = "Enables background playback for all content including kids content"
) {
    dependsOn(
        settingsPatch,
        versionCheckPatch,
        sharedExtensionPatch
    )
    
    compatibleWith(COMPATIBILITY_JIOSAAVN)
    
    execute {
        PreferenceScreen.PLAYBACK.addPreferences(
            SwitchPreference("morphe_enable_background_playback"),
            SwitchPreference("morphe_background_play_kids_content"),
            SwitchPreference("morphe_background_play_radio"),
            ListPreference("morphe_auto_pause_on_lock"),
        )
        
        // ============================================================
        // TODO: REPLACE THESE FINGERPRINTS WITH ACTUAL JIOSAAVN ONES
        // ============================================================
        //
        // Search for in decompiled APK:
        // - "background" "Background" "BACKGROUND"
        // - "pause" "Pause" "PAUSE" in service/player classes
        // - "isBackground" "allowBackground" "backgroundPlayback"
        // - Service classes that handle MediaSession/PlaybackService
        // ============================================================
        
        // --- MAIN BACKGROUND PLAYBACK CHECK ---
        // Hook the method that determines if background playback is allowed
        BackgroundPlaybackCheckFingerprint.method.apply {
            findInstructionIndicesReversedOrThrow(Opcode.RETURN).forEach { index ->
                val register = getInstruction<OneRegisterInstruction>(index).registerA
                addInstructionsWithLabels(
                    index,
                    """
                        invoke-static { v$register }, $EXTENSION_CLASS->isBackgroundPlaybackAllowed(Z)Z
                        move-result v$register
                    """
                )
            }
        }
        
        // --- KIDS CONTENT RESTRICTION ---
        // Hook kids content background playback check
        KidsContentBackgroundCheckFingerprint.method.apply {
            findInstructionIndicesReversedOrThrow(Opcode.RETURN).forEach { index ->
                val register = getInstruction<OneRegisterInstruction>(index).registerA
                addInstructionsWithLabels(
                    index,
                    """
                        invoke-static { v$register }, $EXTENSION_CLASS->isKidsContentBackgroundAllowed(Z)Z
                        move-result v$register
                    """
                )
            }
        }
        
        // --- SCREEN LOCK PAUSE ---
        // Hook the method that pauses on screen lock
        if (is_10_0_or_greater) {
            ScreenLockPauseFingerprint.method.apply {
                findInstructionIndicesReversedOrThrow(Opcode.RETURN).forEach { index ->
                    val register = getInstruction<OneRegisterInstruction>(index).registerA
                    addInstructionsWithLabels(
                        index,
                        """
                            invoke-static { v$register }, $EXTENSION_CLASS->shouldPauseOnScreenLock(Z)Z
                            move-result v$register
                        """
                    )
                }
            }
        }
        
        // --- MEDIA SESSION CALLBACKS ---
        // Ensure media session properly handles background state
        MediaSessionCallbackFingerprint.method.addInstructionsWithLabels(
            0,
            """
                invoke-static { p0 }, $EXTENSION_CLASS->onMediaSessionCallback(Landroid/media/session/MediaSession$Callback;)V
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
 * # Background playback permission check
 * grep -r "background.*playback\|playback.*background" jiosaavn/smali/ -i
 * 
 * # Kids content restriction
 * grep -r "kids\|children\|family" jiosaavn/smali/ -i | grep -i "background\|playback"
 * 
 * # Screen lock handling
 * grep -r "screen.*lock\|lock.*screen\|keyguard" jiosaavn/smali/ -i
 * 
 * # MediaSession/PlaybackService
 * grep -r "MediaSession\|PlaybackService\|MediaController" jiosaavn/smali/
 */

// Main background playback check
internal object BackgroundPlaybackCheckFingerprint : Fingerprint(
    accessFlags = listOf(),
    returnType = "Z",
    parameters = listOf(),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.IGET,
        Opcode.IF_EQZ,
        Opcode.CONST_4,
        Opcode.RETURN
    )
)

// Kids content background restriction
internal object KidsContentBackgroundCheckFingerprint : Fingerprint(
    accessFlags = listOf(),
    returnType = "Z",
    parameters = listOf("Ljava/lang/Object;"),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.INVOKE_VIRTUAL,
        Opcode.CHECK_CAST,
        Opcode.IF_EQZ,
        Opcode.RETURN
    )
)

// Screen lock pause behavior
internal object ScreenLockPauseFingerprint : Fingerprint(
    accessFlags = listOf(),
    returnType = "V",
    parameters = listOf("Z"),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.IF_EQZ,
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN_VOID
    )
)

// Media session callback
internal object MediaSessionCallbackFingerprint : Fingerprint(
    accessFlags = listOf(),
    returnType = "V",
    parameters = listOf("Landroid/media/session/MediaSession$Callback;"),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN_VOID
    )
)