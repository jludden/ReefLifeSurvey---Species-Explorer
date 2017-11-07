package me.jludden.reeflifesurvey;

import android.app.Application;
import android.os.StrictMode;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by Jason on 11/7/2017.
 *
 * set up leak canary basically
 */

public class ReefLifeSurveyApplication extends Application {
    @Override public void onCreate() {
        super.onCreate();
        setupLeakCanary();
    }

    protected void setupLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        enabledStrictMode();
        LeakCanary.install(this);
    }

    private static void enabledStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder() //
                .detectAll() //
                .penaltyLog() //
                .penaltyDeath() //
                .build());
    }
}
