/*
 * JioSaavn Ad Filter Extension Class
 * 
 * Provides ad filtering logic called from bytecode patches.
 * Compiled to DEX and loaded at runtime.
 */

package app.morphe.extension.jiosaavn.patches

/**
 * Ad filtering logic for JioSaavn.
 * Methods called from HideAdsPatch bytecode injections.
 */
public class AdFilter {

    companion object {
        
        /**
         * Should hide audio ads between songs?
         */
        @JvmStatic
        public fun shouldHideAudioAds(): Boolean {
            return JioSaavnShared.isPatchEnabled("morphe_hide_audio_ads")
        }
        
        /**
         * Should hide video ads?
         */
        @JvmStatic
        public fun shouldHideVideoAds(): Boolean {
            return JioSaavnShared.isPatchEnabled("morphe_hide_video_ads")
        }
        
        /**
         * Should hide banner ads?
         */
        @JvmStatic
        public fun shouldHideBannerAds(): Boolean {
            return JioSaavnShared.isPatchEnabled("morphe_hide_banner_ads")
        }
        
        /**
         * Filter sponsored content from a list
         */
        @JvmStatic
        public fun filterSponsoredContent(items: java.util.List<Any>): java.util.List<Any> {
            if (!JioSaavnShared.isPatchEnabled("morphe_hide_sponsored_content")) {
                return items
            }
            
            val filtered = java.util.ArrayList<Any>()
            for (item in items) {
                if (!isSponsoredContent(item)) {
                    filtered.add(item)
                }
            }
            return filtered
        }
        
        /**
         * Check if an item is sponsored content
         * Override this based on JioSaavn's data model
         */
        private fun isSponsoredContent(item: Any): Boolean {
            // Check for common sponsored indicators
            // This depends on JioSaavn's actual data structures
            val className = item.javaClass.simpleName.lowercase()
            return className.contains("sponsor") || 
                   className.contains("ad") || 
                   className.contains("promo")
        }
        
        /**
         * Check if a network request is for ads
         */
        @JvmStatic
        public fun isAdRequest(url: String): Boolean {
            if (!JioSaavnShared.isPatchEnabled("morphe_hide_audio_ads") &&
                !JioSaavnShared.isPatchEnabled("morphe_hide_video_ads")) {
                return false
            }
            
            val lowerUrl = url.lowercase()
            return lowerUrl.contains("/ad") || 
                   lowerUrl.contains("/ads/") || 
                   lowerUrl.contains("advertisement") ||
                   lowerUrl.contains("doubleclick") ||
                   lowerUrl.contains("googlesyndication") ||
                   lowerUrl.contains("adservice")
        }
    }
}