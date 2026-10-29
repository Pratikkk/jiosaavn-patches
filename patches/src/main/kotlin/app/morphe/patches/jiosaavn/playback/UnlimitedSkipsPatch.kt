/*
 * JioSaavn Unlimited Skips Patch
 * 
 * Removes skip limits for free users:
 * - Unlimited track skips (normally 6/hour for free)
 * - Unlimited forward/backward skips
 * - Removes skip cooldown/timers
 * - Works with radio/station modes
 * 
 * TODO: Replace fingerprints with actual JioSaavn method signatures
 */

package app.morphe.patches.jiosaavn.playback

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.OpcodesFilter
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patches.jiosaavn.Constants.COMPATIBILITY_JIOSAAVN
import app.morphe.patches.jiosaavn.shared.JioSaavnSharedPatchBundle.{settingsPatch, versionCheckPatch, sharedExtensionPatch}
import app.morphe.patches.jiosaavn.shared.settings.PreferenceScreen
import app.morphe.patches.shared.misc.settings.preference.SwitchPreference
import app.morphe.patches.shared.misc.settings.preference.IntSliderPreference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val EXTENSION_CLASS = "Lapp/morphe/extension/jiosaavn/patches/SkipManager;"

private val unlimitedSkipsResourcePatch = resourcePatch {
    dependsOn(
        settingsPatch,
        versionCheckPatch,
        sharedExtensionPatch
    )
    
    execute {
        PreferenceScreen.PLAYBACK.addPreferences(
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
    }
}

/**
 * Main unlimited skips patch
 */
@Suppress("unused")
val unlimitedSkipsPatch = bytecodePatch(
    name = "Unlimited Skips",
    description = "Removes skip limits and cooldowns for free users"
) {
    dependsOn(
        unlimitedSkipsResourcePatch,
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
        // - "skip" "Skip" "SKIP" in player/queue classes
        // - "canSkip" "isSkipAllowed" "skipCount" "skipLimit"
        // - "skipCooldown" "skipTimer" "lastSkipTime"
        // - "free" "premium" "subscription" in skip logic
        // - Radio/Station skip restrictions
        // ============================================================
        
        // --- SKIP ALLOWANCE CHECK ---
        // Hook the method that checks if user can skip
        CanSkipCheckFingerprint.method.apply {
            findInstructionIndicesReversedOrThrow(Opcode.RETURN).forEach { index ->
                val register = getInstruction<OneRegisterInstruction>(index).registerA
                addInstructionsWithLabels(
                    index,
                    """
                        invoke-static { v$register }, $EXTENSION_CLASS->canSkip(Z)Z
                        move-result v$register
                    """
                )
            }
        }
        
        // --- SKIP COUNT INCREMENT ---
        // Hook skip count increment to prevent counting
        SkipCountIncrementFingerprint.method.addInstructionsWithLabels(
            0,
            """
                invoke-static { }, $EXTENSION_CLASS->shouldCountSkip()Z
                move-result v0
                if-eqz v0, :skip_counting
                return-void
                :skip_counting
                nop
            """
        )
        
        // --- SKIP LIMIT CHECK ---
        // Hook the method that enforces skip limit
        SkipLimitCheckFingerprint.method.apply {
            findInstructionIndicesReversedOrThrow(Opcode.RETURN).forEach { index ->
                val register = getInstruction<OneRegisterInstruction>(index).registerA
                addInstructionsWithLabels(
                    index,
                    """
                        invoke-static { v$register }, $EXTENSION_CLASS->isSkipLimitExceeded(Z)Z
                        move-result v$register
                    """
                )
            }
        }
        
        // --- SKIP COOLDOWN ---
        // Hook cooldown timer check
        SkipCooldownCheckFingerprint.method.apply {
            findInstructionIndicesReversedOrThrow(Opcode.RETURN).forEach { index ->
                val register = getInstruction<OneRegisterInstruction>(index).registerA
                addInstructionsWithLabels(
                    index,
                    """
                        invoke-static { v$register }, $EXTENSION_CLASS->isSkipCooldownActive(Z)Z
                        move-result v$register
                    """
                )
            }
        }
        
        // --- RADIO/STATION SKIP RESTRICTION ---
        // Hook radio-specific skip limits
        RadioSkipCheckFingerprint.method.apply {
            findInstructionIndicesReversedOrThrow(Opcode.RETURN).forEach { index ->
                val register = getInstruction<OneRegisterInstruction>(index).registerA
                addInstructionsWithLabels(
                    index,
                    """
                        invoke-static { v$register }, $EXTENSION_CLASS->canSkipRadio(Z)Z
                        move-result v$register
                    """
                )
            }
        }
        
        // --- SKIP BUTTON ENABLED STATE ---
        // Hook UI skip button enable/disable logic
        SkipButtonEnabledFingerprint.method.apply {
            findInstructionIndicesReversedOrThrow(Opcode.RETURN).forEach { index ->
                val register = getInstruction<OneRegisterInstruction>(index).registerA
                addInstructionsWithLabels(
                    index,
                    """
                        invoke-static { v$register }, $EXTENSION_CLASS->isSkipButtonEnabled(Z)Z
                        move-result v$register
                    """
                )
            }
        }
    }
}

/*
 * ================================================================
 * FINGERPRINTS - REPLACE WITH ACTUAL JIOSAAVN METHOD SIGNATURES
 * ================================================================
 * 
 * Search patterns in decompiled smali:
 * 
 * # Skip allowance
 * grep -r "canSkip\|isSkipAllowed\|skipAllowed" jiosaavn/smali/ -i
 * grep -r "skipCount\|skipLimit\|maxSkips" jiosaavn/smali/ -i
 * 
 * # Skip tracking
 * grep -r "incrementSkip\|skipCount++\|addSkip" jiosaavn/smali/ -i
 * grep -r "lastSkipTime\|skipTimestamp" jiosaavn/smali/ -i
 * 
 * # Cooldown/timer
 * grep -r "skipCooldown\|skipTimer\|skipDelay" jiosaavn/smali/ -i
 * grep -r "Handler\|postDelayed\|Runnable" jiosaavn/smali/ | grep -i skip
 * 
 * # Radio/station skips
 * grep -r "radio.*skip\|station.*skip" jiosaavn/smali/ -i
 * grep -r "RadioPlayer\|StationPlayer" jiosaavn/smali/ -i
 * 
 * # UI button state
 * grep -r "skipButton\|btnSkip\|fabSkip" jiosaavn/smali/ -i
 * grep -r "setEnabled.*skip\|skip.*setEnabled" jiosaavn/smali/ -i
 */

// Main skip allowance check
internal object CanSkipCheckFingerprint : Fingerprint(
    accessFlags = listOf(),
    returnType = "Z",
    parameters = listOf(),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.IGET,
        Opcode.IF_LT,
        Opcode.CONST_4,
        Opcode.RETURN
    )
)

// Skip count increment
internal object SkipCountIncrementFingerprint : Fingerprint(
    accessFlags = listOf(),
    returnType = "V",
    parameters = listOf(),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.IGET,
        Opcode.ADD_INT_LIT8,
        Opcode.IPUT,
        Opcode.RETURN_VOID
    )
)

// Skip limit enforcement
internal object SkipLimitCheckFingerprint : Fingerprint(
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

// Skip cooldown check
internal object SkipCooldownCheckFingerprint : Fingerprint(
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

// Radio/station skip restriction
internal object RadioSkipCheckFingerprint : Fingerprint(
    accessFlags = listOf(),
    returnType = "Z",
    parameters = listOf(),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.INVOKE_VIRTUAL,
        Opcode.CHECK_CAST,
        Opcode.IF_EQZ,
        Opcode.RETURN
    )
)

// Skip button enabled state
internal object SkipButtonEnabledFingerprint : Fingerprint(
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