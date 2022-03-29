package com.doublesymmetry.trackplayer;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

final class RNHockeyAppUsageTracker {
  private static final ActivityLifecycleCallbacks callbacks = new ActivityLifecycleCallbacks();
  private static boolean isInitialized = false;

  private RNHockeyAppUsageTracker() {
  }

  public static void initialize(Application app) {
    if (!isInitialized) {
      app.registerActivityLifecycleCallbacks(callbacks);

      isInitialized = true;
    }
  }

  private static final class ActivityLifecycleCallbacks implements android.app.Application.ActivityLifecycleCallbacks {
    public void onActivityCreated(Activity activity, Bundle bundle) {
      Log.d("TEST", "TEST");
    }

    public void onActivityStarted(Activity activity) {
    }

    public void onActivityResumed(Activity activity) {
    }

    public void onActivityPaused(Activity activity) {
    }

    public void onActivityStopped(Activity activity) {
    }

    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    public void onActivityDestroyed(Activity activity) {
    }
  }
}