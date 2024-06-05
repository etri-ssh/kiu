package com.example.ccp.common;

import android.annotation.SuppressLint;
import android.app.Application;

@SuppressLint("StaticFieldLeak")
public class MyApplication extends Application {
    public static BackgroundDetector backgroundDetector;
    public static SharedUtil shared;

    @Override
    public void onCreate() {
        super.onCreate();
        backgroundDetector = new BackgroundDetector(MyApplication.this);
        shared = new SharedUtil(getApplicationContext());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        backgroundDetector.unregisterCallbacks();
    }
}
