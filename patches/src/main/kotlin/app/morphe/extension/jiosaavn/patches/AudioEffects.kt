/*
 * JioSaavn Audio Effects Extension Class
 * 
 * Provides equalizer/audio effects logic called from bytecode patches.
 * Uses Android's audiofx APIs: Equalizer, BassBoost, Virtualizer, EnvironmentalReverb
 */

package app.morphe.extension.jiosaavn.patches

import android.content.Context
import android.content.SharedPreferences
import android.media.audiofx.AudioEffect
import android.media.audiofx.Equalizer
import android.media.audiofx.BassBoost
import android.media.audiofx.Virtualizer
import android.media.audiofx.EnvironmentalReverb
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Audio effects management for JioSaavn.
 * Methods called from AudioEffectsPatch bytecode injections.
 */
public class AudioEffects {

    companion object {
        
        private const val PREFS_NAME = "morphe_audio_effects"
        private var prefs: SharedPreferences? = null
        private var equalizer: Equalizer? = null
        private var bassBoost: BassBoost? = null
        private var virtualizer: Virtualizer? = null
        private var reverb: EnvironmentalReverb? = null
        private val gson = Gson()
        
        /**
         * Initialize audio effects with audio session
         */
        @JvmStatic
        public fun initialize(context: Context, audioSessionId: Int) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            
            if (JioSaavnShared.isPatchEnabled("morphe_unlock_equalizer")) {
                try {
                    equalizer = Equalizer(0, audioSessionId)
                    equalizer?.enabled = true
                } catch (e: Exception) {
                    JioSaavnShared.log("AudioEffects", "Equalizer init failed: ${e.message}")
                }
            }
            
            if (JioSaavnShared.isPatchEnabled("morphe_enable_bass_boost")) {
                try {
                    bassBoost = BassBoost(0, audioSessionId)
                    bassBoost?.enabled = true
                } catch (e: Exception) {
                    JioSaavnShared.log("AudioEffects", "BassBoost init failed: ${e.message}")
                }
            }
            
            if (JioSaavnShared.isPatchEnabled("morphe_enable_virtualizer")) {
                try {
                    virtualizer = Virtualizer(0, audioSessionId)
                    virtualizer?.enabled = true
                } catch (e: Exception) {
                    JioSaavnShared.log("AudioEffects", "Virtualizer init failed: ${e.message}")
                }
            }
            
            if (JioSaavnShared.isPatchEnabled("morphe_enable_reverb")) {
                try {
                    reverb = EnvironmentalReverb(0, audioSessionId)
                    reverb?.enabled = true
                } catch (e: Exception) {
                    JioSaavnShared.log("AudioEffects", "Reverb init failed: ${e.message}")
                }
            }
        }
        
        /**
         * Release audio effects
         */
        @JvmStatic
        public fun release() {
            equalizer?.release()
            bassBoost?.release()
            virtualizer?.release()
            reverb?.release()
            equalizer = null
            bassBoost = null
            virtualizer = null
            reverb = null
        }
        
        /**
         * Is equalizer available?
         */
        @JvmStatic
        public fun isEqualizerAvailable(originalAvailable: Boolean): Boolean {
            if (JioSaavnShared.isPatchEnabled("morphe_unlock_equalizer")) {
                return Equalizer.getNumberOfBands() > 0
            }
            return originalAvailable
        }
        
        /**
         * Is bass boost available?
         */
        @JvmStatic
        public fun isBassBoostAvailable(originalAvailable: Boolean): Boolean {
            if (JioSaavnShared.isPatchEnabled("morphe_enable_bass_boost")) {
                return true
            }
            return originalAvailable
        }
        
        /**
         * Is virtualizer available?
         */
        @JvmStatic
        public fun isVirtualizerAvailable(originalAvailable: Boolean): Boolean {
            if (JioSaavnShared.isPatchEnabled("morphe_enable_virtualizer")) {
                return true
            }
            return originalAvailable
        }
        
        /**
         * Is reverb available?
         */
        @JvmStatic
        public fun isReverbAvailable(originalAvailable: Boolean): Boolean {
            if (JioSaavnShared.isPatchEnabled("morphe_enable_reverb")) {
                return true
            }
            return originalAvailable
        }
        
