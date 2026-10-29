# JioSaavn Patches for Morphe

A patch bundle for [Morphe Manager](https://github.com/MorpheApp/morphe-manager) that adds premium features to JioSaavn.

## Features

| Patch | Description | Status |
|-------|-------------|--------|
| **Hide Ads** | Removes audio, video, and banner ads | Template |
| **Background Playback** | Play music with screen off / in background | Template |
| **High Quality Audio** | 320kbps / Lossless streaming | Template |
| **High Refresh Rate** | 90Hz/120Hz/144Hz display support | Template |
| **Unlimited Skips** | Remove skip limits & cooldowns | Template |
| **Offline Download** | High-quality downloads without premium | Template |
| **Lyrics Access** | Synced lyrics, translation, romanization | Template |
| **Equalizer & Effects** | Full EQ, bass boost, virtualizer, reverb | Template |
| **Unlimited Skips** | Remove skip limits | Planned |
| **Offline Download** | Enable downloads without premium | Planned |
| **Lyrics Access** | Show lyrics for all songs | Planned |

## Quick Start

### 1. Prerequisites

- JDK 17+
- Android SDK (for `d8` dex compiler)
- Morphe Manager installed on device

### 2. Build the Patch Bundle

```bash
# Clone and build
git clone <this-repo> jiosaavn-patches
cd jiosaavn-patches
./gradlew :patches:assembleRelease

# Output: patches/build/mpp/patches-1.0.0.mpp
```

### 3. Install in Morphe Manager

1. Open Morphe Manager
2. Settings → Patch Sources → Add Source
3. Enter your GitHub repo URL (where you host the `.mpp` file)
4. Select "JioSaavn Patches" from the source list
5. Patch JioSaavn APK

## Developing Real Patches

### Step 1: Get JioSaavn APKs

```bash
# Download multiple versions for fingerprint testing
# Version 9.0.0, 9.5.0, 9.10.0, 9.15.0, etc.
```

### Step 2: Reverse Engineer

```bash
# Decompile
apktool d jiosaavn_v9.10.0.apk -o jiosaavn_v9.10.0

# Search for ad-related code
grep -r "ad\|Ad\|AD" jiosaavn_v9.10.0/smali/ -l | xargs grep -l "class" | head -20

# Search for background playback
grep -r "background\|Background" jiosaavn_v9.10.0/smali/ -i | grep -i "playback\|play"

# Search for quality/bitrate
grep -r "bitrate\|Bitrate\|quality\|Quality" jiosaavn_v9.10.0/smali/ -i | head -30

# Search for refresh rate / frame rate (NEW)
grep -r "setFrameRate\|frameRate\|refreshRate\|preferredDisplayMode\|60\.0\|60fps\|60hz" jiosaavn_v9.10.0/smali/ -i | head -30
grep -r "onCreate\|onResume\|onAttachedToWindow\|surfaceCreated\|surfaceChanged" jiosaavn_v9.10.0/smali/ | grep -i "activity\|mainactivity\|view" | head -20
grep -r "Choreographer\|postFrameCallback\|doFrame" jiosaavn_v9.10.0/smali/ | head -10
grep -r "DisplayManager\|getDisplay\|getSupportedModes\|supportedModes" jiosaavn_v9.10.0/smali/ | head -10

# Search for skip limits (NEW)
grep -r "canSkip\|isSkipAllowed\|skipAllowed" jiosaavn_v9.10.0/smali/ -i
grep -r "skipCount\|skipLimit\|maxSkips" jiosaavn_v9.10.0/smali/ -i
grep -r "skipCooldown\|skipTimer\|skipDelay" jiosaavn_v9.10.0/smali/ -i
grep -r "radio.*skip\|station.*skip" jiosaavn_v9.10.0/smali/ -i

# Search for download/offline (NEW)
grep -r "canDownload\|isDownloadAllowed\|downloadEligible" jiosaavn_v9.10.0/smali/ -i
grep -r "downloadQuality\|downloadBitrate\|offlineQuality" jiosaavn_v9.10.0/smali/ -i
grep -r "downloadLimit\|maxDownloads\|playlistDownload" jiosaavn_v9.10.0/smali/ -i
grep -r "wifiOnly\|mobileData\|networkType" jiosaavn_v9.10.0/smali/ -i
grep -r "expiry\|expiration\|renewal\|license.*expire" jiosaavn_v9.10.0/smali/ -i

# Search for lyrics (NEW)
grep -r "canShowLyrics\|isLyricsAvailable\|lyricsEligible" jiosaavn_v9.10.0/smali/ -i
grep -r "synced.*lyrics\|timestamp.*lyrics\|timeSynced" jiosaavn_v9.10.0/smali/ -i
grep -r "translate.*lyrics\|lyrics.*translate" jiosaavn_v9.10.0/smali/ -i
grep -r "romaniz\|translit" jiosaavn_v9.10.0/smali/ -i

# Search for equalizer/audio effects (NEW)
grep -r "Equalizer\|equalizer" jiosaavn_v9.10.0/smali/ -i | grep -v ".png"
grep -r "BassBoost\|bassBoost" jiosaavn_v9.10.0/smali/ -i
grep -r "Virtualizer\|virtualizer" jiosaavn_v9.10.0/smali/ -i
grep -r "Reverb\|reverb\|EnvironmentalReverb" jiosaavn_v9.10.0/smali/ -i
grep -r "preset\|Preset" jiosaavn_v9.10.0/smali/ -i | grep -i "equalizer\|bass\|virtualizer"
```

### Step 3: Create Fingerprints

Replace the placeholder fingerprints in:
- `patches/src/main/kotlin/app/morphe/patches/jiosaavn/ad/HideAdsPatch.kt`
- `patches/src/main/kotlin/app/morphe/patches/jiosaavn/backgroundplayback/BackgroundPlaybackPatch.kt`
- `patches/src/main/kotlin/app/morphe/patches/jiosaavn/audio/HighQualityAudioPatch.kt`

**Fingerprint Pattern:**
```kotlin
internal object MyMethodFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "V",
    parameters = listOf(),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.INVOKE_VIRTUAL,
        Opcode.CONST_STRING,
        Opcode.INVOKE_STATIC,
        Opcode.RETURN_VOID
    )
)
```

### Step 4: Build Extension Classes

```bash
# Compile Kotlin extension classes to JAR
kotlinc -d jiosaavn_extensions.jar \
    patches/src/main/kotlin/app/morphe/extension/jiosaavn/patches/*.kt

# Convert to DEX (requires Android SDK d8)
d8 --release --output patches/src/main/dex jiosaavn_extensions.jar

# The DEX file will be packaged automatically
```

### Step 5: Test

```bash
# Build and test
./gradlew :patches:assembleRelease

# Install .mpp in Morphe Manager
# Patch JioSaavn APK
# Install patched APK
# Test features
```

## Project Structure

```
jiosaavn-patches/
├── settings.gradle.kts
├── build.gradle.kts
├── patches/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── kotlin/
│       │   └── app/morphe/patches/jiosaavn/
│       │       ├── JioSaavnPatchBundle.kt          # Main bundle entry
│       │       ├── Constants.kt                     # Package name, versions
│       │       ├── ad/
│       │       │   └── HideAdsPatch.kt             # Ad removal
│       │       ├── backgroundplayback/
│       │       │   └── BackgroundPlaybackPatch.kt  # Background play
│       │       ├── audio/
│       │       │   └── HighQualityAudioPatch.kt    # HQ audio
│       │       ├── display/
│       │       │   └── HighRefreshRatePatch.kt     # 120Hz display
│       │       ├── playback/
│       │       │   └── UnlimitedSkipsPatch.kt      # Unlimited skips
│       │       ├── download/
│       │       │   └── OfflineDownloadPatch.kt     # Offline downloads
│       │       ├── lyrics/
│       │       │   └── LyricsAccessPatch.kt        # Lyrics access
│       │       ├── audiofx/
│       │       │   └── AudioEffectsPatch.kt        # Equalizer & effects
│       │       ├── shared/
│       │       │   ├── JioSaavnSharedPatchBundle.kt # Shared patches
│       │       │   └── settings/PreferenceScreen.kt # Settings UI
│       │       └── extension/
│       │           └── jiosaavn/patches/
│       │               ├── JioSaavnShared.kt       # Shared utils
│       │               ├── AdFilter.kt             # Ad logic
│       │               ├── BackgroundPlayback.kt   # BG playback logic
│       │               ├── AudioQuality.kt         # Audio quality logic
│       │               ├── DisplayRefreshRate.kt   # Refresh rate logic
│       │               ├── SkipManager.kt          # Skip logic
│       │               ├── DownloadManager.kt      # Download logic
│       │               ├── LyricsManager.kt        # Lyrics logic
│       │               └── AudioEffects.kt         # Audio effects logic
│       └── dex/                                    # Compiled extension DEX (auto-generated)
└── README.md
```

## Key Concepts

### Fingerprints
Pattern-based method matching that works across app versions. Instead of hardcoding method names, fingerprints match unique opcode sequences.

### Extension Classes
Kotlin classes compiled to DEX and injected into the patched app. They contain the actual logic (settings checks, URL modification, etc.) called from bytecode patches.

### Bytecode Patches
Smali-level modifications that hook target methods and redirect to extension classes.

### Resource Patches
Modify Android resources (XML, strings, preferences) to add Morphe settings UI.

### High Refresh Rate Patch Details (NEW)

The 120Hz patch uses multiple Android APIs depending on API level:

| API Level | Android Version | API Used |
|-----------|-----------------|----------|
| 30+ | Android 11 | `View.setFrameRate()`, `Surface.setFrameRate()` |
| 33+ | Android 13 | `Display.Mode`, `Window.preferredDisplayModeId` |
| 34+ | Android 14 | `Surface.FRAME_RATE_COMPATIBILITY_FIXED_SOURCE` for seamless switching |

**Hook Points:**
1. `Activity.onCreate()` / `onResume()` - Early frame rate request
2. `View.onAttachedToWindow()` - Decor view attachment
3. `SurfaceHolder.Callback.surfaceCreated/Changed()` - Surface readiness
4. `WindowManager.LayoutParams` configuration - Persistent settings
5. `Display.Mode` selection (Android 13+) - Precise mode control
6. `Choreographer` frame callback - Prevent throttling

**Manifest/Theme modifications (via resource patch):**
- `android:preferredDisplayModeId` on activities
- Theme attributes: `android:preferredRefreshRate`, `android:frameRate`

## Updating for New JioSaavn Versions

1. Download new APK
2. Decompile and verify fingerprints still match
3. Update version flags in `JioSaavnSharedPatchBundle.kt`
4. Add new fingerprints if methods changed
5. Rebuild and test

## Hosting Your Patch Bundle

1. Create a GitHub repo for your patches
2. Use GitHub Actions to build `.mpp` on release
3. Host `patches-bundle.json` and `.mpp` on GitHub Releases
4. Add repo URL as custom patch source in Morphe Manager

Example `patches-bundle.json`:
```json
{
  "version": "1.0.0",
  "download_url": "https://github.com/yourname/jiosaavn-patches/releases/download/v1.0.0/patches-1.0.0.mpp",
  "description": "JioSaavn patches for Morphe"
}
```

## Legal Disclaimer

⚠️ **This is a template for educational purposes.**

- Patching JioSaavn may violate their Terms of Service
- This code is GPLv3 (like Morphe) - you must open source your derivative
- Do not use "Morphe" name/branding in your distribution
- Use at your own risk

## Resources

- [Morphe Documentation](https://github.com/MorpheApp/morphe-documentation)
- [Morphe Patches (reference)](https://github.com/MorpheApp/morphe-patches)
- [ReVanced Patches (similar patterns)](https://github.com/ReVanced/revanced-patches)
- [Smali Documentation](https://github.com/JesusFreke/smali)