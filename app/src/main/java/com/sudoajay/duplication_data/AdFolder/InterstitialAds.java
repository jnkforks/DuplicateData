package com.sudoajay.duplication_data.AdFolder;

import android.content.Context;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.sudoajay.duplication_data.R;


public class InterstitialAds {

    private InterstitialAd mInterstitialAd;
    private Context context;


    public InterstitialAds(final Context context) {
        this.context = context;
        mInterstitialAd = newInterstitialAd();
        loadInterstitial();
    }

    private InterstitialAd newInterstitialAd() {
        InterstitialAd interstitialAd = new InterstitialAd(context);
        interstitialAd.setAdUnitId(context.getString(R.string.interstitial_ad_unit_id));
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                goToNextLevel();
            }

            @Override
            public void onAdClosed() {
                // Proceed to the next level.

                goToNextLevel();

            }
        });
        return interstitialAd;
    }



    private void goToNextLevel() {
        // Show the next level and reload the ad to prepare for the level after.

        mInterstitialAd = newInterstitialAd();
        loadInterstitial();
    }

    private void loadInterstitial() {
        // Disable the next level button and load the ad.

        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        mInterstitialAd.loadAd(adRequest);
    }


    public boolean isLoaded() {
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            return true;
        } else {
            goToNextLevel();
            return false;
        }
    }

    public InterstitialAd getmInterstitialAd() {
        return mInterstitialAd;
    }


}
