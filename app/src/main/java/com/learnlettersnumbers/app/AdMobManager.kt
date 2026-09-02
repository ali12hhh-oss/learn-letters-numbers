package com.learnlettersnumbers.app

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdMobConfig {
    const val APP_ID = "ca-app-pub-8995513369904529~3340659334"
    const val BANNER_UNIT_ID = "ca-app-pub-8995513369904529/8602443507"
    const val INTERSTITIAL_UNIT_ID = "ca-app-pub-8995513369904529/6306462974"
    const val REWARDED_UNIT_ID = "ca-app-pub-8995513369904529/1054136299"
    const val TEST_BANNER_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"
    const val TEST_INTERSTITIAL_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
}

class AdMobManager(private val context: Context) {
    private var interstitial: InterstitialAd? = null
    private var rewarded: RewardedAd? = null

    init {
        val configuration = RequestConfiguration.Builder()
            .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
            .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
            .build()
        MobileAds.setRequestConfiguration(configuration)
        MobileAds.initialize(context.applicationContext) {
            preloadInterstitial()
            preloadRewarded()
        }
    }

    private fun interstitialUnitId(): String =
        if (BuildConfig.DEBUG) AdMobConfig.TEST_INTERSTITIAL_UNIT_ID else AdMobConfig.INTERSTITIAL_UNIT_ID

    private fun rewardedUnitId(): String =
        if (BuildConfig.DEBUG) AdMobConfig.TEST_REWARDED_UNIT_ID else AdMobConfig.REWARDED_UNIT_ID

    fun preloadInterstitial() {
        if (interstitial != null) return
        InterstitialAd.load(context.applicationContext, interstitialUnitId(), AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitial = ad
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() { interstitial = null; preloadInterstitial() }
                    override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) { interstitial = null; preloadInterstitial() }
                }
            }
            override fun onAdFailedToLoad(error: LoadAdError) { interstitial = null }
        })
    }

    fun showInterstitial(activity: Activity): Boolean {
        val ad = interstitial ?: run { preloadInterstitial(); return false }
        interstitial = null
        ad.show(activity)
        return true
    }

    fun preloadRewarded() {
        if (rewarded != null) return
        RewardedAd.load(context.applicationContext, rewardedUnitId(), AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                rewarded = ad
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() { rewarded = null; preloadRewarded() }
                    override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) { rewarded = null; preloadRewarded() }
                }
            }
            override fun onAdFailedToLoad(error: LoadAdError) { rewarded = null }
        })
    }

    fun showRewarded(activity: Activity, onReward: () -> Unit): Boolean {
        val ad = rewarded ?: run { preloadRewarded(); return false }
        rewarded = null
        ad.show(activity) { onReward() }
        return true
    }
}

@Composable
fun AdMobBanner(modifier: Modifier = Modifier) {
    val adView = remember { arrayOfNulls<AdView>(1) }
    AndroidView(
        modifier = modifier,
        factory = { context ->
            AdView(context).also { view ->
                adView[0] = view
                view.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, 360))
                view.adUnitId = if (BuildConfig.DEBUG) AdMobConfig.TEST_BANNER_UNIT_ID else AdMobConfig.BANNER_UNIT_ID
                view.loadAd(AdRequest.Builder().build())
            }
        },
        update = { it.resume() }
    )
    DisposableEffect(Unit) {
        onDispose {
            adView[0]?.destroy()
            adView[0] = null
        }
    }
}
