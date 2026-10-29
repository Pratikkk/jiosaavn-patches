/*
 * JioSaavn 120Hz Display Support Patch
 * 
 * Enables high refresh rate (90Hz/120Hz/144Hz) display support:
 * - Removes 60Hz frame rate cap
 * - Requests highest available refresh rate
 * - Supports seamless frame rate switching
 * - Works with Android 11+ (API 30+) setFrameRate API
 * 
 * TODO: Replace fingerprints with actual JioSaavn method signatures
 */

package app.morphe.patches.jiosaavn.display

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.OpcodesFilter
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patches.jiosaavn.Constants.COMPATIBILITY_JIOSAAVN
import app.morphe.patches.jiosaavn.shared.JioSaavnSharedPatchBundle.{settingsPatch, versionCheckPatch, sharedExtensionPatch, is_10_0_or_greater}
import app.morphe.patches.jiosaavn.shared.settings.PreferenceScreen
import app.morphe.patches.shared.misc.settings.preference.SwitchPreference
import app.morphe.patches.shared.misc.settings.preference.ListPreference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val EXTENSION_CLASS = "Lapp/morphe/extension/jiosaavn/patches/DisplayRefreshRate;"

private val displayRefreshRateResourcePatch = resourcePatch {
    dependsOn(
        settingsPatch,
        versionCheckPatch,
        sharedExtensionPatch
    )
    
    execute {
        PreferenceScreen.DISPLAY.addPreferences(
            SwitchPreference("morphe_enable_high_refresh_rate"),
            ListPreference("morphe_preferred_refresh_rate"),
            SwitchPreference("morphe_seamless_frame_rate_switching"),
            SwitchPreference("morphe_disable_frame_rate_throttling"),
        )
    }
}

/**
 * Main 120Hz display support patch
 */
@Suppress("unused")
val highRefreshRatePatch = bytecodePatch(
    name = "High Refresh Rate Display",
    description = "Enables 90Hz/120Hz/144Hz display support, removes 60Hz cap"
) {
    dependsOn(
        displayRefreshRateResourcePatch,
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
        // - "setFrameRate" "frameRate" "FrameRate"
        // - "preferredDisplayMode" "displayMode" "DisplayMode"
        // - "60.0" "60fps" "60hz" "refreshRate"
        // - "WindowManager" "LayoutParams" "FLAG_"
        // - "onCreate" "onResume" "onAttachedToWindow" in Activity classes
        // - "SurfaceView" "TextureView" "SurfaceHolder" callbacks
        // ============================================================
        
        // --- ACTIVITY ONCREATE / ONRESUME ---
        // Hook Activity lifecycle to set preferred frame rate early
        ActivityOnCreateFingerprint.method.addInstructionsWithLabels(
            0,
            """
                invoke-static { p0 }, $EXTENSION_CLASS->onActivityCreate(Landroid/app/Activity;)V
            """
        )
        
        ActivityOnResumeFingerprint.method.addInstructionsWithLabels(
            0,
            """
                invoke-static { p0 }, $EXTENSION_CLASS->onActivityResume(Landroid/app/Activity;)V
            """
        )
        
        // --- WINDOW / DECOR VIEW SETUP ---
        // Hook window/decor view attachment to apply frame rate
        ViewAttachedToWindowFingerprint.method.addInstructionsWithLabels(
            0,
            """
                invoke-static { p0 }, $EXTENSION_CLASS->onViewAttachedToWindow(Landroid/view/View;)V
            """
        )
        
        // --- SURFACE CREATED / CHANGED ---
        // Hook SurfaceHolder callbacks for SurfaceView/TextureView
        SurfaceCreatedFingerprint.method.addInstructionsWithLabels(
            0,
            """
                invoke-static { p0, p1 }, $EXTENSION_CLASS->onSurfaceCreated(Landroid/view/SurfaceHolder;Landroid/view/Surface;)V
            """
        )
        
        SurfaceChangedFingerprint.method.addInstructionsWithLabels(
            0,
            """
                invoke-static { p0, p1, p2, p3 }, $EXTENSION_CLASS->onSurfaceChanged(Landroid/view/SurfaceHolder;III)V
            """
        )
        
        // --- WINDOW MANAGER LAYOUT PARAMS ---
        // Hook where WindowManager.LayoutParams are configured
        WindowLayoutParamsFingerprint.method.apply {
            findInstructionIndicesReversedOrThrow(Opcode.RETURN_VOID).forEach { index ->
                addInstructionsWithLabels(
                    index,
                    """
                        invoke-static { p0 }, $EXTENSION_CLASS->applyHighRefreshRateParams(Landroid/view/WindowManager$LayoutParams;)V
                    """
                )
            }
        }
        
        // --- DISPLAY MODE SELECTION ---
        // Hook display mode selection (Android 13+)
        if (is_10_0_or_greater) {  // Assuming API 33+ for JioSaavn 10.0+
            DisplayModeSelectionFingerprint.method.apply {
                findInstructionIndicesReversedOrThrow(Opcode.RETURN).forEach { index ->
                    val register = getInstruction<OneRegisterInstruction>(index).registerA
                    addInstructionsWithLabels(
                        index,
                        """
                            invoke-static { v$register }, $EXTENSION_CLASS->selectHighestRefreshRateMode(I)I
                            move-result v$register
                        """
                    )
                }
            }
        }
        
        // --- CHOREOGRAPHER / VSYNC ---
        // Prevent frame rate throttling in Choreographer
        ChoreographerFrameCallbackFingerprint.method.addInstructionsWithLabels(
            0,
            """
                invoke-static { }, $EXTENSION_CLASS->shouldThrottleFrameRate()Z
                move-result v0
                if-eqz v0, :no_throttle
                return-void
                :no_throttle
                nop
            """
        )
        
        // --- MANIFEST / THEME OVERRIDES ---
        // These are handled via resource patch (see below)
    }
}

