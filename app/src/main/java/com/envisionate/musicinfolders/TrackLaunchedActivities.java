package com.envisionate.musicinfolders;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TrackLaunchedActivities extends Application implements Application.ActivityLifecycleCallbacks {

    private String lastLaunchedActivity = null;

    public TrackLaunchedActivities() {
        super();
        Globals.trackLaunchedActivities = this;
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
    }

    public String getLastLaunchedActivity() {
        return lastLaunchedActivity;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        if ( ! ( null == lastLaunchedActivity ) )
            lastLaunchedActivity = null;
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        lastLaunchedActivity = activity.getLocalClassName();
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        lastLaunchedActivity = activity.getLocalClassName();
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
    }


}
