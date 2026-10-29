/*
 * JioSaavn Lyrics Access Patch
 * 
 * Enables lyrics for all users:
 * - Removes premium requirement for lyrics
 * - Enables real-time/synced lyrics
 * - Unlocks translation/romanization
 * - Enables lyrics search
 * - Removes lyrics display limits
 * 
 * TODO: Replace fingerprints with actual JioSaavn method signatures
 */

package app.morphe.patches.jiosaavn.lyrics

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

private const val EXTENSION_CLASS = "Lapp/morphe/extension/jiosaavn/patches/LyricsManager;"

private val lyricsAccessResourcePatch = resourcePatch {
    dependsOn(
        settingsPatch,
        versionCheckPatch,
        sharedExtensionPatch
    )
    
    execute {
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
 * Main lyrics access patch
 */
@Suppress("unused")
val lyricsAccessPatch = bytecodePatch(
    name = "Lyrics Access",
    description = "Enables synced lyrics, translation, and romanization for all users"
) {
    dependsOn(
        lyricsAccessResourcePatch,
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
        // - "lyrics" "Lyrics" "LYRICS" in API/manager classes
        // - "canShowLyrics" "isLyricsAvailable" "lyricsEligible"
        // - "synced" "Synced" "SYNCED" "timestamp" "timeSynced"
        // - "translation" "translate" "romanization" "romanise"
        // - "lyricsSearch" "searchLyrics" "findLyrics"
        // - "lyricsProvider" "lyricsSource" "musixmatch" "genius"
        // ============================================================
        
        // --- LYRICS ELIGIBILITY CHECK ---
        // Hook the method that checks if lyrics are available for user
        CanShowLyricsFingerprint.method.apply {
            findInstructionIndicesReversedOrThrow(Opcode.RETURN).forEach { index ->
                val register = getInstruction<OneRegisterInstruction>(index).registerA
                addInstructionsWithLabels(
                    index,
                    """
                        invoke-static { v$register }, $EXTENSION_CLASS->canShowLyrics(Z)Z
                        move-result v$register
                    """
                )
            }
        }
        
        // --- SYNCED LYRICS CHECK ---
        // Hook synced/timestamped lyrics availability
        SyncedLyricsCheckFingerprint.method.apply {
            findInstructionIndicesReversedOrThrow(Opcode.RETURN).forEach { index ->
                val register = getInstruction<OneRegisterInstruction>(index).registerA
                addInstructionsWithLabels(
                    index,
                    """
                        invoke-static { v$register }, $EXTENSION_CLASS->canShowSyncedLyrics(Z)Z
                        move-result v$register
                    """
                )
            }
        }
        
        // --- LYRICS TRANSLATION ---
        // Hook translation availability
        LyricsTranslationCheckFingerprint.method.apply {
            findInstructionIndicesReversedOrThrow(Opcode.RETURN).forEach { index ->
                val register = getInstruction<OneRegisterInstruction>(index).registerA
                addInstructionsWithLabels(
                    index,
                    """
                        invoke-static { v$register }, $EXTENSION_CLASS->canShowTranslation(Z)Z
                        move-result v$register
                    """
                )
            }
        }
        
        // --- ROMANIZATION ---
        // Hook romanization/transliteration availability
        RomanizationCheckFingerprint.method.apply {
            findInstructionIndicesReversedOrThrow(Opcode.RETURN).forEach { index ->
                val register = getInstruction<OneRegisterInstruction>(index).registerA
                addInstructionsWithLabels(
                    index,
                    """
                        invoke-static { v$register }, $EXTENSION_CLASS->canShowRomanization(Z)Z
                        move-result v$register
                    """
                )
            }
        }
        
        // --- LYRICS SEARCH ---
        // Hook lyrics search functionality
        LyricsSearchFingerprint.method.addInstructionsWithLabels(
            0,
            """
                invoke-static { p0 }, $EXTENSION_CLASS->modifyLyricsSearchQuery(Ljava/lang/String;)Ljava/lang/String;
                move-result-object p0
            """
        )
        
        // --- LYRICS PROVIDER SELECTION ---
        // Hook lyrics provider/source selection
        LyricsProviderFingerprint.method.apply {
            findInstructionIndicesReversedOrThrow(Opcode.RETURN).forEach { index ->
                val register = getInstruction<OneRegisterInstruction>(index).registerA
                addInstructionsWithLabels(
                    index,
                    """
                        invoke-static { v$register }, $EXTENSION_CLASS->getPreferredLyricsProvider(I)I
                        move-result v$register
                    """
                )
            }
        }
        
        // --- LYRICS FETCH/LOAD ---
        // Hook lyrics loading to inject custom logic
        LyricsLoadFingerprint.method.addInstructionsWithLabels(
            0,
            """
                invoke-static { p0, p1 }, $EXTENSION_CLASS->onLyricsLoad(Ljava/lang/String;Ljava/lang/Object;)V
            """
        )
        
        // --- KARAOKE/WORD-BY-WORD ---
        // Hook karaoke mode availability
        KaraokeModeCheckFingerprint.method.apply {
            findInstructionIndicesReversedOrThrow(Opcode.RETURN).forEach { index ->
                val register = getInstruction<OneRegisterInstruction>(index).registerA
                addInstructionsWithLabels(
                    index,
                    """
                        invoke-static { v$register }, $EXTENSION_CLASS->canShowKaraoke(Z)Z
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
 * # Lyrics eligibility
 * grep -r "canShowLyrics\|isLyricsAvailable\|lyricsEligible" jiosaavn/smali/ -i
 * grep -r "premium.*lyrics\|lyrics.*premium" jiosaavn/smali/ -i
 * 
 * # Synced lyrics
 * grep -r "synced.*lyrics\|timestamp.*lyrics\|timeSynced" jiosaavn/smali/ -i
 * grep -r "LyricsLine\|LyricsSegment\|LyricsWord" jiosaavn/smali/ -i
 * 
 * # Translation
 * grep -r "translate.*lyrics\|lyrics.*translate" jiosaavn/smali/ -i
 * grep -r "translation\|Translation" jiosaavn/smali/ | grep -i lyrics
 * 
 * # Romanization
 * grep -r "romaniz\|translit" jiosaavn/smali/ -i
 * grep -r "romanization\|Romanization" jiosaavn/smali/ -i
 * 
 * # Search
 * grep -r "searchLyrics\|findLyrics\|lyricsSearch" jiosaavn/smali/ -i
 * 
 * # Provider
 * grep -r "lyricsProvider\|lyricsSource\|musixmatch\|genius" jiosaavn/smali/ -i
 * 
 * # Load/fetch
 * grep -r "loadLyrics\|fetchLyrics\|getLyrics" jiosaavn/smali/ -i
 * 
 * # Karaoke
 * grep -r "karaoke\|Karaoke" jiosaavn/smali/ -i
 */

// Lyrics eligibility check
internal object CanShowLyricsFingerprint : Fingerprint(
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

// Synced lyrics check
internal object SyncedLyricsCheckFingerprint : Fingerprint(
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

// Lyrics translation check
internal object LyricsTranslationCheckFingerprint : Fingerprint(
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

// Romanization check
internal object RomanizationCheckFingerprint : Fingerprint(
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

// Lyrics search
internal object LyricsSearchFingerprint : Fingerprint(
    accessFlags = listOf(),
    returnType = "Ljava/lang/String;",
    parameters = listOf("Ljava/lang/String;"),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN_OBJECT
    )
)

// Lyrics provider selection
internal object LyricsProviderFingerprint : Fingerprint(
    accessFlags = listOf(),
    returnType = "I",
    parameters = listOf(),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.IGET,
        Opcode.IF_LT,
        Opcode.CONST_4,
        Opcode.RETURN
    )
)

// Lyrics load
internal object LyricsLoadFingerprint : Fingerprint(
    accessFlags = listOf(),
    returnType = "V",
    parameters = listOf("Ljava/lang/String;", "Ljava/lang/Object;"),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN_VOID
    )
)

// Karaoke mode check
internal object KaraokeModeCheckFingerprint : Fingerprint(
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