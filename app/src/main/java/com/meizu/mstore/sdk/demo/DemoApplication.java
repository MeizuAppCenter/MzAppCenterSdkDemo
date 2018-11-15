package com.meizu.mstore.sdk.demo;

import android.app.Application;

import com.meizu.mstore.sdk.MzAppCenterPlatform;

public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MzAppCenterPlatform.Companion.init(this, "your_appKey", BuildConfig.DEBUG);
    }
}