/**
 * Resource patch for manifest/theme modifications
 */
val displayManifestPatch = resourcePatch {
    dependsOn(displayRefreshRateResourcePatch)
    
    execute {
        // Add android:preferredDisplayModeId to manifest activities
        // This requires ARSC resource manipulation
        // Example: Add meta-data to activities for high refresh rate
        
        // Add theme attributes to disable 60Hz forcing
        // <item name="android:preferredRefreshRate">120</item>
        // <item name="android:frameRate">120</item>
    }
}

/*
 * ================================================================
 * FINGERPRINTS - REPLACE WITH ACTUAL JIOSAAVN METHOD SIGNATURES
 * ================================================================
 * 
 * Search patterns in decompiled smali:
 * 
 * # Activity lifecycle
 * grep -r "onCreate\|onResume\|onAttachedToWindow" jiosaavn/smali/ | grep -i "activity\|mainactivity" | head -20
 * 
 * # Window/DecorView
 * grep -r "DecorView\|WindowManager\|LayoutParams" jiosaavn/smali/ | head -20
 * 
 * # SurfaceView/TextureView callbacks
 * grep -r "SurfaceCreated\|SurfaceChanged\|surfaceCreated\|surfaceChanged" jiosaavn/smali/ | head -20
 * 
 * # Frame rate / refresh rate
 * grep -r "setFrameRate\|frameRate\|refreshRate\|preferredDisplayMode" jiosaavn/smali/ -i
 * grep -r "60\.0\|60fps\|60hz" jiosaavn/smali/ -i
 * 
 * # Choreographer
 * grep -r "Choreographer\|postFrameCallback\|doFrame" jiosaavn/smali/ | head -10
 * 
 * # Display/WindowManager usage
 * grep -r "DisplayManager\|getDisplay\|getSupportedModes" jiosaavn/smali/ | head -10
 */

// Activity.onCreate
internal object ActivityOnCreateFingerprint : Fingerprint(
    accessFlags = listOf(),
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.INVOKE_SUPER,
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN_VOID
    )
)

// Activity.onResume
internal object ActivityOnResumeFingerprint : Fingerprint(
    accessFlags = listOf(),
    returnType = "V",
    parameters = listOf(),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.INVOKE_SUPER,
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN_VOID
    )
)

// View.onAttachedToWindow
internal object ViewAttachedToWindowFingerprint : Fingerprint(
    accessFlags = listOf(),
    returnType = "V",
    parameters = listOf(),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.INVOKE_SUPER,
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN_VOID
    )
)

// SurfaceHolder.Callback.surfaceCreated
internal object SurfaceCreatedFingerprint : Fingerprint(
    accessFlags = listOf(),
    returnType = "V",
    parameters = listOf("Landroid/view/SurfaceHolder;", "Landroid/view/Surface;"),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.INVOKE_INTERFACE,
        Opcode.RETURN_VOID
    )
)

// SurfaceHolder.Callback.surfaceChanged
internal object SurfaceChangedFingerprint : Fingerprint(
    accessFlags = listOf(),
    returnType = "V",
    parameters = listOf("Landroid/view/SurfaceHolder;", "I", "I", "I"),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.INVOKE_INTERFACE,
        Opcode.RETURN_VOID
    )
)

// WindowManager.LayoutParams configuration
internal object WindowLayoutParamsFingerprint : Fingerprint(
    accessFlags = listOf(),
    returnType = "V",
    parameters = listOf("Landroid/view/WindowManager$LayoutParams;"),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.IPUT,
        Opcode.IPUT,
        Opcode.RETURN_VOID
    )
)

// Display mode selection (Android 13+)
internal object DisplayModeSelectionFingerprint : Fingerprint(
    accessFlags = listOf(),
    returnType = "I",
    parameters = listOf(),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.INVOKE_VIRTUAL,
        Opcode.CHECK_CAST,
        Opcode.RETURN
    )
)

// Choreographer frame callback
internal object ChoreographerFrameCallbackFingerprint : Fingerprint(
    accessFlags = listOf(),
    returnType = "V",
    parameters = listOf("J"),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.LCMP,
        Opcode.IF_GT,
        Opcode.RETURN_VOID
    )
)