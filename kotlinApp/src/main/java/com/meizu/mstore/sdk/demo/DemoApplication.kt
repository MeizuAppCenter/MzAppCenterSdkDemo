package com.meizu.mstore.sdk.demo

import android.app.Application
import com.meizu.mstore.sdk.MzAppCenterPlatform

class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MzAppCenterPlatform.init(this, "your_appKey", BuildConfig.DEBUG)
    }
}