        /**
         * Called when preset is saved
         */
        @JvmStatic
        public fun onPresetSave(name: String, bandLevels: IntArray) {
            if (!JioSaavnShared.isPatchEnabled("morphe_custom_presets")) return
            
            val json = gson.toJson(bandLevels)
            prefs?.edit()?.putString("preset_$name", json)?.apply()
        }
        
        /**
         * Called when preset is loaded
         */
        @JvmStatic
        public fun onPresetLoad(name: String): IntArray? {
            val json = prefs?.getString("preset_$name", null)
            if (json != null) {
                try {
                    val type = object : TypeToken<IntArray>() {}.type
                    return gson.fromJson(json, type)
                } catch (e: Exception) {
                    return null
                }
            }
            return null
        }
        
        /**
         * Get equalizer band count
         */
        @JvmStatic
        public fun getEqualizerBandCount(originalCount: Int): Int {
            if (JioSaavnShared.isPatchEnabled("morphe_unlock_equalizer")) {
                return Equalizer.getNumberOfBands()
            }
            return originalCount
        }
        
        /**
         * Get audio session ID for effect attachment
         */
        @JvmStatic
        public fun getAudioSessionId(originalId: Int): Int {
            // Return the session ID used for initialization
            return originalId
        }
        
        /**
         * Is specific effect enabled?
         */
        @JvmStatic
        public fun isEffectEnabled(originalEnabled: Boolean): Boolean {
            // Could track per-effect enabled state
            return originalEnabled
        }
        
        /**
         * Set equalizer band level
         */
        @JvmStatic
        public fun setEqualizerBandLevel(band: Int, level: Short): Boolean {
            return try {
                equalizer?.setBandLevel(band, level)
                true
            } catch (e: Exception) {
                false
            }
        }
        
        /**
         * Get equalizer band level
         */
        @JvmStatic
        public fun getEqualizerBandLevel(band: Int): Short {
            return try {
                equalizer?.getBandLevel(band) ?: 0
            } catch (e: Exception) {
                0
            }
        }
        
        /**
         * Set bass boost strength
         */
        @JvmStatic
        public fun setBassBoostStrength(strength: Short): Boolean {
            return try {
                bassBoost?.setStrength(strength)
                true
            } catch (e: Exception) {
                false
            }
        }
        
        /**
         * Set virtualizer strength
         */
        @JvmStatic
        public fun setVirtualizerStrength(strength: Short): Boolean {
            return try {
                virtualizer?.setStrength(strength)
                true
            } catch (e: Exception) {
                false
            }
        }
        
        /**
         * Apply preset by name
         */
        @JvmStatic
        public fun applyPreset(presetName: String): Boolean {
            val bandLevels = onPresetLoad(presetName)
            if (bandLevels != null && equalizer != null) {
                for (i in bandLevels.indices) {
                    equalizer?.setBandLevel(i, bandLevels[i].toShort())
                }
                return true
            }
            
            // Try built-in preset
            val preset = when (presetName.lowercase()) {
                "bass" -> Equalizer.PRESET_BASS
                "dance" -> Equalizer.PRESET_DANCE
                "hip hop" -> Equalizer.PRESET_HIPHOP
                "rock" -> Equalizer.PRESET_ROCK
                "pop" -> Equalizer.PRESET_POP
                "classical" -> Equalizer.PRESET_CLASSICAL
                "jazz" -> Equalizer.PRESET_JAZZ
                "treble" -> Equalizer.PRESET_TREBLE
                "vocal" -> Equalizer.PRESET_VOCAL
                "flat" -> Equalizer.PRESET_FLAT
                else -> -1
            }
            
            if (preset != -1) {
                return try {
                    equalizer?.usePreset(preset)
                    true
                } catch (e: Exception) {
                    false
                }
            }
            
            return false
        }
        
        /**
         * Get available built-in presets
         */
        @JvmStatic
        public fun getAvailablePresets(): Array<String> {
            return Equalizer.getPresetNames()
        }
    }
}