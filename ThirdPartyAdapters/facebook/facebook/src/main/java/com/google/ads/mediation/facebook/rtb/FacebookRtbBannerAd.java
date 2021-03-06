package com.google.ads.mediation.facebook.rtb;

import static com.google.ads.mediation.facebook.FacebookMediationAdapter.ERROR_INVALID_REQUEST;
import static com.google.ads.mediation.facebook.FacebookMediationAdapter.TAG;
import static com.google.ads.mediation.facebook.FacebookMediationAdapter.createAdapterError;
import static com.google.ads.mediation.facebook.FacebookMediationAdapter.setMixedAudience;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdView;
import com.facebook.ads.ExtraHints;
import com.google.ads.mediation.facebook.FacebookMediationAdapter;
import com.google.android.gms.ads.mediation.MediationAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationBannerAd;
import com.google.android.gms.ads.mediation.MediationBannerAdCallback;
import com.google.android.gms.ads.mediation.MediationBannerAdConfiguration;

public class FacebookRtbBannerAd implements MediationBannerAd, AdListener {

  private MediationBannerAdConfiguration adConfiguration;
  private MediationAdLoadCallback<MediationBannerAd, MediationBannerAdCallback> callback;
  private AdView adView;
  private MediationBannerAdCallback mBannerAdCallback;

  public FacebookRtbBannerAd(MediationBannerAdConfiguration adConfiguration,
      MediationAdLoadCallback<MediationBannerAd, MediationBannerAdCallback> callback) {
    this.adConfiguration = adConfiguration;
    this.callback = callback;
  }

  public void render() {
    Bundle serverParameters = adConfiguration.getServerParameters();
    String placementID = FacebookMediationAdapter.getPlacementID(serverParameters);
    if (TextUtils.isEmpty(placementID)) {
      String errorMessage = createAdapterError(ERROR_INVALID_REQUEST,
          "Failed to request ad, placementID is null or empty.");
      Log.e(TAG, errorMessage);
      callback.onFailure(errorMessage);
      return;
    }

    setMixedAudience(adConfiguration);
    try {
      adView = new AdView(adConfiguration.getContext(), placementID,
          adConfiguration.getBidResponse());
      if (!TextUtils.isEmpty(adConfiguration.getWatermark())) {
        adView.setExtraHints(
            new ExtraHints.Builder().mediationData(adConfiguration.getWatermark()).build());
      }

      adView.loadAd(
          adView.buildLoadAdConfig()
              .withAdListener(this)
              .withBid(adConfiguration.getBidResponse())
              .build()
      );
    } catch (Exception e) {
      callback.onFailure("FacebookRtbBannerAd Failed to load: " + e.getMessage());
    }
  }

  @NonNull
  @Override
  public View getView() {
    return adView;
  }

  @Override
  public void onError(Ad ad, AdError adError) {
    callback.onFailure(adError.getErrorMessage());
  }

  @Override
  public void onAdLoaded(Ad ad) {
    mBannerAdCallback = callback.onSuccess(this);
  }

  @Override
  public void onAdClicked(Ad ad) {
    if (mBannerAdCallback != null) {
      // TODO: Upon approval, add this callback back in.
      //mBannerAdCallback.reportAdClicked();
      mBannerAdCallback.onAdOpened();
      mBannerAdCallback.onAdLeftApplication();
    }
  }

  @Override
  public void onLoggingImpression(Ad ad) {
    if (mBannerAdCallback != null) {
      // TODO: Upon approval, add this callback back in.
      //mBannerAdCallback.reportAdImpression();
    }
  }
}
